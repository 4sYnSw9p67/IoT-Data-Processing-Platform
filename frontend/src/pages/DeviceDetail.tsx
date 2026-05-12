import { useState } from "react";
import { useParams } from "react-router-dom";
import { useQuery } from "@tanstack/react-query";
import { useTranslation } from "react-i18next";
import {
  CartesianGrid,
  Legend,
  Line,
  LineChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";
import { PageHeader } from "@/components/PageHeader";
import { Spinner } from "@/components/Spinner";
import { getDevice } from "@/api/devices";
import { listMeasurements } from "@/api/measurements";

const RANGES = [
  { label: "24h", hours: 24 },
  { label: "7d", hours: 24 * 7 },
  { label: "30d", hours: 24 * 30 },
];

export function DeviceDetailPage() {
  const { t } = useTranslation();
  const { deviceId } = useParams<{ deviceId: string }>();
  const [hours, setHours] = useState(24);

  const deviceQuery = useQuery({
    queryKey: ["device", deviceId],
    queryFn: () => getDevice(deviceId!),
    enabled: !!deviceId,
  });

  const from = new Date(Date.now() - hours * 3600_000).toISOString();
  const to = new Date().toISOString();
  const measurementsQuery = useQuery({
    queryKey: ["measurements", deviceId, hours],
    queryFn: () => listMeasurements(deviceId!, from, to),
    enabled: !!deviceId,
  });

  if (deviceQuery.isLoading) return <Spinner />;
  if (!deviceQuery.data) return <p>{t("common.empty")}</p>;
  const device = deviceQuery.data;
  const data = (measurementsQuery.data ?? []).map((m) => ({
    timestamp: new Date(m.takenAt).getTime(),
    label: new Date(m.takenAt).toLocaleString(),
    temperature: m.temperatureC,
    humidity: m.humidityPct,
  }));

  return (
    <div>
      <PageHeader
        title={device.name}
        subtitle={`${device.type} · ${device.twinName ?? "—"}${device.twinType ? ` (${device.twinType})` : ""}`}
        actions={
          <div className="flex gap-1">
            {RANGES.map((r) => (
              <button
                key={r.label}
                onClick={() => setHours(r.hours)}
                className={`btn ${hours === r.hours ? "btn-primary" : "btn-secondary"}`}
              >
                {r.label}
              </button>
            ))}
          </div>
        }
      />
      <div className="card">
        {measurementsQuery.isLoading ? (
          <Spinner />
        ) : data.length === 0 ? (
          <p className="text-sm text-slate-500">{t("common.empty")}</p>
        ) : (
          <div style={{ width: "100%", height: 360 }}>
            <ResponsiveContainer>
              <LineChart data={data}>
                <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" />
                <XAxis
                  dataKey="timestamp"
                  tickFormatter={(value: number) =>
                    new Date(value).toLocaleTimeString([], {
                      hour: "2-digit",
                      minute: "2-digit",
                    })
                  }
                  fontSize={12}
                />
                <YAxis yAxisId="left" fontSize={12} unit="°C" />
                <YAxis
                  yAxisId="right"
                  orientation="right"
                  fontSize={12}
                  unit="%"
                />
                <Tooltip
                  labelFormatter={(value: number) =>
                    new Date(value).toLocaleString()
                  }
                />
                <Legend />
                <Line
                  yAxisId="left"
                  type="monotone"
                  dataKey="temperature"
                  stroke="#2563eb"
                  dot={false}
                  name="°C"
                />
                <Line
                  yAxisId="right"
                  type="monotone"
                  dataKey="humidity"
                  stroke="#16a34a"
                  dot={false}
                  name="%"
                />
              </LineChart>
            </ResponsiveContainer>
          </div>
        )}
      </div>
    </div>
  );
}
