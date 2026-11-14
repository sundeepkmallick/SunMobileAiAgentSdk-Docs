# Developer examples

Copyable Kotlin examples for the published SunMobile AI Agent SDK release candidate.

- [Core tool and policy example](CoreToolExample.kt) — implement a host-owned tool and fail-closed policy.
- [Firebase AI Logic + Google ADK](FirebaseAdkExample.kt) — use the shared ADK runner with Firebase.
- [LiteRT-LM + Google ADK](LiteRtLmExample.kt) — run a host-provisioned on-device model.
- [Gemini Nano + ML Kit](GeminiNanoExample.kt) — check availability and create the AppFunctions-backed agent.

The longer [Developer examples guide](../docs/examples.md) explains dependency setup, lifecycle, streaming, sessions, host responsibilities and security boundaries.

These examples target `0.1.0-rc.2`. They are API examples, not a standalone Gradle project. Host-owned types such as repositories, Firebase initialization, AppFunctions declarations, model provisioning and UI must be supplied by the consuming app. Test against the exact artifacts and target devices you ship.
