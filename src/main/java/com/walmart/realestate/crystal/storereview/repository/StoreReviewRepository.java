package com.walmart.realestate.crystal.storereview.repository;

import com.walmart.realestate.crystal.storereview.entity.StoreReviewEntity;
import com.walmart.realestate.crystal.storereview.repository.projection.StoreReviewStoreNumberStateReviewType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StoreReviewRepository extends JpaRepository<StoreReviewEntity, String>, JpaSpecificationExecutor<StoreReviewEntity> {

    StoreReviewEntity findByUuid(String uuid);

    List<StoreReviewEntity> findByIdIn(List<String> idList);

    List<StoreReviewEntity> findByStoreNumber(Long storeNumber);

    Long countByStateInAndAssignedAtAfter(List<String> state, Instant after);

    List<StoreReviewStoreNumberStateReviewType> findStoreNumbersByAssignedAtGreaterThanOrStartDateBetween(
            Instant assignedAt,
            LocalDateTime startDateFirst,
            LocalDateTime startDateSecond);
}

