package examples

import com.sunmobile.aiagent.adk.AdkAgentBackendProvider
import com.sunmobile.aiagent.core.*
import com.sunmobile.aiagent.firebase.FirebaseAiModelFactory

suspend fun createFirebaseAgent(
    tools: ToolRegistry,
    policy: AgentPolicy,
): Agent {
    // FirebaseApp must already be initialized by the host application.
    val backend = AdkAgentBackendProvider(
        modelFactory = FirebaseAiModelFactory(
            modelName = "gemini-2.5-flash",
        ),
    )

    return AgentBuilder()
        .configuration(
            AgentConfiguration(
                name = "StoreAssistant",
                instructions = "Answer from approved store data. Do not invent facts.",
            ),
        )
        .backend(backend)
        .tools(tools)
        .policy(policy)
        .build()
}

// Usage from a lifecycle-aware coroutine:
// val agent = createFirebaseAgent(tools, policy)
// try { val answer = agent.execute(AgentRequest("When does BER-01 close?")) }
// finally { agent.close() }
