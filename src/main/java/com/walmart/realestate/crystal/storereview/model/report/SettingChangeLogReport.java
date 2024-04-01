package com.walmart.realestate.crystal.storereview.model.report;

import lombok.*;

import java.time.LocalDateTime;

@EqualsAndHashCode
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettingChangeLogReport {

    private String id;

    private String referenceId;

    private String assetMappingId;

    private Long storeNumber;

    private String setting;

    private String settingValue;

    private String oldValue;

    private String newValue;

    private String unit;

    private String notes;

    private String reason;

    private String source;

    private LocalDateTime createdAt;

}
