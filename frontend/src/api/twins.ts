import apiClient from "./client";

export type TwinType =
  | "SITE"
  | "BUILDING"
  | "FLOOR"
  | "ROOM"
  | "ZONE"
  | "OUTDOOR"
  | "EQUIPMENT";

export const TWIN_TYPES: TwinType[] = [
  "SITE",
  "BUILDING",
  "FLOOR",
  "ROOM",
  "ZONE",
  "OUTDOOR",
  "EQUIPMENT",
];

export interface DigitalTwin {
  id: string;
  name: string;
  type: TwinType;
  parentId: string | null;
  parentName: string | null;
  ownerUserId: string;
  description: string | null;
  floor: string | null;
  color: string | null;
  latitude: number | null;
  longitude: number | null;
  deviceCount: number;
  childCount: number;
  createdAt: string;
  updatedAt: string;
}

export interface TwinRequest {
  name: string;
  type: TwinType;
  parentId?: string | null;
  description?: string | null;
  floor?: string | null;
  color?: string | null;
  latitude?: number | null;
  longitude?: number | null;
}

export interface TwinTreeNode {
  id: string;
  name: string;
  type: TwinType;
  color: string | null;
  deviceCount: number;
  children: TwinTreeNode[];
}

export interface TwinTypeDescriptor {
  type: TwinType;
  allowedParents: TwinType[];
  root: boolean;
}

export async function listTwins(params?: { type?: TwinType; parentId?: string }): Promise<DigitalTwin[]> {
  const response = await apiClient.get<DigitalTwin[]>("/api/twins", { params });
  return response.data;
}

export async function getTwin(id: string): Promise<DigitalTwin> {
  const response = await apiClient.get<DigitalTwin>(`/api/twins/${id}`);
  return response.data;
}

export async function getTwinTree(): Promise<TwinTreeNode[]> {
  const response = await apiClient.get<TwinTreeNode[]>("/api/twins/tree");
  return response.data;
}

export async function getTwinTypeDescriptors(): Promise<TwinTypeDescriptor[]> {
  const response = await apiClient.get<TwinTypeDescriptor[]>("/api/twins/types");
  return response.data;
}

export async function createTwin(payload: TwinRequest): Promise<DigitalTwin> {
  const response = await apiClient.post<DigitalTwin>("/api/twins", payload);
  return response.data;
}

export async function updateTwin(id: string, payload: TwinRequest): Promise<DigitalTwin> {
  const response = await apiClient.put<DigitalTwin>(`/api/twins/${id}`, payload);
  return response.data;
}

export async function deleteTwin(id: string): Promise<void> {
  await apiClient.delete(`/api/twins/${id}`);
}
