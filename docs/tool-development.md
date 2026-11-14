# Tool development

Tools connect an agent to capabilities provided by the host application. Keep tool implementations in the host or a domain integration whenever they depend on application data, identity or business rules.

## Recommended workflow

1. Implement the SDK's `AgentTool` contract.
2. Give the tool a stable, unique name.
3. Define a JSON object parameter schema and describe its constraints.
4. Validate arguments again when the tool executes.
5. Use the host-provided `AgentPolicy` to allow or deny execution, or require approval.
6. Authenticate and authorize against the authoritative backend.
7. Return only the minimum information the agent needs.
8. Test malformed input, denied access, timeouts, cancellation and side effects.

Exact constructors and interface signatures are versioned API details; consult the API/source corresponding to the dependency version you resolve.

## Security rules

- Never trust model-supplied user IDs, tenant IDs, prices, inventory, permissions or other authorization claims.
- Derive identity and resource scope from authenticated host state.
- Re-check authoritative state immediately before a consequential operation.
- Prefer read-only tools initially.
- Require explicit host-side confirmation for sensitive or high-impact operations.
- Bound execution time, response size, retries and concurrency.
- Do not return credentials or unnecessary personal data.

A schema describes input shape; it does not grant permission. The host remains responsible for all authorization and business validation.
