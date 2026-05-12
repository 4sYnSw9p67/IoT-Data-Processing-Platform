package iot.platform.twin.web.dto;

import iot.platform.twin.model.TwinType;
import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record TwinTreeNode(
        UUID id,
        String name,
        TwinType type,
        String color,
        long deviceCount,
        List<TwinTreeNode> children
) {
}
