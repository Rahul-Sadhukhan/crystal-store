package com.walmart.realestate.crystal.metadata.service;

import com.walmart.realestate.crystal.metadata.entity.MetadataItemEntity;
import com.walmart.realestate.crystal.metadata.exception.MetadataTypeInvalidException;
import com.walmart.realestate.crystal.metadata.model.LocalizedValue;
import com.walmart.realestate.crystal.metadata.model.MetadataItem;
import com.walmart.realestate.crystal.metadata.model.MetadataType;
import com.walmart.realestate.crystal.metadata.properties.MetadataProperties;
import com.walmart.realestate.crystal.metadata.repository.MetadataItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {MetadataItemService.class, MetadataProperties.class})
@EnableConfigurationProperties(MetadataProperties.class)
@ActiveProfiles("test")
class MetadataItemServiceTest {

    @Autowired
    private MetadataItemService metadataItemService;

    @MockBean
    private MetadataTypeService metadataTypeService;

    @MockBean
    private MetadataItemRepository metadataItemRepository;

    private MetadataItemEntity metadataItemEntityOne;

    private MetadataType metadataType;

    private MetadataItem metadataItemOne;

    private List<MetadataItemEntity> itemEntityList;

    @BeforeEach
    void setUp() {
        metadataItemEntityOne = MetadataItemEntity.builder()
                .id("test")
                .defaultValue("test")
                .assetTypes(Arrays.asList("type0", "type1"))
                .index(1)
                .isEnabled(true)
                .values(Arrays.asList(LocalizedValue.builder()
                                .value("testcanada")
                                .locale(Locale.CANADA)
                                .build(),
                        LocalizedValue.builder()
                                .value("testfrench")
                                .locale(Locale.FRANCE)
                                .build()))
                .type("settings")
                .maxValue("10")
                .minValue("20")
                .unit("psi")
                .build();

        MetadataItemEntity metadataItemEntityTwo = MetadataItemEntity.builder()
                .id("suction-pressure")
                .defaultValue("suction-pressure test")
                .assetTypes(Arrays.asList("type0", "type1"))
                .index(2)
                .isEnabled(true)
                .values(Arrays.asList(LocalizedValue.builder()
                                .value("suction-pressure test")
                                .locale(Locale.CANADA)
                                .build(),
                        LocalizedValue.builder()
                                .value("suction-pressure test")
                                .locale(Locale.FRANCE)
                                .build()))
                .type("settings")
                .maxValue("10")
                .minValue("20")
                .unit("psi")
                .build();

        metadataItemOne = MetadataItem.builder()
                .id("test")
                .defaultValue("test")
                .assetTypes(Arrays.asList("type0", "type1"))
                .index(1)
                .isEnabled(true)
                .values(Arrays.asList(LocalizedValue
                                .builder()
                                .value("testcanada")
                                .locale(Locale.CANADA)
                                .build(),
                        LocalizedValue.builder()
                                .value("testfrench")
                                .locale(Locale.FRANCE)
                                .build()))
                .metadataType("settings")
                .maxValue("10")
                .minValue("20")
                .unit("psi")
                .build();

        metadataType = MetadataType.builder()
                .name("settings")
                .id("settings")
                .build();

        itemEntityList = Arrays.asList(metadataItemEntityOne, metadataItemEntityTwo);
    }

    @Test
    void createMetadataItemSaveTest() {
        when(metadataItemRepository.findById("test")).thenReturn(Optional.empty());
        when(metadataTypeService.getMetadataTypeById("settings")).thenReturn(metadataType);
        when(metadataItemRepository.save(metadataItemEntityOne)).thenReturn(metadataItemEntityOne);

        MetadataItem metadataItem = metadataItemService.createMetadataItem(metadataItemOne);

        assertThat(metadataItem.getId()).isEqualTo("test");
        assertThat(metadataItem.getDefaultValue()).isEqualTo("test");
        assertThat(metadataItem.getValues()).hasSize(2);
        assertThat(metadataItem.getIndex()).isEqualTo(1);

        verify(metadataItemRepository).findById("test");
        verify(metadataTypeService).getMetadataTypeById("settings");
        verify(metadataItemRepository).save(metadataItemEntityOne);
    }

    @Test
    void createMetadataItemUpdateTest() {
        when(metadataItemRepository.findById("test")).thenReturn(Optional.of(metadataItemEntityOne));
        when(metadataTypeService.getMetadataTypeById("settings")).thenReturn(metadataType);
        when(metadataItemRepository.save(metadataItemEntityOne)).thenReturn(metadataItemEntityOne);

        MetadataItem metadataItem = metadataItemService.createMetadataItem(metadataItemOne);

        assertThat(metadataItem.getId()).isEqualTo("test");
        assertThat(metadataItem.getDefaultValue()).isEqualTo("test");
        assertThat(metadataItem.getValues()).hasSize(2);
        assertThat(metadataItem.getIndex()).isEqualTo(1);

        verify(metadataItemRepository).findById("test");
        verify(metadataTypeService).getMetadataTypeById("settings");
        verify(metadataItemRepository).save(metadataItemEntityOne);
    }

    @Test
    void createMetadataItemExceptionTest() {
        when(metadataTypeService.getMetadataTypeById("settings")).thenReturn(null);

        assertThatThrownBy(() -> metadataItemService.createMetadataItem(metadataItemOne))
                .isInstanceOf(MetadataTypeInvalidException.class);

        verify(metadataTypeService).getMetadataTypeById("settings");
    }

    @Test
    void getMetadataItemsWithParamTest() {
        when(metadataItemRepository.findByTypeInAndAssetTypesInAndIsEnabledTrue(Collections.singletonList("settings"), Arrays.asList("rack", "case"))).thenReturn(itemEntityList);

        List<MetadataItem> metadataItems = metadataItemService.getMetadataItems(Collections.singletonList("settings"), Arrays.asList("Rack", "Refrigerated Case"), false);

        assertThat(metadataItems).hasSize(2);
        assertThat(metadataItems.get(0).getId()).isEqualTo("test");
        assertThat(metadataItems.get(1).getId()).isEqualTo("suction-pressure");

        verify(metadataItemRepository).findByTypeInAndAssetTypesInAndIsEnabledTrue(Collections.singletonList("settings"), Arrays.asList("rack", "case"));
    }

    @Test
    void getMetadataItemsNoParamTest() {
        when(metadataItemRepository.findByIsEnabledTrue()).thenReturn(itemEntityList);

        List<MetadataItem> metadataItems = metadataItemService.getMetadataItems(new ArrayList<>(), new ArrayList<>(), false);

        assertThat(metadataItems).hasSize(2);
        assertThat(metadataItems.get(0).getId()).isEqualTo("test");
        assertThat(metadataItems.get(1).getId()).isEqualTo("suction-pressure");

        verify(metadataItemRepository).findByIsEnabledTrue();
    }

    @Test
    void getMetadataItemsWithParamIsDisabledTrueTest() {
        when(metadataItemRepository.findByTypeInAndAssetTypesIn(Collections.singletonList("settings"), Arrays.asList("rack", "type1"))).thenReturn(itemEntityList);

        List<MetadataItem> metadataItems = metadataItemService.getMetadataItems(Collections.singletonList("settings"), Arrays.asList("Rack", "type1"), true);

        assertThat(metadataItems).hasSize(2);
        assertThat(metadataItems.get(0).getId()).isEqualTo("test");
        assertThat(metadataItems.get(1).getId()).isEqualTo("suction-pressure");

        verify(metadataItemRepository).findByTypeInAndAssetTypesIn(Collections.singletonList("settings"), Arrays.asList("rack", "type1"));
    }

}
