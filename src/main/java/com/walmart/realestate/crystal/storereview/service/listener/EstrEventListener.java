package com.walmart.realestate.crystal.storereview.service.listener;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.walmart.realestate.crystal.storereview.service.StoreReviewHealthScoreService;
import com.walmart.realestate.crystal.storereview.service.StoreReviewService;
import com.walmart.realestate.crystal.storereview.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.listener.adapter.ConsumerRecordMetadata;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class EstrEventListener {

    private final StoreReviewHealthScoreService storeReviewHealthScoreService;

    private final StoreReviewService storeReviewService;

    private final ObjectMapper objectMapper;

    public static final String STARTED_EVENT = "com.walmart.realestate.crystal.storereview.event.StoreReviewStartedEvent";
    public static final String MONITORING_EVENT = "com.walmart.realestate.crystal.storereview.event.StoreReviewMonitoringEvent";
    public static final String DETERIORATED_EVENT = "com.walmart.realestate.crystal.storereview.event.StoreReviewDeterioratedEvent";
    public static final String POST_REVIEW_EVENT = "com.walmart.realestate.crystal.storereview.event.StoreReviewPostReviewEvent";
    public static final String POST_MAINTENANCE_EVENT = "com.walmart.realestate.crystal.storereview.event.StoreReviewPostMaintenanceEvent";

    public static final String POST_PREVENTIVE_MAINTENANCE_EVENT = "com.walmart.realestate.crystal.storereview.event.StoreReviewPostPreventiveMaintenanceEvent";
    private static final List<String> subscribedEvents = Arrays.asList(STARTED_EVENT, MONITORING_EVENT, DETERIORATED_EVENT, POST_REVIEW_EVENT, POST_MAINTENANCE_EVENT, POST_PREVENTIVE_MAINTENANCE_EVENT);
    public static final String ASSIGNED_EVENT = "com.walmart.realestate.crystal.storereview.event.StoreReviewAssignedEvent";

    @SneakyThrows
    @KafkaListener(topics = "${spring.kafka.consumer.properties.topics}")
    public void consumeEvent(@Payload Message message, ConsumerRecordMetadata metadata) {

        TenantContext.setCurrentTenant(message.getHeaders().get("x-tenant").toString());
        log.info("Received event: {} from partition {} topic {} with offset {} and timestamp {}",
                message.getPayload(), metadata.partition(), metadata.topic(), metadata.offset(), metadata.timestamp());
        JsonNode eventPayload = objectMapper.readTree(message.getPayload().toString());
        String eventName = eventPayload.get("eventName").textValue();
        if (ASSIGNED_EVENT.equals(eventName))
            storeReviewService.processEvent(eventPayload);
        if (subscribedEvents.contains(eventName))
            storeReviewHealthScoreService.processEvent(eventPayload);
    }
}
