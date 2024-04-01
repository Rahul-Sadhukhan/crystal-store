package com.walmart.realestate.crystal.storereview.service;

import com.walmart.realestate.crystal.annotation.Logger;
import com.walmart.realestate.crystal.storereview.client.asset.AssetClient;
import com.walmart.realestate.crystal.storereview.client.asset.AssetDQClient;
import com.walmart.realestate.crystal.storereview.client.asset.model.*;
import com.walmart.realestate.crystal.storereview.properties.StoreReviewProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class AssetService {

    private final StoreReviewProperties storeReviewProperties;

    private final AssetClient assetClient;

    private final AssetDQClient assetDQClient;

    private static final List<String> fields;

    @Logger
    public List<Asset> getAssetsForStore(Long storeNumber) {
        return getAssetsForStore(storeNumber, fields);
    }

    @Logger
    public List<Asset> getAssetsForStore(Long storeNumber, List<String> assetFields) {
        log.info("Get assets for store number {}", storeNumber);

        return assetClient.getAsset(SearchRequest.builder()
                                .assetQuery(AssetQuery.builder()
                                        .storeNumber(storeNumber.toString())
                                        .active("true")
                                        .assetType(storeReviewProperties.getAssetTypes().stream()
                                                .collect(Collectors.joining(",", "[", "]")))
                                        .build())
                                .assetFields(assetFields != null ? assetFields : fields)
                                .build(),
                        PageRequest.of(0, 1000))
                .getContent().stream()
                .map(EntityModel::getContent)
                .collect(Collectors.toList());
    }

    public Object updateAsset(Asset asset) {
        log.info("Update asset {} for store number {}", asset.getId(), asset.getStoreNumber());
        return assetClient.editAsset(asset, asset.getId());
    }

    @Logger
    public List<StoreDataQuality> getStoreDataQuality(List<Long> storeNumbers) {
        log.info("Get data quality of assets for store numbers {}", storeNumbers);
        return getStoreDataQuality(StoreDataQualityFilter.builder()
                .storeNumbers(storeNumbers)
                .assetTypes(storeReviewProperties.getAssetTypes())
                .build());
    }

    private List<StoreDataQuality> getStoreDataQuality(StoreDataQualityFilter storeDataQualityFilter) {
        return assetDQClient.getStoreDataQuality(storeDataQualityFilter);
    }

    static {
        fields = Arrays.asList("qrCode", "storeNumber", "assetType", "assetTypeMDM", "installDate", "manufacturedDate", "brand", "modelNumber",
                "serialNumber", "tagId", "area", "assetStatus", "comments", "attributes", "sapArticleNumber", "hostSupplierNumber", "sapSupplierNumber",
                "sapPoNumber", "createDataSource", "assetId", "createdOn", "createdBy", "locationId", "dwEquipmentId", "brandId", "primaryTradeId",
                "primaryTradeName", "deactivatedDate", "active", "purchaseDate", "lifeExpectancy", "parentId", "parentIdDescription", "isLeased", "leaseDate",
                "leasePeriod", "originalValue", "condition", "energyEfficiency", "capacity", "usesRefrigerant", "hasLeakDetector", "hasComponents", "hasCircuits",
                "warrantyExpirationDate", "warrantyNTE", "warrantyProviderId", "warrantyProviderType", "warrantyProviderName", "warrantyCategory", "warrantyExpirationType",
                "warrantyExpirationPeriod", "warrantyPriority", "warrantyProviderWebsite", "warrantyDoc", "contactInfo", "lastTaggedOn", "qrRemappedFrom",
                "qrRemapReason", "taggedUserName", "taggedUserEmail", "taggedUserCompany", "productWebsite", "supportDocsLink", "humanName", "brandWebsite",
                "dataQualityIssues{\n ruleId \n ruleGroup\n ruleDescription \n targetAttributes \n}", "refrigerantUsageType", "refrigerantPartNumber");
    }

}
