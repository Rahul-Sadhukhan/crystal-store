package com.walmart.realestate.crystal.storereview.report;

import lombok.Builder;
import lombok.Value;

import java.util.function.Function;

@Value
@Builder
public class WorkbookManifest<T> {

    String field;

    Type dataType;

    Function<T, Object> extractor;

    public enum Type {
        BOOLEAN, STRING, NUMBER, DATE
    }

}
