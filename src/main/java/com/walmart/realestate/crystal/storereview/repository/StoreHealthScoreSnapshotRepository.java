package com.walmart.realestate.crystal.storereview.repository;

import com.walmart.realestate.crystal.storereview.entity.StoreHealthScoreSnapshotEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface StoreHealthScoreSnapshotRepository extends JpaRepository<StoreHealthScoreSnapshotEntity, Long> {

    StoreHealthScoreSnapshotEntity findTopByOrderByRunTimeDesc();


}
