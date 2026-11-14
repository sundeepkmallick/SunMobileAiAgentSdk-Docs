# Release status

## Current published version

The current published release candidate is **`0.1.0-rc.2`** under Maven group `io.github.sundeepkmallick`.

| Module | Coordinate |
| --- | --- |
| Core | `io.github.sundeepkmallick:agent-core:0.1.0-rc.2` |
| ADK | `io.github.sundeepkmallick:agent-adk:0.1.0-rc.2` |
| Firebase | `io.github.sundeepkmallick:agent-firebase:0.1.0-rc.2` |
| LiteRT-LM | `io.github.sundeepkmallick:agent-litertlm:0.1.0-rc.2` |
| ML Kit | `io.github.sundeepkmallick:agent-mlkit:0.1.0-rc.2` |
| Legacy Gemini Nano | `io.github.sundeepkmallick:agent-gemini-nano:0.1.0-rc.2` |

See the [Maven Central namespace](https://central.sonatype.com/namespace/io.github.sundeepkmallick) for publication listings.

## Maturity and limitations

This is a **release candidate**, not a stable release or production-readiness statement. The SDK's CI/build and packaging checks establish compilation and the specific tests they execute; they do not establish:

- Physical-device Gemini Nano behavior or availability.
- LiteRT-LM inference and lifecycle behavior across supported hardware.
- End-to-end Firebase/ADK behavior with real host configuration.
- Independent security or privacy review.
- Complete public API or binary compatibility qualification.
- Model provenance, license and device suitability for host-provided model files.

The host app must perform device/runtime qualification for its target devices and backend configuration. Gemini Nano requires eligible device/runtime conditions. LiteRT-LM model download, integrity, licensing and device admission remain host responsibilities.

## Before a stable release

Complete and review device/runtime testing, API/binary compatibility analysis, security/privacy review, dependency vulnerability/license checks, model provenance review and host integration qualification. Do not treat a successful build or emulator test as a substitute for physical-device evidence. No stable `0.1.0` release is documented here.
