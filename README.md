# SunMobile AI Agent SDK

A modular Kotlin/Android SDK for building AI agents with backend-neutral core contracts and optional integrations for Google ADK, Firebase AI Logic, LiteRT-LM and ML Kit GenAI.

**Current release:** `0.1.0-rc.2` (release candidate; not a stable 1.0 or 0.1.0 release).  
**Maven group:** `io.github.sundeepkmallick`  
**License:** Apache-2.0

## What it provides

- A Kotlin/JVM core with agent contracts, capabilities, request/result/event types, session helpers and tool registry.
- Host-controlled tool execution with JSON-schema-subset validation and an explicit policy boundary.
- Optional adapters for Google ADK, Firebase AI Logic, LiteRT-LM and ML Kit GenAI Agent/Prompt.
- An offline Android sample in the SDK source project, using clearly labelled mock data.

## Quick start

Add Maven Central and the modules your app needs:

```kotlin
repositories {
    google()
    mavenCentral()
}

dependencies {
    implementation("io.github.sundeepkmallick:agent-core:0.1.0-rc.2")
    // Add only the integrations you use:
    implementation("io.github.sundeepkmallick:agent-adk:0.1.0-rc.2")
    implementation("io.github.sundeepkmallick:agent-firebase:0.1.0-rc.2")
    implementation("io.github.sundeepkmallick:agent-litertlm:0.1.0-rc.2")
    implementation("io.github.sundeepkmallick:agent-mlkit:0.1.0-rc.2")
    // Legacy compatibility adapter; new Gemini Nano integrations should use agent-mlkit:
    implementation("io.github.sundeepkmallick:agent-gemini-nano:0.1.0-rc.2")
}
```

See [Installation](docs/installation.md) for Gradle setup and module selection.

## Developer examples

Start with the [developer examples guide](docs/examples.md) for copyable Kotlin patterns covering core tools and policies, Firebase AI Logic + ADK, LiteRT-LM + ADK, Gemini Nano + ML Kit, streaming and sessions. The [examples directory](examples/) contains individual snippets.

## Documentation

- [Installation and dependency setup](docs/installation.md)
- [Developer examples](docs/examples.md)
- [Modules and backend selection](docs/modules.md)
- [Architecture and lifecycle](docs/architecture.md)
- [Tool development](docs/tool-development.md)
- [Security model and host responsibilities](docs/security.md)
- [Release status and compatibility notes](docs/release-status.md)
- [Changelog](CHANGELOG.md)

## Important safety boundaries

The SDK does not replace application authorization. The host app owns identity, credentials, consent, domain rules, authoritative validation and UI. Tool registration is not permission to execute a tool. Supply an explicit policy; the core default denies tool calls.

Gemini Nano through `agent-mlkit` uses host-declared Android AppFunctions. Framework-dispatched AppFunctions must independently authenticate and authorize their callers; SDK `AgentPolicy` does not intercept those calls.

## Release maturity

`0.1.0-rc.2` is an experimental release candidate. CI/build success does not establish physical-device runtime support, production readiness, security clearance or binary/API compatibility. In particular, Gemini Nano and LiteRT-LM require device/runtime qualification on the target hardware. See [Release status](docs/release-status.md).

## Project links

- [SDK source repository](https://github.com/sundeepkmallick/SunMobileAiAgentSdk)
- [Maven Central group](https://central.sonatype.com/namespace/io.github.sundeepkmallick)
- [Apache License 2.0](LICENSE)

## License

This documentation and the SDK are provided under the Apache License, Version 2.0. See [LICENSE](LICENSE).
