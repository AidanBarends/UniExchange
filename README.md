# UniExchange

A campus marketplace platform for buying, selling, and exchanging goods and services within the university community. Built as a group project by students of the Cape Peninsula University of Technology (CPUT).

Unlike Facebook Marketplace or Gumtree, UniExchange is closed to the public: only someone who can prove they hold a `@mycput.ac.za` mailbox can create an account, which is what keeps listings campus-relevant and cuts down on scams.

**Current status:** the backend is fully layered with Spring Security + JWT, and verified-student authentication (signup → email OTP → login) works end to end, with the emailed code acting as a second factor at sign-in and a "Remember me" trusted-device option. The frontend implements those screens, and all remaining pages are scaffolded and routed with an owner assigned to each — the team is filling them in now.

## Table of Contents

- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [Running in VS Code](#running-in-vs-code)
- [Configuration Reference](#configuration-reference)
- [Email / OTP Delivery](#email--otp-delivery)
- [Payments (PayFast)](#payments-payfast)
- [Backend](#backend)
- [Frontend](#frontend)
- [Testing](#testing)
- [Troubleshooting](#troubleshooting)
- [Database](#database)
- [Git Workflow](#git-workflow)
- [Team](#team)
- [Roadmap](#roadmap)

---

## Tech Stack

### Backend

| Component      | Choice                                                             |
|----------------|--------------------------------------------------------------------|
| Language       | Java 25                                                            |
| Framework      | Spring Boot 4.1.1 (Spring Framework 7.0.9)                         |
| Build tool     | Maven, via the bundled wrapper (`mvnw` / `mvnw.cmd`, Maven 3.9.16) |
| Persistence    | Spring Data JPA / Hibernate 7.4                                    |
| Database       | MySQL 8+ (9.x works; schema designed in MySQL Workbench)           |
| Security       | Spring Security 7.1 + JWT (`jjwt` 0.13, HS256)                     |
| Password hash  | BCrypt                                                             |
| Email          | Spring Mail (Jakarta Mail / Angus) for OTP delivery                |
| Validation     | Jakarta Bean Validation + Apache Commons Validator                 |
| Testing        | JUnit 5, Spring Boot Test, H2 in-memory                            |

### Frontend

| Component      | Choice                                                        |
|----------------|---------------------------------------------------------------|
| Framework      | React 19.2                                                    |
| Language       | TypeScript 6                                                  |
| Build tool     | Vite 8                                                        |
| Routing        | react-router-dom 7                                            |
| Styling        | Tailwind CSS v4 (CSS-first config, no `tailwind.config.js`)   |
| Forms          | react-hook-form 7 + zod 4 (`@hookform/resolvers`)             |
| HTTP           | native `fetch`, wrapped in `src/lib/api/`                      |
| Linting        | ESLint 10 (flat config) + typescript-eslint                   |

No component library and no HTTP client dependency — the API surface is small enough that `fetch` behind one client is enough. `lib/api/` is split one module per feature so team members do not collide in a single file.

## Project Structure

```
UniExchange/
├── Backend/     → Spring Boot REST API      (runs on :8080)
├── Frontend/    → React + TypeScript client (runs on :5173)
└── README.md    → this file
```

## Prerequisites

Four things need to be installed. Check each before starting.

### 1. A JDK, version 25 or newer

The project compiles with `--release 25`, so **JDK 25 is the minimum**. JDK 26 also works.

```bash
java -version          # e.g. openjdk version "25.0.2"
echo $JAVA_HOME        # macOS/Linux
echo %JAVA_HOME%       # Windows cmd
```

**Maven uses `JAVA_HOME`, not whatever `java` is on your `PATH`** — if those two disagree, the build follows `JAVA_HOME`. If you have several JDKs installed:

```bash
# macOS - list every installed JDK
/usr/libexec/java_home -V

# macOS/Linux - point this shell at JDK 25
export JAVA_HOME=$(/usr/libexec/java_home -v 25)

# Windows PowerShell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-25"
```

If `java -version` reports 24 or lower, install JDK 25+ from [Adoptium](https://adoptium.net/) or [jdk.java.net](https://jdk.java.net/) first. Otherwise the build fails with `invalid target release: 25`.

### 2. Node.js 20.19+ or 22.12+

Required by Vite 8. Node 24 LTS is a safe choice.

```bash
node -v    # must be >= 20.19, or >= 22.12
npm -v
```

Get it from [nodejs.org](https://nodejs.org/).

### 3. MySQL server, version 8 or newer

**The backend will not start without it.** H2 is only on the test classpath, so it covers the test suite but cannot run the app.

```bash
mysql --version
```

If that command isn't found, install MySQL Community Server from [dev.mysql.com/downloads/mysql](https://dev.mysql.com/downloads/mysql/) — note the macOS installer puts it in `/usr/local/mysql/bin`, which is not on your `PATH` by default — or via Homebrew:

```bash
brew install mysql && brew services start mysql
```

MySQL Workbench is optional, but handy for inspecting the schema.

### 4. Maven — already included

Do **not** install Maven. Use the wrapper in `Backend/`. On macOS/Linux, make it executable once after cloning:

```bash
chmod +x Backend/mvnw
```

---

## Getting Started

### 1. Clone and branch

`main` is protected by team convention — never commit to it directly.

```bash
git clone <repo-url>
cd UniExchange
git checkout -b MYK-240453182     # your own initials-studentNumber
```

### 2. Set up MySQL

Make sure the server is running, then create the database. The app creates the **tables** itself (`ddl-auto=update`), but not the database.

```bash
mysql -u root -p
```

> **`zsh: command not found: mysql`?** The official macOS installer does not add
> MySQL to your `PATH`. Add it:
>
> ```bash
> export PATH="/usr/local/mysql/bin:$PATH"     # add to ~/.zshrc to keep it
> ```
>
> The **server** can be running perfectly well even when the `mysql` client is
> missing from `PATH` — check with `pgrep -fl mysqld`, or use MySQL Workbench, or
> System Settings → MySQL. Homebrew installs land in `/opt/homebrew/bin` instead
> and are already on `PATH`.

```sql
CREATE DATABASE IF NOT EXISTS uniexchange
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

The connection URL also passes `createDatabaseIfNotExist=true`, so this is belt-and-braces — but doing it explicitly makes failures easier to diagnose.

### 3. Configure your database password

**Do not type your password into `application.properties`.** That file is committed to git, so a password there gets shared with the whole team and pushed to GitHub. It reads from environment variables instead:

```properties
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD:}
```

Pick whichever option suits how you run the app.

**Option A — environment variable** (works from the terminal):

```bash
# macOS/Linux; add to ~/.zshrc or ~/.bashrc to make it permanent
export DB_PASSWORD='your-mysql-password'

# Windows PowerShell
$env:DB_PASSWORD = "your-mysql-password"
```

**Option B — an untracked properties file** (easier in IntelliJ / VS Code, since you don't have to configure env vars in the run profile). Create `Backend/src/main/resources/application-local.properties`:

```properties
spring.datasource.password=your-mysql-password
```

`application.properties` sets `spring.profiles.active=local`, so this file is
picked up automatically — no extra flags. When it does not exist, Spring simply
ignores it, so teammates who never create one are unaffected. The filename is
already in `.gitignore`, so it can never be committed.

This is also where SMTP credentials go — see
[Email / OTP Delivery](#email--otp-delivery).

> Why a profile file and not `spring.config.import`? Imported config has *lower*
> precedence than the file importing it, so an imported override is silently
> ignored. A profile-specific file overrides the base file, which is what we need.

> If your MySQL `root` account has no password, skip this step — the default is empty.

### 3b. Seed the campuses and categories

Hibernate creates the tables but never puts rows in them, so on a fresh database
the **Create listing** form has empty Campus and Category dropdowns and cannot be
submitted — which makes the whole marketplace look broken when it is only
unseeded. Run this once:

```bash
cd Backend
mysql -u root -p uniexchange < src/main/resources/db/seed-reference-data.sql
```

It adds the five CPUT campuses and nine listing categories, and is safe to run
again — every statement does nothing if the row is already there.

### 4. Run the backend

```bash
cd Backend
./mvnw spring-boot:run          # Windows: .\mvnw.cmd spring-boot:run
```

> **In VS Code you can skip steps 4 and 5** — press `F5` with
> "Full Stack: Backend + Frontend" selected and both start together.
> See [Running in VS Code](#running-in-vs-code).

The first run downloads dependencies, so it needs internet and takes a minute. You should see `Started UniExchangeApplication`, 22 `create table` statements on a fresh database, and:

```
No spring.mail.host configured - verification emails will be logged, not sent.
```

That warning is expected and intentional — see [Email / OTP Delivery](#email--otp-delivery).

The API is now on **http://localhost:8080**. Quick check:

```bash
curl http://localhost:8080/api/listings     # -> []  (a public endpoint)
```

### 5. Run the frontend

In a **second terminal**:

```bash
cd Frontend
npm install                 # first time only
cp .env.example .env.local  # Windows: copy .env.example .env.local
npm run dev
```

Open **http://localhost:5173**.

`.env.local` only needs editing if your backend is not on port 8080:

```
VITE_API_BASE_URL=http://localhost:8080
```

No dev proxy is configured or needed — the backend already allows `http://localhost:5173` as a CORS origin.

### 6. Try it

1. Go to http://localhost:5173 → you are redirected to `/login`.
2. Click **Create an account** and sign up with a real student email such as `240453182@mycput.ac.za`. Anything else is rejected.
3. You land on the code screen. **The code is in the backend terminal**, in a box like this:

   ```
   ======================= EMAIL (NOT SENT) =======================
   To      : 240453182@mycput.ac.za
   Subject : Your UniExchange verification code
   ----------------------------------------------------------------
   Your UniExchange verification code is:

       429183
   ```

4. Type or paste those 6 digits → you are signed in and land on the feed.

---

## Running in VS Code

The repo ships run/debug profiles, so once the prerequisites and your DB password
are set up you do **not** need two terminals.

1. Install the recommended extensions — VS Code prompts you when you open the
   repo, or run **"Extensions: Show Recommended Extensions"**. The important one
   is the **Extension Pack for Java**.
2. Open the **Run and Debug** panel (the play-with-bug icon, or `Cmd/Ctrl+Shift+D`).
3. Choose **"Full Stack: Backend + Frontend"** and press the green play button (or `F5`).

That starts the API on :8080 and the Vite dev server on :5173, then opens Chrome
attached to the debugger. Breakpoints work in Java and in the Vite process, and
the site opens in your normal browser. Stopping one stops both.

React `.tsx` breakpoints inside VS Code are deliberately **not** wired into the
F5 profile. Attaching VS Code's JavaScript debugger makes Chrome open a blank
page first and then navigate to the app, which leaves `about:blank` in the
browser history — so the first press of the back button after signing in lands
on a blank page. Use Chrome DevTools, which is unaffected, or run
`Frontend: Chrome (debugger)` by hand when you specifically want VS Code
breakpoints in React.

The individual profiles are also there if you only want one half:

| Profile | Purpose |
|---|---|
| `Full Stack: Backend + Frontend` | Both at once — the usual choice |
| `Full Stack + PayFast tunnel` | The same, plus a cloudflared tunnel so PayFast sandbox top-ups actually complete. Needs cloudflared — see [Payments (PayFast)](#payments-payfast) |
| `Backend: Spring Boot` | API only, with Java breakpoints |
| `Frontend: Vite dev server` | Dev server only; opens the site in your browser |
| `Frontend: Chrome (debugger)` | Optional. React breakpoints in VS Code, at the cost of the `about:blank` history entry. Start the dev server first |

### Tasks

**"Tasks: Run Task"** (`Cmd/Ctrl+Shift+P`) gives you:

| Task | What it does |
|---|---|
| `frontend: install` | `npm install` — run once after cloning |
| `frontend: dev server` / `build` / `lint` | The npm scripts |
| `backend: test` / `backend: build` | Maven, via the wrapper |
| `payfast: tunnel` | cloudflared quick tunnel to `localhost:8080`, started automatically by the PayFast F5 profile |
| **`full stack: verify`** | Backend tests → frontend lint → frontend build. **Run this before opening a PR.** |

> The Vite profile sets `NO_COLOR=1`. Vite otherwise embeds ANSI colour codes
> inside its printed URL, which breaks the auto-attach pattern match. The
> banner is monochrome; Chrome attaching reliably is worth it.

> `.vscode/launch.json`, `tasks.json`, `extensions.json` and `settings.json` are
> committed so the whole team gets them. `settings.json` deliberately contains no
> JDK path — the Java extension auto-detects, and an absolute path would be wrong
> on everyone else's machine.

## Configuration Reference

### Files you may need to edit

| File | When | Committed? |
|---|---|---|
| `Frontend/.env.local` | Backend on a non-default port | No (gitignored) |
| `Backend/src/main/resources/application-local.properties` | **Your DB password, SMTP credentials and PayFast tunnel settings** — everything personal or secret | No (gitignored) |
| `Backend/src/main/resources/application.properties` | Changing a setting **for the whole team** | **Yes — never put secrets here** |
| `Backend/src/test/resources/application.properties` | Test-only config (H2) | Yes |

### Backend properties worth knowing

In `Backend/src/main/resources/application.properties`:

| Property | Default | Notes |
|---|---|---|
| `spring.datasource.url` | `jdbc:mysql://localhost:3306/uniexchange` | `DB_NAME` overrides the database name |
| `spring.datasource.username` | `${DB_USERNAME:root}` | |
| `spring.datasource.password` | `${DB_PASSWORD:}` | Never hardcode |
| `spring.jpa.hibernate.ddl-auto` | `update` | Hibernate creates/updates tables on boot |
| `app.jwt.secret` | `${JWT_SECRET:change-me-...}` | **Must be ≥ 32 bytes** or startup fails |
| `app.jwt.ttl-seconds` | `3600` | One hour; there is no refresh endpoint |
| `app.jwt.remembered-ttl-seconds` | `2592000` | 30 days, for a "Remember me" sign-in. A JWT cannot be revoked, so shorten this if that trade is unacceptable |
| `app.trusted-device.remembered-days` | `30` | How long a remembered browser may skip the OTP. Slides forward on each use |
| `app.trusted-device.session-hours` | `12` | The same, when "Remember me" was **not** ticked. A hard cap |
| `app.auth.student-email-pattern` | `^\d{8,10}@mycput\.ac\.za$` | The signup gate |
| `app.otp.length` | `6` | |
| `app.otp.ttl-minutes` | `10` | |
| `app.otp.max-attempts` | `5` | After this the code is dead |
| `app.otp.resend-cooldown-seconds` | `60` | |
| `app.cors.allowed-origins` | `http://localhost:5173,http://localhost:3000` | Add your origin if you change the dev port |

Do not set `spring.jpa.properties.hibernate.dialect` — Hibernate 7 auto-detects it, and `MySQL8Dialect` no longer exists (only `org.hibernate.dialect.MySQLDialect`).

### Frontend environment

`Frontend/.env.local`, copied from `.env.example`:

| Variable | Default | Notes |
|---|---|---|
| `VITE_API_BASE_URL` | `http://localhost:8080` | Where the API lives |
| `VITE_STUDENT_EMAIL_PATTERN` | `^\d{8,10}@mycput\.ac\.za$` | Mirrors the backend rule, for instant form feedback |

## Email / OTP Delivery

By default **no email is sent**. With `spring.mail.host` unset, the app logs every message instead. This is deliberate: the whole signup flow can be run and tested with no SMTP credentials, so every team member can work on it and the test suite never mails anyone.

To send real email you need a Gmail address plus a **Google App Password** — a
16-character code that is *not* your Google login password.

**Getting the App Password**

1. Sign in to the personal Google account you want to send from. A
   `@mycput.ac.za` address will not work — it is a Microsoft mailbox, so Google
   cannot issue an App Password for it.
2. Turn on **2-Step Verification** at
   [myaccount.google.com/security](https://myaccount.google.com/security).
   App Passwords do not exist until 2FA is on; this is the step people get stuck on.
3. Go to [myaccount.google.com/apppasswords](https://myaccount.google.com/apppasswords),
   name it `UniExchange`, and click **Create**.
4. Copy the 16-character code (four blocks of four). It is shown only once.

If that page says App Passwords are unavailable, it is one of: 2FA still off, a
Workspace/school account whose admin disabled them, or Advanced Protection on.

**Where to put it — the untracked file, never `application.properties`**

Add these to `Backend/src/main/resources/application-local.properties`. That file
is gitignored and loads automatically, so nothing secret is ever committed:

```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=you@gmail.com
spring.mail.password=abcd efgh ijkl mnop
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
app.otp.from=you@gmail.com
```

Restart the backend. The `verification emails will be logged, not sent` warning
disappearing is how you know real SMTP is live. Comment out the `spring.mail.host`
line to go back to console logging. If the SMTP server is unreachable,
registration returns a clean `503` rather than failing silently.

Mail from Gmail into a university Microsoft tenant often lands in **Junk** — check there before assuming it failed.

> **Why an emailed code rather than "Sign in with Microsoft"?** Because the OTP is what actually proves the account is real: if `240453182@mycput.ac.za` isn't a genuine mailbox, the code never arrives, so the account can never be activated. Entra ID SSO would be a good addition later, but it needs an Azure app registration and CPUT's tenant admin can disable third-party consent at any time — so it can only ever be an extra door, not the only one. Note also that `mycput.ac.za` (students) and `cput.ac.za` (staff) are two separate Entra tenants.

## Payments (PayFast)

Students add money to their wallet through [PayFast](https://payfast.io). Out of
the box this runs against **PayFast's sandbox**: a real payment screen, but no real
money moves.

### How a top-up works

1. The student enters an amount on `/wallet` and clicks **Continue to PayFast**.
2. `POST /api/wallet/topup` records a `PENDING` row in `wallet_top_up` and returns
   a set of form fields, signed with an MD5 signature.
3. The browser posts those fields to `https://sandbox.payfast.co.za/eng/process`,
   and the student pays on PayFast's page.
4. PayFast's **servers** call our `notify_url` (`POST /api/payfast/itn`). This is
   the **ITN** (Instant Transaction Notification). It is the only thing that
   credits a wallet.
5. The backend checks the ITN: the signature, the source address, that the amount
   and merchant match, that the status is `COMPLETE`, and finally asks PayFast
   directly whether it really sent it. Only then does it mark the top-up
   `COMPLETED` and credit the wallet.
6. PayFast sends the student back to `/wallet?topup=done`, which polls until the
   new balance appears.

The student arriving back at `return_url` proves nothing, because anyone can
open that link. Money moves only on a validated ITN.

**Where the code lives** (`Backend/src/main/java/za/ac/cput/`)

| File | Job |
|---|---|
| `service/transactions/PayFastService.java` | Builds the signed form, validates the ITN, credits the wallet |
| `service/transactions/PayFastSignature.java` | The MD5 signature, PHP-compatible |
| `controller/transactions/MyWalletController.java` | `POST /api/wallet/topup` |
| `controller/transactions/PayFastController.java` | `POST /api/payfast/itn`, open to everyone (`permitAll`) because PayFast sends no JWT |
| `controller/transactions/PayFastSimulatorController.java` | Dev-only stand-in for PayFast, off by default |
| `Frontend/src/components/wallet/TopUpForm.tsx` | Builds and submits the form that goes to PayFast |

### Running the sandbox on your machine

The catch: in step 4, PayFast has to reach your backend **from the internet**, and
`localhost` isn't reachable from there. A free Cloudflare quick tunnel gives your
`localhost:8080` a public HTTPS address. No Cloudflare account is needed.

**1. Sandbox credentials.** The team's sandbox merchant is already in
`application.properties` (`10054859` / `dku03dr7i156u`, no passphrase), so you
don't need your own. If you want one, register at
[sandbox.payfast.co.za](https://sandbox.payfast.co.za) and put your merchant ID
and key in your `application-local.properties`:

```properties
app.payfast.merchant-id=<your sandbox merchant id>
app.payfast.merchant-key=<your sandbox merchant key>
# Only if you set a passphrase on the sandbox dashboard. It must match exactly.
app.payfast.passphrase=<your passphrase>
```

> PayFast's old shared demo merchant (`10000100` / `46f0cd694581a`), which older
> tutorials still quote, **no longer works**. Every payment fails with
> *"Generated signature does not match submitted signature"*.

**2. Install cloudflared** (once):

```bash
brew install cloudflared                       # macOS
winget install --id Cloudflare.cloudflared     # Windows
```

**3. Tell the backend about the tunnel** (once). Add these two lines to
`Backend/src/main/resources/application-local.properties`:

```properties
app.payfast.tunnel-discovery-url=http://127.0.0.1:20241/quicktunnel
app.payfast.validate-source-ip=false
```

- The first line lets the backend find the tunnel's public URL by itself (see
  the next section).
- The second is needed because, through a tunnel, the ITN arrives from
  cloudflared rather than from PayFast's own address, so the source check would
  always fail. The signature check and the confirmation call to PayFast still run.
- **Never** put either line in `application.properties`.

**4. Start everything.** In VS Code, open Run and Debug, choose **"Full Stack +
PayFast tunnel"**, and press F5. This starts the tunnel (the `payfast: tunnel`
task), then the backend and frontend. F5 remembers the profile you picked.

**5. Make a test top-up.**
1. Open `/wallet` and click **Continue to PayFast**.
2. You land on PayFast's sandbox checkout page. Finish the payment there; it's a
   test environment, so nothing is charged.
3. PayFast sends you back to your wallet.

**6. Check it worked.**
- The balance on `/wallet` updates within a few seconds.
- The backend Debug Console shows `PayFast ITN received …` followed by
  `Credited 100.00 to user …`.
- In MySQL, the top-up is `COMPLETED`:
  ```sql
  SELECT top_up_id, amount, status, created_at, completed_at
  FROM wallet_top_up ORDER BY top_up_id DESC LIMIT 5;
  ```
- The sandbox dashboard's **"All received ITNs"** list shows the delivery. There,
  "Success" only means our endpoint replied `200`, and it always does, even when it
  rejects an ITN. The database and the log are the real proof.

### Does the tunnel URL change?

**Yes, every time cloudflared starts** you get a new random
`https://<words>.trycloudflare.com`. You never copy it anywhere, though.
cloudflared reports its current address on a local status server
(`http://127.0.0.1:20241/quicktunnel`), and when `app.payfast.tunnel-discovery-url`
is set, `PayFastService` asks it on **every** top-up. A new URL is picked up on the
next top-up, with no config edit and no backend restart.

Things to know:

- A top-up started **before** the tunnel restarted is signed with the old URL, so
  its ITN never arrives and it stays `PENDING`. Start a new one.
- The tunnel keeps running after you stop debugging. The next F5 **reuses** it
  instead of starting a second one, so the URL stays the same between sessions.
  Use **"Tasks: Terminate Task" → payfast: tunnel** to stop it.
- Only one cloudflared can use port `20241`. A tunnel you started by hand with
  the same `--metrics 127.0.0.1:20241` flag is reused too. One started without
  that flag is not, so stop it.
- If the backend can't reach cloudflared, **it refuses to start the top-up**, and
  the wallet page shows *"The PayFast tunnel isn't running …"*. Without the tunnel
  PayFast could never confirm the payment, so you'd pay and the wallet would never
  move.
- Quick tunnels are for testing only. They have no uptime guarantee.

### No tunnel? Use the simulator

Set `app.payfast.simulator.enabled=true` in `application-local.properties` and
restart. The wallet page then completes top-ups itself, without contacting
PayFast, through `POST /api/dev/payfast/complete/{m_payment_id}`. That runs the
same crediting code a real ITN does.

It's for offline demos. It only lets a student complete **their own** pending
top-up, and `@Profile("!prod")` stops it from ever loading in production.

### Going live (real money)

**No code changes are needed.** Going live is settings only. Set these as
**Application settings** in Azure App Service (environment variables), never in a
committed file:

| Property | Sandbox (now) | Live | Azure env var |
|---|---|---|---|
| `app.payfast.sandbox` | `true` | `false` (switches to `www.payfast.co.za`) | `APP_PAYFAST_SANDBOX` |
| `app.payfast.merchant-id` | `10054859` | From your **live** PayFast dashboard | `APP_PAYFAST_MERCHANTID` |
| `app.payfast.merchant-key` | `dku03dr7i156u` | From your live dashboard | `APP_PAYFAST_MERCHANTKEY` |
| `app.payfast.passphrase` | empty | **Set one** on the live dashboard and copy it exactly | `APP_PAYFAST_PASSPHRASE` |
| `app.payfast.notify-url` | `localhost` | `https://<backend>.azurewebsites.net/api/payfast/itn` | `APP_PAYFAST_NOTIFYURL` |
| `app.payfast.return-url` | `localhost:5173/...` | `https://<frontend>/wallet?topup=done` | `APP_PAYFAST_RETURNURL` |
| `app.payfast.cancel-url` | `localhost:5173/...` | `https://<frontend>/wallet?topup=cancelled` | `APP_PAYFAST_CANCELURL` |
| `app.payfast.validate-source-ip` | `false` (tunnel) | **`true`** | `APP_PAYFAST_VALIDATESOURCEIP` |
| `app.payfast.tunnel-discovery-url` | set (tunnel) | **unset** | leave out |
| `app.payfast.simulator.enabled` | `false` | **`false`** | leave out |

The env var names follow Spring's rule: dots become `_`, dashes are dropped, and
everything is uppercase. Also add the live frontend's address to
`app.cors.allowed-origins`.

**Steps**

1. Open and activate a **live** PayFast merchant account. Sandbox credentials
   don't work on the live site, and live ones don't work on sandbox.
2. Set a passphrase on the live dashboard. It's optional for once-off payments,
   but it stops anyone who has seen a form from forging one.
3. Deploy with the table's **sandbox** values, except `notify-url`, which points
   at the deployed backend. Do a sandbox top-up against the real server. This
   proves PayFast can reach your ITN endpoint over HTTPS without a tunnel.
4. Switch to the live values and do one small real top-up, say R5. Check the log
   and `wallet_top_up` exactly as in step 6 above.
5. Keep an eye on `PayFast ITN …` warnings in the logs for the first few days.

> Behind Azure's proxy, `server.forward-headers-strategy=framework` means the
> source-IP check reads `X-Forwarded-For`. That's defence in depth only. The
> checks that actually authenticate an ITN are the signature and the
> confirmation call back to PayFast, so never remove either.

### Troubleshooting PayFast

| Symptom | Cause and fix |
|---|---|
| PayFast shows *"Generated signature does not match submitted signature"* | Wrong merchant ID/key or passphrase, most often the retired `10000100` demo merchant, or a passphrase set on the dashboard but not in `app.payfast.passphrase` (or the other way round). |
| "Continue to PayFast" spins and nothing happens | The request to PayFast was never sent. Check the browser console. `TopUpForm` builds the form outside React for exactly this reason; don't move it back into JSX. |
| Paid, but the balance stays R0 and the top-up stays `PENDING` | The ITN didn't arrive or was rejected. No `PayFast ITN received` in the log means PayFast couldn't reach you: is the tunnel running, and was the top-up started after it? A `PayFast ITN …` warning names the check that failed. |
| Wallet page says *"The PayFast tunnel isn't running …"* | cloudflared isn't running, or isn't on port 20241. Start **"Full Stack + PayFast tunnel"**, or run `cloudflared tunnel --url http://localhost:8080 --metrics 127.0.0.1:20241` in a terminal. Check the **payfast: tunnel** tab in the Terminal panel for its output. |
| `404` on a new wallet endpoint | The backend is still the old build. Restart it. |

**Things in this code that are easy to break**

- **Signatures follow PHP's `urlencode`**, not Java's `URLEncoder`. They differ on
  `*`, so always use `PayFastSignature.phpUrlEncode`.
- **Empty fields:** the outgoing form **skips** them, but the ITN check
  **includes** them. PayFast's ITN is full of empty fields (`custom_str1=`, …).
  Mixing the two rules rejects every real payment without an error.
- **`@Transactional` on a method called through `this` does nothing.**
  `handleNotification` therefore opens its transaction with a
  `TransactionTemplate`. Without it, crediting fails with "no transaction".
- **Field order is part of the signature.** Keep the fields in a `LinkedHashMap`
  and don't reorder `beginTopUp`.

## Backend

The main class is `za.ac.cput.UniExchangeApplication`. It sits at the base package root, so component scan, entity scan and Spring Data repository scan all root at `za.ac.cput` — no `scanBasePackages`, `@EntityScan` or `@EnableJpaRepositories` needed.

### Package layout

| Package | Contents |
|---|---|
| `domain/<subdomain>` | 22 JPA entities + 13 enums |
| `repository/<subdomain>` | Spring Data JPA repositories |
| `factory/<subdomain>` | The **only** construction path for entities, validated via `Helper` |
| `service` + `service/<subdomain>` | `IService<T, ID>` plus an interface + impl per entity |
| `controller/<subdomain>` | REST controllers, `AuthController`, `GlobalExceptionHandler` |
| `dto/<subdomain>`, `dto/auth` | Request/response records |
| `security` | `JwtService`, `JwtAuthenticationFilter`, `UniExchangeUserDetailsService` |
| `config` | `SecurityConfig`, `MailConfig` |
| `mail` | `EmailSender` plus SMTP and logging implementations |
| `validation` | The `@StudentEmail` constraint |
| `util` | `Helper` — all shared validation |

Sub-domains are consistent across every layer: `identity`, `marketplace`, `transactions`, `trust`, `communication`, `community`, `admin`.

Entities expose only a nested fluent `Builder` (with `copy()` and `build()`), a protected no-arg constructor for JPA, and getters — **no public setters**. Always construct through the matching factory.

### Domain model

| Package | Entities |
|---|---|
| `identity` | Campus, Role, User, UserRole, Verification |
| `marketplace` | Category, Listing, ListingImage |
| `communication` | Conversation, ConversationParticipant, Message, **ChatMedia**, Notification |
| `trust` | Review, Report, VendorApplication, TrustedSellerBadge |
| `transactions` | Transaction, Payment, Wallet, WalletTransaction, **WalletTopUp** |
| `community` | BulletinPost |
| `admin` | AuditLog |
| `enums` | 14 enums matching the MySQL `ENUM` columns |

Foreign keys are modelled as plain scalar `long` columns (`Listing.sellerId`, not `Listing.seller`) — there are no JPA relationship annotations anywhere.

### Authentication endpoints

Only verified CPUT students can obtain a token.

| Endpoint | Purpose |
|---|---|
| `POST /api/auth/register` | Creates a `PENDING_VERIFICATION` account and emails a 6-digit code. **Returns 202 with no token.** |
| `POST /api/auth/verify-otp` | Exchanges a valid code for a JWT and sets the account `ACTIVE`. The only endpoint that issues a token, and the only one that trusts a device. |
| `POST /api/auth/resend-otp` | New code, subject to the cooldown. Always 202 for an unknown address, so registered student numbers cannot be enumerated. |
| `POST /api/auth/login` | Email + password, plus an optional `deviceToken` and `rememberMe`. **Two outcomes** — see below. Unverified accounts still get `403` with `code: "EMAIL_NOT_VERIFIED"`. |
| `GET /api/auth/me` | The authenticated user (`Authorization: Bearer <token>`). |

Codes are stored only as a BCrypt hash, expire in 10 minutes, and are capped at 5 attempts.

### "Remember me" — the emailed code is a second factor, not just an activation step

The password alone is never enough. Once it checks out, `/login` either recognises the
browser or emails a code:

| Outcome | Status | Body |
|---|---|---|
| trusted device | `200` | `AuthResponse` with a token |
| anything else | `202` | no token — a code was sent, finish at `/verify-otp` |

A browser earns that trust only by completing an OTP, and `rememberMe` decides how long
it keeps it:

- **Ticked** — 30 days, sliding forward on each use, and the session itself lasts 30 days.
  The client keeps both in `localStorage`, so a restart changes nothing.
- **Not ticked** — 12 hours at most, and the client keeps both in `sessionStorage`, so
  closing the browser loses them and the next sign-in needs a fresh code.

The device token is an opaque 256-bit value stored only as a SHA-256 hash (not BCrypt —
a salted hash cannot be looked up, and there is nothing to brute-force in 256 random
bits). It is bound to one account, so presenting another student's token does not skip
your code. **It never replaces the password** — it only ever skips the second factor.

Changing your mind is handled: ticking the box on a browser that was trusted for the
session only re-issues the token into `localStorage` and retires the old one, so the two
halves cannot drift apart.

> Because a wrong password is rejected before any of this, `/login` can never be used to
> send someone an unwanted email.

Every other entity has standard CRUD at `/api/<plural-name>` — e.g. `GET /api/listings`, `POST /api/campuses`. `GET` on listings, listing images, categories, campuses, bulletin posts, seller ratings and badges is public; everything else needs a token.

**`ADMIN` only:** audit logs, reports, and the generic CRUD for money, chat and
trust — `/api/wallets`, `/api/wallet-transactions`, `/api/payments`,
`/api/transactions`, `/api/conversations`, `/api/conversation-participants`,
`/api/messages`, `/api/trusted-seller-badges`. Those controllers take ids
straight from the request body with no ownership check, so while they were merely
"authenticated" any signed-in student could credit their own wallet, read
anyone's private messages or grant themselves a Trusted Seller badge. Real use
goes through the authorization-aware flow controllers instead — `/api/chat`,
`/api/wallet`, `/api/purchases` and `POST /api/reviews`. **Keep new endpoints out
of those ADMIN-only prefixes.**

### Messaging, wallet and trust

The three features that turn the marketplace into somewhere two students can
actually complete a deal. All of them resolve the acting student from the JWT -
**an id in the URL or body is never enough on its own**.

#### Chat — `/api/chat`

| Endpoint | Purpose |
|---|---|
| `GET /api/chat/threads` | The inbox: other participant, listing, last message and unread count, assembled server-side |
| `POST /api/chat/threads` | `{otherUserId, listingId?}` → find-or-create. Safe to call twice |
| `GET /api/chat/threads/{id}/messages?afterMessageId=` | The poll. Returns `[]` when nothing is new |
| `POST /api/chat/threads/{id}/messages` | `{content?, mediaId?}` — at least one required |
| `POST /api/chat/threads/{id}/media` | multipart upload → `{mediaId}`, sent with the next message |
| `POST /api/chat/threads/{id}/read` · `GET /api/chat/unread-count` | Read receipts and the nav badge |

**Delivery is polling, not WebSockets.** The app is deployed to Azure App Service,
where the cheaper tiers unload an idle app after ~20 minutes and drop every
persistent connection, and where scaling out would break an in-memory STOMP
broker. The `afterMessageId` cursor makes each poll cheap and is the same shape a
push implementation would use later.

**Attachments are private.** They are stored outside the public `/uploads/**`
tree and served by `GET /api/chat/media/{id}?u=&exp=&sig=`, which verifies an
HMAC bound to `(mediaId, viewerId, expiry)` *and* re-checks conversation
membership on every request. That endpoint is `permitAll` in `SecurityConfig` out
of necessity, not laxity: `<img>`, `<audio>` and `<video>` cannot send an
`Authorization` header, so a filter-chain rule would 401 every attachment on the
page.

Voice notes are recorded in the browser with `MediaRecorder`. Two things about
that are worth knowing before touching it: Safari records **MP4/AAC** and cannot
produce WebM, so the format is negotiated and read back from the recorder; and
the resulting container has **no duration**, so `audio.duration` is `Infinity`
and the UI draws its own progress bar from a `durationMs` measured while
recording.

#### Wallet and escrow — `/api/wallet`, `/api/purchases`

Buying debits the buyer immediately and holds the funds; the seller is paid only
when the buyer confirms receipt. **There is no `held_balance` column** — money in
escrow is simply a `Transaction` in `PENDING`, so the reconciliation identity is:

    sum(wallet.balance) + sum(PENDING transaction amounts) == sum(completed top-ups)

Because of that, `available` is already net of anything held. Show
"Available · In escrow · Total"; never subtract one from the other.

| Endpoint | Purpose |
|---|---|
| `GET /api/wallet` · `GET /api/wallet/ledger` | Balance (+ escrow) and the full audit trail |
| `POST /api/wallet/topup` | Returns the signed PayFast form fields to POST |
| `POST /api/wallet/transfer` | Send money straight to another student, by student email |
| `POST /api/payfast/itn` | PayFast's callback. **`permitAll`** — it carries no JWT |
| `POST /api/purchases` | Buy a listing; money is held |
| `POST /api/purchases/{id}/confirm` | Buyer confirms receipt — this is what pays the seller |
| `POST /api/purchases/{id}/cancel` | Either party cancels; the buyer is refunded in full |

Unconfirmed escrows are auto-released after `app.escrow.auto-release-days` so a
forgetful buyer cannot freeze a seller's money forever.

Two things in the money path are load-bearing and easy to undo by accident:

- **Every balance change goes through `WalletServiceImpl.applyMovement`**, which
  locks the row and then calls `entityManager.refresh(..., PESSIMISTIC_WRITE)`.
  The refresh is not belt-and-braces: Hibernate will not re-read an entity
  already in the persistence context, so without it you hold the lock and still
  compute from a stale balance. `WalletMoneySafetyTest` fails with 10 successful
  R80 debits against a R100 balance if you remove it.
- **Status changes are compare-and-set**, never read-then-write. The row count is
  the authority. This is what stops two "I received it" clicks paying a seller
  twice, and two buyers claiming the same listing.

Top-ups go through PayFast. Sandbox setup, the local tunnel, going live and
troubleshooting are all in [Payments (PayFast)](#payments-payfast).

**Sending money to another student** (`POST /api/wallet/transfer`,
`{ recipientEmail, amount }`) is handled by `TransferServiceImpl`:

- The sender is always the token holder, never a value from the request body.
- The debit and the credit happen in one transaction, in ascending-userId lock
  order, so two students sending to each other at once can't deadlock.
- If the sender can't cover it, nothing moves.
- Rejections come back as `409` with a `code`: `INSUFFICIENT_FUNDS`,
  `RECIPIENT_NOT_FOUND`, `RECIPIENT_UNAVAILABLE` or `CANNOT_SEND_TO_SELF`.
- Both people's Activity lists get a row: `Sent to …` / `Received from …`, with
  `reference_type = TRANSFER`.

#### Reviews and the Trusted Seller badge

A review may only be written by someone who completed a transaction with the
person being rated, and the request body carries **no reviewee** — it is derived
from the transaction, so there is nothing to forge.

The badge is earned at **5 completed sales to 5 different buyers**, each rated at
least 4, while the overall average stays at or above the floor (all three are
properties under `app.trust.badge.*`). Every clause closes a way of farming it:
distinct buyers stops one friend buying five times, the average floor stops five
good reviews outweighing twenty bad ones, and a unique constraint on
`(transaction_id, reviewer_id)` stops one buyer stacking five reviews on one
sale. It is revoked automatically if the average later falls through the floor.

> `ddl-auto=update` **silently skips** creating that unique constraint if the
> table already contains duplicates. Check with
> `SELECT transaction_id, reviewer_id, COUNT(*) FROM review GROUP BY 1,2 HAVING COUNT(*) > 1;`

### Error format

```json
{
  "timestamp": "2026-09-04T21:08:18.648352",
  "status": 400,
  "error": "Bad Request",
  "message": "Request validation failed",
  "fields": { "email": "Use your CPUT student email, for example 240453182@mycput.ac.za" }
}
```

`fields` appears on bean-validation failures; `code` appears on `EMAIL_NOT_VERIFIED`.

## Frontend

See [`Frontend/README.md`](Frontend/README.md) for the full frontend guide.

```
Frontend/src/
├── main.tsx              BrowserRouter + AuthProvider
├── App.tsx               every route, grouped by owner
├── index.css             Tailwind import + design tokens (@theme)
├── lib/
│   ├── session.ts        localStorage session (token + expiry)
│   ├── schemas.ts        zod schemas
│   └── api/              client.ts + types.ts, then one module per feature
│                         (auth, listings, users, messages, notifications, bulletin)
├── auth/                 authContext, AuthProvider, useAuth, ProtectedRoute
├── components/
│   ├── ui/               Button, TextField, Textarea, Select, Card, Badge,
│   │                     Avatar, Spinner, EmptyState, Alert, OtpInput
│   ├── layout/           AppLayout, TopBar, BottomNav, PageHeader, AuthLayout, Logo
│   └── feed/ listings/ profile/ messages/ notifications/ bulletin/
└── pages/                one file per route
```

Routes: `/` redirects by auth state · `/login` `/signup` `/verify` (public) ·
`/feed` `/listings/new` `/listings/:listingId` `/profile` `/profile/:userId`
`/notifications` `/messages` `/messages/:conversationId` `/bulletin` (protected) ·
anything else shows a 404 page.

Every signed-in route renders inside a shared `AppLayout` (top bar + mobile tab
bar), so no page writes its own header. Each page is owned by one team member and
each feature has its own API module — see
[`Frontend/README.md`](Frontend/README.md) for the ownership table.

The JWT and the trusted-device token are kept together, in `localStorage` when "Remember me" is ticked and in `sessionStorage` when it is not — which is exactly what makes an unticked sign-in end when the browser closes. There is no refresh endpoint, so an expired token is treated as signed out on load.

```bash
npm run dev       # dev server on :5173
npm run build     # type-check + production bundle into dist/
npm run lint
npx tsc -b        # type-check only
```

## Testing

### Backend — 140 tests, no MySQL or SMTP required

```bash
cd Backend
./mvnw test
```

Tests run against **H2 in-memory** using `src/test/resources/application.properties`, so they need neither a database server nor mail credentials.

| Test | Covers |
|---|---|
| `UniExchangeApplicationTests` | Spring context loads — proves the whole bean graph wires |
| `FactoryTest` | Builds all 22 entities through their factories; asserts each rejects invalid input |
| `HelperTest` | Validation utilities, including the student-email rule |
| `OtpServiceTest` | OTP expiry, the attempt cap, single use, and that codes are hashed |
| `DeviceTrustServiceTest` | "Remember me": tokens are hashed and never logged, are bound to one account, honour expiry and revocation, and only remembered devices get a sliding window |

### Frontend

```bash
cd Frontend
npm run build && npm run lint
```

## Troubleshooting

| Symptom | Cause and fix |
|---|---|
| `invalid target release: 25` | `JAVA_HOME` points at a JDK older than 25 — see [Prerequisites](#1-a-jdk-version-25-or-newer) |
| `zsh: permission denied: ./mvnw` | `chmod +x Backend/mvnw` |
| `Communications link failure` or `Cannot load driver class` at startup | MySQL isn't running, or the URL/credentials are wrong. Confirm the server with `pgrep -fl mysqld` |
| `command not found: mysql` | The client isn't on `PATH` — `export PATH="/usr/local/mysql/bin:$PATH"`. This says nothing about whether the server is running |
| `Access denied for user 'root'@'localhost'` | `DB_PASSWORD` is unset or wrong — see [step 3](#3-configure-your-database-password) |
| `WeakKeyException` at startup | `app.jwt.secret` is under 32 bytes; set a longer `JWT_SECRET` |
| No verification email arrives | Expected by default — the code is printed in the backend terminal. For real mail see [Email / OTP Delivery](#email--otp-delivery), and check Junk |
| Signing in asks for a code every time | Working as designed on a browser that has not been trusted. Tick **Remember me**, or note that without it the trust is dropped when the browser closes. The code is in the backend terminal |
| Login returns 403 `EMAIL_NOT_VERIFIED` | That account never completed the OTP step. The frontend redirects to `/verify` automatically |
| Frontend shows "Cannot reach the UniExchange server" | The backend isn't running, or `VITE_API_BASE_URL` is wrong |
| CORS error in the browser console | Your origin isn't in `app.cors.allowed-origins`. Only the `Authorization` and `Content-Type` request headers are allowed — adding any custom header breaks the preflight |
| `Cannot access central ... in offline mode` | You used `-o`. Run once with internet to cache dependencies |
| Port 8080 or 5173 already in use | `./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=8081`, or `npm run dev -- --port 5174` (then add that origin to `app.cors.allowed-origins`) |

## Database

The schema is the **`uniexchange`** database (MySQL 8+, `utf8mb4`): 22 tables across identity, marketplace, communication, trust, transactions, community and admin, with foreign keys, indexes and CHECK constraints for business rules (rating 1–5, non-negative money, buyer ≠ seller). Hibernate generates the tables from the entities on startup via `ddl-auto=update`.

The schema was designed against MySQL 8, but 9.x works too — Hibernate 7
auto-detects the dialect from the live connection, which is why
`spring.jpa.properties.hibernate.dialect` must be left unset.

> Entity timestamp columns are populated by the factories rather than by MySQL `DEFAULT CURRENT_TIMESTAMP`, which keeps persistence portable between MySQL and the H2 used in tests.

## Git Workflow

- `main` is the integration branch — **never commit to it directly**.
- Each team member works on their own branch, named `initials-studentNumber` (e.g. `JRA-230317693`).
- Completed work is merged into `main` via a **pull request** reviewed by a teammate.
- Before opening a PR, pull the latest `main` and merge it into your branch to avoid conflicts.
- Never commit secrets. `.env*` files and `application-local.properties` are gitignored — keep it that way.

## Team

| Name                      | Student Number | Branch            |
|---------------------------|----------------|-------------------|
| Aidan Barends             | 230255639      | `AB-230255639`    |
| Joshua Reid Adams         | 230317693      | `JRA-230317693`   |
| Raul Ja'aim Everts        | 230270565      | `RJE-230270565`   |
| Mogamat Yaseen Kannemeyer | 240453182      | `MYK-240453182`   |
| Mogamat Wazeer Gilbert    | 221374698      | `MWG-221374698`   |

## Roadmap

1. **Done** — full backend layering (`domain → repository → factory → service → controller`), Spring Security + JWT, and verified-student auth with email OTP.
2. **Done** — frontend signup, OTP verification and login (React 19 + TypeScript + Tailwind v4 + react-router 7), plus the shared app shell and a routed, owner-assigned stub for every remaining page.
3. **In progress** — the team building out feed, product details, create listing, profile, notifications, messaging and the bulletin board.
4. **Done** — messaging (with photo, video and voice-note attachments), the wallet with PayFast-funded top-ups and escrow purchases, reviews, and the Trusted Seller badge. See "Messaging, wallet and trust" below.
5. **Later** — Swagger/OpenAPI docs (springdoc 3.x, already a commented-out placeholder in `pom.xml`); authenticator-app TOTP as a login second factor; optional "Sign in with Microsoft" via Entra ID.
