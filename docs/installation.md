# Installation

## Requirements

- JDK 17.
- Android SDK for Android integration modules.
- Gradle dependency repositories configured for Google Maven and Maven Central.

The core module is Kotlin/JVM. Android integrations have additional Android and vendor-runtime requirements; see [Modules](modules.md).

## Add repositories

In your settings or project repository configuration, ensure Google Maven and Maven Central are available. For example, with centralized repository management:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}
```

## Add dependencies

The current published release candidate is `0.1.0-rc.2`, with group `io.github.sundeepkmallick`.

Add only the modules needed by your application:

```kotlin
dependencies {
    implementation("io.github.sundeepkmallick:agent-core:0.1.0-rc.2")

    // Optional integrations:
    implementation("io.github.sundeepkmallick:agent-adk:0.1.0-rc.2")
    implementation("io.github.sundeepkmallick:agent-firebase:0.1.0-rc.2")
    implementation("io.github.sundeepkmallick:agent-litertlm:0.1.0-rc.2")
    implementation("io.github.sundeepkmallick:agent-mlkit:0.1.0-rc.2")

    // Legacy compatibility adapter. Prefer agent-mlkit for new Gemini Nano work.
    implementation("io.github.sundeepkmallick:agent-gemini-nano:0.1.0-rc.2")
}
```

The sample application is not published as a Maven artifact.

## Verify resolution

After adding dependencies, run your normal Gradle dependency resolution/build tasks. You can inspect the resolved graph with:

```bash
./gradlew :app:dependencies
```

Use the actual module path for your application if it is not `:app`.

## Next: working examples

See the [Developer examples](examples.md) for complete patterns using Firebase AI Logic, LiteRT-LM, Gemini Nano, tools, policies, streaming and sessions.

## Minimal agent flow

The following illustrates the core composition shape. Your app supplies a backend provider, tool registry and policy implementation:

```kotlin
val agent = AgentBuilder()
    .configuration(AgentConfiguration("ResearchAssistant", "Use the registered tools."))
    .backend(myBackendProvider)
    .tools(myToolRegistry)
    .policy(myApplicationPolicy)
    .build()

try {
    val result = agent.execute(AgentRequest("Summarize the supplied documents"))
    println(result.text)
} finally {
    agent.close()
}
```

Treat this as a structural example: backend construction and host policy depend on the integration you choose. Consult the SDK source/API documentation for exact constructors and types for your selected release.

## Development builds

For SDK development, the source project supports local Maven publication and Gradle composite-build workflows. Those are development paths; ordinary consumers should resolve the published artifacts from Maven Central. Do not place Central credentials or signing keys in an application project.
