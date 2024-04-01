package com.walmart.realestate.crystal.storereview.model;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStoreAssetReviewStatusEstrFact {

    private List<UUID> idList;

}
