import apiClient from "./client";

export interface ChatResponse {
  reply: string;
  respondedAt: string;
  devicesConsidered: string[];
  measurementsConsidered: number;
}

export async function aiStatus(): Promise<{ enabled: boolean }> {
  const response = await apiClient.get<{ enabled: boolean }>("/api/ai/status");
  return response.data;
}

export async function askAi(question: string): Promise<ChatResponse> {
  const response = await apiClient.post<ChatResponse>("/api/ai/chat", {
    question,
  });
  return response.data;
}
