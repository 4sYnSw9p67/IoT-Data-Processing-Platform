package iot.platform.twin.service;

import iot.platform.aspect.Auditable;
import iot.platform.device.repository.DeviceRepository;
import iot.platform.exception.ConflictException;
import iot.platform.exception.NotFoundException;
import iot.platform.security.OwnershipGuard;
import iot.platform.twin.model.DigitalTwin;
import iot.platform.twin.model.TwinType;
import iot.platform.twin.repository.DigitalTwinRepository;
import iot.platform.twin.web.dto.TwinMapper;
import iot.platform.twin.web.dto.TwinRequest;
import iot.platform.twin.web.dto.TwinResponse;
import iot.platform.twin.web.dto.TwinTreeNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DigitalTwinService {

    private final DigitalTwinRepository twinRepository;
    private final DeviceRepository deviceRepository;
    private final OwnershipGuard ownershipGuard;

    @Transactional(readOnly = true)
    public List<TwinResponse> listForUser(UUID ownerUserId, TwinType type, UUID parentId) {
        List<DigitalTwin> twins;
        if (type != null) {
            twins = twinRepository.findAllByOwnerUserIdAndTypeOrderByNameAsc(ownerUserId, type);
        } else if (parentId != null) {
            DigitalTwin parent = loadOwned(parentId);
            twins = twinRepository.findAllByParentIdOrderByNameAsc(parent.getId());
        } else {
            twins = twinRepository.findAllByOwnerUserIdOrderByTypeAscNameAsc(ownerUserId);
        }
        return twins.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public TwinResponse getById(UUID id) {
        return toResponse(loadOwned(id));
    }

    @Transactional(readOnly = true)
    public List<TwinResponse> children(UUID parentId) {
        DigitalTwin parent = loadOwned(parentId);
        return twinRepository.findAllByParentIdOrderByNameAsc(parent.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TwinTreeNode> tree(UUID ownerUserId) {
        List<DigitalTwin> all = twinRepository.findAllByOwnerUserIdOrderByTypeAscNameAsc(ownerUserId);
        Map<UUID, List<DigitalTwin>> byParent = new HashMap<>();
        List<DigitalTwin> roots = new ArrayList<>();
        for (DigitalTwin t : all) {
            if (t.getParent() == null) {
                roots.add(t);
            } else {
                byParent.computeIfAbsent(t.getParent().getId(), k -> new ArrayList<>()).add(t);
            }
        }
        roots.sort(Comparator.comparing(DigitalTwin::getName));
        return roots.stream().map(r -> buildNode(r, byParent)).toList();
    }

    private TwinTreeNode buildNode(DigitalTwin twin, Map<UUID, List<DigitalTwin>> byParent) {
        List<DigitalTwin> kids = byParent.getOrDefault(twin.getId(), List.of());
        List<TwinTreeNode> childNodes = kids.stream()
                .sorted(Comparator.comparing(DigitalTwin::getName))
                .map(c -> buildNode(c, byParent))
                .toList();
        return TwinTreeNode.builder()
                .id(twin.getId())
                .name(twin.getName())
                .type(twin.getType())
                .color(twin.getColor())
                .deviceCount(deviceRepository.countByTwin(twin))
                .children(childNodes)
                .build();
    }

    @Auditable("twin.create")
    @Transactional
    public TwinResponse create(UUID ownerUserId, TwinRequest request) {
        DigitalTwin parent = resolveParent(ownerUserId, request.parentId(), request.type());
        ensureNameAvailable(ownerUserId, parent == null ? null : parent.getId(), request.name(), null);
        DigitalTwin twin = DigitalTwin.builder()
                .name(request.name().trim())
                .type(request.type())
                .ownerUserId(ownerUserId)
                .parent(parent)
                .description(StringUtils.hasText(request.description()) ? request.description().trim() : null)
                .floor(StringUtils.hasText(request.floor()) ? request.floor().trim() : null)
                .color(normalizeColor(request.color()))
                .latitude(request.latitude())
                .longitude(request.longitude())
                .build();
        DigitalTwin saved = twinRepository.save(twin);
        log.info("Twin created id={} name={} type={} owner={} parent={}",
                saved.getId(), saved.getName(), saved.getType(), ownerUserId,
                parent == null ? "<root>" : parent.getId());
        return toResponse(saved);
    }

    @Auditable("twin.update")
    @Transactional
    public TwinResponse update(UUID id, TwinRequest request) {
        DigitalTwin twin = loadOwned(id);

        if (request.type() != twin.getType()) {
            log.info("Twin {} type change requested {}→{}", id, twin.getType(), request.type());
            twin.setType(request.type());
        }

        DigitalTwin newParent = resolveParent(twin.getOwnerUserId(), request.parentId(), twin.getType());
        if (newParent != null && wouldCreateCycle(twin, newParent)) {
            throw new ConflictException("Cannot set a descendant as parent (would create a cycle)");
        }
        UUID newParentId = newParent == null ? null : newParent.getId();
        UUID oldParentId = twin.getParent() == null ? null : twin.getParent().getId();
        boolean parentChanged = !java.util.Objects.equals(newParentId, oldParentId);
        boolean nameChanged = !twin.getName().equals(request.name().trim());
        if (parentChanged || nameChanged) {
            ensureNameAvailable(twin.getOwnerUserId(), newParentId, request.name(), twin.getId());
        }
        twin.setParent(newParent);
        twin.setName(request.name().trim());
        twin.setDescription(StringUtils.hasText(request.description()) ? request.description().trim() : null);
        twin.setFloor(StringUtils.hasText(request.floor()) ? request.floor().trim() : null);
        twin.setColor(normalizeColor(request.color()));
        twin.setLatitude(request.latitude());
        twin.setLongitude(request.longitude());
        log.info("Twin updated id={} owner={}", twin.getId(), twin.getOwnerUserId());
        return toResponse(twin);
    }

    @Auditable("twin.delete")
    @Transactional
    public void delete(UUID id) {
        DigitalTwin twin = loadOwned(id);
        long childCount = twinRepository.countByParentId(twin.getId());
        if (childCount > 0) {
            throw new ConflictException("Cannot delete twin with " + childCount + " child twin(s) — remove or reparent them first");
        }
        long deviceCount = deviceRepository.countByTwin(twin);
        if (deviceCount > 0) {
            throw new ConflictException("Cannot delete twin with " + deviceCount + " assigned device(s)");
        }
        twinRepository.delete(twin);
        log.info("Twin deleted id={} owner={}", id, twin.getOwnerUserId());
    }

    @Transactional(readOnly = true)
    public DigitalTwin loadOwned(UUID id) {
        DigitalTwin twin = twinRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Digital twin not found: " + id));
        ownershipGuard.checkOwnership(twin.getOwnerUserId());
        return twin;
    }

    private DigitalTwin resolveParent(UUID ownerUserId, UUID parentId, TwinType childType) {
        if (parentId == null) {
            if (childType.allowedParents().isEmpty()) {
                return null;
            }
            return null;
        }
        DigitalTwin parent = twinRepository.findById(parentId)
                .orElseThrow(() -> new NotFoundException("Parent digital twin not found: " + parentId));
        ownershipGuard.checkOwnership(parent.getOwnerUserId());
        if (!parent.getOwnerUserId().equals(ownerUserId)) {
            throw new ConflictException("Parent twin belongs to a different owner");
        }
        if (!childType.canHaveParent(parent.getType())) {
            throw new ConflictException("Twin of type " + childType + " cannot be nested under a "
                    + parent.getType() + " (allowed parents: " + childType.allowedParents() + ")");
        }
        return parent;
    }

    private boolean wouldCreateCycle(DigitalTwin twin, DigitalTwin proposedParent) {
        DigitalTwin cursor = proposedParent;
        while (cursor != null) {
            if (cursor.getId().equals(twin.getId())) {
                return true;
            }
            cursor = cursor.getParent();
        }
        return false;
    }

    private void ensureNameAvailable(UUID ownerUserId, UUID parentId, String name, UUID excludeId) {
        boolean exists = parentId == null
                ? twinRepository.existsByOwnerUserIdAndParentIsNullAndName(ownerUserId, name.trim())
                : twinRepository.existsByOwnerUserIdAndParentIdAndName(ownerUserId, parentId, name.trim());
        if (!exists) {
            return;
        }
        if (excludeId != null) {
            DigitalTwin sibling = parentId == null
                    ? twinRepository.findAllByOwnerUserIdAndParentIsNullOrderByNameAsc(ownerUserId).stream()
                            .filter(t -> t.getName().equalsIgnoreCase(name.trim()))
                            .findFirst()
                            .orElse(null)
                    : twinRepository.findAllByParentIdOrderByNameAsc(parentId).stream()
                            .filter(t -> t.getName().equalsIgnoreCase(name.trim()))
                            .findFirst()
                            .orElse(null);
            if (sibling != null && sibling.getId().equals(excludeId)) {
                return;
            }
        }
        throw new ConflictException("A sibling twin named '" + name + "' already exists");
    }

    private String normalizeColor(String color) {
        if (!StringUtils.hasText(color)) {
            return null;
        }
        return color.startsWith("#") ? color.toLowerCase() : "#" + color.toLowerCase();
    }

    private TwinResponse toResponse(DigitalTwin twin) {
        long deviceCount = deviceRepository.countByTwin(twin);
        long childCount = twinRepository.countByParentId(twin.getId());
        return TwinMapper.toResponse(twin, deviceCount, childCount);
    }
}
