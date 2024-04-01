package com.walmart.realestate.crystal.storereview.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Workflow<T> {

    private T entity;

    private String state;

    private String flow;

    private List<Transition> transitions;

}
