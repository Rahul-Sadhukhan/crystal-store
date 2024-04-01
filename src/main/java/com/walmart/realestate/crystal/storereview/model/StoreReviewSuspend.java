package com.walmart.realestate.crystal.storereview.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY;

@EqualsAndHashCode
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreReviewSuspend {

    @JsonProperty(access = READ_ONLY)
    private LocalDate docPrePmDeliveryDate;

    private Long recordIdNbr;

    private String state;

    @JsonProperty(access = READ_ONLY)
    private Long storeNumber;

    @JsonProperty(access = READ_ONLY)
    private String createdBy;

    @JsonProperty(access = READ_ONLY)
    private Instant createdAt;

    @JsonProperty(access = READ_ONLY)
    private String lastModifiedBy;

    @JsonProperty(access = READ_ONLY)
    private Instant lastModifiedAt;

}
