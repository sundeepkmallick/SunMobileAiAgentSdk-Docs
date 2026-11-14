package examples

import android.content.Context
import com.sunmobile.aiagent.core.*
import com.sunmobile.aiagent.mlkit.*

suspend fun createGeminiNanoAgent(context: Context): Agent {
    val backend = GeminiNanoAgentBackendProvider(
        context = context.applicationContext,
        configuration = GeminiNanoConfiguration(
            agentName = "DeviceAssistant",
            instructions = "Use host-declared AppFunctions when needed.",
            maxTurns = 8,
            minimumAndroidApi = 36,
            enableAppFunctions = true,
        ),
    )

    when (val status = backend.checkAvailability()) {
        GeminiNanoAvailability.Available -> Unit
        is GeminiNanoAvailability.ModelNotReady ->
            error("Gemini Nano not ready: ${status.featureStatus}")
        is GeminiNanoAvailability.UnsupportedAndroidVersion ->
            error("Requires API ${status.requiredApi}; found ${status.actualApi}")
        is GeminiNanoAvailability.StatusCheckFailed -> throw status.cause
    }

    // No dynamic ToolRegistry: host AppFunctions are dispatched by Android's framework.
    return AgentBuilder()
        .configuration(
            AgentConfiguration(
                name = "DeviceAssistant",
                instructions = "Use the AppFunctions available to the host app.",
                maxTurns = 8,
            ),
        )
        .backend(backend)
        .build()
}
