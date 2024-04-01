package com.walmart.realestate.crystal.storereview.entity;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import java.util.Set;

@EqualsAndHashCode
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "UserAccount")
public class UserAccountEntity {

    @Id
    private String userId;

    private String firstName;

    private String lastName;

    private String fullName;

    private String email;

    @Singular
    @ManyToMany(fetch = FetchType.EAGER)
    private Set<UserMembershipEntity> memberships;

}
