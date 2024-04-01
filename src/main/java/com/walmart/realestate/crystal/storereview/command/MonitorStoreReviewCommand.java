package com.walmart.realestate.crystal.storereview.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonitorStoreReviewCommand implements StoreReviewCommand {

    private Integer timeInMonitoringDays;

}
