import {
  createContext,
  ReactNode,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
} from "react";
import { useNavigate } from "react-router-dom";
import {
  loadTokens,
  saveTokens,
  setUnauthorizedHandler,
  AuthTokens,
} from "@/api/client";
import {
  login as loginRequest,
  register as registerRequest,
  logout as logoutRequest,
  getProfile,
  ProfileResponse,
  TokenResponse,
} from "@/api/auth";

interface AuthContextValue {
  user: ProfileResponse | null;
  tokens: AuthTokens | null;
  loading: boolean;
  login: (username: string, password: string) => Promise<void>;
  register: (username: string, email: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
  refreshProfile: () => Promise<void>;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

function mapTokens(payload: TokenResponse): AuthTokens {
  return {
    accessToken: payload.accessToken,
    refreshToken: payload.refreshToken,
    expiresAt: new Date(payload.accessTokenExpiresAt).getTime(),
  };
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const navigate = useNavigate();
  const [tokens, setTokens] = useState<AuthTokens | null>(() => loadTokens());
  const [user, setUser] = useState<ProfileResponse | null>(null);
  const [loading, setLoading] = useState(true);

  const refreshProfile = useCallback(async () => {
    try {
      const profile = await getProfile();
      setUser(profile);
    } catch {
      setUser(null);
    }
  }, []);

  const performLogout = useCallback(async () => {
    const currentTokens = loadTokens();
    if (currentTokens?.refreshToken) {
      try {
        await logoutRequest(currentTokens.refreshToken);
      } catch {
        // intentional swallow — local state still cleared
      }
    }
    saveTokens(null);
    setTokens(null);
    setUser(null);
    navigate("/login", { replace: true });
  }, [navigate]);

  useEffect(() => {
    setUnauthorizedHandler(() => {
      setTokens(null);
      setUser(null);
      navigate("/login", { replace: true });
    });
  }, [navigate]);

  useEffect(() => {
    let cancelled = false;
    async function init() {
      if (tokens) {
        await refreshProfile();
      }
      if (!cancelled) setLoading(false);
    }
    void init();
    return () => {
      cancelled = true;
    };
  }, [tokens, refreshProfile]);

  const login = useCallback(
    async (username: string, password: string) => {
      const payload = await loginRequest(username, password);
      const next = mapTokens(payload);
      saveTokens(next);
      setTokens(next);
      await refreshProfile();
    },
    [refreshProfile]
  );

  const register = useCallback(
    async (username: string, email: string, password: string) => {
      const payload = await registerRequest(username, email, password);
      const next = mapTokens(payload);
      saveTokens(next);
      setTokens(next);
      await refreshProfile();
    },
    [refreshProfile]
  );

  const value = useMemo<AuthContextValue>(
    () => ({
      user,
      tokens,
      loading,
      login,
      register,
      logout: performLogout,
      refreshProfile,
    }),
    [user, tokens, loading, login, register, performLogout, refreshProfile]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within AuthProvider");
  return ctx;
}
