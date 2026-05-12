import { FormEvent, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useTranslation } from "react-i18next";
import { Link } from "react-router-dom";
import { PageHeader } from "@/components/PageHeader";
import { Spinner } from "@/components/Spinner";
import {
  createDevice,
  deleteDevice,
  listDevices,
  type DeviceType,
} from "@/api/devices";
import { listTwins, type DigitalTwin } from "@/api/twins";

const DEVICE_TYPES: DeviceType[] = ["TEMPERATURE", "HUMIDITY", "COMBO"];
const ATTACHABLE_TWIN_TYPES = new Set(["ROOM", "ZONE", "OUTDOOR", "EQUIPMENT"]);

export function DevicesPage() {
  const { t } = useTranslation();
  const qc = useQueryClient();
  const devices = useQuery({ queryKey: ["devices"], queryFn: listDevices });
  const twins = useQuery({ queryKey: ["twins"], queryFn: () => listTwins() });
  const [open, setOpen] = useState(false);

  const createMutation = useMutation({
    mutationFn: createDevice,
    onSuccess: () => {
      void qc.invalidateQueries({ queryKey: ["devices"] });
      setOpen(false);
    },
  });

  const deleteMutation = useMutation({
    mutationFn: deleteDevice,
    onSuccess: () => void qc.invalidateQueries({ queryKey: ["devices"] }),
  });

  const attachableTwins =
    twins.data?.filter((twin) => ATTACHABLE_TWIN_TYPES.has(twin.type)) ?? [];

  return (
    <div>
      <PageHeader
        title={t("devices.title")}
        actions={
          <button className="btn-primary" onClick={() => setOpen((p) => !p)}>
            {open ? t("common.cancel") : t("common.create")}
          </button>
        }
      />

      {open && (
        <CreateDeviceForm
          twins={attachableTwins}
          submitting={createMutation.isPending}
          onSubmit={(payload) => createMutation.mutate(payload)}
        />
      )}

      <div className="mt-4 overflow-hidden rounded-lg border border-slate-200 bg-white">
        {devices.isLoading && (
          <div className="p-6">
            <Spinner />
          </div>
        )}
        {devices.data && devices.data.length === 0 && (
          <p className="p-6 text-sm text-slate-500">{t("common.empty")}</p>
        )}
        <table className="w-full divide-y divide-slate-200 text-sm">
          <thead className="bg-slate-50 text-left text-xs uppercase tracking-wider text-slate-500">
            <tr>
              <th className="px-4 py-2">{t("devices.name")}</th>
              <th className="px-4 py-2">{t("devices.type")}</th>
              <th className="px-4 py-2">{t("devices.twin")}</th>
              <th className="px-4 py-2">{t("devices.thresholds")}</th>
              <th className="px-4 py-2">{t("devices.active")}</th>
              <th className="px-4 py-2 text-right">{t("common.actions")}</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {devices.data?.map((d) => (
              <tr key={d.id} className="hover:bg-slate-50">
                <td className="px-4 py-2">
                  <Link
                    to={`/devices/${d.id}`}
                    className="font-medium text-brand-700 hover:underline"
                  >
                    {d.name}
                  </Link>
                </td>
                <td className="px-4 py-2">{d.type}</td>
                <td className="px-4 py-2">
                  {d.twinName ? (
                    <span>
                      {d.twinName}{" "}
                      <span className="text-xs text-slate-400">({d.twinType})</span>
                    </span>
                  ) : (
                    "—"
                  )}
                </td>
                <td className="px-4 py-2 text-xs text-slate-500">
                  T: {d.minTemperatureC ?? "-"} / {d.maxTemperatureC ?? "-"} · H:{" "}
                  {d.minHumidityPct ?? "-"} / {d.maxHumidityPct ?? "-"}
                </td>
                <td className="px-4 py-2">{d.active ? "✓" : "—"}</td>
                <td className="px-4 py-2 text-right">
                  <button
                    className="btn-danger"
                    onClick={() => {
                      if (confirm(t("common.confirm"))) {
                        deleteMutation.mutate(d.id);
                      }
                    }}
                  >
                    {t("common.delete")}
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}

function CreateDeviceForm({
  twins,
  submitting,
  onSubmit,
}: {
  twins: DigitalTwin[];
  submitting: boolean;
  onSubmit: (payload: {
    name: string;
    type: DeviceType;
    twinId?: string | null;
    minTemperatureC?: number | null;
    maxTemperatureC?: number | null;
    minHumidityPct?: number | null;
    maxHumidityPct?: number | null;
  }) => void;
}) {
  const { t } = useTranslation();
  const [name, setName] = useState("");
  const [type, setType] = useState<DeviceType>("COMBO");
  const [twinId, setTwinId] = useState<string>("");
  const [minT, setMinT] = useState<string>("");
  const [maxT, setMaxT] = useState<string>("");
  const [minH, setMinH] = useState<string>("");
  const [maxH, setMaxH] = useState<string>("");

  function handle(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    onSubmit({
      name,
      type,
      twinId: twinId || null,
      minTemperatureC: minT === "" ? null : Number(minT),
      maxTemperatureC: maxT === "" ? null : Number(maxT),
      minHumidityPct: minH === "" ? null : Number(minH),
      maxHumidityPct: maxH === "" ? null : Number(maxH),
    });
  }

  return (
    <form onSubmit={handle} className="card mt-4 grid grid-cols-1 gap-3 md:grid-cols-2">
      <div>
        <label className="label">{t("devices.name")}</label>
        <input className="input" value={name} onChange={(e) => setName(e.target.value)} required />
      </div>
      <div>
        <label className="label">{t("devices.type")}</label>
        <select
          className="input"
          value={type}
          onChange={(e) => setType(e.target.value as DeviceType)}
        >
          {DEVICE_TYPES.map((dt) => (
            <option key={dt} value={dt}>
              {dt}
            </option>
          ))}
        </select>
      </div>
      <div>
        <label className="label">{t("devices.twin")}</label>
        <select
          className="input"
          value={twinId}
          onChange={(e) => setTwinId(e.target.value)}
        >
          <option value="">—</option>
          {twins.map((twin) => (
            <option key={twin.id} value={twin.id}>
              {twin.name} ({twin.type})
            </option>
          ))}
        </select>
      </div>
      <div />
      <div>
        <label className="label">{t("devices.minTempC")}</label>
        <input
          type="number"
          step="0.1"
          className="input"
          value={minT}
          onChange={(e) => setMinT(e.target.value)}
        />
      </div>
      <div>
        <label className="label">{t("devices.maxTempC")}</label>
        <input
          type="number"
          step="0.1"
          className="input"
          value={maxT}
          onChange={(e) => setMaxT(e.target.value)}
        />
      </div>
      <div>
        <label className="label">{t("devices.minHumidityPct")}</label>
        <input
          type="number"
          step="0.1"
          className="input"
          value={minH}
          onChange={(e) => setMinH(e.target.value)}
        />
      </div>
      <div>
        <label className="label">{t("devices.maxHumidityPct")}</label>
        <input
          type="number"
          step="0.1"
          className="input"
          value={maxH}
          onChange={(e) => setMaxH(e.target.value)}
        />
      </div>
      <div className="md:col-span-2 flex justify-end">
        <button type="submit" disabled={submitting} className="btn-primary">
          {submitting ? t("common.loading") : t("common.save")}
        </button>
      </div>
    </form>
  );
}
