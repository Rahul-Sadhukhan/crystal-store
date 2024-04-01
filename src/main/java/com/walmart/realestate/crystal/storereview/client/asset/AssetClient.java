package com.walmart.realestate.crystal.storereview.client.asset;


import com.walmart.realestate.crystal.storereview.client.asset.model.Asset;
import com.walmart.realestate.crystal.storereview.client.asset.model.SearchRequest;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.web.bind.annotation.*;

@FeignClient(value = "asset", url = "${asset-api.url}", configuration = AssetConfig.class)
public interface AssetClient {

    @PutMapping("/assets/{id}")
    Object editAsset(@RequestBody Asset asset, @PathVariable Long id);

    @PostMapping("/assets-search")
    @Retry(name = "asset")
    PagedModel<EntityModel<Asset>> getAsset(@RequestBody SearchRequest searchRequest, @SpringQueryMap Pageable pageable);

}
