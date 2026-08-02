# GitHub Login — Taiga Docker Setup

This guide covers everything needed to enable GitHub OAuth login on a self-hosted Taiga Docker
instance, including support for the TaigaMobileNova Android app.

> **Why this isn't documented elsewhere**
> The official Taiga Docker setup has two undocumented quirks that silently break GitHub auth
> when public registration is disabled. Both are worked around below.

---

## Prerequisites

- A running Taiga Docker installation (the official [taiga-docker](https://github.com/taigaio/taiga-docker) stack)
- A domain with HTTPS (GitHub OAuth requires it for the web callback)
- Admin access to your GitHub account

---

## 1. Create a GitHub OAuth App

Go to **GitHub → Settings → Developer settings → OAuth Apps → New OAuth App**.

Fill in:

| Field | Value |
|---|---|
| Application name | Taiga (or anything recognisable) |
| Homepage URL | `https://your-taiga-domain.com` |
| Authorization callback URL | `https://your-taiga-domain.com` |

After saving, note your **Client ID** and generate a **Client Secret**.

### Add the mobile app callback URL

In the OAuth app's settings, click **Add another** next to the callback URL and add:

```
http://127.0.0.1
```

GitHub ignores the port number for loopback addresses, so this single entry covers all
ports the TaigaMobileNova app may use. No second OAuth app is needed.

---

## 2. Add credentials to `.env`

```dotenv
# GitHub Auth
ENABLE_GITHUB_AUTH=True
GITHUB_API_CLIENT_ID=your-client-id
GITHUB_API_CLIENT_SECRET=your-client-secret
```

---

## 3. Fix the frontend — mount a custom `conf.json`

**The bug:** the `taiga-front` Docker image ships with a pre-baked `conf.json`. Its entrypoint
only renders from the template when the file is absent — which it never is — so env vars are
never applied. The fix is to mount your own file.

Create `conf.json` next to your `docker-compose.yml`:

```json
{
    "api": "https://your-taiga-domain.com/api/v1/",
    "eventsUrl": "wss://your-taiga-domain.com/events",
    "baseHref": "/",
    "eventsMaxMissedHeartbeats": 5,
    "eventsHeartbeatIntervalTime": 60000,
    "eventsReconnectTryInterval": 10000,
    "debug": false,
    "debugInfo": false,
    "defaultLanguage": "en",
    "themes": ["taiga"],
    "defaultTheme": "taiga",
    "defaultLoginEnabled": true,
    "publicRegisterEnabled": false,
    "feedbackEnabled": true,
    "supportUrl": "https://community.taiga.io/",
    "privacyPolicyUrl": null,
    "termsOfServiceUrl": null,
    "maxUploadFileSize": null,
    "contribPlugins": ["plugins/github-auth/github-auth.json"],
    "gitHubClientId": "your-client-id",
    "gitLabClientId": "",
    "gitLabUrl": "",
    "tagManager": { "accountId": null },
    "tribeHost": null,
    "enableAsanaImporter": false,
    "enableGithubImporter": false,
    "enableJiraImporter": false,
    "enableTrelloImporter": false,
    "gravatar": false,
    "rtlLanguages": ["ar", "fa", "he"]
}
```

> `contribPlugins` includes the GitHub auth plugin even though `publicRegisterEnabled` is
> `false`. This allows **existing users** to log in via GitHub without opening public
> registration. Only the Client ID belongs here — never the secret.

In `docker-compose.yml`, under the `taiga-front` service, add the volume mount:

```yaml
taiga-front:
  volumes:
    - ./conf.json:/usr/share/nginx/html/conf.json
```

---

## 4. Fix the backend — mount a custom `config.py`

**The bug:** the `taiga-back` image has `taiga-contrib-github-auth` pre-installed, but
its `config.py` only activates it when `PUBLIC_REGISTER_ENABLED` is also `True`. To
decouple them, copy and patch the file.

Extract the original:

```bash
docker cp taiga-docker-taiga-back-1:/taiga-back/settings/config.py ./config.py
```

Find this block and remove the `PUBLIC_REGISTER_ENABLED` condition:

```python
# BEFORE
ENABLE_GITHUB_AUTH = os.getenv('ENABLE_GITHUB_AUTH', 'False') == 'True'
if PUBLIC_REGISTER_ENABLED and ENABLE_GITHUB_AUTH:
    INSTALLED_APPS += ["taiga_contrib_github_auth"]
    GITHUB_API_CLIENT_ID = os.getenv('GITHUB_API_CLIENT_ID')
    GITHUB_API_CLIENT_SECRET = os.getenv('GITHUB_API_CLIENT_SECRET')

# AFTER
ENABLE_GITHUB_AUTH = os.getenv('ENABLE_GITHUB_AUTH', 'False') == 'True'
if ENABLE_GITHUB_AUTH:
    INSTALLED_APPS += ["taiga_contrib_github_auth"]
    GITHUB_API_CLIENT_ID = os.getenv('GITHUB_API_CLIENT_ID')
    GITHUB_API_CLIENT_SECRET = os.getenv('GITHUB_API_CLIENT_SECRET')
```

In `docker-compose.yml`, under the shared backend volumes (used by both `taiga-back` and
`taiga-async`), add:

```yaml
- ./config.py:/taiga-back/settings/config.py
```

---

## 5. Wire up the backend env vars

In `docker-compose.yml`, in the `x-environment` block shared by `taiga-back` and
`taiga-async`:

```yaml
ENABLE_GITHUB_AUTH: "${ENABLE_GITHUB_AUTH}"
GITHUB_API_CLIENT_ID: "${GITHUB_API_CLIENT_ID}"
GITHUB_API_CLIENT_SECRET: "${GITHUB_API_CLIENT_SECRET}"
```

---

## 6. Restart

```bash
docker compose down && docker compose up -d
```

---

## Summary of what was changed and why

| Component | Problem | Fix |
|---|---|---|
| `taiga-front` | Entrypoint skips template rendering if `conf.json` already exists (it always does) | Mount a custom `conf.json` with `gitHubClientId` and `contribPlugins` set |
| `taiga-back` | Activates GitHub auth only when `PUBLIC_REGISTER_ENABLED=True` | Mount a patched `config.py` that removes that condition |
| GitHub OAuth App | Only has the web callback URL by default | Add `http://127.0.0.1` as a second callback URL for mobile app support |
