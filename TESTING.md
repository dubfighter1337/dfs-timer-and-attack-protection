# Testing

## Current scope

The automated suite contains **53 tests**, covering:

- Three successive cooldown cycles, repeated samples and no duplicate alerts.
- Every advance-warning offset from 0 to 10 seconds over three simulated cycles each.
- Login, world hops, resynchronization, stale samples and plugin restarts.
- Weapon-only, shield-only and combined loadouts; blank fields, item names and item IDs.
- NPC/player Attack filtering while preserving unrelated menu entries.
- Flash clipping, blinking, resizing, duration and a five-second regression check.
- Chime dispatch, volume, mute, absent audio devices and bundled WAV validity.
- Real Guice injection and RuneLite event-bus dispatch through the tracker and renderer.

`launcherSmoke` checks plugin registration and the RuneLite help entry point. It is not a live game session. Local test logs and generated reports are excluded from Git; the GitHub Actions build provides reproducible public verification.

## Run

On Windows:

```powershell
powershell -ExecutionPolicy Bypass -File .\local.ps1 test
```

On macOS / Linux, with JDK 17:

```bash
bash ./gradlew test jar launcherSmoke
```

Open `build/reports/tests/test/index.html` for the report. The overlay test also writes a synthetic rendering preview to `build/verification/flash-preview.png`.

## Live acceptance before Plugin Hub review

The owner has tried the development client and reported that the core behavior works. The full matrix below has not yet been signed off. Simulated tests cannot prove actual server timing, audio-device behavior or compatibility with other menu plugins.

- Complete at least three real DFS discharge cycles and compare warnings with actual availability.
- Test offsets 0, 1, 5 and 10 seconds. Confirm one warning per cycle and appropriate early-warning text.
- Try failed activations, invalid targets, the final shield charge and same-tick shield swaps.
- Unequip/re-equip with **Only alert while equipped** both off and on.
- Hop, reconnect and disable/re-enable during cooldown; check for false or duplicate alerts.
- Test weapon-only, shield-only, combined and blank loadouts with names and IDs.
- Verify **Activate**, movement and examine actions remain available. Check behavior with other menu plugins enabled.
- Check both settings sections, fixed/resizable layouts, five-second flashes, chime volume and muted audio.

Report results to the owner before submission. This repository does not automatically request RuneLite review, publish a Plugin Hub entry or release binaries.
