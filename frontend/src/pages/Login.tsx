import { FormEvent, useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { useAuth } from "@/auth/AuthContext";
import { LanguageSwitcher } from "@/components/LanguageSwitcher";

export function LoginPage() {
  const { t } = useTranslation();
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const from = (location.state as { from?: { pathname?: string } } | null)?.from
    ?.pathname;

  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);
    setLoading(true);
    try {
      await login(username, password);
      navigate(from ?? "/", { replace: true });
    } catch {
      setError(t("auth.error"));
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-gradient-to-br from-brand-50 via-slate-50 to-slate-100 p-4">
      <div className="w-full max-w-md">
        <div className="mb-4 flex items-center justify-between">
          <span className="rounded bg-brand-600 px-3 py-1 text-sm font-bold text-white">
            IoT
          </span>
          <LanguageSwitcher />
        </div>
        <div className="card">
          <h1 className="text-xl font-semibold">{t("auth.login")}</h1>
          <p className="mt-1 text-sm text-slate-500">{t("app.subtitle")}</p>
          <form className="mt-5 space-y-3" onSubmit={handleSubmit}>
            <div>
              <label className="label" htmlFor="username">
                {t("auth.username")}
              </label>
              <input
                id="username"
                className="input"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                autoComplete="username"
                required
              />
            </div>
            <div>
              <label className="label" htmlFor="password">
                {t("auth.password")}
              </label>
              <input
                id="password"
                type="password"
                className="input"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                autoComplete="current-password"
                required
              />
            </div>
            {error && (
              <p className="rounded bg-red-50 px-3 py-2 text-sm text-red-700">
                {error}
              </p>
            )}
            <button type="submit" disabled={loading} className="btn-primary w-full">
              {loading ? t("common.loading") : t("auth.submit")}
            </button>
          </form>
          <p className="mt-4 text-center text-sm text-slate-500">
            {t("auth.needAccount")}{" "}
            <Link
              to="/register"
              className="font-medium text-brand-600 hover:underline"
            >
              {t("auth.register")}
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
}
