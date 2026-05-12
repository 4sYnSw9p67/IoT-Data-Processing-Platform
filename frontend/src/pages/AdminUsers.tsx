import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useTranslation } from "react-i18next";
import { PageHeader } from "@/components/PageHeader";
import { Spinner } from "@/components/Spinner";
import { listUsers, updateUserRole } from "@/api/admin";

export function AdminUsersPage() {
  const { t } = useTranslation();
  const qc = useQueryClient();
  const users = useQuery({ queryKey: ["admin", "users"], queryFn: listUsers });
  const roleMutation = useMutation({
    mutationFn: ({ id, role }: { id: string; role: "USER" | "ADMIN" }) =>
      updateUserRole(id, role),
    onSuccess: () => void qc.invalidateQueries({ queryKey: ["admin", "users"] }),
  });

  return (
    <div>
      <PageHeader title={t("admin.title")} />
      <div className="overflow-hidden rounded-lg border border-slate-200 bg-white">
        {users.isLoading && (
          <div className="p-6">
            <Spinner />
          </div>
        )}
        {users.data && (
          <table className="w-full divide-y divide-slate-200 text-sm">
            <thead className="bg-slate-50 text-left text-xs uppercase tracking-wider text-slate-500">
              <tr>
                <th className="px-4 py-2">{t("auth.username")}</th>
                <th className="px-4 py-2">{t("auth.email")}</th>
                <th className="px-4 py-2">{t("admin.role")}</th>
                <th className="px-4 py-2">{t("admin.enabled")}</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {users.data.map((u) => (
                <tr key={u.id}>
                  <td className="px-4 py-2 font-medium">{u.username}</td>
                  <td className="px-4 py-2">{u.email}</td>
                  <td className="px-4 py-2">
                    <select
                      className="input"
                      value={u.role}
                      onChange={(e) =>
                        roleMutation.mutate({
                          id: u.id,
                          role: e.target.value as "USER" | "ADMIN",
                        })
                      }
                    >
                      <option value="USER">USER</option>
                      <option value="ADMIN">ADMIN</option>
                    </select>
                  </td>
                  <td className="px-4 py-2">{u.enabled ? "✓" : "—"}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}
