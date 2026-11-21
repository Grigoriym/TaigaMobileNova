# Security Policy

## Supported versions

Only the latest released version of TaigaMobileNova (Google Play, F-Droid, or
[GitHub Releases](https://github.com/Grigoriym/TaigaMobileNova/releases)) receives security
fixes. Please reproduce against the latest version before reporting.

## Reporting a vulnerability

**Do not open a public issue for security problems.**

Report privately through GitHub:
[Security → Report a vulnerability](https://github.com/Grigoriym/TaigaMobileNova/security/advisories/new).

Please include:

- the affected platform (Android, iOS, Desktop) and app version,
- a description of the issue and its impact,
- steps to reproduce or a minimal proof of concept.

All communication happens in the GitHub advisory. Reports will be acknowledged on a
best-effort basis; this is a personal project maintained in spare time. There is no bug
bounty.

## Scope

This policy covers the code in this repository. Some behaviour is a deliberate, bounded
tradeoff rather than a vulnerability — for example trust-on-first-use acceptance of
self-signed certificates and cleartext HTTP for self-hosted servers; these are recorded in
[docs/security/masvs.md](docs/security/masvs.md).

Issues in the Taiga server itself should be reported to the
[Taiga project](https://github.com/taigaio). Issues whose root cause is in a shared
`grappim-kit` module should be reported in
[grappim-kit](https://github.com/Grigoriym/grappim-kit/security/advisories/new).
