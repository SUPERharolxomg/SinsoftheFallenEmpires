# Contributing to Sins of the Fallen Empires

Thanks for helping! This guide explains how to set up the project and how changes get in.

## Before you start

- Read the [README](README.md) for the story and the [documentation index](README.md#4-documentation).
- Check the [sprint plan](docs/Sprints.md) and the open issues to see what is being worked on.
- For anything larger than a small fix, open an issue first so we can agree on the approach.

## Setup

Requirements: Git and any JDK 17 or newer. Gradle downloads a Java 17 toolchain by itself if needed.

```bash
git clone https://github.com/SUPERharolxomg/SinsoftheFallenEmpires.git
cd SinsoftheFallenEmpires
./gradlew build          # compile and run unit tests (first run takes several minutes)
./gradlew runClient      # launch Minecraft with the mod
./gradlew runGameTestServer   # run the in-game tests and exit
```

On Windows, use `gradlew.bat` instead of `./gradlew`.

IDE: open the folder in IntelliJ IDEA (recommended) or VS Code with the Java extensions, then run `./gradlew genIntellijRuns` or `./gradlew genVSCodeRuns`.

## Rules for code

- **Target:** Minecraft 1.20.1, Forge 47.x, Java 17.
- **Package:** everything lives under `com.sofe`; follow the layout in [docs/Arquitectura.md](docs/Arquitectura.md).
- **No player-facing text in code.** Every name, dialogue line and description goes in `assets/sofe/lang/en_us.json` and `es_es.json`.
- **Balance values in data files** (`data/sofe/...`), not hard-coded.
- **Server authority:** the client only sends intent; the server validates everything.
- **Tests:** pure logic gets a JUnit test in `src/test`; behavior that needs the game gets a GameTest in `com.sofe.gametest`.

## Pull requests

1. Create a branch from `main` (`feature/...` or `fix/...`).
2. Keep each pull request focused on one change.
3. Make sure `./gradlew build` passes; CI runs the build and the GameTests on every pull request.
4. Fill in the pull request template, including which sprint task or issue it covers.

## Art and sound

Placeholders are fine. The asset list with sizes and formats is in [docs/Anexos.md](docs/Anexos.md#a2-splash-arts-and-3d-models) and the style guide in [docs/Arte.md](docs/Arte.md). Only submit assets you made or that have a license compatible with MIT distribution, and say which in the pull request.

## License

By contributing, you agree that your contributions are licensed under the [MIT License](LICENSE).
