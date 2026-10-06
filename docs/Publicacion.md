# Publishing on CurseForge

How *Sins of the Fallen Empires* goes up on CurseForge: first the mod, then the modpack that uses it. A CurseForge modpack holds no mods of its own: its `manifest.json` names each mod by its CurseForge project and file, so the mod must be on CurseForge before the pack can point at it.

Everything for the pages is in `art/curseforge/`: the square logo (`logo_400.png`), the two descriptions (`description_mod.md`, `description_modpack.md`) and the screenshots.

## 1. The mod

1. Build the release: `./gradlew build` makes `build/libs/sofe-<version>.jar` (the version is `mod_version` in `gradle.properties`).
2. On curseforge.com, *Create a project* → Minecraft → **Mods**.
   - Name: Sins of the Fallen Empires
   - Summary: *A dark fantasy RPG campaign: five Bearers, five fallen empires, seven Archsins and the First Fallen.*
   - Description: `art/curseforge/description_mod.md`
   - Logo: `art/curseforge/logo_400.png`
   - Main category *Adventure and RPG*; also *World Gen*, *Armor, Tools, and Weapons*, *Mobs*
   - License: MIT (the repository's `LICENSE`)
   - Source and issues: the GitHub repository
3. Upload the jar as the first file: Minecraft 1.20.1, Forge, release type **Beta**. Add the relations **GeckoLib** and **Curios API** as *Required dependency*; the furniture mod, Supplementaries and Small Ships as *Optional dependency*.
4. Add the screenshots (`art/curseforge/screenshots/`) to the gallery.
5. Wait for CurseForge's review (usually hours, sometimes a few days). Then note the **project id** (on the project's page, *About Project*) and the **file id** (in the address of the file's page).

## 2. The modpack

1. Build it with the mod's ids:

   ```text
   python scripts/make_modpack.py --sofe-project <project id> --sofe-file <file id>
   ```

   It makes `build/modpack/Sins-of-the-Fallen-Empires-<version>.zip`. The other mods, and their exact files, are in `pack/curseforge.json` (the versions checked with the mod, docs/Rendimiento.md).
2. On curseforge.com, *Create a project* → Minecraft → **Modpacks**, with `art/curseforge/description_modpack.md` and the same logo, category *Adventure and RPG*.
3. Upload the zip: Minecraft 1.20.1, release type **Beta**.
4. After the review, try it from the CurseForge app: install it, *Begin the Journey*, play the first minutes.

**Try the pack before publishing:** `python scripts/make_modpack.py` without the ids puts the mod's own jar inside the zip. That zip is not for uploading, but the CurseForge app imports it (*Create Custom Profile* → *Import*) to check the whole pack on the player's side.

## Updating

Upload the new jar to the mod's project, put its file id in `pack/curseforge.json` (or `--sofe-file`), raise `mod_version`, build the pack again and upload it to the modpack's project.

## Notes

- **Memory Leak Fix** has no 1.20.1 build on CurseForge, so the CurseForge pack goes without it; the rest of the performance mods are in.
- **Not on the server:** Embeddium, Entity Culling, ImmediatelyFast, Dynamic FPS and Xaero's Minimap are client mods; a server pack leaves them out ([Servidor.md](Servidor.md)).
- The illustrations made with an image AI should be said so on the mod's page; CurseForge allows them.
