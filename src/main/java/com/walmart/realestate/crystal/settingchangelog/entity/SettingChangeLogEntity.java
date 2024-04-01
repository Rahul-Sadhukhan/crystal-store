package com.walmart.realestate.crystal.settingchangelog.entity;

import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@EqualsAndHashCode
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document("settingChangeLog")
public class SettingChangeLogEntity {

    @Id
    private String id;

    private String referenceId;

    @Deprecated
    @Indexed(useGeneratedName = true)
    private Long assetId;

    @Indexed(useGeneratedName = true)
    private String assetMappingId;

    @Indexed(useGeneratedName = true)
    private Long storeNumber;

    @Deprecated
    private Instant timestamp;

    private String setting;

    private String settingValue;

    private String oldValue;

    private String newValue;

    private String unit;

    private String notes;

    private String reason;

    private String source;

    @CreatedBy
    private String createdBy;

    @CreatedDate
    private Instant createdAt;

}
