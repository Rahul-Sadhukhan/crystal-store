package com.walmart.realestate.crystal.storereview.entity;

import lombok.*;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import javax.persistence.*;
import java.time.Instant;
import java.time.LocalDateTime;

@EqualsAndHashCode
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "StoreReview")
public class StoreReviewEntity {

    @Id
    private String id;

    private String uuid;

    private Long storeNumber;

    private String sdm;

    private String fmRegion;

    private String reviewType;

    private String assignee;

    @NotFound(action = NotFoundAction.IGNORE)
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assignee", referencedColumnName = "userId", insertable = false, updatable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private UserAccountEntity assigneeUser;

    private Instant assignedAt;

    private LocalDateTime startDate;

    private String state;

    private String flow;

    private Instant startedAt;

    private Instant lastStartedAt;

    private Integer timeInMonitoringDays;

    private Instant monitoringStartedAt;

    private Instant validationStartedAt;

    private String closedBy;

    @NotFound(action = NotFoundAction.IGNORE)
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "closedBy", referencedColumnName = "userId", insertable = false, updatable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private UserAccountEntity closedByUser;

    private Instant closedAt;

    private String stateAtClosure;

    private String commentsAtClosure;

    private String declinedBy;

    @NotFound(action = NotFoundAction.IGNORE)
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "declinedBy", referencedColumnName = "userId", insertable = false, updatable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private UserAccountEntity declinedByUser;

    private Instant declinedAt;

    private String reasonForDeclining;

    @NotFound(action = NotFoundAction.IGNORE)
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "storeNumber", referencedColumnName = "storeNumber", insertable = false, updatable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private StoreHealthScoreSnapshotEntity healthScore;

    private String createdBy;

    @NotFound(action = NotFoundAction.IGNORE)
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "createdBy", referencedColumnName = "userId", insertable = false, updatable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private UserAccountEntity createdByUser;

    private Instant createdAt;

    private String lastModifiedBy;

    @NotFound(action = NotFoundAction.IGNORE)
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "lastModifiedBy", referencedColumnName = "userId", insertable = false, updatable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private UserAccountEntity lastModifiedByUser;

    private Instant lastModifiedAt;

    private Instant preventiveMaintenanceStartedAt;

    private Instant postPreventiveMaintenanceStartedAt;

    private String refrigerantType;

}
