import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useTranslation } from "react-i18next";
import { PageHeader } from "@/components/PageHeader";
import { Spinner } from "@/components/Spinner";
import { SeverityBadge } from "@/components/SeverityBadge";
import { acknowledgeAlert, listAlerts } from "@/api/alerts";

export function AlertsPage() {
  const { t } = useTranslation();
  const qc = useQueryClient();
  const [showAck, setShowAck] = useState(false);
  const alerts = useQuery({
    queryKey: ["alerts", showAck],
    queryFn: () => listAlerts(showAck ? undefined : false),
  });

  const ackMutation = useMutation({
    mutationFn: acknowledgeAlert,
    onSuccess: () => void qc.invalidateQueries({ queryKey: ["alerts"] }),
  });

  return (
    <div>
      <PageHeader
        title={t("alerts.title")}
        actions={
          <label className="flex items-center gap-2 text-sm">
            <input
              type="checkbox"
              checked={showAck}
              onChange={(e) => setShowAck(e.target.checked)}
            />
            {t("alerts.title")} (all)
          </label>
        }
      />

      <div className="overflow-hidden rounded-lg border border-slate-200 bg-white">
        {alerts.isLoading && (
          <div className="p-6">
            <Spinner />
          </div>
        )}
        {alerts.data && alerts.data.length === 0 && (
          <p className="p-6 text-sm text-slate-500">{t("common.empty")}</p>
        )}
        {alerts.data && alerts.data.length > 0 && (
          <table className="w-full divide-y divide-slate-200 text-sm">
            <thead className="bg-slate-50 text-left text-xs uppercase tracking-wider text-slate-500">
              <tr>
                <th className="px-4 py-2">{t("alerts.severity")}</th>
                <th className="px-4 py-2">{t("alerts.source")}</th>
                <th className="px-4 py-2">Message</th>
                <th className="px-4 py-2">{t("alerts.raisedAt")}</th>
                <th className="px-4 py-2 text-right">{t("common.actions")}</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {alerts.data.map((alert) => (
                <tr key={alert.id} className={alert.acknowledgedAt ? "opacity-60" : ""}>
                  <td className="px-4 py-2">
                    <SeverityBadge severity={alert.severity} />
                  </td>
                  <td className="px-4 py-2">{alert.source}</td>
                  <td className="px-4 py-2">{alert.message}</td>
                  <td className="px-4 py-2 text-xs text-slate-500">
                    {new Date(alert.raisedAt).toLocaleString()}
                  </td>
                  <td className="px-4 py-2 text-right">
                    {!alert.acknowledgedAt && (
                      <button
                        className="btn-primary"
                        onClick={() => ackMutation.mutate(alert.id)}
                      >
                        {t("alerts.acknowledge")}
                      </button>
                    )}
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
