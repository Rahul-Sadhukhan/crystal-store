package com.walmart.realestate.crystal.storereview.service;

import com.walmart.realestate.crystal.storereview.client.amg.AmgClient;
import com.walmart.realestate.crystal.storereview.client.amg.model.AmgNote;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {AssetMaintenanceService.class})
@ActiveProfiles("test")
class AssetMaintenanceServiceTest {

    @Autowired
    private AssetMaintenanceService assetMaintenanceService;

    @MockBean
    private AmgClient amgClient;

    @Test
    void testGetServiceModel() {
        AmgNote testServiceModelNote = AmgNote.builder()
                .header("SDM")
                .value("Service Model")
                .build();
        when(amgClient.getNote(124L, "SDM")).thenReturn(Optional.of(testServiceModelNote));

        AmgNote serviceModelNote = assetMaintenanceService.getServiceModel(124L);

        assertThat(serviceModelNote).isNotNull();
        assertThat(serviceModelNote).isEqualTo(testServiceModelNote);

        verify(amgClient).getNote(124L, "SDM");
    }

}
