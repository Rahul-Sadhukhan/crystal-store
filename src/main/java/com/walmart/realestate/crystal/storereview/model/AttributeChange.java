package com.walmart.realestate.crystal.storereview.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode
@Getter
@SuperBuilder
public class AttributeChange<T> {

    private final String attribute;

    private final T left;

    private final T right;

}
