# Modules

The SDK is split into a backend-neutral core and optional integration modules.

| Artifact | Purpose | Notes |
| --- | --- | --- |
| `agent-core` | Kotlin/JVM contracts, capabilities, request/result/events, session facade, tool registry, JSON Schema subset validation and host policy executor | No Android or vendor runtime dependency |
| `agent-adk` | Google ADK runner, in-memory sessions, streaming/event translation and dynamic bridge for core tools | Android integration |
| `agent-firebase` | Firebase AI Logic model factory for the shared ADK provider | Host initializes Firebase and configures auth/App Check |
| `agent-litertlm` | LiteRT-LM model factory | Accepts a model file already installed by the host |
| `agent-mlkit` | ML Kit GenAI Agent + Prompt backend | Preferred path for new Gemini Nano integrations; uses host-declared AppFunctions |
| `agent-gemini-nano` | Legacy ML Kit adapter | Retained for compatibility; prefer `agent-mlkit` for new integrations |

The offline Research Assistant sample is part of the SDK source project and is not a published Maven artifact.

## Choosing an integration

### Core only

Use `agent-core` when you provide your own backend implementation or want to build against the backend-neutral contracts.

### Google ADK

Add `agent-adk` when using the shared ADK runner and tool bridge. Firebase and LiteRT-LM integrations build on this adapter.

### Firebase AI Logic

Add `agent-firebase` with `agent-adk`. The host application remains responsible for Firebase initialization, project configuration, authentication, App Check, billing and model selection.

### LiteRT-LM

Add `agent-litertlm` with `agent-adk`. The host must install and verify the model file before creating the factory. The SDK does not download model weights or verify their provenance, license, checksum or device suitability. The host should serialize native model lifecycle operations and close the engine before replacing or deleting its file.

### Gemini Nano / ML Kit

For new integrations, use `agent-mlkit`. It uses ML Kit GenAI Agent and Prompt APIs and relies on AppFunctions declared and implemented by the host app. It intentionally does not dynamically register the SDK's core ToolRegistry. Each AppFunction must independently enforce caller identity, permission, consent and business authorization. SDK `AgentPolicy` does not intercept framework-dispatched AppFunctions.

The provider has an Android API 36 minimum by default and checks runtime availability. Actual availability depends on eligible devices and runtime conditions; compile success is not device qualification.

## Sessions and lifecycle

ADK sessions are in-memory and are not durable across process death. The host owns the agent lifecycle and must call `close()`. The session facade serializes calls for a conversation but does not persist state to disk. Backend capabilities and multi-turn semantics differ; do not assume all providers are interchangeable.
