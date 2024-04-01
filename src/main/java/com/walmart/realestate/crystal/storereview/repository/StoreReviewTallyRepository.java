package com.walmart.realestate.crystal.storereview.repository;

import com.walmart.realestate.crystal.storereview.entity.StoreReviewEntity;
import com.walmart.realestate.crystal.storereview.model.StoreReviewTally;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoreReviewTallyRepository extends JpaRepository<StoreReviewEntity, String> {

    @Query("select new com.walmart.realestate.crystal.storereview.model.StoreReviewTally(state, count(state)) from StoreReview group by state")
    List<StoreReviewTally> countState();

    @Query("select new com.walmart.realestate.crystal.storereview.model.StoreReviewTally(state, count(state), sum(case assignee when :user then 1 else 0 end), sum(case createdBy when :user then 1 else 0 end)) from StoreReview group by state")
    List<StoreReviewTally> countStateByUser(String user);

}
