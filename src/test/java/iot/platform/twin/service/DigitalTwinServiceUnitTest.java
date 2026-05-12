package iot.platform.twin.service;

import iot.platform.device.repository.DeviceRepository;
import iot.platform.exception.ConflictException;
import iot.platform.exception.NotFoundException;
import iot.platform.security.OwnershipGuard;
import iot.platform.twin.model.DigitalTwin;
import iot.platform.twin.model.TwinType;
import iot.platform.twin.repository.DigitalTwinRepository;
import iot.platform.twin.web.dto.TwinRequest;
import iot.platform.twin.web.dto.TwinResponse;
import iot.platform.twin.web.dto.TwinTreeNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DigitalTwinServiceUnitTest {

    @Mock
    private DigitalTwinRepository twinRepository;
    @Mock
    private DeviceRepository deviceRepository;
    @Mock
    private OwnershipGuard ownershipGuard;

    @InjectMocks
    private DigitalTwinService twinService;

    private UUID owner;

    @BeforeEach
    void setUp() {
        owner = UUID.randomUUID();
    }

    @Test
    void create_rootSite_savesWithoutParent() {
        when(twinRepository.existsByOwnerUserIdAndParentIsNullAndName(owner, "Home")).thenReturn(false);
        when(twinRepository.save(any(DigitalTwin.class))).thenAnswer(inv -> {
            DigitalTwin t = inv.getArgument(0);
            t.setId(UUID.randomUUID());
            return t;
        });

        TwinResponse response = twinService.create(owner, new TwinRequest(
                "Home", TwinType.SITE, null, "My main residence", null, "3b82f6", 42.7, 23.3));

        assertThat(response.type()).isEqualTo(TwinType.SITE);
        assertThat(response.parentId()).isNull();
        assertThat(response.color()).isEqualTo("#3b82f6");
    }

    @Test
    void create_childRoomUnderSite_succeeds() {
        UUID siteId = UUID.randomUUID();
        DigitalTwin site = DigitalTwin.builder()
                .id(siteId).ownerUserId(owner).name("Home").type(TwinType.SITE).build();
        when(twinRepository.findById(siteId)).thenReturn(Optional.of(site));
        when(twinRepository.existsByOwnerUserIdAndParentIdAndName(owner, siteId, "Bedroom")).thenReturn(false);
        when(twinRepository.save(any(DigitalTwin.class))).thenAnswer(inv -> {
            DigitalTwin t = inv.getArgument(0);
            t.setId(UUID.randomUUID());
            return t;
        });

        TwinResponse response = twinService.create(owner, new TwinRequest(
                "Bedroom", TwinType.ROOM, siteId, null, "1", null, null, null));

        assertThat(response.parentId()).isEqualTo(siteId);
        assertThat(response.type()).isEqualTo(TwinType.ROOM);
    }

    @Test
    void create_rejectsInvalidParentType() {
        UUID equipmentId = UUID.randomUUID();
        DigitalTwin equipment = DigitalTwin.builder()
                .id(equipmentId).ownerUserId(owner).name("Fridge").type(TwinType.EQUIPMENT).build();
        when(twinRepository.findById(equipmentId)).thenReturn(Optional.of(equipment));

        TwinRequest req = new TwinRequest("Kitchen", TwinType.ROOM, equipmentId, null, null, null, null, null);

        assertThatThrownBy(() -> twinService.create(owner, req))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("cannot be nested under");
    }

    @Test
    void create_rejectsDuplicateSiblingName() {
        UUID siteId = UUID.randomUUID();
        DigitalTwin site = DigitalTwin.builder()
                .id(siteId).ownerUserId(owner).name("Home").type(TwinType.SITE).build();
        when(twinRepository.findById(siteId)).thenReturn(Optional.of(site));
        when(twinRepository.existsByOwnerUserIdAndParentIdAndName(owner, siteId, "Bedroom")).thenReturn(true);

        TwinRequest req = new TwinRequest("Bedroom", TwinType.ROOM, siteId, null, null, null, null, null);

        assertThatThrownBy(() -> twinService.create(owner, req))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("sibling");
    }

    @Test
    void delete_rejectsTwinWithChildren() {
        UUID id = UUID.randomUUID();
        DigitalTwin twin = DigitalTwin.builder().id(id).ownerUserId(owner).name("Home").type(TwinType.SITE).build();
        when(twinRepository.findById(id)).thenReturn(Optional.of(twin));
        when(twinRepository.countByParentId(id)).thenReturn(3L);

        assertThatThrownBy(() -> twinService.delete(id)).isInstanceOf(ConflictException.class);
        verify(twinRepository, never()).delete(any());
    }

    @Test
    void delete_rejectsTwinWithDevices() {
        UUID id = UUID.randomUUID();
        DigitalTwin twin = DigitalTwin.builder().id(id).ownerUserId(owner).name("Bedroom").type(TwinType.ROOM).build();
        when(twinRepository.findById(id)).thenReturn(Optional.of(twin));
        when(twinRepository.countByParentId(id)).thenReturn(0L);
        when(deviceRepository.countByTwin(twin)).thenReturn(2L);

        assertThatThrownBy(() -> twinService.delete(id)).isInstanceOf(ConflictException.class);
        verify(twinRepository, never()).delete(any());
    }

    @Test
    void delete_emptyTwin_deletes() {
        UUID id = UUID.randomUUID();
        DigitalTwin twin = DigitalTwin.builder().id(id).ownerUserId(owner).name("Bedroom").type(TwinType.ROOM).build();
        when(twinRepository.findById(id)).thenReturn(Optional.of(twin));
        when(twinRepository.countByParentId(id)).thenReturn(0L);
        when(deviceRepository.countByTwin(twin)).thenReturn(0L);

        twinService.delete(id);

        verify(twinRepository).delete(twin);
    }

    @Test
    void update_renameWithinSameParent_succeeds() {
        UUID siteId = UUID.randomUUID();
        UUID roomId = UUID.randomUUID();
        DigitalTwin site = DigitalTwin.builder().id(siteId).ownerUserId(owner).name("Home").type(TwinType.SITE).build();
        DigitalTwin room = DigitalTwin.builder().id(roomId).ownerUserId(owner).name("Bedroom").type(TwinType.ROOM).parent(site).build();
        when(twinRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(twinRepository.findById(siteId)).thenReturn(Optional.of(site));
        when(twinRepository.existsByOwnerUserIdAndParentIdAndName(owner, siteId, "Master Bedroom")).thenReturn(false);

        TwinResponse updated = twinService.update(roomId,
                new TwinRequest("Master Bedroom", TwinType.ROOM, siteId, null, null, null, null, null));

        assertThat(updated.name()).isEqualTo("Master Bedroom");
        assertThat(updated.parentId()).isEqualTo(siteId);
    }

    @Test
    void loadOwned_notFound_throws() {
        UUID id = UUID.randomUUID();
        when(twinRepository.findById(id)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> twinService.loadOwned(id)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void tree_buildsForestWithChildren() {
        UUID siteId = UUID.randomUUID();
        UUID roomId = UUID.randomUUID();
        UUID zoneId = UUID.randomUUID();
        DigitalTwin site = DigitalTwin.builder().id(siteId).ownerUserId(owner).name("Home").type(TwinType.SITE).build();
        DigitalTwin room = DigitalTwin.builder().id(roomId).ownerUserId(owner).name("Bedroom").type(TwinType.ROOM).parent(site).build();
        DigitalTwin zone = DigitalTwin.builder().id(zoneId).ownerUserId(owner).name("Window").type(TwinType.ZONE).parent(room).build();
        when(twinRepository.findAllByOwnerUserIdOrderByTypeAscNameAsc(owner)).thenReturn(List.of(site, room, zone));

        List<TwinTreeNode> tree = twinService.tree(owner);

        assertThat(tree).hasSize(1);
        TwinTreeNode root = tree.get(0);
        assertThat(root.name()).isEqualTo("Home");
        assertThat(root.children()).hasSize(1);
        assertThat(root.children().get(0).children().get(0).name()).isEqualTo("Window");
    }
}
