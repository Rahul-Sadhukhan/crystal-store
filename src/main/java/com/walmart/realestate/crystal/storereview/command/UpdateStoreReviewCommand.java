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
public class UpdateStoreReviewCommand implements StoreReviewCommand {

    private String storeReviewId;

    private String assignee;

    private LocalDateTime startDate;

}
