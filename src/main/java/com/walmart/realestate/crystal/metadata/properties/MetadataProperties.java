package com.walmart.realestate.crystal.metadata.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@Getter
@Setter
@ConfigurationProperties("metadata")
public class MetadataProperties {

    private Map<String, String> assetTypes;

}
