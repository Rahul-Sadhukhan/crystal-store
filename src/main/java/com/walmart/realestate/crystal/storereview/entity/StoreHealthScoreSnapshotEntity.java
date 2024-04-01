package com.walmart.realestate.crystal.storereview.entity;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.Instant;

@EqualsAndHashCode
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "StoreHealthScoreSnapshot")
public class StoreHealthScoreSnapshotEntity {

    @Id
    private Long storeNumber;

    private String rowId;

    private Double value;

    private Instant runTime;

}
