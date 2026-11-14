package examples

import com.sunmobile.aiagent.adk.AdkAgentBackendProvider
import com.sunmobile.aiagent.core.*
import com.sunmobile.aiagent.litertlm.LiteRtLmModelFactory
import java.io.File

suspend fun createLocalLiteRtAgent(
    modelFile: File,
    cacheDirectory: File,
    tools: ToolRegistry,
    policy: AgentPolicy,
): Agent {
    // modelFile must be downloaded/provisioned and cryptographically verified by the host.
    val backend = AdkAgentBackendProvider(
        modelFactory = LiteRtLmModelFactory(
            modelFile = modelFile,
            cacheDirectory = cacheDirectory,
            modelName = "OnDeviceAssistant",
        ),
    )

    return AgentBuilder()
        .configuration(
            AgentConfiguration(
                name = "OnDeviceAssistant",
                instructions = "Use the approved local tools and state uncertainty.",
            ),
        )
        .backend(backend)
        .tools(tools)
        .policy(policy)
        .build()
}
