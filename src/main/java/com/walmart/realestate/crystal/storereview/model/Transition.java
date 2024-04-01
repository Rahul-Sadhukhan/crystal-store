package com.walmart.realestate.crystal.storereview.model;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transition {

    private String action;

    private String command;

    private JsonNode commandModel;

    private String event;

    private List<String> eventAttributes;

}
