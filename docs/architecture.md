# Architecture

SunMobile AI Agent SDK separates reusable agent infrastructure from application-specific behavior.

## Responsibilities

**SDK responsibilities**
- Backend-neutral agent contracts and capability reporting.
- Request, result and event abstractions.
- Tool registration and a deliberately limited JSON Schema subset validator.
- Host-supplied policy enforcement for supported tool-execution paths.
- Optional backend adapters and session helpers.

**Host application responsibilities**
- User identity, tenant scope, credentials and consent.
- Domain instructions, data access, repositories and tool implementations.
- Authorization, business validation and authoritative state checks.
- Approval flows, user interface, logging policy and data retention.
- Model acquisition, provenance, integrity and device admission where applicable.

## Dependency direction

```text
host application
    |            \
    |             +--> optional backend adapter --> vendor runtime
    |
    +----------------> agent-core
```

`agent-core` is Kotlin/JVM and is designed to remain independent of Android, Firebase, Google ADK, LiteRT-LM, ML Kit, AppFunctions, Compose and application-specific models.

## Backend boundaries

- Firebase AI Logic and LiteRT-LM use the ADK-backed path. `agent-adk` owns the shared runner, in-memory session service, dynamic tool bridge and event translation.
- Gemini Nano uses the ML Kit Agent/Prompt path in `agent-mlkit`; it is not an ADK model adapter and does not expose dynamic SDK ToolRegistry execution.
- Providers should advertise only capabilities supported by their runtime. Session persistence and multi-turn behavior are backend-specific.

## Tool execution trust boundary

Model output and tool arguments are untrusted. Registering a tool does not authorize it. The host supplies an explicit `AgentPolicy`; the default policy denies tool calls. For supported ADK tool execution, the core executor validates the supported schema subset and applies the host policy.

Schema validation is not authorization. Tool implementations must validate inputs again and use authenticated host state for identity and scope. Framework-dispatched Gemini Nano AppFunctions bypass the SDK's dynamic ToolRegistry and must implement their own authorization.

## Lifecycle

- The host creates and closes the agent.
- ADK sessions are in-memory and are lost with process lifetime.
- Use unique session IDs where workflow isolation is required.
- Close native engines before replacing or deleting model files.
- Serialize unsafe native model load/run/release operations.
- Verify cancellation, timeout, partial-stream and error behavior in the consuming app.
