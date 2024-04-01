package com.walmart.realestate.crystal.storereview.repository;

import com.walmart.realestate.crystal.storereview.entity.StoreReviewSuspendEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoreReviewSuspendRepository extends JpaRepository<StoreReviewSuspendEntity, Long> {

    List<StoreReviewSuspendEntity> findByState(String state);
}
