package com.walmart.realestate.crystal.storereview.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Getter
@SuperBuilder
public class AssigneeChange<T> extends AttributeChange<T> {

    private final String leftName;

    private final String rightName;

}
