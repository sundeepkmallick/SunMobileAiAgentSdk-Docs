# Security model

## Trust boundaries

Treat prompts, model completions, tool names, tool arguments, structured output and retrieved content as untrusted. A model can request an operation; it cannot authorize that operation.

The host application owns:
- Identity and tenant/resource scoping.
- Credentials, authentication, consent and authorization.
- Domain rules and authoritative server-side validation.
- Human approval and confirmation UX.
- Privacy, retention, redaction and audit policy.

## Policy behavior

Supply an explicit `AgentPolicy`. The core default denies tool calls. For supported core/ADK tool execution, the executor validates a limited JSON Schema subset and applies the host policy. Deny and RequireApproval paths fail closed; approval must be implemented by the host, which must re-authorize before executing a consequential operation.

Schema validation is not a security boundary by itself. Tool implementations must validate arguments again and check permissions against authoritative services.

## Gemini Nano AppFunctions

The `agent-mlkit` integration uses AppFunctions declared and implemented by the host application. Framework-dispatched AppFunctions are not intercepted by SDK `AgentPolicy`. Each AppFunction must independently authenticate and authorize its caller, enforce permissions and consent, validate every argument and apply domain rules.

## Host security checklist

- Derive identity and tenant scope from authenticated application state, never model arguments.
- Re-check live state immediately before side effects.
- Apply per-user and per-resource authorization on the authoritative service.
- Require explicit confirmation for sensitive or high-impact operations.
- Bound tool time, response size, retries and concurrent operations.
- Redact credentials and personal data from prompts, logs, metadata and tool output.
- Treat tool output and retrieved documents as untrusted data/instructions.
- Audit consequential actions with the authenticated principal and authoritative result.
- Define cancellation and partial-failure semantics for mutations.
- Never execute model-generated code or shell commands in the host process.

## What the SDK does not guarantee

The SDK does not authenticate users, enforce remote permissions, display approval UI, or guarantee policy enforcement for framework-dispatched AppFunctions. Review each backend adapter and every host tool before production use.
