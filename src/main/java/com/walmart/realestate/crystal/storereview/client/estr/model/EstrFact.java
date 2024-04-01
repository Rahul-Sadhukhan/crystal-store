package com.walmart.realestate.crystal.storereview.client.estr.model;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@EqualsAndHashCode
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstrFact {

    private UUID id;

    private String type;

    private String state;

    private String flow;

    private ObjectNode attributes;

    private String createdBy;

    private Instant createdAt;

    private String lastModifiedBy;

    private Instant lastModifiedAt;

}
