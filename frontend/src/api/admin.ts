import apiClient from "./client";
import type { ProfileResponse } from "./auth";

export async function listUsers(): Promise<ProfileResponse[]> {
  const response = await apiClient.get<ProfileResponse[]>("/api/admin/users");
  return response.data;
}

export async function updateUserRole(
  userId: string,
  role: "USER" | "ADMIN"
): Promise<ProfileResponse> {
  const response = await apiClient.put<ProfileResponse>(
    `/api/admin/users/${userId}/role`,
    { role }
  );
  return response.data;
}
