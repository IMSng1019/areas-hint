# Repository Guidelines

## Project Structure & Module Organization
This repository is a Fabric mod (`areas-hint`) with a split client/server source layout:
- `src/main/java/areahint/`: server/common logic (commands, networking, data, file I/O, world handling).
- `src/client/java/areahint/`: client logic (detection, rendering, UI flows, key handlers, client networking).
- `src/main/resources/`: mod metadata and assets, including language files in `assets/areas-hint/lang/`.
- `.github/workflows/build.yml`: CI build pipeline (`./gradlew build`).
- Root scripts like `translate_*.py` and `extract_*.py` support localization/content workflows.

## Build, Test, and Development Commands
Use the Gradle wrapper from repo root:
- `./gradlew build` (Windows: `.\gradlew.bat build`): compile and package the mod (`build/libs`).
- `./gradlew clean`: remove generated artifacts.
- `./gradlew runClient`: launch a dev client for manual testing.
- `./gradlew runServer`: launch a dev server.
- `./gradlew runDatagen`: run Fabric data generation.
- `./gradlew publishToMavenLocal`: publish local Maven artifact for integration testing.

## Coding Style & Naming Conventions
- Java 21 is the build target (`JavaCompile.release = 21`).
- Keep package names lowercase (`areahint.*`) and classes in PascalCase (`ExpandAreaManager`).
- Use descriptive suffixes already common in this codebase: `*Manager`, `*Networking`, `*UI`, `*Command`.
- Follow existing formatting: tabs are used in Gradle files; Java style in `src/**/java` should match surrounding code.
- Keep resource identifiers and language keys consistent with existing `assets/areas-hint/lang/*.json`.

## Testing Guidelines
There is no dedicated `src/test` suite currently; verification is build + runtime checks:
- Run `./gradlew build` before opening a PR.
- Manually validate affected gameplay paths in `runClient`/`runServer` (commands, area detection, UI/network sync).
- If adding automated tests, place them under `src/test/java` and keep test names behavior-oriented (for example, `AreaDetectorAltitudeFilterTest`).

## Commit & Pull Request Guidelines
Recent history follows concise versioned subjects such as `4.0.1` and `fix-4.0.0-1.21.4`. Keep commits short, scoped, and consistent with this pattern.
- Commit format examples: `4.0.2`, `fix-4.0.2-1.21.4`.
- PRs should include: change summary, impacted modules/paths, validation steps run, and screenshots/GIFs for UI-visible changes.
- Link related issues and call out compatibility impacts (Minecraft/Fabric/Java version changes).
