import type { AlertSeverity } from "@/api/alerts";

const STYLES: Record<AlertSeverity, string> = {
  INFO: "bg-slate-100 text-slate-700",
  WARNING: "bg-amber-100 text-amber-800",
  CRITICAL: "bg-red-100 text-red-800",
};

export function SeverityBadge({ severity }: { severity: AlertSeverity }) {
  return <span className={`badge ${STYLES[severity]}`}>{severity}</span>;
}
