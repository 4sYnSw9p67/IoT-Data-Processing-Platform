package iot.platform.report.service;

import iot.platform.device.model.Device;
import iot.platform.device.model.DeviceType;
import iot.platform.device.service.DeviceService;
import iot.platform.measurement.model.Measurement;
import iot.platform.measurement.repository.MeasurementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceUnitTest {

    @Mock
    private DeviceService deviceService;
    @Mock
    private MeasurementRepository measurementRepository;

    @InjectMocks
    private ReportService reportService;

    private Device device;
    private List<Measurement> measurements;

    @BeforeEach
    void setUp() {
        device = Device.builder()
                .id(UUID.randomUUID())
                .name("Living Room Sensor")
                .type(DeviceType.COMBO)
                .ownerUserId(UUID.randomUUID())
                .active(true)
                .build();
        Measurement m1 = Measurement.builder()
                .id(UUID.randomUUID())
                .device(device)
                .takenAt(Instant.parse("2025-01-01T10:00:00Z"))
                .temperatureC(21.5)
                .humidityPct(45.0)
                .build();
        Measurement m2 = Measurement.builder()
                .id(UUID.randomUUID())
                .device(device)
                .takenAt(Instant.parse("2025-01-01T11:00:00Z"))
                .temperatureC(22.0)
                .humidityPct(46.0)
                .build();
        measurements = List.of(m1, m2);
    }

    @Test
    void generateExcelReport_returnsXlsxBytes() {
        when(deviceService.loadOwned(device.getId())).thenReturn(device);
        when(measurementRepository.findRange(any(), any(), any())).thenReturn(measurements);

        ReportService.Report report = reportService.generateMeasurementReport(
                device.getId(),
                Instant.parse("2025-01-01T00:00:00Z"),
                Instant.parse("2025-01-02T00:00:00Z"),
                ReportFormat.EXCEL);

        assertThat(report.filename()).endsWith(".xlsx");
        assertThat(report.contentType()).isEqualTo(ReportFormat.EXCEL.contentType());
        assertThat(report.bytes()).isNotEmpty();
        assertThat(new String(report.bytes(), 0, 2)).isEqualTo("PK");
    }

    @Test
    void generatePdfReport_returnsPdfBytes() {
        when(deviceService.loadOwned(device.getId())).thenReturn(device);
        when(measurementRepository.findRange(any(), any(), any())).thenReturn(measurements);

        ReportService.Report report = reportService.generateMeasurementReport(
                device.getId(),
                Instant.parse("2025-01-01T00:00:00Z"),
                Instant.parse("2025-01-02T00:00:00Z"),
                ReportFormat.PDF);

        assertThat(report.filename()).endsWith(".pdf");
        assertThat(report.contentType()).isEqualTo(ReportFormat.PDF.contentType());
        assertThat(report.bytes()).isNotEmpty();
        assertThat(new String(report.bytes(), 0, 4)).startsWith("%PDF");
    }
}
