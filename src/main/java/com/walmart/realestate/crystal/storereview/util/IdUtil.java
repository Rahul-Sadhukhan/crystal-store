package com.walmart.realestate.crystal.storereview.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class IdUtil {

    public static UUID uuid(String uuid) {
        return Objects.nonNull(uuid) ? UUID.fromString(uuid) : null;
    }

    public static List<UUID> uuids(List<String> uuidList){
        return uuidList.stream().map(IdUtil::uuid).collect(Collectors.toList());
    }

}
