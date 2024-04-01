package com.walmart.realestate.crystal.storereview.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Getter
@Setter
@ConfigurationProperties("server.cors")
public class WebProperties {

    private List<String> origins;

    private List<String> originPatterns;

    private List<String> allowedMethods;

}
