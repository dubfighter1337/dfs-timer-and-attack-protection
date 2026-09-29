# DFS Timer and Attack Protection

A RuneLite plugin for dragonfire shield cooldown alerts and optional equipment-based Attack protection.

## DFS Timer

Flashes the game screen red when the DFS cooldown finishes.

- Adjustable colour, opacity and flash duration.
- Optional soft chime with adjustable volume. Off by default.
- Optional on-screen message.
- Advance warning from 0–10 seconds. Early warnings are approximate; 0 waits for cooldown completion.
- Optional Windows / desktop notification. Off by default.
- One alert per cooldown; desktop notifications use the same advance-warning setting.

The default flash lasts two seconds. **Duration (ms)** accepts 200–30000; enter **5000** for five seconds. Cooldown completion does not guarantee that charges remain.

Desktop notifications depend on system notification settings. The option does not add a RuneLite notification beep or take window focus; the soft chime remains a separate setting.

## Attack Protection

Hides NPC **Attack** options when the configured equipment is not equipped. Off by default. Player menu entries are never changed. Protection is disabled on PvP/Deadman/PvP Arena worlds and in detected PvP, adjacent PvP and Wilderness areas.

**The default is bronze crossbow and DFS.** The weapon and shield fields accept exact item names or item IDs.

- Both fields filled: both items must match.
- Weapon blank: only the shield must match.
- Shield blank: only the weapon must match.
- Both blank: no restriction.

Blank slots are ignored. Item names are case-insensitive. DFS **Activate** remains available. This feature does not stop combat already in progress or disable auto-retaliate.

## License

Plugin code and the bundled chime use [CC0 1.0](LICENSE): free to use, modify and share, without attribution requirements. Included third-party build tools retain their existing licenses.
