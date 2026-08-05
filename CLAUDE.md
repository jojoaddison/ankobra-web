# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Repository state

The **JHipster 9.1.0 monolith is built end-to-end** from `app.jdl` — Java 25, Spring Boot 4.0.6, Angular 21, PostgreSQL, JWT. All phases are complete and committed:

- **Design system** (`content/scss/global.scss`, `app/core/theme/theme.service.ts`): demo tokens ported verbatim; `data-theme` on `<html>` defaults by time of day (06–18 light / 18–06 dark) with a manual toggle override.
- **Public marketing front** (`app/home/`): full site (hero + animated terminal, about, services, portfolio, markets, team, contact). Contact form posts to `PublicEnquiryResource` `POST /api/public/enquiries` (permitAll) → saves a `Lead`. Renders full-bleed; the JHipster navbar is hidden on `/`, `/portal` and `/cms` (each renders its own chrome — see `navbar.ts` `ownsChrome`).
- **Portal / CMS** (`app/portal/`): shell (top bar + collapsible sidebar) at `/portal`, with bespoke views — Overview (KPIs + hand-rolled SVG charts), Projects (milestone timeline), Clients, Service catalogue, Quote builder (persists Quote+QuoteLines), Support desk, Training, Team. Views hit the generated entity services; shared helpers in `app/portal/shared/portal-format.ts`, shared `.pview` styles in `global.scss`.
- **Admin & CMS** (`app/admin/admin-home/`, `app/cms/`): navbar has no Entities/Administration dropdowns — admins get one **Administration** link to `/admin`, a card landing (Portal + CMS + every system tool). `/cms` is a **portal-style shell** (`cms.ts` + `cms.routes.ts`) whose sidebar hosts the 10 domain entity managers as children, so the CRUD pages live at **`/cms/<entity>`** and keep the shell mounted (dashboard = `cms/overview/`). The generated entity list/detail templates were rewritten from `['/<entity>', …]` to `['/cms/<entity>', …]`; the 10 domain entities were moved out of `entities/entity.routes.ts` (which now keeps only `authority` + `user-management`) into `cms.routes.ts`. ADMIN gating is enforced once at the parent `/cms` route. **Bootstrap theming**: `ThemeService` sets `data-bs-theme` alongside `data-theme`, and `$primary` = brand green in `_bootstrap-variables.scss`, so all admin/entity Bootstrap pages follow light/dark + brand.
- **Data & security**: `config/DataSeeder.java` (dev profile) loads the real demo fixtures + logins `kojo`/`ama` (both `demo1234`); faker disabled in dev. **Role scoping** in `security/PortalSecurityService` restricts a client (`ROLE_USER`) to their own `Client`'s projects/tickets/quotes; staff (`ROLE_ADMIN`/`ROLE_CONSULTANT`) see all. Authorization is two layers (see `docs/security-20260805-0936.md`): a **coarse deny-by-default block in `SecurityConfiguration`** — CMS reference data (leads/service-items/courses/team-members/milestones/quote-lines) is staff-only, writes on projects/clients/quotes are staff-only, and no client deletes anything — plus **per-object ownership checks in `TicketResource`**, the one entity clients may write, where `POST` forces the owner to the caller's own client and `PUT`/`PATCH` refuse both foreign tickets and re-parenting. `/count` is scoped alongside its list endpoint on every scoped resource, or criteria filters turn it into an extraction oracle. `PortalScopingIT` covers reads; **`PortalWriteScopingIT` (56 cases) covers the write matrix** and is the guard against a regenerated resource silently reopening it.
- **Observability** (`config/MetricsConfiguration.java`): JHipster's Prometheus metrics (`/management/prometheus`, JVM/HTTP/process, percentile histograms) plus **app-level instrumentation** — a `ankobra.enquiries.submitted` counter (tagged by need) + `@Timed` on `PublicEnquiryResource`, and `@Observed` on `PortalSecurityService`'s authz methods. `@Timed`/`@Counted`/`@Observed` are enabled by the aspect beans in `MetricsConfiguration`. **Distributed tracing** is Micrometer Tracing → OpenTelemetry over OTLP via **`spring-boot-starter-opentelemetry`** (Boot 4 splits the tracing/OTLP autoconfig into `spring-boot-micrometer-tracing-opentelemetry` + `spring-boot-opentelemetry`, which that starter pulls — the raw `micrometer-tracing-bridge-otel`/`opentelemetry-exporter-otlp` jars do **not** register it, so no `Tracer`/exporter is created; `TracingAutoConfigurationIT` guards this). The starter also brings an OTLP metrics registry + log appender, both disabled in config so metrics stay on Prometheus and logs on stdout→Alloy→Loki. Config-gated via `OTEL_*` env. **In prod it's on** and exports to the server's shared `otel-collector` (`http://otel-collector:4318/v1/traces`, reached over the external `monitoring` docker network the app also joins) → Tempo; metrics→Mimir and logs→Loki flow through the same collector/Alloy. Trace/span ids are woven into the console log pattern (`logback-spring.xml`), so Alloy-shipped logs correlate with traces in Grafana. Defaults live in `deploy/prod-server/compose.yml` (`OTEL_TRACING_ENABLED=true`, sampling `1.0`); `application-prod.yml` keeps a generic localhost fallback for other hosts. Tracing is disabled in the test profile. **These files (`pom.xml` deps, the three `application*.yml` `management.tracing`/`otlp` blocks, `logback-spring.xml`, `MetricsConfiguration`) are custom — re-apply after regeneration.**
- **Hardening** (audit P1, `docs/security-20260805-0936.md`): `security/LoginAttemptService` throttles failed logins per source IP **and** per username with a progressive lockout, checked in `AuthenticateController` before authentication (429, not 401). It is **in-memory and per-instance** — scaling out means moving it to Redis or the DB. `security/PasswordPolicy` enforces a 12-char floor plus a denylist and login-containment check. The public enquiry endpoint carries an off-screen honeypot field (`website`) whose submissions are discarded with a 201 and no counter increment. nginx adds HSTS, `server_tokens off`, a body cap and `limit_req` zones — the zones live in `deploy/prod-server/ankobra-web-limits.conf`, installed to `conf.d/` because `limit_req_zone` is only valid in `http{}`; `deploy.sh --with-nginx` ships it **before** the site file or `nginx -t` fails on the missing zone. CSP has lost `'unsafe-eval'` and `storage.googleapis.com` (`'unsafe-inline'` still remains).
- **Verified green**: `./mvnw verify` (553 tests) and `npm test` (130 files, lint clean). Angular runtime is pinned at 21.2.19+ / tooling 21.2.20+ to clear the XSS sanitization-bypass advisories; `npm audit --omit=dev` is clean and CI now fails if it stops being.

When regenerating entities via JDL, re-apply: the five scoped resources (Project/Ticket/Client/Quote + `PublicEnquiryResource`), the `@WithMockUser(authorities = "ROLE_CONSULTANT")` on **ten** generated `ResourceIT`s (Project/Ticket/Client/Quote/Lead/Course/Milestone/QuoteLine/ServiceItem/TeamMember — the generator emits a bare `@WithMockUser`, i.e. `ROLE_USER`, which the authorization rules below now correctly reject), the **domain API authorization block in `SecurityConfiguration`** and the per-object checks in `TicketResource` (see below), the full-bleed `main.html`/hidden-navbar tweaks, and the **CMS nesting**: move the 10 domain entities out of `entities/entity.routes.ts` back into `cms/cms.routes.ts` children, and re-prefix the regenerated `['/<entity>', …]` links in each entity list/detail template to `['/cms/<entity>', …]`. The originating reference files remain the spec:

| File                                | Role                                                                                                                                                                                                          |
| ----------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `create-web-app-prompt.txt`         | The brief. Build a **JHipster monolith** with a public marketing front and a CMS/portal; author a **JDL file for the models before implementation**; plan in phases + TODO lists; ask questions where unsure. |
| `jojoaddison-consultancy-demo.html` | Single-file, dependency-free HTML/CSS/JS prototype of both halves of the app. **The authoritative source for design tokens, layout, copy and the domain model.**                                              |
| `jojoaddison-consultancy.pdf`       | 2020 company profile — background content source.                                                                                                                                                             |

Do not treat the demo HTML as code to port line-by-line; treat it as the spec. Extract tokens, IA, and entity shapes from it.

## Toolchain on this machine

Node and JHipster are managed by **nvm**, which `~/.bashrc` sources but non-interactive shells do not. A bare tool shell sees only the system `node` (`/usr/bin/node` v22.22.1, **no npm**), `java`, `mvn` 3.9.15 and `docker` — `npm`/`npx`/`jhipster` will all look missing. Source nvm first in every shell that needs them:

```bash
export NVM_DIR="$HOME/.nvm"; . "$NVM_DIR/nvm.sh"; nvm use v22.22.2
```

**Do not install node, npm, or generator-jhipster — they are already here.** `generator-jhipster` is installed under six nvm node versions, and the newest node is _not_ the newest JHipster:

| nvm node          | generator-jhipster       |
| ----------------- | ------------------------ |
| **v22.22.2**      | **9.1.0** ← use this one |
| v24.13.0, v24.3.0 | 8.11.0                   |
| v20.18.0          | 8.7.1                    |
| v22.17.0          | 8.0.0                    |
| v16.20.2          | 7.9.3                    |

nvm's `default` alias is `stable` → v24.18.0, which has **no** jhipster at all. JHipster 9.1.0 requires node `^22.18.0 || >=24.11.0`, so v22.22.2 is the intended pairing.

Once the app is generated the standard JHipster commands (`./mvnw`, `npm start`, `./mvnw verify`, `npm test`, `jhipster jdl <file>.jdl`) apply; there are no project-specific commands yet.

## Target tech stack — Java 25 · Spring Boot 4 · Angular 21 · PostgreSQL

The chosen stack is exactly what generator-jhipster 9.1.0 targets; verified against the installed generator, not assumed:

- **PostgreSQL** — the fixed database (brief, line 8). Keep it non-reactive (see Spring Boot note) — a servlet/Spring MVC + JPA stack. Dev can use H2 in-memory with Postgres in prod (JHipster default) unless told otherwise.
- **Auth: JWT** — self-contained token auth (JHipster default). The demo's client-vs-consultant split maps onto authorities: consultants are staff (a `ROLE_CONSULTANT`/admin-like authority seeing all data), clients (`ROLE_USER`) are scoped to their own `Client` and its projects/tickets. No external identity server.
- **Angular 21** — pinned by the `angular` generator (`@angular/common` 21.2.14, `@angular/cli` 21.2.12). Select with `--client-framework angular`.
- **Spring Boot 4.0.6** — shipped as `generators/spring-boot/resources/spring-boot-dependencies-4.json`. The generator flips to it via a computed `springBoot4` default = `!(databaseTypeSql && reactive) && !databaseTypeCouchbase`. **A non-reactive SQL monolith gets Spring Boot 4 automatically** — the 3.5.14 set (`spring-boot-dependencies.json`) is only the fallback for reactive-SQL / Couchbase. Do not pick a reactive stack, or you silently drop to Boot 3.5.
- **Java 25** — supported but NOT the default. `JAVA_COMPATIBLE_VERSIONS = ['21', '25']` (generator default 21). It is NOT a JDL config key and NOT a `jhipster app` CLI flag; set it by seeding `.yo-rc.json` with `"javaVersion": "25"` before generating (it overrides the `RECOMMENDED_JAVA_VERSION = '21'` default). **Use `JAVA_HOME=/usr/lib/jvm/jdk-25.0.2-oracle-x64` — the full Oracle JDK.** `/usr/lib/jvm/java-25-openjdk-amd64` is a **JRE with no `javac`**, so Maven falls through PATH to the ambient JDK 26 and fails with "release version 25 not supported". The ambient `JAVA_HOME` (JDK 26) is outside the compatible list, so always override it for this project:

  ```bash
  export JAVA_HOME=/usr/lib/jvm/jdk-25.0.2-oracle-x64; export PATH="$JAVA_HOME/bin:$PATH"
  ```

## Design system contract

The demo defines a complete token set in `:root` and `html[data-theme="dark"]` at the top of `jojoaddison-consultancy-demo.html`. **Port these tokens verbatim into the generated app's global stylesheet** — every component styles off `var(--…)`, never hardcoded colors.

- Brand: `--brand:#0d6b2f` (deep green), `--brand-deep:#08511f`, accent `--accent:#f2d024` (yellow). Light plane `#f9f9f7`, dark plane `#0d0d0d`.
- Categorical/sequential chart series (`--series-1..3`, `--seq-100..550`) and status colors (`--good/--warning/--serious/--critical`) are already defined — reuse them for any dashboard chart rather than inventing a palette.
- Theming is `data-theme` on `<html>`, toggled by a button in both the marketing header and the app top bar. **Per the brief, the default is chosen by time of day, not `prefers-color-scheme`: 06:00–18:00 → light, 18:00–06:00 → dark.** Keep the manual toggle as an override on top of that time-based default (and re-evaluate the clock when the app is left open across the 6/18:00 boundary).
- Type: system sans (`--font`) plus a mono stack (`--mono`) used for the `<jojoaddison/>` wordmark and the animated terminal. Radii `--r-sm..--r-xl`, shadows `--shadow-1..3`, page max width `--maxw:1200px`.
- Charts in the demo are hand-rolled SVG (`drawGroupedBars`, `barTop`, `niceStep`, …). No charting library is assumed.

## Application shape

Two surfaces in one monolith, matching the demo:

**Public marketing site** — sections `#about`, `#services`, `#work`, `#markets`, `#team`, `#contact`, plus footer. The four service pillars (Bespoke solutions, Digital transformation, Capacity building, Enterprise integration) and the Consultancy/Solutions/Training tabs are the spine of the services IA. The contact form is the lead-capture entry point.

**Authenticated portal / CMS** — app shell with top bar (global search over projects, clients, tickets), collapsible sidebar, and a main view region. Sidebar groups and views (`NAV` in the demo):

- Delivery: Overview (KPIs + charts), Projects, Clients
- Commercial: Service catalogue, Quote builder
- Operations: Support desk, Training, Team

The demo logs in as either a **client** or a **consultant** — role-based scoping (a client sees only their own projects/tickets) is part of the design, and maps onto JHipster authorities.

## Domain model source

The JDL should be derived from the demo's in-memory fixtures, which already carry field-level detail:

- `PROJECTS` — id, name, client, pillar, status, progress %, lead, due date, budget/spent, tech stack, ordered milestones with state (`done`/`now`/`next`).
- `CLIENTS` — name, sector, since, project count, health, total spend. Sectors come from `MARKETS` (12 entries).
- `TICKETS` — id, subject, priority, client, owner, age, SLA, open/closed state (support desk).
- `CATALOGUE` — service line items with rate, unit (`per phase`, `per module`, `per month`, …) and pillar; the quote builder composes these into estimates.
- `COURSES` — training courses with module counts, delivery mode, enrolment, progress.
- `TEAM` — consultant name, initials, role, qualification, bio.
- Aggregates for the overview: `HOURS` (delivery hours by month × pillar), `TREND` (tickets raised vs resolved, 12 weeks), `REVENUE` (by pillar).

Status vocabulary is shared across projects, clients and tickets — `good | warn | serious | crit | done` rendered as _On track / At risk / Delayed / Blocked / Delivered_, and reused as ticket priority _Low / Medium / High / Critical_. Model it as one enum.

## Working style for this project

The brief asks explicitly for phased planning with TODO task lists, and for the JDL to land before implementation. Follow that order: JDL → generation → public front → portal views. Ask when the brief is ambiguous rather than guessing at business rules.
