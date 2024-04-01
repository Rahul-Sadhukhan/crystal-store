package com.walmart.realestate.crystal.storereview.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateStoreReviewCommand implements StoreReviewCommand {

    private String storeReviewId;

    private Long storeNumber;

    private String fmRegion;

    private String reviewType;

    private String assignee;

    private String refrigerantType;

    private String sdm;

    private LocalDateTime startDate;

}
