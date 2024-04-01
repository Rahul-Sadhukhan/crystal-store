package com.walmart.realestate.crystal.storereview.command;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.walmart.realestate.crystal.storereview.util.JacksonUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeclineStoreReviewCommand implements StoreReviewCommand {

    private final String assignee = null;

    @JsonProperty(access = READ_ONLY)
    @JsonSerialize(nullsUsing = JacksonUtil.UserIdSerializer.class)
    private String declinedBy;

    private String reasonForDeclining;

}
