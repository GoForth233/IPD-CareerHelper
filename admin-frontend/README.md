# CareerLoop admin console

Stand-alone Vite + Vue 3 + Element Plus SPA for back-office staff.
Independent from the WeChat mini-program in `frontend/` so we can pull in
desktop-grade tooling (Element Plus, Pinia) without bloating the
mini-program package.

## Local dev

```bash
cd admin-frontend
npm install
npm run dev   # http://localhost:5174
```

The dev server proxies `/api` and `/auth` to the Spring backend at
`http://localhost:8080`. Override with `VITE_API_BASE_URL` if your
backend lives elsewhere.

## Auth

The admin reuses the C-side `/auth/login` endpoint (so we don't have to
maintain a second auth surface). After login we call `/api/admin/whoami`
to confirm the JWT belongs to a user with the `ADMIN` role; non-admin
logins are rejected client-side and the token is dropped.

To grant an existing user admin access:

```sql
INSERT INTO user_roles (user_id, role_id)
SELECT :userId, role_id FROM roles WHERE role_code = 'ADMIN';
```

## Pages

- **Login** — email/password, role check.
- **Dashboard** — org KPIs, radar averages, weekly-report manual run.
- **Students** — list of students per org with interview counts + last score.
- **Skill Map Editor** — CRUD over `career_paths` and `career_nodes`.
- **Question Bank** — moderate the crowd-sourced interview market: edit, hide, delete.

## Build

```bash
npm run build  # outputs to dist/, deploy behind nginx as a static site
```
