import { FormEvent, useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { useTranslation } from "react-i18next";
import { PageHeader } from "@/components/PageHeader";
import { Spinner } from "@/components/Spinner";
import { aiStatus, askAi, type ChatResponse } from "@/api/ai";

interface ChatTurn {
  role: "user" | "assistant";
  content: string;
  meta?: ChatResponse;
}

export function AiAssistantPage() {
  const { t } = useTranslation();
  const status = useQuery({ queryKey: ["ai-status"], queryFn: aiStatus });
  const [question, setQuestion] = useState("");
  const [history, setHistory] = useState<ChatTurn[]>([]);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function handle(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!question.trim()) return;
    setError(null);
    setHistory((h) => [...h, { role: "user", content: question }]);
    setBusy(true);
    const q = question;
    setQuestion("");
    try {
      const reply = await askAi(q);
      setHistory((h) => [
        ...h,
        { role: "assistant", content: reply.reply, meta: reply },
      ]);
    } catch (err) {
      console.error(err);
      setError("Request failed");
    } finally {
      setBusy(false);
    }
  }

  return (
    <div>
      <PageHeader title={t("ai.title")} />
      {status.isLoading ? (
        <Spinner />
      ) : !status.data?.enabled ? (
        <p className="card text-sm text-amber-800">{t("ai.disabled")}</p>
      ) : (
        <div className="card">
          <div className="max-h-96 space-y-3 overflow-y-auto">
            {history.map((turn, idx) => (
              <div
                key={idx}
                className={`rounded-lg p-3 text-sm ${
                  turn.role === "user"
                    ? "bg-brand-50 text-brand-900"
                    : "bg-slate-100 text-slate-900"
                }`}
              >
                <p className="whitespace-pre-wrap">{turn.content}</p>
                {turn.meta && (
                  <p className="mt-1 text-xs text-slate-500">
                    {turn.meta.devicesConsidered.length} devices ·{" "}
                    {turn.meta.measurementsConsidered} measurements
                  </p>
                )}
              </div>
            ))}
            {history.length === 0 && (
              <p className="text-sm text-slate-500">{t("ai.placeholder")}</p>
            )}
          </div>
          <form className="mt-4 flex gap-2" onSubmit={handle}>
            <input
              className="input flex-1"
              value={question}
              onChange={(e) => setQuestion(e.target.value)}
              placeholder={t("ai.placeholder")}
              maxLength={2000}
            />
            <button
              type="submit"
              disabled={busy}
              className="btn-primary whitespace-nowrap"
            >
              {busy ? t("common.loading") : t("ai.ask")}
            </button>
          </form>
          {error && <p className="mt-2 text-sm text-red-700">{error}</p>}
        </div>
      )}
    </div>
  );
}
