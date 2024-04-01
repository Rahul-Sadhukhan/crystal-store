package com.walmart.realestate.crystal.storereview.client.asset.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AssetQuery {

    private String qrCode;

    private String storeId;

    private String storeNumber;

    private String assetType;

    private String assetTypeMDM;

    private String installDate;

    private String manufacturedDate;

    private String brand;

    private String modelNumber;

    private String serialNumber;

    private String tagId;

    private String area;

    private String assetStatus;

    private String comments;

    private String sapArticleNumber;

    private String hostSupplierNumber;

    private String sapSupplierNumber;

    private String sapPoNumber;

    private String createDataSource;

    private String lastUpdDataSource;

    private String assetId;

    private String createdOn;

    private String updatedOn;

    private String createdBy;

    private String updatedBy;

    private String locationId;

    private String dwEquipmentId;

    private String brandId;

    private String primaryTradeId;

    private String primaryTradeName;

    private String deactivatedDate;

    private String active;

    private String purchaseDate;

    private String lifeExpectancy;

    private String parentId;

    private String parentIdDescription;

    private String isLeased;

    private String leaseDate;

    private String leasePeriod;

    private String originalValue;

    private String condition;

    private String energyEfficiency;

    private String capacity;

    private String usesRefrigerant;

    private String hasLeakDetector;

    private String hasComponents;

    private String hasCircuits;

    private String warrantyExpirationDate;

    private String warrantyNTE;

    private String warrantyProviderId;

    private String warrantyProviderType;

    private String warrantyProviderName;

    private String warrantyCategory;

    private String warrantyExpirationType;

    private String warrantyExpirationPeriod;

    private String warrantyId;

    private String warrantyPriority;

    private String lastTaggedOn;

    private String qrRemappedFrom;

    private String qrRemapReason;

    private String taggedUserName;

    private String taggedUserEmail;

    private String taggedUserCompany;

    private String tagDetailId;

    private Boolean badData;

}
