# Changelog

## 1.0.1-test.1

First standalone tester release of the latest FORM round shield source (project revision 886).

- Fix the extra half-block translation in the live held-shield OBJ binding that caused the shield to float away from the hand.
- Allow vanilla lantern storage in any unlocked equipment slot, keeping its visible rear-waist position and sway independent of the storage slot.
- Use the same unheld-lantern selection for the body renderer, LambDynamicLights and Iris lighting; do not duplicate a held lantern on the waist.
- Retain independent wheel storage, per-hand selection, normal durability/drop/swap behavior, quiver insertion/ejection and native bow/crossbow ammo handling.
- Preserve mod ID `round_shield_`; stowed shields intentionally do not render on the body.
- Add an independent Gradle build with pinned, verified compile-only dependency downloads. FORM and its camera/client runtime are not required or included.
