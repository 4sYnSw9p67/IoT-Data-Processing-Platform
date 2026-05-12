package iot.platform.report.service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import iot.platform.aspect.Auditable;
import iot.platform.device.model.Device;
import iot.platform.device.service.DeviceService;
import iot.platform.measurement.model.Measurement;
import iot.platform.measurement.repository.MeasurementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final DeviceService deviceService;
    private final MeasurementRepository measurementRepository;

    @Auditable("report.measurements")
    @Transactional(readOnly = true)
    public Report generateMeasurementReport(UUID deviceId, Instant from, Instant to, ReportFormat format) {
        Device device = deviceService.loadOwned(deviceId);
        List<Measurement> measurements = measurementRepository.findRange(device, from, to);
        log.info("Generating {} report for device={} rows={} window=[{},{}]",
                format, deviceId, measurements.size(), from, to);
        byte[] body = switch (format) {
            case EXCEL -> renderExcel(device, measurements, from, to);
            case PDF -> renderPdf(device, measurements, from, to);
        };
        String filename = "measurements-%s-%s-%s.%s".formatted(
                device.getName().replaceAll("[^a-zA-Z0-9-]", "_"),
                from.toEpochMilli(), to.toEpochMilli(), format.extension());
        return new Report(filename, format.contentType(), body);
    }

    private byte[] renderExcel(Device device, List<Measurement> measurements, Instant from, Instant to) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Measurements");
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(boldFont(workbook));
            Row titleRow = sheet.createRow(0);
            titleRow.createCell(0).setCellValue("Device: " + device.getName());
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));
            Row windowRow = sheet.createRow(1);
            windowRow.createCell(0).setCellValue("Window: " + from + " - " + to);
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 3));

            Row header = sheet.createRow(3);
            String[] columns = {"Taken At (UTC)", "Temperature (C)", "Humidity (%)", "Raw Payload"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }
            int rowNum = 4;
            for (Measurement m : measurements) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(m.getTakenAt().toString());
                if (m.getTemperatureC() != null) {
                    row.createCell(1).setCellValue(m.getTemperatureC());
                }
                if (m.getHumidityPct() != null) {
                    row.createCell(2).setCellValue(m.getHumidityPct());
                }
                if (m.getRawPayload() != null) {
                    row.createCell(3).setCellValue(m.getRawPayload());
                }
            }
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to render Excel report", ex);
        }
    }

    private byte[] renderPdf(Device device, List<Measurement> measurements, Instant from, Instant to) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, out);
            document.open();
            Font titleFont = new Font(Font.HELVETICA, 16, Font.BOLD);
            Font normalFont = new Font(Font.HELVETICA, 10);
            document.add(new Paragraph("Measurement Report", titleFont));
            document.add(new Paragraph("Device: " + device.getName(), normalFont));
            document.add(new Paragraph("Window: " + from + " - " + to, normalFont));
            document.add(new Paragraph("Rows: " + measurements.size(), normalFont));
            document.add(new Paragraph(" ", normalFont));

            PdfPTable table = new PdfPTable(3);
            table.setWidthPercentage(100);
            table.setSpacingBefore(8f);
            table.setWidths(new float[]{3f, 2f, 2f});
            addHeaderCell(table, "Taken At (UTC)");
            addHeaderCell(table, "Temperature (C)");
            addHeaderCell(table, "Humidity (%)");
            for (Measurement m : measurements) {
                table.addCell(new PdfPCell(new Phrase(m.getTakenAt().toString(), normalFont)));
                table.addCell(new PdfPCell(new Phrase(m.getTemperatureC() == null ? "-" : String.format("%.2f", m.getTemperatureC()), normalFont)));
                table.addCell(new PdfPCell(new Phrase(m.getHumidityPct() == null ? "-" : String.format("%.2f", m.getHumidityPct()), normalFont)));
            }
            document.add(table);
            document.add(new Paragraph("Generated at " + LocalDateTime.now(ZoneOffset.UTC) + " UTC", normalFont));
            document.close();
            return out.toByteArray();
        } catch (DocumentException | IOException ex) {
            throw new IllegalStateException("Failed to render PDF report", ex);
        }
    }

    private void addHeaderCell(PdfPTable table, String label) {
        PdfPCell cell = new PdfPCell(new Phrase(label, new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE)));
        cell.setBackgroundColor(new Color(60, 90, 200));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(6f);
        table.addCell(cell);
    }

    private static org.apache.poi.ss.usermodel.Font boldFont(Workbook workbook) {
        org.apache.poi.ss.usermodel.Font font = workbook.createFont();
        font.setBold(true);
        return font;
    }

    public record Report(String filename, String contentType, byte[] bytes) {
    }
}
