import apiClient from "./client";
import type { TwinType } from "./twins";

export type DeviceType = "TEMPERATURE" | "HUMIDITY" | "COMBO";

export interface Device {
  id: string;
  name: string;
  type: DeviceType;
  twinId?: string | null;
  twinName?: string | null;
  twinType?: TwinType | null;
  ownerUserId: string;
  minTemperatureC?: number | null;
  maxTemperatureC?: number | null;
  minHumidityPct?: number | null;
  maxHumidityPct?: number | null;
  active: boolean;
  registeredAt: string;
  updatedAt: string;
}

export interface DeviceCreateRequest {
  name: string;
  type: DeviceType;
  twinId?: string | null;
  minTemperatureC?: number | null;
  maxTemperatureC?: number | null;
  minHumidityPct?: number | null;
  maxHumidityPct?: number | null;
  active?: boolean;
}

export interface DeviceUpdateRequest {
  name?: string;
  minTemperatureC?: number | null;
  maxTemperatureC?: number | null;
  minHumidityPct?: number | null;
  maxHumidityPct?: number | null;
  active?: boolean;
}

export async function listDevices(): Promise<Device[]> {
  const response = await apiClient.get<Device[]>("/api/devices");
  return response.data;
}

export async function getDevice(id: string): Promise<Device> {
  const response = await apiClient.get<Device>(`/api/devices/${id}`);
  return response.data;
}

export async function createDevice(payload: DeviceCreateRequest): Promise<Device> {
  const response = await apiClient.post<Device>("/api/devices", payload);
  return response.data;
}

export async function updateDevice(
  id: string,
  payload: DeviceUpdateRequest
): Promise<Device> {
  const response = await apiClient.put<Device>(`/api/devices/${id}`, payload);
  return response.data;
}

export async function deleteDevice(id: string): Promise<void> {
  await apiClient.delete(`/api/devices/${id}`);
}

export async function assignTwin(
  id: string,
  twinId: string | null
): Promise<Device> {
  const response = await apiClient.put<Device>(`/api/devices/${id}/twin`, {
    twinId,
  });
  return response.data;
}
