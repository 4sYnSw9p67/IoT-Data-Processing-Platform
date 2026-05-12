import apiClient from "./client";

export interface Measurement {
  id: string;
  deviceId: string;
  takenAt: string;
  temperatureC?: number | null;
  humidityPct?: number | null;
  rawPayload?: string | null;
}

export async function listMeasurements(
  deviceId: string,
  from?: string,
  to?: string
): Promise<Measurement[]> {
  const response = await apiClient.get<Measurement[]>(
    `/api/devices/${deviceId}/measurements`,
    { params: { from, to } }
  );
  return response.data;
}

export async function latestMeasurement(deviceId: string): Promise<Measurement | null> {
  const response = await apiClient.get<Measurement | null>(
    `/api/devices/${deviceId}/measurements/latest`
  );
  return response.data;
}

export async function uploadCsv(file: File): Promise<{ inserted: number; errors: string[] }> {
  const formData = new FormData();
  formData.append("file", file);
  const response = await apiClient.post<{ inserted: number; errors: string[] }>(
    "/api/measurements/bulk",
    formData,
    { headers: { "Content-Type": "multipart/form-data" } }
  );
  return response.data;
}
