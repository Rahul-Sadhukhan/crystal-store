package com.walmart.realestate.crystal.storereview.service.selfheal;

import com.walmart.realestate.crystal.annotation.Logger;
import com.walmart.realestate.crystal.storereview.entity.StoreReviewEntity;
import com.walmart.realestate.crystal.storereview.service.StoreReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

@Component
@RequiredArgsConstructor
@Slf4j
public class SelfHealSchedulerService {

    public static final long ACTIVE_DAYS = 10L;
    private final List<SelfHealHandler> selfHealHandlers;

    private final StoreReviewService storeReviewService;

    @Scheduled(cron = "0 0 */2 * * ?")
    @SchedulerLock(name = "executeScheduledHeal")
    @Logger
    public void scheduledHealExecute() {
        Long days = ACTIVE_DAYS;
        Long count = storeReviewService.getOpenReviewsCount(days);
        log.info("Running scheduled healer for {} open reviews", count);

        IntStream.rangeClosed(0, count.intValue() / 100)
                .forEach(i -> {
                    List<StoreReviewEntity> storeReviewList = storeReviewService.getActiveStoreReviews(PageRequest.of(i, 100), days);
                    selfHealHandlers.stream()
                            .sorted(Comparator.comparing(SelfHealHandler::getPriority))
                            .forEach(handler -> handler.handle(storeReviewList));
                });

    }


}
