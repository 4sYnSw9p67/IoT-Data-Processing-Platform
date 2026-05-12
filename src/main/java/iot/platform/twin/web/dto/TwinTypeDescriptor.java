package iot.platform.twin.web.dto;

import iot.platform.twin.model.TwinType;

import java.util.Set;

public record TwinTypeDescriptor(
        TwinType type,
        Set<TwinType> allowedParents,
        boolean root
) {
    public static TwinTypeDescriptor of(TwinType type) {
        return new TwinTypeDescriptor(type, type.allowedParents(), type.isRoot());
    }
}
