# **Tempestra**

**Tempestra** is a client-side Fabric visual overhaul for Minecraft lightning, maintained for **1.20.1**, **1.21.1**, and **1.21.11**.

This mod replaces the normal vanilla lightning visual with thinner, more natural-looking light-blue bolts that arc through the sky, glow, and form fine aerial branches. Each strike keeps a single ground contact; secondary ground-split bolts are not generated. Impacts have cosmetic radial debris and a restrained fire/smoke afterglow; soul-fire surfaces use soul flame, while copper produces an electricity-only impact. It is safe to install only on clients: it adds no commands, persisted state, world changes, lightning damage changes, or server-side weather logic.

## Supported versions

| Minecraft | Java target | Butterfly API |
| --- | --- | --- |
| 1.20.1 | 17 | 1.1.2 |
| 1.21.1 | 21 | 1.4.0+1.21.1 |
| 1.21.11 | 21 | 1.4.0+1.21.11 |

The 1.20.1 development target also loads GeckoLib 4.4.9, which is required by Butterfly API 1.1.2. Fabric Loader will request it automatically when users install Butterfly API in a release profile.

## Development and builds

Stonecutter manages the shared source and version-specific build properties. The repository keeps 1.21.11 active for commits.

Run Gradle with Java 21 (required by Loom); the 1.20.1 target automatically compiles with its Java 17 toolchain.

```powershell
.\gradlew buildAll
```

Build output is written to `versions/<minecraft-version>/build/libs/`. For version-specific development, use one of Stonecutter's generated switch tasks, then return to 1.21.11 before committing:

```powershell
.\gradlew stonecutterSwitchTo1.20.1
.\gradlew stonecutterSwitchTo1.21.1
.\gradlew stonecutterSwitchTo1.21.11
```

Launch a single target explicitly; an unqualified `runClient` task selects all three Gradle subprojects:

```powershell
.\gradlew :1.20.1:runClient
.\gradlew :1.21.1:runClient
.\gradlew :1.21.11:runClient
```

**Tempestra** is meant to be a simple, purely visual improvement for Vanilla+, survival, fantasy, and atmospheric modpacks.

---

## **Download**

**Tempestra** is officially published on Modrinth.

**Download Tempestra here:**  
https://modrinth.com/mod/tempestra
