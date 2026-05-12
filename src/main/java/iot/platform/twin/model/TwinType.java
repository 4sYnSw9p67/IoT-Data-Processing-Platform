package iot.platform.twin.model;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Categories of digital twins supported by the platform. The platform expects
 * the spatial hierarchy {@code SITE > BUILDING > FLOOR > ROOM > ZONE}, while
 * {@link #OUTDOOR} and {@link #EQUIPMENT} cover the common non-room locations
 * (gardens, balconies, fridges, server cabinets, HVAC units, etc.).
 *
 * The {@link #allowedParents()} set is advisory and is enforced by
 * {@code DigitalTwinService} so the UI can rely on consistent trees.
 */
public enum TwinType {
    SITE,
    BUILDING,
    FLOOR,
    ROOM,
    ZONE,
    OUTDOOR,
    EQUIPMENT;

    private static final Map<TwinType, Set<TwinType>> ALLOWED_PARENTS;

    static {
        ALLOWED_PARENTS = new EnumMap<>(TwinType.class);
        ALLOWED_PARENTS.put(SITE, EnumSet.noneOf(TwinType.class));
        ALLOWED_PARENTS.put(BUILDING, EnumSet.of(SITE));
        ALLOWED_PARENTS.put(FLOOR, EnumSet.of(SITE, BUILDING));
        ALLOWED_PARENTS.put(ROOM, EnumSet.of(SITE, BUILDING, FLOOR));
        ALLOWED_PARENTS.put(ZONE, EnumSet.of(ROOM));
        ALLOWED_PARENTS.put(OUTDOOR, EnumSet.of(SITE, BUILDING));
        ALLOWED_PARENTS.put(EQUIPMENT, EnumSet.of(ROOM, ZONE, OUTDOOR));
    }

    public Set<TwinType> allowedParents() {
        return ALLOWED_PARENTS.get(this);
    }

    public boolean canHaveParent(TwinType parentType) {
        return parentType != null && allowedParents().contains(parentType);
    }

    public boolean isRoot() {
        return allowedParents().isEmpty();
    }
}
