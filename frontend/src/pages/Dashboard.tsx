import { useQuery } from "@tanstack/react-query";
import { useTranslation } from "react-i18next";
import { Link } from "react-router-dom";
import { PageHeader } from "@/components/PageHeader";
import { Spinner } from "@/components/Spinner";
import { SeverityBadge } from "@/components/SeverityBadge";
import { listDevices } from "@/api/devices";
import { listTwins } from "@/api/twins";
import { listAlerts } from "@/api/alerts";
import { latestMeasurement } from "@/api/measurements";

export function DashboardPage() {
  const { t } = useTranslation();
  const devicesQuery = useQuery({ queryKey: ["devices"], queryFn: listDevices });
  const twinsQuery = useQuery({ queryKey: ["twins"], queryFn: () => listTwins() });
  const alertsQuery = useQuery({
    queryKey: ["alerts", "open"],
    queryFn: () => listAlerts(false),
  });

  const devices = devicesQuery.data ?? [];
  const stats = [
    {
      label: t("dashboard.deviceCount"),
      value: devicesQuery.data?.length ?? 0,
    },
    { label: t("dashboard.twinCount"), value: twinsQuery.data?.length ?? 0 },
    {
      label: t("dashboard.activeAlerts"),
      value: alertsQuery.data?.length ?? 0,
    },
  ];

  return (
    <div>
      <PageHeader
        title={t("nav.dashboard")}
        subtitle={t("app.subtitle")}
      />
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
        {stats.map((s) => (
          <div key={s.label} className="card">
            <p className="text-sm text-slate-500">{s.label}</p>
            <p className="mt-2 text-3xl font-semibold text-slate-900">
              {s.value}
            </p>
          </div>
        ))}
      </div>

      <h2 className="mt-8 text-lg font-semibold text-slate-900">
        {t("dashboard.latestReadings")}
      </h2>
      <div className="mt-3 grid grid-cols-1 gap-3 md:grid-cols-2 lg:grid-cols-3">
        {devicesQuery.isLoading && <Spinner />}
        {devices.length === 0 && !devicesQuery.isLoading && (
          <p className="text-sm text-slate-500">{t("dashboard.noReadings")}</p>
        )}
        {devices.map((d) => (
          <DeviceLatestCard
            key={d.id}
            deviceId={d.id}
            name={d.name}
            twinLabel={d.twinName ? `${d.twinName} (${d.twinType ?? ""})` : null}
            type={d.type}
          />
        ))}
      </div>

      <h2 className="mt-8 text-lg font-semibold text-slate-900">
        {t("dashboard.activeAlerts")}
      </h2>
      <div className="mt-3 space-y-2">
        {alertsQuery.isLoading && <Spinner />}
        {(alertsQuery.data ?? []).slice(0, 5).map((alert) => (
          <div
            key={alert.id}
            className="flex items-center justify-between rounded-lg border border-slate-200 bg-white p-3"
          >
            <div>
              <p className="font-medium text-slate-800">{alert.message}</p>
              <p className="text-xs text-slate-500">
                {new Date(alert.raisedAt).toLocaleString()}
              </p>
            </div>
            <SeverityBadge severity={alert.severity} />
          </div>
        ))}
        {(alertsQuery.data ?? []).length > 5 && (
          <Link to="/alerts" className="text-sm text-brand-600 hover:underline">
            {t("nav.alerts")} →
          </Link>
        )}
      </div>
    </div>
  );
}

function DeviceLatestCard({
  deviceId,
  name,
  twinLabel,
  type,
}: {
  deviceId: string;
  name: string;
  twinLabel: string | null;
  type: string;
}) {
  const latest = useQuery({
    queryKey: ["measurement", "latest", deviceId],
    queryFn: () => latestMeasurement(deviceId),
  });
  return (
    <Link
      to={`/devices/${deviceId}`}
      className="card transition-shadow hover:shadow-md"
    >
      <div className="flex items-center justify-between">
        <div>
          <p className="font-semibold text-slate-900">{name}</p>
          <p className="text-xs text-slate-500">{twinLabel ?? "—"} · {type}</p>
        </div>
        <span className="badge bg-brand-50 text-brand-700">{type}</span>
      </div>
      <div className="mt-3 flex items-baseline gap-3">
        <span className="text-2xl font-semibold text-slate-800">
          {latest.data?.temperatureC != null
            ? `${latest.data.temperatureC.toFixed(1)}°C`
            : "—"}
        </span>
        <span className="text-sm text-slate-500">
          {latest.data?.humidityPct != null
            ? `${latest.data.humidityPct.toFixed(0)}%`
            : ""}
        </span>
      </div>
      {latest.data?.takenAt && (
        <p className="mt-2 text-xs text-slate-400">
          {new Date(latest.data.takenAt).toLocaleString()}
        </p>
      )}
    </Link>
  );
}
