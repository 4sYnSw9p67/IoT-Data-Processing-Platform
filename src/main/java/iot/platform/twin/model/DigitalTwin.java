package iot.platform.twin.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Generic spatial / asset twin. The same entity models a Site, Building, Floor,
 * Room, Zone, Outdoor area or Equipment instance — the difference is captured by
 * {@link TwinType}. Twins form a forest per owner (each twin has at most one
 * parent, plus zero or more children).
 */
@Entity
@Table(
        name = "digital_twins",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_twin_owner_parent_name",
                        columnNames = {"owner_user_id", "parent_id", "name"})
        },
        indexes = {
                @Index(name = "idx_twin_owner", columnList = "owner_user_id"),
                @Index(name = "idx_twin_owner_type", columnList = "owner_user_id, type"),
                @Index(name = "idx_twin_parent", columnList = "parent_id")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DigitalTwin {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(nullable = false, length = 120)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private TwinType type;

    @Column(name = "owner_user_id", nullable = false)
    private UUID ownerUserId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private DigitalTwin parent;

    @Builder.Default
    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    private List<DigitalTwin> children = new ArrayList<>();

    @Column(length = 500)
    private String description;

    @Column(length = 40)
    private String floor;

    @Column(length = 16)
    private String color;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
