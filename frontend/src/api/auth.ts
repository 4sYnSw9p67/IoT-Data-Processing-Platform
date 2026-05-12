import apiClient from "./client";

export interface TokenResponse {
  accessToken: string;
  refreshToken: string;
  accessTokenExpiresAt: string;
  refreshTokenExpiresAt: string;
  tokenType: string;
  username: string;
  email: string;
  role: string;
  userId: string;
}

export interface ProfileResponse {
  id: string;
  username: string;
  email: string;
  role: string;
  enabled: boolean;
}

export async function login(
  username: string,
  password: string
): Promise<TokenResponse> {
  const response = await apiClient.post<TokenResponse>("/api/auth/login", {
    username,
    password,
  });
  return response.data;
}

export async function register(
  username: string,
  email: string,
  password: string
): Promise<TokenResponse> {
  const response = await apiClient.post<TokenResponse>("/api/auth/register", {
    username,
    email,
    password,
  });
  return response.data;
}

export async function refresh(refreshToken: string): Promise<TokenResponse> {
  const response = await apiClient.post<TokenResponse>("/api/auth/refresh", {
    refreshToken,
  });
  return response.data;
}

export async function logout(refreshToken: string): Promise<void> {
  await apiClient.post("/api/auth/logout", { refreshToken });
}

export async function getProfile(): Promise<ProfileResponse> {
  const response = await apiClient.get<ProfileResponse>("/api/profile");
  return response.data;
}

export async function updateProfile(payload: {
  email?: string;
  password?: string;
}): Promise<ProfileResponse> {
  const response = await apiClient.put<ProfileResponse>(
    "/api/profile",
    payload
  );
  return response.data;
}
