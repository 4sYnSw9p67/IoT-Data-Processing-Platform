import { NavLink } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { useAuth } from "@/auth/AuthContext";
import { LanguageSwitcher } from "./LanguageSwitcher";

const NAV_ITEMS = [
  { to: "/", labelKey: "nav.dashboard", end: true },
  { to: "/devices", labelKey: "nav.devices" },
  { to: "/twins", labelKey: "nav.twins" },
  { to: "/alerts", labelKey: "nav.alerts" },
  { to: "/rules", labelKey: "nav.rules" },
  { to: "/forecast", labelKey: "nav.forecast" },
  { to: "/reports", labelKey: "nav.reports" },
  { to: "/ai", labelKey: "nav.ai" },
  { to: "/profile", labelKey: "nav.profile" },
  { to: "/about", labelKey: "nav.about" },
];

export function Navbar() {
  const { t } = useTranslation();
  const { user, logout } = useAuth();

  return (
    <nav className="sticky top-0 z-20 border-b border-slate-200 bg-white/90 backdrop-blur">
      <div className="mx-auto flex max-w-7xl items-center justify-between gap-4 px-4 py-3">
        <div className="flex items-center gap-2">
          <span className="rounded bg-brand-600 px-2 py-1 text-sm font-bold text-white">
            IoT
          </span>
          <span className="hidden text-sm font-semibold text-slate-700 sm:block">
            {t("app.title")}
          </span>
        </div>
        <div className="flex flex-1 flex-wrap items-center justify-end gap-1">
          {NAV_ITEMS.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.end}
              className={({ isActive }) =>
                `rounded-md px-3 py-1.5 text-sm font-medium transition-colors ${
                  isActive
                    ? "bg-brand-100 text-brand-700"
                    : "text-slate-600 hover:bg-slate-100"
                }`
              }
            >
              {t(item.labelKey)}
            </NavLink>
          ))}
          {user?.role === "ADMIN" && (
            <NavLink
              to="/admin/users"
              className={({ isActive }) =>
                `rounded-md px-3 py-1.5 text-sm font-medium transition-colors ${
                  isActive
                    ? "bg-amber-100 text-amber-800"
                    : "text-amber-700 hover:bg-amber-50"
                }`
              }
            >
              {t("nav.admin")}
            </NavLink>
          )}
          <LanguageSwitcher />
          <button
            type="button"
            onClick={() => void logout()}
            className="ml-2 rounded-md border border-slate-200 px-3 py-1.5 text-sm font-medium text-slate-600 hover:bg-slate-50"
          >
            {t("nav.logout")}
          </button>
        </div>
      </div>
    </nav>
  );
}
