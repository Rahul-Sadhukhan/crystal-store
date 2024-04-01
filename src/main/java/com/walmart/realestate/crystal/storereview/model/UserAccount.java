package com.walmart.realestate.crystal.storereview.model;

import com.walmart.realestate.crystal.storereview.entity.UserMembershipEntity;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAccount {

    private String userId;

    private String firstName;

    private String lastName;

    private String fullName;

    private String email;

    private Set<UserMembershipEntity> memberships;

}
