package com.walmart.realestate.crystal.storereview.repository;

import com.walmart.realestate.crystal.storereview.entity.UserAccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<UserAccountEntity, String> {

    List<UserAccountEntity> findDistinctByMembershipsGroupNameIn(List<String> groupNames);

    UserAccountEntity findTopByFullName(String fullName);

}
