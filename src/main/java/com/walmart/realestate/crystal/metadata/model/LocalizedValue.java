package com.walmart.realestate.crystal.metadata.model;

import lombok.Builder;
import lombok.Data;

import java.util.Locale;

@Data
@Builder
public class LocalizedValue {

    private String value;

    private Locale locale;

}
