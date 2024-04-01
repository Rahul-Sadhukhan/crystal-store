package com.walmart.realestate.crystal.storereview.client.estr.model;

import lombok.*;

@EqualsAndHashCode
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EstrChange<T> {

    private String attribute;

    private T left;

    private T right;

}
