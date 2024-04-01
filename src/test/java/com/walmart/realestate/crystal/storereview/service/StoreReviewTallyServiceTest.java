package com.walmart.realestate.crystal.storereview.service;

import com.walmart.realestate.crystal.storereview.model.StoreReviewTally;
import com.walmart.realestate.crystal.storereview.repository.StoreReviewTallyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {StoreReviewTallyService.class})
@ActiveProfiles("test")
public class StoreReviewTallyServiceTest {

    @Autowired
    private StoreReviewTallyService storeReviewTallyService;

    @MockBean
    private StoreReviewTallyRepository storeReviewTallyRepository;

    @Test
    void testGetStoreReviewTally() {
        List<StoreReviewTally> testStoreReviewTallies = Arrays.asList(
                StoreReviewTally.builder()
                        .state("state0")
                        .totalCount(15)
                        .build(),
                StoreReviewTally.builder()
                        .state("state1")
                        .totalCount(25)
                        .build()
        );
        when(storeReviewTallyRepository.countState()).thenReturn(testStoreReviewTallies);

        List<StoreReviewTally> storeReviewTallies = storeReviewTallyService.getStoreReviewTallyByState(Optional.empty());

        assertThat(storeReviewTallies).hasSize(2);
        assertThat(storeReviewTallies).isEqualTo(testStoreReviewTallies);

        verify(storeReviewTallyRepository).countState();
    }

    @Test
    void testGetStoreReviewTallyWithUser() {
        List<StoreReviewTally> testStoreReviewTallies = Arrays.asList(
                StoreReviewTally.builder()
                        .state("state0")
                        .totalCount(110)
                        .assigneeCount(5)
                        .createdByCount(25)
                        .build(),
                StoreReviewTally.builder()
                        .state("state1")
                        .totalCount(80)
                        .assigneeCount(3)
                        .createdByCount(0)
                        .build());
        when(storeReviewTallyRepository.countStateByUser("user0")).thenReturn(testStoreReviewTallies);

        List<StoreReviewTally> storeReviewTallies = storeReviewTallyService.getStoreReviewTallyByState(Optional.of("user0"));

        assertThat(storeReviewTallies).hasSize(2);

        List<StoreReviewTally> expectedStoreReviewTallies = Arrays.asList(
                StoreReviewTally.builder()
                        .user("user0")
                        .state("state0")
                        .totalCount(110)
                        .assigneeCount(5)
                        .createdByCount(25)
                        .build(),
                StoreReviewTally.builder()
                        .user("user0")
                        .state("state1")
                        .totalCount(80)
                        .assigneeCount(3)
                        .createdByCount(0)
                        .build());
        assertThat(storeReviewTallies).isEqualTo(expectedStoreReviewTallies);

        verify(storeReviewTallyRepository).countStateByUser("user0");
    }

}
