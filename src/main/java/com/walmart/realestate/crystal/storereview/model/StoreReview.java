package com.walmart.realestate.crystal.storereview.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.springframework.hateoas.server.core.Relation;

import javax.validation.constraints.Future;
import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Relation(itemRelation = "storeReview", collectionRelation = "storeReviews")
public class StoreReview {

    @JsonProperty(access = READ_ONLY)
    private String id;

    @JsonProperty(access = READ_ONLY)
    private UUID uuid;

    @NotNull
    private Long storeNumber;

    @JsonProperty(access = READ_ONLY)
    private String sdm;

    @JsonProperty(access = READ_ONLY)
    private String fmRegion;

    private String reviewType;

    private String assignee;

    @JsonProperty(access = READ_ONLY)
    private String assigneeName;

    @JsonProperty(access = READ_ONLY)
    private UserAccount assigneeUser;

    @JsonProperty(access = READ_ONLY)
    private Instant assignedAt;

    @Future
    private LocalDateTime startDate;

    @JsonProperty(access = READ_ONLY)
    private String state;

    @JsonProperty(access = READ_ONLY)
    private String flow;

    @JsonProperty(access = READ_ONLY)
    private Instant startedAt;

    @JsonProperty(access = READ_ONLY)
    private Instant lastStartedAt;

    @JsonProperty(access = READ_ONLY)
    private Integer timeInMonitoringDays;

    @JsonProperty(access = READ_ONLY)
    private Instant monitoringStartedAt;

    @JsonProperty(access = READ_ONLY)
    private Instant validationStartedAt;

    @JsonProperty(access = READ_ONLY)
    private String closedBy;

    @JsonProperty(access = READ_ONLY)
    private String closedByName;

    @JsonProperty(access = READ_ONLY)
    private UserAccount closedByUser;

    @JsonProperty(access = READ_ONLY)
    private Instant closedAt;

    @JsonProperty(access = READ_ONLY)
    private String stateAtClosure;

    @JsonProperty(access = READ_ONLY)
    private String commentsAtClosure;

    @JsonProperty(access = READ_ONLY)
    private String declinedBy;

    @JsonProperty(access = READ_ONLY)
    private String declinedByName;

    @JsonProperty(access = READ_ONLY)
    private UserAccount declinedByUser;

    @JsonProperty(access = READ_ONLY)
    private Instant declinedAt;

    @JsonProperty(access = READ_ONLY)
    private String reasonForDeclining;

    @JsonProperty(access = READ_ONLY)
    private String createdBy;

    @JsonProperty(access = READ_ONLY)
    private String createdByName;

    @JsonProperty(access = READ_ONLY)
    private UserAccount createdByUser;

    @JsonProperty(access = READ_ONLY)
    private Instant createdAt;

    @JsonProperty(access = READ_ONLY)
    private StoreHealthScoreSnapshot healthScore;

    @JsonProperty(access = READ_ONLY)
    private String lastModifiedBy;

    @JsonProperty(access = READ_ONLY)
    private UserAccount lastModifiedByUser;

    @JsonProperty(access = READ_ONLY)
    private Instant lastModifiedAt;

    @JsonProperty(access = READ_ONLY)
    private Instant preventiveMaintenanceStartedAt;

    @JsonProperty(access = READ_ONLY)
    private Instant postPreventiveMaintenanceStartedAt;

    private String refrigerantType;

}
