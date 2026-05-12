package iot.platform.alert.model;

import iot.platform.device.model.Device;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "alerts", indexes = {
        @Index(name = "idx_alert_owner_raised", columnList = "owner_user_id, raised_at DESC")
})
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Alert {

    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @Column(name = "owner_user_id", nullable = false)
    private UUID ownerUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private AlertSource source;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private AlertSeverity severity;

    @Column(nullable = false, length = 500)
    private String message;

    @Column(name = "rule_id")
    private UUID ruleId;

    @Column(name = "raised_at", nullable = false)
    private Instant raisedAt;

    @Column(name = "acknowledged_at")
    private Instant acknowledgedAt;

    @PrePersist
    void onCreate() {
        if (this.raisedAt == null) {
            this.raisedAt = Instant.now();
        }
    }
}
