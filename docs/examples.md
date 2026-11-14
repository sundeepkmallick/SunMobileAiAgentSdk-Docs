# Developer examples

These examples are based on the SDK's current `0.1.0-rc.2` API. They show the supported composition patterns; replace the clearly marked host placeholders with your own repositories, Firebase setup, model provisioning and authorization.

> **Release note:** `0.1.0-rc.2` is a release candidate. It is not a stable compatibility promise. Run the examples on the Android versions and physical devices you intend to support.

## 1. Add the dependencies

Use JDK 17. Add Google Maven and Maven Central to your dependency repositories:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}
```

Add only the modules used by the selected backend:

```kotlin
dependencies {
    implementation("io.github.sundeepkmallick:agent-core:0.1.0-rc.2")

    // Google ADK runner (required by Firebase and LiteRT-LM integrations)
    implementation("io.github.sundeepkmallick:agent-adk:0.1.0-rc.2")

    // Choose one or both ADK model integrations:
    implementation("io.github.sundeepkmallick:agent-firebase:0.1.0-rc.2")
    implementation("io.github.sundeepkmallick:agent-litertlm:0.1.0-rc.2")

    // ML Kit Agent/Prompt backend for Gemini Nano (does not use agent-adk):
    implementation("io.github.sundeepkmallick:agent-mlkit:0.1.0-rc.2")
}
```

The legacy `agent-gemini-nano` artifact is retained for compatibility. Prefer `agent-mlkit` for new Gemini Nano integrations.

## 2. Define a host tool and an explicit policy

This is the central pattern for the core and ADK-backed integrations. The model may request a tool, but your host policy and tool implementation decide whether it can run.

```kotlin
import com.sunmobile.aiagent.core.AgentPolicy
import com.sunmobile.aiagent.core.AgentTool
import com.sunmobile.aiagent.core.ToolAuthorization
import com.sunmobile.aiagent.core.ToolDefinition
import com.sunmobile.aiagent.core.ToolExecutionContext
import com.sunmobile.aiagent.core.ToolRegistry
import com.sunmobile.aiagent.core.ToolResult
import com.sunmobile.aiagent.core.ToolRiskLevel
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.add
import kotlinx.serialization.json.jsonPrimitive

class ReadStoreHoursTool(
    private val storeRepository: StoreRepository,
) : AgentTool {
    override val definition = ToolDefinition(
        name = "readStoreHours",
        description = "Read opening hours for a store the signed-in user can access.",
        riskLevel = ToolRiskLevel.READ_ONLY,
        parameters = buildJsonObject {
            put("type", "object")
            putJsonObject("properties") {
                putJsonObject("storeId") { put("type", "string") }
            }
            putJsonArray("required") { add("storeId") }
            put("additionalProperties", false)
        },
    )

    override suspend fun execute(
        arguments: kotlinx.serialization.json.JsonObject,
        context: ToolExecutionContext,
    ): ToolResult {
        val storeId = arguments["storeId"]?.jsonPrimitive?.content
            ?: return ToolResult("storeId is required", isError = true)

        // The repository must enforce access using authenticated host state.
        val hours = storeRepository.getHoursForCurrentUser(storeId)
            ?: return ToolResult("Store not found or access denied", isError = true)

        return ToolResult("Store hours: $hours")
    }
}

val tools = ToolRegistry()
    .register(ReadStoreHoursTool(storeRepository))

val policy = AgentPolicy { toolName, _, context ->
    if (toolName == "readStoreHours" &&
        context.toolRiskLevel == ToolRiskLevel.READ_ONLY
    ) {
        ToolAuthorization.Allow
    } else {
        ToolAuthorization.Deny("This tool is not permitted")
    }
}
```

`StoreRepository` above is an application-owned type, not part of the SDK. Do not take user IDs, tenant IDs or authorization claims from model arguments. Resolve identity from authenticated host state inside the repository/tool.

## 3. Firebase AI Logic + Google ADK

Use this when your app wants a Firebase AI Logic model behind the shared Google ADK runner. Initialize Firebase in the host app before creating the model factory.

```kotlin
import com.sunmobile.aiagent.adk.AdkAgentBackendProvider
import com.sunmobile.aiagent.core.AgentBuilder
import com.sunmobile.aiagent.core.AgentConfiguration
import com.sunmobile.aiagent.core.AgentRequest
import com.sunmobile.aiagent.firebase.FirebaseAiModelFactory

suspend fun askWithFirebase(
    tools: ToolRegistry,
    policy: AgentPolicy,
): String? {
    val provider = AdkAgentBackendProvider(
        modelFactory = FirebaseAiModelFactory(
            modelName = "gemini-2.5-flash",
        ),
    )

    val agent = AgentBuilder()
        .configuration(
            AgentConfiguration(
                name = "StoreAssistant",
                instructions = "Answer using the registered store tools. Do not invent store hours.",
            ),
        )
        .backend(provider)
        .tools(tools)
        .policy(policy)
        .build()

    return try {
        agent.execute(
            AgentRequest("What time does store BER-01 close today?"),
        ).text
    } finally {
        agent.close()
    }
}
```

Host responsibilities:
- Initialize the intended `FirebaseApp` before this function runs.
- Configure Firebase project, authentication, App Check, quotas and billing.
- Select a model supported by the Firebase AI Logic configuration.
- Keep credentials out of source code and logs.
- Apply real access checks inside every tool.

## 4. LiteRT-LM + Google ADK (on-device model)

Use this when your app manages a LiteRT-LM model file and wants the shared ADK runner and core tool bridge.

```kotlin
import com.sunmobile.aiagent.adk.AdkAgentBackendProvider
import com.sunmobile.aiagent.core.AgentBuilder
import com.sunmobile.aiagent.core.AgentConfiguration
import com.sunmobile.aiagent.core.AgentRequest
import com.sunmobile.aiagent.litertlm.LiteRtLmModelFactory
import java.io.File

suspend fun askWithLiteRtLm(
    installedModelFile: File,
    cacheDirectory: File,
    tools: ToolRegistry,
    policy: AgentPolicy,
): String? {
    // installedModelFile must already be provisioned and verified by the host.
    val provider = AdkAgentBackendProvider(
        modelFactory = LiteRtLmModelFactory(
            modelFile = installedModelFile,
            cacheDirectory = cacheDirectory,
            modelName = "LocalAssistant",
        ),
    )

    val agent = AgentBuilder()
        .configuration(
            AgentConfiguration(
                name = "LocalAssistant",
                instructions = "Use registered tools when needed. Be clear when information is unavailable.",
            ),
        )
        .backend(provider)
        .tools(tools)
        .policy(policy)
        .build()

    return try {
        agent.execute(AgentRequest("Summarize my saved notes.")).text
    } finally {
        agent.close()
    }
}
```

Before constructing the factory, the host must:
- Obtain the model from a trusted source and review its license.
- Verify a pinned revision and cryptographic checksum.
- Check device compatibility and available storage.
- Store the model in an app-private location.
- Serialize native model lifecycle operations and close the agent before replacing/deleting the model file.

The SDK does not download model weights or validate their provenance, license, checksum or device suitability. The default factory backend is CPU; consult the exact LiteRT-LM runtime/device support before choosing another backend.

## 5. Gemini Nano with ML Kit Agent + Prompt

Use `agent-mlkit` for new Gemini Nano integrations. It runs on-device through ML Kit and uses AppFunctions declared by the host app.

```kotlin
import android.content.Context
import com.sunmobile.aiagent.core.AgentBuilder
import com.sunmobile.aiagent.core.AgentConfiguration
import com.sunmobile.aiagent.core.AgentRequest
import com.sunmobile.aiagent.mlkit.GeminiNanoAgentBackendProvider
import com.sunmobile.aiagent.mlkit.GeminiNanoAvailability
import com.sunmobile.aiagent.mlkit.GeminiNanoConfiguration

suspend fun askWithGeminiNano(context: Context): String? {
    val provider = GeminiNanoAgentBackendProvider(
        context = context.applicationContext,
        configuration = GeminiNanoConfiguration(
            agentName = "PrivateNotesAssistant",
            instructions = "Answer using the capabilities provided by the host app.",
            maxTurns = 8,
            minimumAndroidApi = 36,
            enableAppFunctions = true,
        ),
    )

    when (val readiness = provider.checkAvailability()) {
        GeminiNanoAvailability.Available -> Unit
        is GeminiNanoAvailability.ModelNotReady ->
            error("Gemini Nano is not ready: ${readiness.featureStatus}")
        is GeminiNanoAvailability.UnsupportedAndroidVersion ->
            error("Requires Android API ${readiness.requiredApi}; device API is ${readiness.actualApi}")
        is GeminiNanoAvailability.StatusCheckFailed ->
            throw readiness.cause
    }

    // This backend intentionally does not accept a dynamic core ToolRegistry.
    val agent = AgentBuilder()
        .configuration(
            AgentConfiguration(
                name = "PrivateNotesAssistant",
                instructions = "Answer using the host app's declared AppFunctions.",
                maxTurns = 8,
            ),
        )
        .backend(provider)
        .build()

    return try {
        agent.execute(AgentRequest("Summarize my selected note.")).text
    } finally {
        agent.close()
    }
}
```

Important AppFunctions boundary:
- Declare and implement AppFunctions in the host Android app.
- Configure the AppFunctions tooling/metadata and required build-time processing in the host app.
- The SDK's dynamic `ToolRegistry` and `AgentPolicy` do not intercept framework-dispatched AppFunctions.
- Every AppFunction must independently validate caller identity, permissions, arguments, consent and current business state.
- Do not expose sensitive data or perform mutations solely because the model requested them.

Availability checking does not download the model. Gemini Nano availability depends on Android version, device eligibility and runtime provisioning. Test on supported physical devices.

## 6. Stream partial output

The ADK adapter advertises streaming. Collect its events from a coroutine and update your UI on the appropriate dispatcher:

```kotlin
import com.sunmobile.aiagent.core.AgentEvent
import com.sunmobile.aiagent.core.AgentRequest
import kotlinx.coroutines.flow.collect

agent.executeStreaming(
    AgentRequest("Explain the store's opening hours."),
).collect { event ->
    when (event) {
        is AgentEvent.Started -> { /* mark request active */ }
        is AgentEvent.TextDelta -> appendToUi(event.text)
        is AgentEvent.ToolStarted -> { /* show tool activity */ }
        is AgentEvent.ToolCompleted -> { /* show tool completion */ }
        is AgentEvent.Completed -> { /* mark request complete */ }
        is AgentEvent.Failed -> showError(event.cause)
    }
}
```

Use the backend's advertised capabilities before depending on streaming, tool calling, session clearing or other optional features. The Gemini Nano adapter advertises local execution, streaming, cancellation and AppFunctions according to its configuration; it does not advertise the ADK session/tool capabilities.

## 7. Keep a conversation session

`AgentSession` supplies a stable session ID and serializes calls made through that facade:

```kotlin
import com.sunmobile.aiagent.core.AgentRequest
import com.sunmobile.aiagent.core.AgentSession

val session = AgentSession(agent)
try {
    val first = session.execute(AgentRequest("My preferred store is BER-01."))
    val second = session.execute(AgentRequest("What time does it close?"))
    println(second.text)
} finally {
    session.close()
    agent.close()
}
```

This is an in-memory conversation facade, not durable storage. The ADK adapter uses in-memory sessions; process death loses them. Session clearing is only supported when the backend implements `SessionAwareAgent`.

## 8. Android lifecycle guidance

- Build and execute agents from a lifecycle-aware coroutine scope, not on the main thread.
- Keep one clear owner for each agent instance and call `close()` when that owner is finished.
- Do not share one native LiteRT-LM model instance across concurrent owners unless the runtime explicitly supports it.
- Surface unavailable-device/model states and provide a fallback where appropriate.
- Treat cancellation and partial streaming as normal lifecycle cases.
- Never put model credentials, Firebase secrets or signing credentials in the app.

## Related examples and references

- [Core tool and policy example](../examples/CoreToolExample.kt)
- [Firebase + ADK example](../examples/FirebaseAdkExample.kt)
- [LiteRT-LM + ADK example](../examples/LiteRtLmExample.kt)
- [Gemini Nano + ML Kit example](../examples/GeminiNanoExample.kt)
- [Offline sample app source](https://github.com/sundeepkmallick/SunMobileAiAgentSdk/tree/main/sample-app) (private source repository; link is for project maintainers)
