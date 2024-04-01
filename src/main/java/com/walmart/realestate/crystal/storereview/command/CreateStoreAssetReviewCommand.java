package com.walmart.realestate.crystal.storereview.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateStoreAssetReviewCommand implements StoreAssetReviewCommand {

    private String storeAssetReviewId;

    private String storeReviewId;

    private Long storeNumber;

    private Long assetId;

    private String assetMappingId;

    private String assetType;

}
