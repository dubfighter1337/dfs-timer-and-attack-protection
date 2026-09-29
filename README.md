# DFS Timer and Attack Protection

A RuneLite plugin with two focused features: **dragonfire shield cooldown alerts** and **optional Attack menu protection based on your equipment**.

**Status: development / local testing. Not submitted to or approved by the RuneLite Plugin Hub.**

## DFS Timer

Get a red screen flash when your dragonfire shield's discharge cooldown finishes. Optionally add a quiet chime or move the alert earlier so you can prepare your next activation.

- Red, translucent flashing across the game canvas, similar to RuneLite's event warnings.
- Adjustable colour, opacity and duration, with optional on-screen text.
- Optional soft chime, played once when the alert starts, with its own volume control.
- Advance warning from **0–10 seconds**. Zero waits for confirmed cooldown completion; positive offsets are estimates.
- One alert per cooldown, with safeguards against duplicate and login/hop alerts.
- No Windows notifications, system pings or taskbar alerts.

| Setting | Default | What it controls |
| --- | --- | --- |
| Flash colour | Red | Colour of the screen flash |
| Opacity | 25% | Transparency of the flash |
| Duration (ms) | 2000 | 200–30000 ms; **5000 = five seconds** |
| Show message | Off | “DFS cooldown finished” or “DFS cooldown ending soon” |
| Only alert while equipped | Off | Suppress the alert if the DFS is no longer equipped |
| Warn early (seconds) | 0 | Move the single alert up to 10 seconds earlier |
| Play soft chime | Off | One gentle, bundled 0.65-second tone |
| Chime volume | 35% | Independent volume; 0 mutes it |

The flash blinks at 400 ms on / 400 ms off. The chime does not repeat on every blink. Audio-device latency may slightly delay sound playback. [Listen to the bundled chime](src/main/resources/com/dfsready/soft-chime.wav) (native file volume; the default playback setting is quieter).

## Attack Protection

Reduce accidental attacks while using the wrong equipment. This feature is **off by default** and has its own settings section.

Enable **Attack protection**, then enter an exact item name or item ID in **Allowed weapon** and **Allowed shield**. Names are case-insensitive. Every filled field must match your equipped item; blank fields are ignored.

| Allowed weapon | Allowed shield | Attack is available when… |
| --- | --- | --- |
| Bronze crossbow | Dragonfire shield | Both are equipped — the default loadout |
| Bronze crossbow | *(blank)* | The bronze crossbow is equipped, with any shield or none |
| *(blank)* | Dragonfire shield | The DFS is equipped, with any weapon or none |
| Abyssal whip | *(blank)* | The abyssal whip is equipped |
| *(blank)* | *(blank)* | Always; no equipment restriction |

When the loadout does not match, NPC and player **Attack** menu entries are hidden. **DFS Activate**, movement, examining and other actions remain available.

Name matching allows item variants with the same displayed name. Use a numeric item ID to require a specific variant. A misspelled name will not match.

This is a menu filter: it **does not stop combat already in progress or disable auto-retaliate**. Close an already-open menu after changing equipment or settings so the next menu reflects the change. Your saved in-game Attack option settings are not modified.

## Try it locally

This repository launches a separate RuneLite development client. The plugin is not currently available through the Plugin Hub.

1. Install **JDK 17** and set `JAVA_HOME` to its installation directory.
2. Clone this repository and open a terminal in its folder.
3. Start the development client:

   **Windows PowerShell**
   ```powershell
   powershell -ExecutionPolicy Bypass -File .\local.ps1 run
   ```

   **macOS / Linux**
   ```bash
   bash ./gradlew run
   ```

4. Search for **DFS Timer and Attack Protection** in RuneLite's plugin list. Settings are grouped under **DFS Timer** and **Attack Protection**.

The Windows helper uses a project-local JDK when present, otherwise `JAVA_HOME`. Downloads and caches are not part of this repository. The build uses RuneLite **1.12.39** and targets Java 11 bytecode.

For a Jagex account, follow RuneLite's official [development login instructions](https://github.com/runelite/wiki/blob/master/Using-Jagex-Accounts.md). Keep login credentials local; never add them to this repository or an issue. The plugin itself does not read or export credentials.

Restart the development client after updating the code. A plain plugin JAR is not installed by copying it into the normal RuneLite plugins folder. Existing settings from the previous DFS Ready name are preserved.

## How timing works

The timer reads RuneLite's dragonfire shield recharge value each game tick. With no advance warning, it waits for that value to reach zero. Early warnings interpolate between the server's eight-tick cooldown updates, so they are approximate rather than exact wall-clock promises.

An observed DFS cooldown remains tracked after unequipping. Login, loading, reconnects and world hops establish a fresh baseline without flashing; an active cycle can continue afterward. Completion during an interruption does not trigger a delayed alert, and resuming inside the early-warning window does not trigger a catch-up alert.

Cooldown completion does not guarantee that charges remain. Other draconic shields share cooldown state, so a shield swap in the same tick or enabling the plugin during another shield's cooldown can make attribution ambiguous. The configurable attack-protection loadout does not change which item's cooldown is tracked: the timer is for the DFS.

## Build and verification

```powershell
powershell -ExecutionPolicy Bypass -File .\local.ps1 test
```

Or on any supported platform:

```bash
bash ./gradlew test jar launcherSmoke
```

This compiles the plugin, runs **53 automated tests**, builds the JAR, and checks the development launcher using RuneLite's help entry point. Tests cover cooldown cycles, all advance-warning offsets, equipment matching, menu filtering, rendering and sound dispatch. See [TESTING.md](TESTING.md) for scope and remaining live-game checks.

- JAR: `build/libs/dfs-timer-and-attack-protection-0.1.0.jar`
- Test report: `build/reports/tests/test/index.html`
- CI runs the build and tests on pushes and pull requests.

## Feedback and review status

For a bug report, describe your settings, equipped items, expected result and actual result. Include whether you had recently logged in, hopped worlds or swapped equipment. Do not attach credentials or unredacted account logs.

Publishing this source repository is separate from RuneLite Plugin Hub submission. **No RuneLite review or submission has been requested.** Further live testing and owner approval are required before that step.

## License

[BSD 2-Clause](LICENSE). The soft chime is an original synthesized asset; its generation script is included under `scripts/`.
