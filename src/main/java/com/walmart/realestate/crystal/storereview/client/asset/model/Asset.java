package com.walmart.realestate.crystal.storereview.client.asset.model;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.walmart.realestate.crystal.storereview.util.JacksonUtil;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY;

@Getter
@Setter
@Builder
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Asset implements AssetTypeAware, Serializable {

    @JsonAlias("assetId")
    private Long id;

    private String qrCode;

    private Integer storeId;

    private Integer storeNumber;

    private String assetType;

    private String assetTypeMDM;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate installDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate manufacturedDate;

    private String brand;

    private String modelNumber;

    private String serialNumber;

    private String tagId;

    private String area;

    private String assetStatus;

    private String comments;

    private String attributes;

    private Integer sapArticleNumber;

    private Integer hostSupplierNumber;

    private Integer sapSupplierNumber;

    private Long sapPoNumber;

    private String createDataSource;

    @JsonProperty(access = READ_ONLY)
    private String lastUpdDataSource = "Crystal";

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate createdOn;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate updatedOn;

    private String createdBy;

    @JsonProperty(access = READ_ONLY)
    @JsonSerialize(nullsUsing = JacksonUtil.UserIdSerializer.class)
    private String updatedBy;

    private Integer locationId;

    private Long dwEquipmentId;

    private Integer brandId;

    private Integer primaryTradeId;

    private String primaryTradeName;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate deactivatedDate;

    private Boolean active;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate purchaseDate;

    private Integer lifeExpectancy;

    private Integer parentId;

    private String parentIdDescription;

    private Boolean isLeased;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate leaseDate;

    private Integer leasePeriod;

    private Integer originalValue;

    private Integer condition;

    private String energyEfficiency;

    private String capacity;

    private Integer usesRefrigerant;

    private Boolean hasLeakDetector;

    private Boolean hasComponents;

    private Boolean hasCircuits;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate warrantyExpirationDate;

    private Integer warrantyNTE;

    private Integer warrantyProviderId;

    private Integer warrantyProviderType;

    private String warrantyProviderName;

    private String warrantyCategory;

    private Integer warrantyExpirationType;

    private Integer warrantyExpirationPeriod;

    private Integer warrantyId;

    private String warrantyPriority;

    private String warrantyProviderWebsite;

    private String warrantyDoc;

    private String contactInfo;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate lastTaggedOn;

    private String qrRemappedFrom;

    private String qrRemapReason;

    private String taggedUserName;

    private String taggedUserEmail;

    private String taggedUserCompany;

    private Integer tagDetailId;

    private String productWebsite;

    private String supportDocsLink;

    private String humanName;

    private String brandWebsite;

    private List<DataQualityIssue> dataQualityIssues;

    private Float rackMetricsSpShSuperHeatScore;

    private Float rackMetricsSpShSuctionPressureScore;

    private String refrigerantUsageType;

    private String refrigerantPartNumber;

    private List<IotNormalization> iotNormalization;

}
