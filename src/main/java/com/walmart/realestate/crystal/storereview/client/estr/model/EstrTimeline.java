package com.walmart.realestate.crystal.storereview.client.estr.model;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@EqualsAndHashCode
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstrTimeline {

    private UUID id;

    private Instant latestTimestamp;

    private int latestVersion;

    private ObjectNode attributes;

    private List<EstrTimelineEvent> events;

}
