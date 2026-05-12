import { FormEvent, useMemo, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useTranslation } from "react-i18next";
import { PageHeader } from "@/components/PageHeader";
import { Spinner } from "@/components/Spinner";
import {
  createTwin,
  deleteTwin,
  getTwinTree,
  getTwinTypeDescriptors,
  listTwins,
  TWIN_TYPES,
  type DigitalTwin,
  type TwinRequest,
  type TwinTreeNode,
  type TwinType,
  type TwinTypeDescriptor,
} from "@/api/twins";

const TYPE_BADGE_COLOR: Record<TwinType, string> = {
  SITE: "bg-indigo-100 text-indigo-700",
  BUILDING: "bg-sky-100 text-sky-700",
  FLOOR: "bg-cyan-100 text-cyan-700",
  ROOM: "bg-emerald-100 text-emerald-700",
  ZONE: "bg-lime-100 text-lime-700",
  OUTDOOR: "bg-amber-100 text-amber-700",
  EQUIPMENT: "bg-rose-100 text-rose-700",
};

export function TwinsPage() {
  const { t } = useTranslation();
  const qc = useQueryClient();

  const tree = useQuery({ queryKey: ["twin-tree"], queryFn: getTwinTree });
  const flat = useQuery({ queryKey: ["twins"], queryFn: () => listTwins() });
  const types = useQuery({ queryKey: ["twin-types"], queryFn: getTwinTypeDescriptors });

  const [open, setOpen] = useState(false);

  const createMutation = useMutation({
    mutationFn: createTwin,
    onSuccess: () => {
      void qc.invalidateQueries({ queryKey: ["twin-tree"] });
      void qc.invalidateQueries({ queryKey: ["twins"] });
      setOpen(false);
    },
  });

  const deleteMutation = useMutation({
    mutationFn: deleteTwin,
    onSuccess: () => {
      void qc.invalidateQueries({ queryKey: ["twin-tree"] });
      void qc.invalidateQueries({ queryKey: ["twins"] });
    },
  });

  const isLoading = tree.isLoading || flat.isLoading || types.isLoading;

  return (
    <div>
      <PageHeader
        title={t("twins.title")}
        subtitle={t("twins.subtitle")}
        actions={
          <button className="btn-primary" onClick={() => setOpen((p) => !p)}>
            {open ? t("common.cancel") : t("twins.create")}
          </button>
        }
      />

      {open && types.data && flat.data && (
        <CreateTwinForm
          descriptors={types.data}
          existing={flat.data}
          submitting={createMutation.isPending}
          onSubmit={(payload) => createMutation.mutate(payload)}
        />
      )}

      {isLoading && <Spinner />}

      {tree.data && tree.data.length === 0 && (
        <div className="card mt-4">
          <p className="text-sm text-slate-500">{t("twins.empty")}</p>
        </div>
      )}

      <div className="mt-4 space-y-3">
        {tree.data?.map((node) => (
          <TwinTreeRow
            key={node.id}
            node={node}
            depth={0}
            onDelete={(id) => {
              if (confirm(t("common.confirm"))) deleteMutation.mutate(id);
            }}
          />
        ))}
      </div>
    </div>
  );
}

function TwinTreeRow({
  node,
  depth,
  onDelete,
}: {
  node: TwinTreeNode;
  depth: number;
  onDelete: (id: string) => void;
}) {
  const { t } = useTranslation();
  return (
    <div>
      <div
        className="card flex items-center justify-between"
        style={{
          marginLeft: depth * 24,
          borderLeft: `5px solid ${node.color ?? "#cbd5f5"}`,
        }}
      >
        <div className="flex items-center gap-3">
          <span
            className={`rounded px-2 py-0.5 text-xs font-medium ${TYPE_BADGE_COLOR[node.type]}`}
          >
            {node.type}
          </span>
          <div>
            <div className="font-semibold text-slate-900">{node.name}</div>
            <div className="text-xs text-slate-500">
              {t("twins.deviceCount", { count: node.deviceCount })} ·{" "}
              {t("twins.childCount", { count: node.children.length })}
            </div>
          </div>
        </div>
        <button className="btn-danger" onClick={() => onDelete(node.id)}>
          {t("common.delete")}
        </button>
      </div>
      {node.children.map((child) => (
        <TwinTreeRow key={child.id} node={child} depth={depth + 1} onDelete={onDelete} />
      ))}
    </div>
  );
}

function CreateTwinForm({
  descriptors,
  existing,
  submitting,
  onSubmit,
}: {
  descriptors: TwinTypeDescriptor[];
  existing: DigitalTwin[];
  submitting: boolean;
  onSubmit: (payload: TwinRequest) => void;
}) {
  const { t } = useTranslation();
  const [type, setType] = useState<TwinType>("SITE");
  const [name, setName] = useState("");
  const [parentId, setParentId] = useState<string>("");
  const [floor, setFloor] = useState("");
  const [color, setColor] = useState("#3b82f6");
  const [description, setDescription] = useState("");

  const descriptor = useMemo(
    () => descriptors.find((d) => d.type === type),
    [descriptors, type]
  );

  const eligibleParents = useMemo(() => {
    if (!descriptor || descriptor.root) return [];
    const allowed = new Set(descriptor.allowedParents);
    return existing.filter((twin) => allowed.has(twin.type));
  }, [descriptor, existing]);

  function handle(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    onSubmit({
      name: name.trim(),
      type,
      parentId: parentId === "" ? null : parentId,
      floor: floor.trim() || null,
      color: color || null,
      description: description.trim() || null,
    });
  }

  return (
    <form className="card mt-4 grid grid-cols-1 gap-3 md:grid-cols-3" onSubmit={handle}>
      <div>
        <label className="label">{t("twins.type")}</label>
        <select
          className="input"
          value={type}
          onChange={(e) => {
            setType(e.target.value as TwinType);
            setParentId("");
          }}
        >
          {TWIN_TYPES.map((tt) => (
            <option key={tt} value={tt}>
              {tt}
            </option>
          ))}
        </select>
      </div>
      <div>
        <label className="label">{t("twins.name")}</label>
        <input
          className="input"
          value={name}
          onChange={(e) => setName(e.target.value)}
          required
        />
      </div>
      <div>
        <label className="label">{t("twins.parent")}</label>
        <select
          className="input"
          value={parentId}
          disabled={descriptor?.root}
          onChange={(e) => setParentId(e.target.value)}
        >
          <option value="">{descriptor?.root ? t("twins.parentRootOnly") : t("twins.parentRoot")}</option>
          {eligibleParents.map((p) => (
            <option key={p.id} value={p.id}>
              {p.name} ({p.type})
            </option>
          ))}
        </select>
      </div>
      <div>
        <label className="label">{t("twins.floor")}</label>
        <input className="input" value={floor} onChange={(e) => setFloor(e.target.value)} />
      </div>
      <div>
        <label className="label">{t("twins.color")}</label>
        <input
          type="color"
          className="input h-10"
          value={color}
          onChange={(e) => setColor(e.target.value)}
        />
      </div>
      <div className="md:col-span-3">
        <label className="label">{t("twins.description")}</label>
        <input
          className="input"
          value={description}
          onChange={(e) => setDescription(e.target.value)}
          maxLength={500}
        />
      </div>
      <div className="md:col-span-3 flex justify-end">
        <button type="submit" disabled={submitting} className="btn-primary">
          {submitting ? t("common.loading") : t("common.save")}
        </button>
      </div>
    </form>
  );
}
