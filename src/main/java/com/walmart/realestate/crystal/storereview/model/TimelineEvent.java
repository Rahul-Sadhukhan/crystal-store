package com.walmart.realestate.crystal.storereview.model;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimelineEvent {

    private String eventName;

    private Instant timestamp;

    private int version;

    private String author;

    private String authorName;

    private String state;

    private String flow;

    private List<AttributeChange<JsonNode>> attributeChanges;

}
