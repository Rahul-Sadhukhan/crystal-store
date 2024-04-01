package com.walmart.realestate.crystal.storereview.service;

import com.walmart.realestate.crystal.storereview.client.healthmetrics.HealthMetricsClient;
import com.walmart.realestate.crystal.storereview.client.healthmetrics.model.RefrigerationSensor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.hateoas.CollectionModel;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {StoreReviewAssetService.class})
@ActiveProfiles("test")
class StoreReviewAssetServiceTest {

    @Autowired
    private StoreReviewAssetService storeReviewAssetService;

    @MockBean
    private HealthMetricsClient healthMetricsClient;

    @Test
    void testGetAssetsForStoreEmpty() {
        when(healthMetricsClient.getRackAssets(438L)).thenReturn(CollectionModel.empty());

        when(healthMetricsClient.getCaseAssets(438L)).thenReturn(CollectionModel.empty());

        List<RefrigerationSensor> storeAssets = storeReviewAssetService.getAssetsForStore(438L);

        assertThat(storeAssets).hasSize(0);

        verify(healthMetricsClient).getRackAssets(438L);
        verify(healthMetricsClient).getCaseAssets(438L);
    }

    @Test
    void testGetAssetsForStoreRack() {
        List<RefrigerationSensor> assets = List.of(
                RefrigerationSensor.builder()
                        .id("C-438")
                        .rackCallLetter("C")
                        .build(),
                RefrigerationSensor.builder()
                        .id("D-438")
                        .rackCallLetter("D")
                        .equipmentId(4661L)
                        .build());
        when(healthMetricsClient.getRackAssets(438L)).thenReturn(CollectionModel.wrap(assets));

        when(healthMetricsClient.getCaseAssets(438L)).thenReturn(CollectionModel.empty());

        List<RefrigerationSensor> storeAssets = storeReviewAssetService.getAssetsForStore(438L);

        assertThat(storeAssets).hasSize(2);

        assertThat(storeAssets.get(0).getId()).isEqualTo("C-438");
        assertThat(storeAssets.get(0).getRackCallLetter()).isEqualTo("C");
        assertThat(storeAssets.get(0).getAssetName()).isEqualTo("C-438");
        assertThat(storeAssets.get(0).getEquipmentId()).isNull();

        assertThat(storeAssets.get(1).getId()).isEqualTo("D-438");
        assertThat(storeAssets.get(1).getRackCallLetter()).isEqualTo("D");
        assertThat(storeAssets.get(1).getAssetName()).isEqualTo("D-438");
        assertThat(storeAssets.get(1).getEquipmentId()).isEqualTo(4661L);

        verify(healthMetricsClient).getRackAssets(438L);
        verify(healthMetricsClient).getCaseAssets(438L);
    }

    @Test
    void testGetAssetsForStoreRackMerge() {
        List<RefrigerationSensor> assets = List.of(
                RefrigerationSensor.builder()
                        .id("B-438")
                        .rackCallLetter("B")
                        .equipmentId(4659L)
                        .build(),
                RefrigerationSensor.builder()
                        .id("B-438")
                        .rackCallLetter("B")
                        .build(),
                RefrigerationSensor.builder()
                        .id("C-438")
                        .rackCallLetter("C")
                        .build(),
                RefrigerationSensor.builder()
                        .id("C-438")
                        .rackCallLetter("C")
                        .equipmentId(4660L)
                        .build(),
                RefrigerationSensor.builder()
                        .id("D-438")
                        .rackCallLetter("D")
                        .equipmentId(4661L)
                        .build());
        when(healthMetricsClient.getRackAssets(438L)).thenReturn(CollectionModel.wrap(assets));

        when(healthMetricsClient.getCaseAssets(438L)).thenReturn(CollectionModel.empty());

        List<RefrigerationSensor> storeAssets = storeReviewAssetService.getAssetsForStore(438L);

        assertThat(storeAssets).hasSize(3);

        assertThat(storeAssets.get(0).getId()).isEqualTo("B-438");
        assertThat(storeAssets.get(0).getRackCallLetter()).isEqualTo("B");
        assertThat(storeAssets.get(0).getAssetName()).isEqualTo("B-438");
        assertThat(storeAssets.get(0).getEquipmentId()).isEqualTo(4659L);

        assertThat(storeAssets.get(1).getId()).isEqualTo("C-438");
        assertThat(storeAssets.get(1).getRackCallLetter()).isEqualTo("C");
        assertThat(storeAssets.get(1).getAssetName()).isEqualTo("C-438");
        assertThat(storeAssets.get(1).getEquipmentId()).isEqualTo(4660L);

        assertThat(storeAssets.get(2).getId()).isEqualTo("D-438");
        assertThat(storeAssets.get(2).getRackCallLetter()).isEqualTo("D");
        assertThat(storeAssets.get(2).getAssetName()).isEqualTo("D-438");
        assertThat(storeAssets.get(2).getEquipmentId()).isEqualTo(4661L);

        verify(healthMetricsClient).getRackAssets(438L);
        verify(healthMetricsClient).getCaseAssets(438L);
    }

    @Test
    void testGetAssetsForStoreRackEmpty() {
        when(healthMetricsClient.getRackAssets(438L)).thenReturn(CollectionModel.empty());

        List<RefrigerationSensor> assets = List.of(
                RefrigerationSensor.builder()
                        .build());
        when(healthMetricsClient.getCaseAssets(438L)).thenReturn(CollectionModel.wrap(assets));

        List<RefrigerationSensor> storeAssets = storeReviewAssetService.getAssetsForStore(438L);

        assertThat(storeAssets).hasSize(0);

        verify(healthMetricsClient).getRackAssets(438L);
        verify(healthMetricsClient).getCaseAssets(438L);
    }

    @Test
    void testGetAssetsForStoreCase() {
        when(healthMetricsClient.getRackAssets(438L)).thenReturn(CollectionModel.empty());

        List<RefrigerationSensor> assets = List.of(
                RefrigerationSensor.builder()
                        .id("67234-2342")
                        .caseName("C01a")
                        .build(),
                RefrigerationSensor.builder()
                        .id("67234-2343")
                        .caseName("D02b")
                        .equipmentId(7656L)
                        .build(),
                RefrigerationSensor.builder()
                        .id("67234-2344")
                        .equipmentId(7658L)
                        .build());
        when(healthMetricsClient.getCaseAssets(438L)).thenReturn(CollectionModel.wrap(assets));

        List<RefrigerationSensor> storeAssets = storeReviewAssetService.getAssetsForStore(438L);

        assertThat(storeAssets).hasSize(2);

        assertThat(storeAssets.get(0).getId()).isEqualTo("67234-2342");
        assertThat(storeAssets.get(0).getCaseName()).isEqualTo("C01a");
        assertThat(storeAssets.get(0).getAssetName()).isEqualTo("C01a-438");
        assertThat(storeAssets.get(0).getEquipmentId()).isNull();

        assertThat(storeAssets.get(1).getId()).isEqualTo("67234-2343");
        assertThat(storeAssets.get(1).getCaseName()).isEqualTo("D02b");
        assertThat(storeAssets.get(1).getAssetName()).isEqualTo("D02b-438");
        assertThat(storeAssets.get(1).getEquipmentId()).isEqualTo(7656L);

        verify(healthMetricsClient).getRackAssets(438L);
        verify(healthMetricsClient).getCaseAssets(438L);
    }

    @Test
    void testGetAssetsForStoreCaseEmpty() {
        List<RefrigerationSensor> assets = List.of(
                RefrigerationSensor.builder()
                        .build());
        when(healthMetricsClient.getRackAssets(438L)).thenReturn(CollectionModel.wrap(assets));

        when(healthMetricsClient.getCaseAssets(438L)).thenReturn(CollectionModel.empty());

        List<RefrigerationSensor> storeAssets = storeReviewAssetService.getAssetsForStore(438L);

        assertThat(storeAssets).hasSize(0);

        verify(healthMetricsClient).getRackAssets(438L);
        verify(healthMetricsClient).getCaseAssets(438L);
    }

    @Test
    void testGetAssetsForStoreRackAndCase() {
        List<RefrigerationSensor> racks = List.of(
                RefrigerationSensor.builder()
                        .id("B-438")
                        .rackCallLetter("B")
                        .equipmentId(4659L)
                        .build(),
                RefrigerationSensor.builder()
                        .id("B-438")
                        .rackCallLetter("B")
                        .build(),
                RefrigerationSensor.builder()
                        .id("C-438")
                        .rackCallLetter("C")
                        .build(),
                RefrigerationSensor.builder()
                        .id("C-438")
                        .rackCallLetter("C")
                        .equipmentId(4660L)
                        .build(),
                RefrigerationSensor.builder()
                        .id("D-438")
                        .rackCallLetter("D")
                        .equipmentId(4661L)
                        .build());
        when(healthMetricsClient.getRackAssets(438L)).thenReturn(CollectionModel.wrap(racks));

        List<RefrigerationSensor> cases = List.of(
                RefrigerationSensor.builder()
                        .id("67234-2342")
                        .caseName("C01a")
                        .build(),
                RefrigerationSensor.builder()
                        .id("67234-2343")
                        .caseName("D02b")
                        .equipmentId(7656L)
                        .build(),
                RefrigerationSensor.builder()
                        .id("67234-2344")
                        .equipmentId(7658L)
                        .build());
        when(healthMetricsClient.getCaseAssets(438L)).thenReturn(CollectionModel.wrap(cases));

        List<RefrigerationSensor> storeAssets = storeReviewAssetService.getAssetsForStore(438L);

        assertThat(storeAssets).hasSize(5);

        assertThat(storeAssets.get(0).getId()).isEqualTo("B-438");
        assertThat(storeAssets.get(0).getRackCallLetter()).isEqualTo("B");
        assertThat(storeAssets.get(0).getAssetName()).isEqualTo("B-438");
        assertThat(storeAssets.get(0).getEquipmentId()).isEqualTo(4659L);

        assertThat(storeAssets.get(1).getId()).isEqualTo("C-438");
        assertThat(storeAssets.get(1).getRackCallLetter()).isEqualTo("C");
        assertThat(storeAssets.get(1).getAssetName()).isEqualTo("C-438");
        assertThat(storeAssets.get(1).getEquipmentId()).isEqualTo(4660L);

        assertThat(storeAssets.get(2).getId()).isEqualTo("D-438");
        assertThat(storeAssets.get(2).getRackCallLetter()).isEqualTo("D");
        assertThat(storeAssets.get(2).getAssetName()).isEqualTo("D-438");
        assertThat(storeAssets.get(2).getEquipmentId()).isEqualTo(4661L);

        assertThat(storeAssets.get(3).getId()).isEqualTo("67234-2342");
        assertThat(storeAssets.get(3).getCaseName()).isEqualTo("C01a");
        assertThat(storeAssets.get(3).getAssetName()).isEqualTo("C01a-438");
        assertThat(storeAssets.get(3).getEquipmentId()).isNull();

        assertThat(storeAssets.get(4).getId()).isEqualTo("67234-2343");
        assertThat(storeAssets.get(4).getCaseName()).isEqualTo("D02b");
        assertThat(storeAssets.get(4).getAssetName()).isEqualTo("D02b-438");
        assertThat(storeAssets.get(4).getEquipmentId()).isEqualTo(7656L);

        verify(healthMetricsClient).getRackAssets(438L);
        verify(healthMetricsClient).getCaseAssets(438L);
    }

}