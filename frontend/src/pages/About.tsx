import { useTranslation } from "react-i18next";
import { PageHeader } from "@/components/PageHeader";

export function AboutPage() {
  const { t } = useTranslation();
  return (
    <div>
      <PageHeader title={t("about.title")} />
      <div className="card max-w-2xl">
        <p className="text-sm leading-6 text-slate-700">{t("about.body")}</p>
        <ul className="mt-4 list-inside list-disc space-y-1 text-sm text-slate-600">
          <li>Spring Boot 3.4 main application (port 8080)</li>
          <li>Spring Boot Weather Forecast microservice (port 8081)</li>
          <li>PostgreSQL (×2 schemas) + Redis cache</li>
          <li>Spring AI · Apache POI · OpenPDF · Spring Cloud OpenFeign</li>
          <li>React 18 + Vite + TypeScript + Tailwind + Recharts</li>
        </ul>
      </div>
    </div>
  );
}
