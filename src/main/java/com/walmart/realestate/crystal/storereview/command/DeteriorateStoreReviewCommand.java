package com.walmart.realestate.crystal.storereview.command;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
public class DeteriorateStoreReviewCommand implements StoreReviewCommand {

    private final Integer timeInMonitoringDays = null;

}
