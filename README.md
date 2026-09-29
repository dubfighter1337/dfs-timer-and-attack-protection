# DFS Timer and Attack Protection

A RuneLite plugin for dragonfire shield cooldown alerts and optional equipment-based Attack protection.

## DFS Timer

Flashes the game screen red when the DFS cooldown finishes.

- Adjustable colour, opacity and flash duration.
- Optional soft chime with adjustable volume. Off by default.
- Optional on-screen message.
- Advance warning from 0–10 seconds. Early warnings are approximate; 0 waits for cooldown completion.
- One alert per cooldown. No Windows notifications.

The default flash lasts two seconds. **Duration (ms)** accepts 200–30000; enter **5000** for five seconds. Cooldown completion does not guarantee that charges remain.

## Attack Protection

Hides NPC and player **Attack** options when the configured equipment is not equipped. Off by default.

**The default is bronze crossbow and DFS.** The weapon and shield fields accept exact item names or item IDs.

- Both fields filled: both items must match.
- Weapon blank: only the shield must match.
- Shield blank: only the weapon must match.
- Both blank: no restriction.

Blank slots are ignored. Item names are case-insensitive. DFS **Activate** remains available. This feature does not stop combat already in progress or disable auto-retaliate.

## Download and run

The plugin is not currently available in the RuneLite Plugin Hub. These steps start a separate RuneLite client with the plugin loaded.

1. [Download the source ZIP](https://github.com/dubfighter1337/dfs-timer-and-attack-protection/archive/refs/heads/main.zip) and extract it.
2. Install **JDK 17** and set `JAVA_HOME` to the JDK installation folder.
3. Open a terminal in the extracted folder and run:

   **Windows PowerShell**
   ```powershell
   powershell -ExecutionPolicy Bypass -File .\local.ps1 run
   ```

   **macOS / Linux**
   ```bash
   bash ./gradlew run
   ```

4. Find **DFS Timer and Attack Protection** in the plugin list. Settings have separate **DFS Timer** and **Attack Protection** sections.

The first launch downloads the required build files. Jagex accounts require RuneLite's [development login setup](https://github.com/runelite/wiki/blob/master/Using-Jagex-Accounts.md). Credential files must remain private.

## License

Plugin code and the bundled chime use [CC0 1.0](LICENSE): free to use, modify and share, without attribution requirements. Included third-party build tools retain their existing licenses.
