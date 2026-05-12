import { FormEvent, useState } from "react";
import { useTranslation } from "react-i18next";
import { PageHeader } from "@/components/PageHeader";
import { useAuth } from "@/auth/AuthContext";
import { updateProfile } from "@/api/auth";

export function ProfilePage() {
  const { t } = useTranslation();
  const { user, refreshProfile } = useAuth();
  const [email, setEmail] = useState(user?.email ?? "");
  const [password, setPassword] = useState("");
  const [busy, setBusy] = useState(false);
  const [info, setInfo] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  async function handle(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);
    setInfo(null);
    setBusy(true);
    try {
      await updateProfile({
        email: email !== user?.email ? email : undefined,
        password: password ? password : undefined,
      });
      await refreshProfile();
      setInfo("Saved");
      setPassword("");
    } catch {
      setError("Update failed");
    } finally {
      setBusy(false);
    }
  }

  return (
    <div>
      <PageHeader title={t("profile.title")} subtitle={user?.username} />
      <form className="card grid max-w-md grid-cols-1 gap-3" onSubmit={handle}>
        <div>
          <label className="label">{t("auth.username")}</label>
          <input className="input" value={user?.username ?? ""} disabled />
        </div>
        <div>
          <label className="label">{t("auth.email")}</label>
          <input
            type="email"
            className="input"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
          />
        </div>
        <div>
          <label className="label">{t("auth.password")}</label>
          <input
            type="password"
            className="input"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            placeholder="••••••••"
            minLength={8}
          />
        </div>
        <div className="flex items-center gap-3">
          <button type="submit" disabled={busy} className="btn-primary">
            {busy ? t("common.loading") : t("common.save")}
          </button>
          {info && <span className="text-sm text-emerald-700">{info}</span>}
          {error && <span className="text-sm text-red-700">{error}</span>}
        </div>
      </form>
    </div>
  );
}
