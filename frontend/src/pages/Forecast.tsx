import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useTranslation } from "react-i18next";
import { PageHeader } from "@/components/PageHeader";
import { Spinner } from "@/components/Spinner";
import {
  createForecastLocation,
  deleteForecastLocation,
  getForecast,
  listForecastLocations,
  refreshForecast,
} from "@/api/forecast";

export function ForecastPage() {
  const { t } = useTranslation();
  const qc = useQueryClient();
  const locations = useQuery({
    queryKey: ["forecast-locations"],
    queryFn: listForecastLocations,
  });
  const [open, setOpen] = useState(false);
  const [selected, setSelected] = useState<string | null>(null);

  const createMutation = useMutation({
    mutationFn: createForecastLocation,
    onSuccess: () => {
      void qc.invalidateQueries({ queryKey: ["forecast-locations"] });
      setOpen(false);
    },
  });
  const deleteMutation = useMutation({
    mutationFn: deleteForecastLocation,
    onSuccess: () => void qc.invalidateQueries({ queryKey: ["forecast-locations"] }),
  });
  const refreshMutation = useMutation({
    mutationFn: refreshForecast,
    onSuccess: () => {
      void qc.invalidateQueries({ queryKey: ["forecast-locations"] });
      if (selected) void qc.invalidateQueries({ queryKey: ["forecast", selected] });
    },
  });

  const forecast = useQuery({
    queryKey: ["forecast", selected],
    queryFn: () => getForecast(selected!),
    enabled: !!selected,
  });

  return (
    <div>
      <PageHeader
        title={t("forecast.title")}
        actions={
          <button className="btn-primary" onClick={() => setOpen((p) => !p)}>
            {open ? t("common.cancel") : t("common.create")}
          </button>
        }
      />

      {open && (
        <form
          className="card mt-4 grid grid-cols-1 gap-3 md:grid-cols-2"
          onSubmit={(e) => {
            e.preventDefault();
            const fd = new FormData(e.currentTarget);
            createMutation.mutate({
              label: String(fd.get("label") ?? ""),
              country: String(fd.get("country") ?? ""),
              latitude: Number(fd.get("latitude")),
              longitude: Number(fd.get("longitude")),
              timezone: String(fd.get("timezone") ?? "") || null,
            });
          }}
        >
          <div>
            <label className="label">{t("forecast.label")}</label>
            <input className="input" name="label" required />
          </div>
          <div>
            <label className="label">{t("forecast.country")}</label>
            <input className="input" name="country" required maxLength={2} />
          </div>
          <div>
            <label className="label">{t("forecast.lat")}</label>
            <input
              type="number"
              step="0.0001"
              className="input"
              name="latitude"
              required
            />
          </div>
          <div>
            <label className="label">{t("forecast.lng")}</label>
            <input
              type="number"
              step="0.0001"
              className="input"
              name="longitude"
              required
            />
          </div>
          <div>
            <label className="label">Timezone</label>
            <input className="input" name="timezone" placeholder="Europe/Sofia" />
          </div>
          <div className="md:col-span-2 flex justify-end">
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

      <div className="mt-4 grid grid-cols-1 gap-3 md:grid-cols-3">
        {locations.isLoading && <Spinner />}
        {locations.data?.map((loc) => (
          <div
            key={loc.id}
            className={`card cursor-pointer transition-shadow ${
              selected === loc.id ? "ring-2 ring-brand-500" : "hover:shadow"
            }`}
            onClick={() => setSelected(loc.id)}
          >
            <div className="flex items-start justify-between">
              <div>
                <p className="font-semibold">{loc.label}</p>
                <p className="text-xs text-slate-500">
                  {loc.country} · {loc.latitude.toFixed(2)}, {loc.longitude.toFixed(2)}
                </p>
              </div>
              <button
                type="button"
                className="btn-secondary"
                onClick={(e) => {
                  e.stopPropagation();
                  refreshMutation.mutate(loc.id);
                }}
              >
                {t("forecast.refresh")}
              </button>
            </div>
            <button
              type="button"
              className="btn-danger mt-2"
              onClick={(e) => {
                e.stopPropagation();
                if (confirm(t("common.confirm"))) deleteMutation.mutate(loc.id);
              }}
            >
              {t("common.delete")}
            </button>
          </div>
        ))}
      </div>

      {selected && (
        <div className="mt-6">
          <h2 className="text-lg font-semibold">
            {forecast.data?.location.label ?? ""}
          </h2>
          {forecast.isLoading && <Spinner />}
          {forecast.data && forecast.data.forecasts.length === 0 && (
            <p className="text-sm text-slate-500">{t("forecast.noData")}</p>
          )}
          <div className="mt-3 grid grid-cols-2 gap-3 sm:grid-cols-3 md:grid-cols-5">
            {forecast.data?.forecasts.map((f) => (
              <div key={f.date} className="card">
                <p className="text-xs uppercase text-slate-500">{f.date}</p>
                <p className="mt-1 text-lg font-semibold text-slate-800">
                  {f.temperatureMinC.toFixed(0)}° / {f.temperatureMaxC.toFixed(0)}°
                </p>
                <p className="text-xs text-slate-500">
                  {f.humidityPct.toFixed(0)}% · {f.precipitationMm.toFixed(1)}mm
                </p>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}
