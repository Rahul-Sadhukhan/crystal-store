package com.walmart.realestate.crystal.settingchangelog.service;

import com.walmart.realestate.crystal.annotation.Logger;
import com.walmart.realestate.crystal.settingchangelog.entity.SettingChangeLogEntity;
import com.walmart.realestate.crystal.settingchangelog.repository.SettingChangeLogRepository;
import com.walmart.realestate.crystal.storereview.model.StoreReview;
import com.walmart.realestate.crystal.storereview.service.StoreReviewService;
import com.walmart.realestate.soteria.model.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import javax.validation.ValidationException;
import java.util.Optional;
import java.util.function.Predicate;

@Service
@RequiredArgsConstructor
public class SettingChangeLogDeleteService {

    private final SettingChangeLogRepository settingChangeLogRepository;

    private final StoreReviewService storeReviewService;

    @Logger
    public void deleteSettingChangeLog(String id, String storeReviewId, String storeReviewState, UserContext userContext) {
        if ("inProgress".equals(storeReviewState)) {
            Optional<SettingChangeLogEntity> settingChangeLogEntityOptional = settingChangeLogRepository.findById(id);
            if (settingChangeLogEntityOptional.isPresent()) {
                SettingChangeLogEntity settingChangeLogEntity = settingChangeLogEntityOptional.get();
                Predicate<StoreReview> isStateMatching = storeReview -> storeReviewState.equals(storeReview.getState());
                Predicate<StoreReview> isReferenceMatching = storeReview -> Optional.ofNullable(settingChangeLogEntity.getReferenceId())
                        .map(reference -> reference.equals(storeReview.getId()))
                        .orElse(false);
                Predicate<StoreReview> isAssigneeMatching = storeReview -> userContext.getUsername().equals(storeReview.getAssignee());
                Predicate<StoreReview> isTimestampValid = storeReview -> Optional.ofNullable(settingChangeLogEntity.getCreatedAt())
                        .map(createdAt -> createdAt.isAfter(storeReview.getLastStartedAt()))
                        .orElse(false);
                boolean isValid = isStateMatching
                        .and(isReferenceMatching)
                        .and(isAssigneeMatching)
                        .and(isTimestampValid)
                        .test(storeReviewService.getStoreReview(storeReviewId));
                if (isValid) settingChangeLogRepository.deleteById(id);
                else throw new ValidationException("Validation failed for state, reference, assignee or timestamp");
            } else throw new EntityNotFoundException("Invalid setting change log");
        } else throw new ValidationException("Log can be deleted only when store review is in progress");
    }

}
