import { Navigate, Route, Routes } from "react-router-dom";
import { Layout } from "@/components/Layout";
import { ProtectedRoute } from "@/auth/ProtectedRoute";
import { LoginPage } from "@/pages/Login";
import { RegisterPage } from "@/pages/Register";
import { DashboardPage } from "@/pages/Dashboard";
import { DevicesPage } from "@/pages/Devices";
import { DeviceDetailPage } from "@/pages/DeviceDetail";
import { TwinsPage } from "@/pages/Twins";
import { AlertsPage } from "@/pages/Alerts";
import { RulesPage } from "@/pages/Rules";
import { ForecastPage } from "@/pages/Forecast";
import { ReportsPage } from "@/pages/Reports";
import { AiAssistantPage } from "@/pages/AiAssistant";
import { AdminUsersPage } from "@/pages/AdminUsers";
import { ProfilePage } from "@/pages/Profile";
import { AboutPage } from "@/pages/About";

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route
        element={
          <ProtectedRoute>
            <Layout />
          </ProtectedRoute>
        }
      >
        <Route path="/" element={<DashboardPage />} />
        <Route path="/devices" element={<DevicesPage />} />
        <Route path="/devices/:deviceId" element={<DeviceDetailPage />} />
        <Route path="/twins" element={<TwinsPage />} />
        <Route path="/alerts" element={<AlertsPage />} />
        <Route path="/rules" element={<RulesPage />} />
        <Route path="/forecast" element={<ForecastPage />} />
        <Route path="/reports" element={<ReportsPage />} />
        <Route path="/ai" element={<AiAssistantPage />} />
        <Route path="/profile" element={<ProfilePage />} />
        <Route path="/about" element={<AboutPage />} />
        <Route
          path="/admin/users"
          element={
            <ProtectedRoute requireRole="ADMIN">
              <AdminUsersPage />
            </ProtectedRoute>
          }
        />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
