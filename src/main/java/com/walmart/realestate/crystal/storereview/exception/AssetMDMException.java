package com.walmart.realestate.crystal.storereview.exception;

import lombok.Getter;

@Getter
public class AssetMDMException extends RuntimeException {

    private final ErrorResponse response;

    public AssetMDMException(ErrorResponse response) {
        this.response = response;
    }

}


