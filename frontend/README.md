# IoT Platform — Frontend SPA

React 18 + TypeScript single-page app for the IoT Data Processing Platform main API. Built with Vite, Tailwind CSS, TanStack Query, React Router, Recharts, react-hook-form + zod, and react-i18next (English + Bulgarian).

## Quick start

```bash
npm install
npm run dev   # http://localhost:5173, proxies /api -> http://localhost:8080
```

The dev server reads `VITE_API_BASE_URL` from `.env.local` (defaults to `/api`).

## Build

```bash
npm run build      # tsc -b && vite build, output: dist/
npm run preview    # serve the production build locally
```

## Docker

The bundled `Dockerfile` produces a multi-stage image: Node builds, nginx serves. The provided `nginx.conf` proxies `/api/*`, `/swagger-ui/*` and `/v3/api-docs` to the `main-app` container, so the SPA and the backend share an origin (no CORS surprises in production).

```bash
docker build -t iot-frontend .
docker run --rm -p 80:80 iot-frontend
```

## Folder layout

```
src/
├── api/            # axios client + endpoint wrappers (auth, devices, rooms, …)
├── auth/           # AuthContext, ProtectedRoute, token storage
├── components/     # Layout, Navbar, LanguageSwitcher, Spinner, SeverityBadge, …
├── i18n/           # react-i18next setup + en.json + bg.json
├── pages/          # Login, Register, Dashboard, Devices, Rooms, Alerts, …
└── main.tsx        # React Query + Router bootstrap
```

## Pages

- `/login`, `/register` — JWT-backed auth flow
- `/dashboard` — overview + recent measurements + latest alerts
- `/devices`, `/devices/:id` — CRUD + per-device time-series chart (Recharts)
- `/rooms` — CRUD with device counts
- `/alerts` — paginated alert feed with acknowledge action
- `/rules` — automation rule builder for the four supported conditions
- `/forecast` — cached weather forecast from the microservice
- `/reports` — date-range Excel / PDF export download
- `/ai` — Spring AI assistant (visible only when backend reports `enabled`)
- `/admin/users` — admin-only role management
- `/profile`, `/about`
