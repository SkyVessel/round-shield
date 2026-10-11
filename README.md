# Round Shield

Standalone Minecraft **26.1.2 / NeoForge 26.1.2.94** mod by SkyVessel, built with FORM Mod Studio. Includes a round shield, five equipment mounts, an independent equipment wheel and a 320-arrow quiver.

The first tester release is **1.0.1-test.1**, including the latest held-shield Attach repair and the lantern storage/anchor repair. The mod ID remains `round_shield_` for existing saves.

## Install

1. Install Minecraft Java 26.1.2 and NeoForge 26.1.2.94 with Java 25.
2. Download the mod JAR from [Releases](https://github.com/SkyVessel/round-shield/releases) and put it in the instance's `mods` folder. Remove any older Round Shield JAR first.
3. Start the game. FORM is not required. For multiplayer, install the same version on the server and participating clients.

No additional mod is required for the shield, equipment wheel or quiver. **LambDynamicLights 4.11.0 for 26.1.2** optionally lights a mounted lantern. Iris/Sodium, Punchy and Joyful Motions are separate, optional visual integrations; they are not included in this download. The local FORM test setup used a patched Iris snapshot; compatibility with every public shader/animation combination has not been verified.

See [the Chinese tester guide](docs/TESTING.zh-CN.md) for controls and a short Attach checklist.

## Controls and equipment

- Put one vanilla lead in the added slot beside the inventory character to unlock five equipment slots. Close the recipe book to interact with the wheel area.
- Slots accept swords, axes, pickaxes, shovels, hoes, shields and vanilla lanterns. The quiver uses the right-back slot. A lantern can be stored in any slot; its visible mount always stays at the rear waist.
- Hold **Mouse 4**, move toward a wheel sector, then release. The center cancels. Rebind the action under **Controls → Inventory**.
- Swords/tools select the main hand; shields/lanterns select the offhand. Selecting the same item again stows it. Stowed shields deliberately have no body model.
- Wheel items remain in their own storage while held. Scrolling or pressing a hotbar number restores the normal main hand and preserves the offhand. Durability, use, Q drop and F swap refer to the actual stored item.
- To remove the lead, first empty the wheel and stow any selected equipment.
- Insert ordinary arrows by clicking the quiver with arrows, or arrows with the quiver. Capacity: 5 × 64. Right-click while holding the quiver ejects up to 64 arrows. Equipped bow/crossbow ammunition is supplied from the quiver; Infinity and Multishot use vanilla consumption rules.

## Recipes

Round shield: `W` is any plank, `I` is an iron ingot.

```text
WWW
WIW
WWW
```

Quiver: `L` is leather; spaces are empty.

```text
L L
L L
LLL
```

## Build from source

Requires **Java 25**, **Python 3** and an internet connection for the first build. Gradle 9.2.1 is pinned by the wrapper.

```sh
python3 scripts/prepare-dependencies.py
./gradlew build
```

Output: `build/libs/round_shield_-1.0.1-test.1.jar`. Windows: use `python` and `gradlew.bat` if those are the installed command names.

The preparation script downloads pinned, SHA-512-verified Iris and LambDynamicLights versions, extracts the compilation APIs, and stores them in ignored `libs/`. These third-party binaries are never committed or bundled. Their original licenses apply; see [third-party notices](THIRD_PARTY_NOTICES.md).

`src/extensions/java` contains authored gameplay code; `src/generated` contains the current generated Java and model resources needed to build without FORM. OBJ/MTL files ending in `.txt` are renamed during resource processing. `src/extensions/form-bindings.json` preserves the corrected model bindings for future editor exports. Optional server GameTests run with `./gradlew -PgearAcceptance runGameTestServer`; acceptance code is excluded from the normal release JAR.

Published source comes from FORM round shield project revision **886**. It excludes the studio client, camera scripts, private player-profile files, worlds, recordings, local resource packs, caches and client smoke-test fixtures. No gameplay Java logic was changed while preparing this repository.

## License

All Rights Reserved. Release binaries are provided for testing. Publishing the source does not change the project's existing license. See [LICENSE](LICENSE).
