package com.walmart.realestate.crystal.storereview.client.estr.model;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;

import java.time.Instant;
import java.util.List;

@EqualsAndHashCode
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstrTimelineEvent {

    private String eventName;

    private Instant timestamp;

    private int version;

    private String author;

    private EstrState state;

    private List<EstrChange<JsonNode>> attributeChanges;

}
