# DFS Timer and Attack Protection

A RuneLite plugin that alerts when the dragonfire shield cooldown ends and optionally hides NPC Attack options based on equipped items.

## DFS Timer

Flashes the screen red when the DFS cooldown finishes. Colour, opacity and duration are configurable. Optional alerts include a soft chime, desktop notification and on-screen message.

The default duration is two seconds. Enter **5000** in **Duration (ms)** for five seconds. **Warn early** allows an approximate warning 0–10 seconds before cooldown completion.

## Attack Protection

Optional PvE protection that hides NPC Attack options unless the configured equipment is equipped. **Off by default. The default equipment is bronze crossbow and DFS.**

Weapon and shield fields accept item names or IDs. Blank fields are ignored: either slot can be used alone, or both can be required. Both blank means no restriction.

Player Attack options are never changed. Protection is disabled in detected PvP areas and worlds. DFS Activate remains available; existing combat and auto-retaliate are not stopped.
