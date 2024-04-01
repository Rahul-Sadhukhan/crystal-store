package com.walmart.realestate.crystal.storereview.command;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, defaultImpl = Void.class)
public interface Command {
}
