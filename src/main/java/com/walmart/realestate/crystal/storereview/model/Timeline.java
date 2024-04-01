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
public class Timeline<T> {

    private T entity;

    private List<TimelineEvent> events;

}
