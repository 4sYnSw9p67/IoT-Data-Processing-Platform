import { FormEvent, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useTranslation } from "react-i18next";
import { PageHeader } from "@/components/PageHeader";
import { Spinner } from "@/components/Spinner";
import {
  createRule,
  deleteRule,
  listRules,
  updateRule,
  type RuleConditionType,
} from "@/api/rules";
import { listDevices } from "@/api/devices";
import type { AlertSeverity } from "@/api/alerts";

const CONDITION_TYPES: RuleConditionType[] = [
  "TEMP_ABOVE",
  "TEMP_BELOW",
  "HUMIDITY_ABOVE",
  "HUMIDITY_BELOW",
];
const SEVERITIES: AlertSeverity[] = ["INFO", "WARNING", "CRITICAL"];

export function RulesPage() {
  const { t } = useTranslation();
  const qc = useQueryClient();
  const rules = useQuery({ queryKey: ["rules"], queryFn: listRules });
  const devices = useQuery({ queryKey: ["devices"], queryFn: listDevices });
  const [open, setOpen] = useState(false);

  const createMutation = useMutation({
    mutationFn: createRule,
    onSuccess: () => {
      void qc.invalidateQueries({ queryKey: ["rules"] });
      setOpen(false);
    },
  });
  const toggleMutation = useMutation({
    mutationFn: ({ id, enabled }: { id: string; enabled: boolean }) =>
      updateRule(id, { enabled }),
    onSuccess: () => void qc.invalidateQueries({ queryKey: ["rules"] }),
  });
  const deleteMutation = useMutation({
    mutationFn: deleteRule,
    onSuccess: () => void qc.invalidateQueries({ queryKey: ["rules"] }),
  });

  return (
    <div>
      <PageHeader
        title={t("rules.title")}
        actions={
          <button className="btn-primary" onClick={() => setOpen((p) => !p)}>
            {open ? t("common.cancel") : t("common.create")}
          </button>
        }
      />

      {open && (
        <form
          className="card mt-4 grid grid-cols-1 gap-3 md:grid-cols-3"
          onSubmit={(e) => {
            e.preventDefault();
            const fd = new FormData(e.currentTarget);
            createMutation.mutate({
              name: String(fd.get("name") ?? ""),
              deviceId: String(fd.get("deviceId") ?? ""),
              conditionType: fd.get("conditionType") as RuleConditionType,
              threshold: Number(fd.get("threshold")),
              severity: fd.get("severity") as AlertSeverity,
              enabled: true,
            });
          }}
        >
          <div>
            <label className="label">{t("devices.name")}</label>
            <input className="input" name="name" required />
          </div>
          <div>
            <label className="label">{t("devices.title")}</label>
            <select className="input" name="deviceId" required>
              <option value="">—</option>
              {devices.data?.map((d) => (
                <option key={d.id} value={d.id}>
                  {d.name}
                </option>
              ))}
            </select>
          </div>
          <div>
            <label className="label">{t("rules.condition")}</label>
            <select className="input" name="conditionType" required>
              {CONDITION_TYPES.map((c) => (
                <option key={c} value={c}>
                  {c}
                </option>
              ))}
            </select>
          </div>
          <div>
            <label className="label">{t("rules.threshold")}</label>
            <input
              className="input"
              type="number"
              step="0.1"
              name="threshold"
              required
            />
          </div>
          <div>
            <label className="label">{t("alerts.severity")}</label>
            <select className="input" name="severity" required>
              {SEVERITIES.map((s) => (
                <option key={s} value={s}>
                  {s}
                </option>
              ))}
            </select>
          </div>
          <div className="md:col-span-3 flex justify-end">
            <button
              type="submit"
              disabled={createMutation.isPending}
              className="btn-primary"
            >
              {createMutation.isPending ? t("common.loading") : t("common.save")}
            </button>
          </div>
        </form>
      )}

      <div className="mt-4 overflow-hidden rounded-lg border border-slate-200 bg-white">
        {rules.isLoading && (
          <div className="p-6">
            <Spinner />
          </div>
        )}
        {rules.data && rules.data.length === 0 && (
          <p className="p-6 text-sm text-slate-500">{t("common.empty")}</p>
        )}
        {rules.data && rules.data.length > 0 && (
          <table className="w-full divide-y divide-slate-200 text-sm">
            <thead className="bg-slate-50 text-left text-xs uppercase tracking-wider text-slate-500">
              <tr>
                <th className="px-4 py-2">{t("devices.name")}</th>
                <th className="px-4 py-2">{t("devices.title")}</th>
                <th className="px-4 py-2">{t("rules.condition")}</th>
                <th className="px-4 py-2">{t("rules.threshold")}</th>
                <th className="px-4 py-2">{t("alerts.severity")}</th>
                <th className="px-4 py-2">{t("rules.enabled")}</th>
                <th className="px-4 py-2 text-right">{t("common.actions")}</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {rules.data.map((r) => (
                <tr key={r.id}>
                  <td className="px-4 py-2 font-medium">{r.name}</td>
                  <td className="px-4 py-2">{r.deviceName ?? r.deviceId}</td>
                  <td className="px-4 py-2">{r.conditionType}</td>
                  <td className="px-4 py-2">{r.threshold}</td>
                  <td className="px-4 py-2">{r.severity}</td>
                  <td className="px-4 py-2">
                    <input
                      type="checkbox"
                      checked={r.enabled}
                      onChange={(e) =>
                        toggleMutation.mutate({ id: r.id, enabled: e.target.checked })
                      }
                    />
                  </td>
                  <td className="px-4 py-2 text-right">
                    <button
                      className="btn-danger"
                      onClick={() => {
                        if (confirm(t("common.confirm")))
                          deleteMutation.mutate(r.id);
                      }}
                    >
                      {t("common.delete")}
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}
