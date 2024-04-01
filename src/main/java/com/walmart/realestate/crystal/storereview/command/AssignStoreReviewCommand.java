package com.walmart.realestate.crystal.storereview.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignStoreReviewCommand implements StoreReviewCommand {

    private String assignee;

    private final String declinedBy = null;

    private final String reasonForDeclining = null;

}
