import apiClient from "./client";

export type AlertSeverity = "INFO" | "WARNING" | "CRITICAL";
export type AlertSource = "THRESHOLD" | "RULE";

export interface Alert {
  id: string;
  deviceId: string;
  deviceName?: string;
  ownerUserId: string;
  source: AlertSource;
  severity: AlertSeverity;
  message: string;
  ruleId?: string | null;
  raisedAt: string;
  acknowledgedAt?: string | null;
}

export async function listAlerts(acknowledged?: boolean): Promise<Alert[]> {
  const response = await apiClient.get<Alert[]>("/api/alerts", {
    params: { acknowledged },
  });
  return response.data;
}

export async function acknowledgeAlert(id: string): Promise<Alert> {
  const response = await apiClient.put<Alert>(`/api/alerts/${id}/ack`);
  return response.data;
}
