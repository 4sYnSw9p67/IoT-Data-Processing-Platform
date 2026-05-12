import apiClient from "./client";

export interface ForecastLocation {
  id: string;
  label: string;
  country: string;
  latitude: number;
  longitude: number;
  timezone?: string | null;
  ownerUserId: string;
  lastPulledAt?: string | null;
}

export interface ForecastDay {
  date: string;
  temperatureMinC: number;
  temperatureMaxC: number;
  humidityPct: number;
  precipitationMm: number;
  windSpeedKmh: number;
  weatherCode?: number;
}

export interface LocationForecast {
  location: ForecastLocation;
  forecasts: ForecastDay[];
}

export interface CreateLocationRequest {
  label: string;
  country: string;
  latitude: number;
  longitude: number;
  timezone?: string | null;
}

export async function listForecastLocations(): Promise<ForecastLocation[]> {
  const response = await apiClient.get<ForecastLocation[]>(
    "/api/forecast/locations"
  );
  return response.data;
}

export async function createForecastLocation(
  payload: CreateLocationRequest
): Promise<ForecastLocation> {
  const response = await apiClient.post<ForecastLocation>(
    "/api/forecast/locations",
    payload
  );
  return response.data;
}

export async function deleteForecastLocation(id: string): Promise<void> {
  await apiClient.delete(`/api/forecast/locations/${id}`);
}

export async function refreshForecast(id: string): Promise<ForecastLocation> {
  const response = await apiClient.put<ForecastLocation>(
    `/api/forecast/locations/${id}/refresh`
  );
  return response.data;
}

export async function getForecast(id: string): Promise<LocationForecast> {
  const response = await apiClient.get<LocationForecast>(
    `/api/forecast/locations/${id}/forecast`
  );
  return response.data;
}
