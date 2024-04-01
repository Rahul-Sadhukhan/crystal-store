package com.walmart.realestate.crystal.storereview.client.estr.model;

import lombok.*;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstrWorkflow {

    private EstrState state;

    @Singular
    private List<EstrTransition> nextActions;

}
