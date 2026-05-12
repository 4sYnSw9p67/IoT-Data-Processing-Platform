import apiClient from "./client";
import type { AlertSeverity } from "./alerts";

export type RuleConditionType =
  | "TEMP_ABOVE"
  | "TEMP_BELOW"
  | "HUMIDITY_ABOVE"
  | "HUMIDITY_BELOW";

export interface AutomationRule {
  id: string;
  name: string;
  deviceId: string;
  deviceName?: string;
  ownerUserId: string;
  conditionType: RuleConditionType;
  threshold: number;
  severity: AlertSeverity;
  enabled: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface RuleRequest {
  name: string;
  deviceId: string;
  conditionType: RuleConditionType;
  threshold: number;
  severity: AlertSeverity;
  enabled?: boolean;
}

export interface RuleUpdateRequest {
  name?: string;
  conditionType?: RuleConditionType;
  threshold?: number;
  severity?: AlertSeverity;
  enabled?: boolean;
}

export async function listRules(): Promise<AutomationRule[]> {
  const response = await apiClient.get<AutomationRule[]>("/api/rules");
  return response.data;
}

export async function createRule(payload: RuleRequest): Promise<AutomationRule> {
  const response = await apiClient.post<AutomationRule>("/api/rules", payload);
  return response.data;
}

export async function updateRule(
  id: string,
  payload: RuleUpdateRequest
): Promise<AutomationRule> {
  const response = await apiClient.put<AutomationRule>(`/api/rules/${id}`, payload);
  return response.data;
}

export async function deleteRule(id: string): Promise<void> {
  await apiClient.delete(`/api/rules/${id}`);
}
