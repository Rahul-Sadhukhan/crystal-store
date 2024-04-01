package com.walmart.realestate.crystal.metadata.service;

import com.walmart.realestate.crystal.metadata.entity.MetadataTypeEntity;
import com.walmart.realestate.crystal.metadata.model.MetadataType;
import com.walmart.realestate.crystal.metadata.repository.MetadataTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {MetadataTypeService.class})
@ActiveProfiles("test")
class MetadataTypeServiceTest {

    @Autowired
    private MetadataTypeService metadataTypeService;

    @MockBean
    private MetadataTypeRepository metadataTypeRepository;

    private MetadataTypeEntity metadataTypeEntity;

    private MetadataType testMetadataType;

    @BeforeEach
    void setUp() {
        metadataTypeEntity = MetadataTypeEntity.builder()
                .name("settings")
                .id("settings")
                .build();

        testMetadataType = MetadataType.builder()
                .name("settings")
                .id("settings")
                .build();
    }

    @Test
    void createMetadataTypeUpdateTest() {
        when(metadataTypeRepository.findById("settings")).thenReturn(Optional.of(metadataTypeEntity));
        when(metadataTypeRepository.save(metadataTypeEntity)).thenReturn(metadataTypeEntity);

        MetadataType metadataType = metadataTypeService.createMetadataType(testMetadataType);

        assertThat(metadataType.getId()).isEqualTo("settings");
        assertThat(metadataType.getName()).isEqualTo("settings");

        verify(metadataTypeRepository).findById("settings");
        verify(metadataTypeRepository).save(metadataTypeEntity);
    }

    @Test
    void createMetadataTypeTest() {
        when(metadataTypeRepository.findById("settings")).thenReturn(Optional.empty());
        when(metadataTypeRepository.save(metadataTypeEntity)).thenReturn(metadataTypeEntity);

        MetadataType metadataType = metadataTypeService.createMetadataType(testMetadataType);

        assertThat(metadataType.getId()).isEqualTo("settings");
        assertThat(metadataType.getName()).isEqualTo("settings");

        verify(metadataTypeRepository).findById("settings");
        verify(metadataTypeRepository).save(metadataTypeEntity);
    }

    @Test
    void getMetadataTypeByIdTest() {
        when(metadataTypeRepository.findById("settings")).thenReturn(Optional.of(metadataTypeEntity));

        MetadataType metadataType = metadataTypeService.getMetadataTypeById("settings");

        assertThat(metadataType.getId()).isEqualTo("settings");
        assertThat(metadataType.getName()).isEqualTo("settings");

        verify(metadataTypeRepository).findById("settings");
    }

    @Test
    void getMetadataTypesest() {
        when(metadataTypeRepository.findAll()).thenReturn(Collections.singletonList(metadataTypeEntity));

        List<MetadataType> metadataTypes = metadataTypeService.getMetadataTypes();

        assertThat(metadataTypes).isNotNull();
        assertThat(metadataTypes).hasSize(1);
        assertThat(metadataTypes.get(0).getId()).isEqualTo("settings");
        assertThat(metadataTypes.get(0).getName()).isEqualTo("settings");

        verify(metadataTypeRepository).findAll();
    }

}
