import apiClient from "./client";

export type ReportFormat = "EXCEL" | "PDF";

export async function downloadReport(
  deviceId: string,
  from: string,
  to: string,
  format: ReportFormat
): Promise<{ blob: Blob; filename: string }> {
  const response = await apiClient.get(
    `/api/reports/devices/${deviceId}/measurements`,
    {
      params: { from, to, format },
      responseType: "blob",
    }
  );
  const disposition = response.headers["content-disposition"] as
    | string
    | undefined;
  const match = disposition?.match(/filename="?([^";]+)"?/);
  return {
    blob: response.data as Blob,
    filename: match?.[1] ?? `report.${format.toLowerCase()}`,
  };
}
