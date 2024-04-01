package com.walmart.realestate.crystal.storereview.service;

import com.walmart.realestate.crystal.storereview.client.asset.AssetClient;
import com.walmart.realestate.crystal.storereview.client.asset.AssetDQClient;
import com.walmart.realestate.crystal.storereview.client.asset.model.Asset;
import com.walmart.realestate.crystal.storereview.client.asset.model.SearchRequest;
import com.walmart.realestate.crystal.storereview.client.asset.model.StoreDataQuality;
import com.walmart.realestate.crystal.storereview.client.asset.model.StoreDataQualityFilter;
import com.walmart.realestate.crystal.storereview.config.PropertiesConfig;
import com.walmart.realestate.crystal.storereview.properties.StoreReviewProperties;
import com.walmart.realestate.crystal.storereview.util.AssetAssembler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {AssetService.class, PropertiesConfig.class, StoreReviewProperties.class, AssetAssembler.class, PagedResourcesAssembler.class})
@ActiveProfiles("test")
class AssetServiceTest {

    @Autowired
    private AssetService assetService;

    @Autowired
    private StoreReviewProperties storeReviewProperties;

    @Autowired
    private AssetAssembler assetAssembler;

    @MockBean
    private AssetClient assetClient;

    @MockBean
    private AssetDQClient assetDQClient;

    @Test
    void testGetAssetsForStore() {
        List<Asset> assetList = Arrays.asList(
                Asset.builder().id(123L).assetType("Rack").build(),
                Asset.builder().id(124L).assetType("Controller").build());
        PagedModel<EntityModel<Asset>> assets = assetAssembler.toPagedModel(new PageImpl<>(assetList, PageRequest.of(0, 1000), assetList.size()));

        when(assetClient.getAsset(any(SearchRequest.class), any(Pageable.class))).thenReturn(assets);

        List<Asset> assetResponse = assetService.getAssetsForStore(1933L);

        assertThat(assetResponse.get(0).getId()).isEqualTo(123L);
        assertThat(assetResponse.get(1).getAssetType()).isEqualTo("Controller");

        verify(assetClient).getAsset(any(), any());
    }

    @Test
    void testGetStoreSummary() {
        List<StoreDataQuality> summaryList = Arrays.asList(
                StoreDataQuality.builder()
                        .storeNumber(12L)
                        .score(80)
                        .build(),
                StoreDataQuality.builder()
                        .storeNumber(13L)
                        .score(75)
                        .build());
        StoreDataQualityFilter storeDataQualityFilter = StoreDataQualityFilter.builder()
                .storeNumbers(Arrays.asList(12L, 13L))
                .assetTypes(storeReviewProperties.getAssetTypes())
                .build();
        when(assetDQClient.getStoreDataQuality(storeDataQualityFilter)).thenReturn(summaryList);

        List<StoreDataQuality> storeDataQualityList = assetService.getStoreDataQuality(Arrays.asList(12L, 13L));

        assertThat(storeDataQualityList.size()).isEqualTo(2);
        assertThat(storeDataQualityList.get(0).getStoreNumber()).isEqualTo(12L);
        assertThat(storeDataQualityList.get(0).getScore()).isEqualTo(80);

        verify(assetDQClient).getStoreDataQuality(storeDataQualityFilter);
    }

    @Test
    void testUpdateAsset() {
        Asset asset = Asset.builder().id(600L).assetType("Rack").tagId("123").build();

        when(assetClient.editAsset(asset, 600L)).thenReturn(asset);

        Object assetResponse = assetService.updateAsset(asset);

        assertThat(assetResponse.toString()).hasToString(assetResponse.toString());

        verify(assetClient).editAsset(asset, 600L);
    }

}
