package com.walmart.realestate.crystal.storereview.client.storeinfo;

import com.walmart.realestate.crystal.storereview.client.storeinfo.model.StoreDetail;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "store", url = "${store-api.url}", configuration = StoreClientConfig.class)
public interface StoreClient {

    @GetMapping("store-info")
    StoreDetail getStoreInfo(@RequestParam Long storeNumber);

    @GetMapping(value = "store-plan/{storeNumber}", produces = MediaType.TEXT_HTML_VALUE)
    String getStorePlan(@PathVariable Long storeNumber);

}
