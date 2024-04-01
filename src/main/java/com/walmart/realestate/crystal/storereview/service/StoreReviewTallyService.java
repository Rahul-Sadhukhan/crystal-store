package com.walmart.realestate.crystal.storereview.service;

import com.walmart.realestate.crystal.annotation.Logger;
import com.walmart.realestate.crystal.storereview.model.StoreReviewTally;
import com.walmart.realestate.crystal.storereview.repository.StoreReviewTallyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class StoreReviewTallyService {

    private final StoreReviewTallyRepository storeReviewTallyRepository;

    @Logger
    public List<StoreReviewTally> getStoreReviewTallyByState(Optional<String> userIdOptional) {
        return userIdOptional.map(user -> storeReviewTallyRepository.countStateByUser(user).stream()
                .peek(tally -> tally.setUser(user))
                .collect(Collectors.toList()))
                .orElseGet(storeReviewTallyRepository::countState);
    }

}
