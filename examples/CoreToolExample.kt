package examples

import com.sunmobile.aiagent.core.*
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.int

/**
 * A host-owned, read-only tool. Replace NotesRepository with your own repository.
 * This file demonstrates the SDK's actual AgentTool and AgentPolicy contracts.
 */
class ReadNoteTitlesTool(
    private val notesRepository: NotesRepository,
) : AgentTool {
    override val definition = ToolDefinition(
        name = "readNoteTitles",
        description = "List note titles visible to the signed-in user.",
        riskLevel = ToolRiskLevel.READ_ONLY,
        parameters = buildJsonObject {
            put("type", "object")
            putJsonObject("properties") {
                putJsonObject("limit") { put("type", "integer") }
            }
            putJsonArray("required") { add("limit") }
            put("additionalProperties", false)
        },
    )

    override suspend fun execute(
        arguments: kotlinx.serialization.json.JsonObject,
        context: ToolExecutionContext,
    ): ToolResult {
        val limit = arguments["limit"]?.jsonPrimitive?.int
            ?: return ToolResult("limit is required", isError = true)
        if (limit !in 1..20) return ToolResult("limit must be between 1 and 20", isError = true)

        // The repository must derive user identity from authenticated host state.
        val titles = notesRepository.listTitlesForCurrentUser(limit)
        return ToolResult(titles.joinToString(separator = "\n"))
    }
}

val noteTools = ToolRegistry()
    .register(ReadNoteTitlesTool(notesRepository))

val notePolicy = AgentPolicy { toolName, _, context ->
    if (toolName == "readNoteTitles" &&
        context.toolRiskLevel == ToolRiskLevel.READ_ONLY
    ) ToolAuthorization.Allow
    else ToolAuthorization.Deny("Only the read-only note listing tool is allowed")
}

// Supply your selected AgentBackendProvider when constructing AgentBuilder.
// The backend-neutral core does not include a model implementation.
