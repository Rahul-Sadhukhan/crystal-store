package com.walmart.realestate.crystal.storereview.entity;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;

@EqualsAndHashCode
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "UserMembership")
public class UserMembershipEntity {

    @Id
    private String groupName;

    private String description;

}
