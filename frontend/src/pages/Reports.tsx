import { FormEvent, useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { useTranslation } from "react-i18next";
import { PageHeader } from "@/components/PageHeader";
import { Spinner } from "@/components/Spinner";
import { listDevices } from "@/api/devices";
import { downloadReport, type ReportFormat } from "@/api/reports";

const FORMATS: ReportFormat[] = ["EXCEL", "PDF"];

export function ReportsPage() {
  const { t } = useTranslation();
  const devices = useQuery({ queryKey: ["devices"], queryFn: listDevices });
  const [deviceId, setDeviceId] = useState("");
  const [from, setFrom] = useState(() => {
    const d = new Date(Date.now() - 24 * 3600_000);
    return d.toISOString().slice(0, 16);
  });
  const [to, setTo] = useState(() => new Date().toISOString().slice(0, 16));
  const [format, setFormat] = useState<ReportFormat>("EXCEL");
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function handle(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);
    setBusy(true);
    try {
      const fromIso = new Date(from).toISOString();
      const toIso = new Date(to).toISOString();
      const { blob, filename } = await downloadReport(deviceId, fromIso, toIso, format);
      const url = URL.createObjectURL(blob);
      const link = document.createElement("a");
      link.href = url;
      link.download = filename;
      document.body.appendChild(link);
      link.click();
      link.remove();
      URL.revokeObjectURL(url);
    } catch (err) {
      console.error(err);
      setError("Failed to download report");
    } finally {
      setBusy(false);
    }
  }

  return (
    <div>
      <PageHeader title={t("reports.title")} />
      {devices.isLoading ? (
        <Spinner />
      ) : (
        <form className="card grid grid-cols-1 gap-3 md:grid-cols-4" onSubmit={handle}>
          <div className="md:col-span-2">
            <label className="label">{t("devices.title")}</label>
            <select
              className="input"
              value={deviceId}
              onChange={(e) => setDeviceId(e.target.value)}
              required
            >
              <option value="">—</option>
              {devices.data?.map((d) => (
                <option key={d.id} value={d.id}>
                  {d.name}
                </option>
              ))}
            </select>
          </div>
          <div>
            <label className="label">{t("reports.format")}</label>
            <select
              className="input"
              value={format}
              onChange={(e) => setFormat(e.target.value as ReportFormat)}
            >
              {FORMATS.map((f) => (
                <option key={f} value={f}>
                  {f}
                </option>
              ))}
            </select>
          </div>
          <div />
          <div>
            <label className="label">{t("reports.from")}</label>
            <input
              type="datetime-local"
              className="input"
              value={from}
              onChange={(e) => setFrom(e.target.value)}
              required
            />
          </div>
          <div>
            <label className="label">{t("reports.to")}</label>
            <input
              type="datetime-local"
              className="input"
              value={to}
              onChange={(e) => setTo(e.target.value)}
              required
            />
          </div>
          <div className="md:col-span-2 flex items-end justify-end gap-2">
            {error && <p className="text-sm text-red-700">{error}</p>}
            <button type="submit" disabled={busy} className="btn-primary">
              {busy ? t("common.loading") : t("reports.download")}
            </button>
          </div>
        </form>
      )}
    </div>
  );
}
