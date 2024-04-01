package com.walmart.realestate.crystal.storereview.model;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStoreAssetReviewStatus {

    private List<String> storeAssetReviewIdList;

}
