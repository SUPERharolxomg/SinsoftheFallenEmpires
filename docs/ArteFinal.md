# Final art brief

The 61 illustrations (2 already done) that replace the drawn stand-ins of the story's screens, one by one: what each shows, its file, its size and a prompt for an image AI. Written for whoever makes them (the author, an artist or an image AI). The full asset list is in [Anexos.md](Anexos.md#a2-splash-arts-and-3d-models); the visual rules in [Arte.md](Arte.md).

The 3D models, portraits, icons, logo and key art are already done. What is left is 2D illustration (this document) and music ([Anexos.md](Anexos.md#a2-splash-arts-and-3d-models), "Music and sound").

## How to deliver a piece

1. Make it at the size given (most are **1920×1080**, 16:9).
2. Save it as PNG (or high-quality JPG renamed `.png` is not allowed: export real PNG) under 1 MB if possible.
3. Put it at its path under `src/main/resources/assets/sofe/textures/gui/splash/`. The game uses it **automatically** the next time it starts; until then it keeps the drawn stand-in. Nothing else to change.

Check one in game with `./gradlew runClient -PplaceShots=ending:<bearer>:<card>` (the ending's cards) or `scene:<bearer>:<second>` (Crowned in Ash).

## The style

The reference is the user's own key art of the five Bearers looking at Sulthari under the eclipse: `art/concepts/style_reference_bearers.png`. Give it to the AI as a **style reference** on every piece, so they all look like one game. Two of the user's pieces in the same style are already in the game and show the target: `splash/intro/night_of_the_eclipse.png` and `splash/codex/cover.png`.

- Dark epic fantasy, painted, highly detailed; cinematic wide shots.
- Light: a strong light source (eclipse corona, sunrise, fire, aetherium glow) against deep shadow; rim light on silhouettes; volumetric rays and mist.
- **Aetherium:** clear cyan-white crystal when pure; black with violet veins and violet lightning when corrupted.
- Empire palettes and looks:

  | Empire | Palette | Look |
  |--------|---------|------|
  | Sulthari | Gold, red, dark brown | Ottoman: brass domes, minarets, clockwork, tramways, the Great Observatory |
  | Nordrath | Ice blue, grey, blood red | Norse: longhouses, fjords, volcanic forges, runestones |
  | Parsivan | Purple, turquoise, silver | Persian: hanging gardens, iwans, turquoise domes, the Silk Road |
  | Khemet | Sand, turquoise, gold | Egyptian: river valley, pyramids, pylons, sphinxes, catacombs |
  | Aureum | Marble, gold, royal blue | Roman/Byzantine: marble forums, aqueducts, the Colosseum, golden domes |

- Everything is fictional: no real flags, religious symbols, logos or lettering.

### Rules every piece must keep

- **No text** in the picture (the game writes its own, in two languages).
- **The lower quarter is for the subtitles:** keep it dark and quiet (ground, shadow, mist). The subject sits in the upper three quarters.
- **The player is drawn by the game**, in their own skin and gear, in two sets: the Bearer epilogues (a figure standing on a rise at the centre, lower third) and Crowned in Ash (a figure standing on the dais before the throne, centre). Leave that spot **empty** and lit, as a stage: do not paint the hero there. The heroes never outshine the players.
- Same time of day within a set: the **fate slides** and **epilogues** at dawn (the world healing), **Crowned in Ash** under a black false sun with the city burning, the **bosses** in their own lair's light.

### A prompt that works

Every prompt below is the subject only. Add the shared style line at the end:

```text
dark epic fantasy digital painting, cinematic wide shot, dramatic rim lighting, volumetric light and mist, highly detailed, painterly, rich colors against deep shadows, no text, no letters, no watermark, no logo, 16:9
```

and the negative prompt (for tools that take one): `text, letters, watermark, logo, signature, frame, border, modern, photo, blurry, deformed hands`.

## The 61 pieces

### 1. Region fate slides: 15 (`splash/fate/<region>_<fate>.png`, 1920×1080)

Shown in the ending, one per region, by the fate the Bearer chose there (*unsettled* when they chose none). All at **dawn**; the *unsettled* ones greyer, with less sun.

| File | Shows | Prompt (subject) |
|------|-------|------------------|
| `sulthari_bazaar.png` | The Low Bazaar thrives through the night; the Observatory dome mended tile by tile | Ottoman brass city at dawn, a crowded night bazaar of lanterns and silk awnings still busy at sunrise, merchants and families, scaffolding on a great observatory dome being repaired with gold tiles in the background, warm gold and red |
| `sulthari_observatory.png` | The Great Observatory rebuilt first, its lens turned to the sky; the bazaar trades in tents among ash | Ottoman brass city at dawn, a gleaming rebuilt observatory with a huge brass telescope lens pointed at the pale sky, below it a poor market of patched canvas tents among ash and rubble |
| `sulthari_unsettled.png` | Sulthari rebuilds as always: the Council argues, the tramways run again | Ottoman brass city at grey dawn, half-repaired domes and walls, a brass tramway running again through damaged streets, a council hall with lit windows, muted colors |
| `nordrath_peace.png` | The clans meet around one fire; the cold forges burn for plows | Norse clans gathered around one great bonfire in a snowy hall at dawn, axes laid down in a pile, a forge glowing in the background hammering plowshares, ice blue and warm firelight |
| `nordrath_war.png` | The endless war goes on beneath the mountains; the clans sing of their dead | Snowy mountain pass at dawn, ghostly armored Norse warriors still fighting in the distance under the mountain, living clansmen watching from the pass with torches and banners, blood red and ice blue |
| `nordrath_unsettled.png` | Snow covers the citadel; each clan keeps to its hold | Abandoned burning citadel now covered in snow, separate Norse holds on distant hills each with its own small fire, grey dawn, lonely and cold |
| `parsivan_awakened.png` | The Dreaming Court wakes, weeps, opens the Gardens to all | Persian palace hanging gardens at dawn, courtiers in purple and turquoise silks waking on cushions, some weeping, gates thrown open, common people walking in among fountains and flowers |
| `parsivan_asleep.png` | The court sleeps on, smiling; the Gardens bloom over it | Persian palace gardens at dawn, courtiers asleep and smiling on silk cushions overgrown by roses and vines, violet petals drifting, travelers passing quietly on a path |
| `parsivan_unsettled.png` | The Gardens grow wild; no one knows who dreams | Persian hanging gardens grown wild at grey dawn, some sleepers, some wandering figures, tangled vines over turquoise domes, uncertain hazy light |
| `khemet_rest.png` | The souls guided below at last; reeds planted where the marsh was | Egyptian river valley at dawn, pale glowing souls descending peacefully into a great tomb gate, villagers planting reeds in dried green fields where a swamp was, sand and gold, serene |
| `khemet_guardians.png` | The souls stand guard over the living | Egyptian desert town at dawn guarded by tall translucent turquoise spirit warriors with jackal-like helms standing at the gates, children sleeping safely inside |
| `khemet_unsettled.png` | The marsh dries slowly; the souls still wander | Half-dried marsh beside pyramids at grey dawn, faint lost spirits drifting among dead reeds and catacomb entrances, muted sand colors |
| `aureum_law.png` | The courts sit again; the Law read aloud to everyone | Roman marble forum at dawn, a magistrate reading a scroll aloud from a rostrum to a large crowd of all classes, royal blue banners, gold light on columns |
| `aureum_shared.png` | The gold shared out in the streets; poorer, louder, fed | Roman marble city street at dawn, chests of gold coins being handed out to crowds, bread and market stalls, cheerful noise, slightly worn marble, gold and royal blue |
| `aureum_unsettled.png` | The forum fills with traders; the courts stay shut; the gold stays in the vault | Roman forum at grey dawn busy with traders and orators, a great court building with closed bronze doors, a sealed golden vault door in shadow, muted colors |

### 2. Bearer epilogues: 10 (`splash/epilogue/<hero>_<full|unfinished>.png`, 1920×1080)

At dawn, each in the hero's own land. **Leave a lit, empty rise at the centre of the lower third**: the game draws the player's own figure there. The *unfinished* ones are the sadder version: lonelier, colder, no resolution.

| File | Shows | Prompt (subject) |
|------|-------|------------------|
| `cassian_full.png` | The Order of the Scale refounded in Aureum, open to any empire | Roman marble fortress-chapel at dawn with a great bronze scale emblem over open gates, knights of many different peoples in blue and gold training in the courtyard, an empty sunlit rise in the foreground center |
| `cassian_unfinished.png` | The Order stays a ruin; a lone guard at its gate | Ruined Roman marble chapel at grey dawn, broken scale emblem fallen, a single torch at an empty gate, an empty rise in the foreground center |
| `ankhareth_full.png` | The gates of the underworld open; the souls, and his master, walk on | Colossal Egyptian underworld gate opening at dawn, a procession of glowing peaceful souls walking through it into golden light, an empty rise in the foreground center |
| `ankhareth_unfinished.png` | The gates open, but his master is not among the souls | Egyptian underworld gate at grey dawn, a few souls passing, one empty place in the procession, cold turquoise light, an empty rise in the foreground center |
| `shirin_full.png` | Laleh freed; a new map of the sky, a star named for her sister | Persian observatory terrace at dawn under a sky of glowing drawn constellation lines forming a new star map, one bright new star, violet and silver, an empty rise in the foreground center |
| `shirin_unfinished.png` | Laleh's voice still drifts through the Gardens; one star unnamed | Persian gardens at grey dawn, a faint ghostly woman's silhouette among the flowers, a star map in the sky with one dark gap, an empty rise in the foreground center |
| `rurik_full.png` | The Vaults' gold returned to the clans; his village rebuilt | Norse village being rebuilt at dawn, new timber longhouses, carts of gold delivered to clan chiefs, smoke from chimneys, an empty rise in the foreground center |
| `rurik_unfinished.png` | He keeps the gold; his village stays ash | Burned Norse village at grey dawn, ash and charred beams, a lone hall with one candle and a pile of gold inside, an empty rise in the foreground center |
| `azhar_full.png` | He refuses the throne and calls a Pact of equals | Ottoman throne hall at dawn with an empty throne, a round table of leaders of five different peoples, the sealed glowing book beneath the throne, an empty rise in the foreground center |
| `azhar_unfinished.png` | No one answers his call; he watches the sealed Codex alone | Vast empty Ottoman throne hall at grey dawn, empty chairs around a round table, a faint glow from a sealed book beneath the throne, an empty rise in the foreground center |

### 3. Crowned in Ash: 5 (`splash/crowned/<class>.png`, 1920×1080)

The secret bad ending: the Bearer rules a burning Aetheris. A black false sun with a burning ring overhead, the hero's own land on fire, their people kneeling. **Leave the dais before the throne empty and lit**: the game draws the player there, crowned.

| File | Shows | Prompt (subject) |
|------|-------|------------------|
| `knight.png` | Aureum burning; the Order kneels; scales melted into a crown | Roman marble city burning under a black eclipse sun with a fiery ring, knights in blue and gold kneeling before an empty dark throne on a dais, melted bronze scales, embers and ash |
| `necromancer.png` | Khemet's dead in endless rows guarding a king who never walks | Egyptian necropolis burning under a black eclipse sun, endless rows of mummified dead standing guard with glowing teal eyes before an empty throne on a dais, ash falling |
| `sorceress.png` | Parsivan's stars burn out one by one | Persian palace burning under a black eclipse sun, stars falling burning from the sky, a dreaming court bowing before an empty throne on a dais, violet and fire |
| `thief.png` | All the gold of Aetheris; the clans digging it in chains | Norse mountain hall burning under a black eclipse sun, mountains of gold coins, chained clansmen carrying sacks, an empty throne on a dais, embers |
| `king.png` | Emperor of Aetheris; every crown melted into one | Ottoman brass city burning under a black eclipse sun, crowds bowing in ash-covered squares, melted crowns at the foot of an empty golden throne on a dais, the observatory in flames |

### 4. The Archsins and Nahrazel: 8 (`splash/boss/<id>.png`, 1920×1080)

Shown when the boss first appears and in the Codex. They must be **terrifying** (the user's bestiary sheet `art/concepts/bosses/bestiary_sheet.png`): huge, broken, glowing eyes in their sin's colour, in their own lair.

| File | Archsin | Prompt (subject) |
|------|---------|------------------|
| `vorath.png` | Vorath, Wrath, the Burning Citadel (Nordrath) | Colossal demon warlord of wrath with cracked horns and molten armor, red glowing eyes, giant flaming axe, in a burning volcanic citadel full of ghostly endless warriors |
| `luxara.png` | Luxara, Lust, the Enchanted Gardens (Parsivan) | Beautiful terrifying winged sorceress demon of lust in pink and violet silks, bat wings, mirror shards orbiting her, hypnotic pink eyes, in moonlit Persian gardens full of sleepers |
| `morthis.png` | Morthis, Sloth, the Stagnant Marsh (Khemet) | Enormous bloated pharaoh demon of sloth seated in a stagnant swamp, rotting gold mask and wrappings, green glowing eyes, mummies rising from the mud around him |
| `avarok.png` | Avarok, Greed, the Golden Vaults (Aureum) | Huge demon of greed made of gold coins and chains, a gaping sack-like belly, yellow glowing eyes, clawed hands grabbing, in a colossal marble vault overflowing with gold |
| `gularth.png` | Gularth, Gluttony, the Feast Halls (under Nordrath) | Monstrous gluttonous demon with a cleaver and a vast maw, devouring the stone floor, orange glowing eyes, in an underground feast hall of rotten banquet tables |
| `envyris.png` | Envyris, Envy, the Shadow Throne (Aureum) | Gaunt green-eyed demoness of envy on a shadowy throne in ruined Aureum, mirror-like shadow copies of heroes around her, emerald glow, scythe |
| `prython.png` | Prython, Pride, the Celestial Spire (above Sulthari) | Radiant fallen angel of pride with golden wings and a cracked halo, a sword of false sunlight, standing atop an upside-down white spire in the sky above a burning Ottoman city |
| `nahrazel.png` | Nahrazel, the First Fallen, the Inverted Throne (beneath Sulthari) | Colossal ash titan, the first fallen devil, seven glowing sin colors in cracks across his body, seated on an inverted throne in a cavern beneath the city, giant open book of light with seven locks before him |

### 5. The Broken Oaths: 10 (`splash/boss/<id>.png`, 1920×1080)

The guardians of the Ten Laws, broken: armor of the empire whose Law they kept, cracked and corrupted, torn shackles, the split seal of their Law. Shown in the Codex.

| File | Broken Oath | Prompt (subject) |
|------|-------------|------------------|
| `kaleth.png` | Kaleth, the Burning Blade (Nordrath Forge) | Corrupted Norse warrior guardian with a burning greatsword, cracked iron armor, torn shackles, in a volcanic forge |
| `serath.png` | Serath, the Blood Maiden (Nordrath Arena) | Pale blood-drinking warrior maiden in red-stained Norse armor, pools of blood, a snowy arena |
| `mirael.png` | Mirael, the Whisperer (Parsivan Baths) | Veiled ghostly whisperer in violet silks half invisible, misty Persian bathhouse with turquoise pools |
| `thessyn.png` | Thessyn, the Silk Weaver (Silk Road) | Spider-bodied weaver in silk robes spinning webs across a caravanserai on the Silk Road, poison green glow |
| `dormiel.png` | Dormiel, the Dreamer (Khemet Catacombs) | Sleeping lantern-bearing guardian in Egyptian wrappings, clouds of drowsy dust, nightmare shapes, catacombs |
| `goldarc.png` | Goldarc, the Coinlord (Aureum Treasury) | Roman guardian in gold-coin armor with a huge golden shield, coins flying, marble treasury |
| `nixara.png` | Nixara, the Hollow Merchant (Aureum Market) | Hollow masked merchant with an empty coat, false gold and scales, market stalls over trapdoors |
| `fenrath.png` | Fenrath, the Devourer (Nordrath Caverns) | Wolf-like devourer with a cavernous acid-dripping maw, icy caverns |
| `shadeyn.png` | Shadeyn, the Mirror (Aureum Colosseum) | Gladiator guardian with a shattered mirror blade, mirrored shadow copies, ruined colosseum sand |
| `solrath.png` | Solrath, the False Prophet (Temple of Sulthari) | False prophet in white and gold robes with a spear of false sunlight, pillars of holy light, an Ottoman temple |

### 6. The empires: 5 (`splash/empire/<region>.png`, 1920×1080)

Shown on the region's loading screen and in the Codex: each empire **corrupted**, as the player first finds it.

| File | Prompt (subject) |
|------|------------------|
| `sulthari.png` | Ottoman brass city on a plateau behind walls of glowing cyan aetherium, domes and minarets, the Great Observatory, the last free city at dusk |
| `nordrath.png` | Norse tundra and fjords under a red sky, volcanic forges, longhouses and runestones veined with black aetherium |
| `parsivan.png` | Persian hanging gardens and turquoise domes overgrown and dreaming, violet mist, black aetherium cracks |
| `khemet.png` | Egyptian river valley with pyramids and catacombs, a stagnant green marsh, black aetherium veins in the sand |
| `aureum.png` | Roman marble republic in ruins, aqueducts and a colosseum, gold everywhere and violet corruption |

### 7. The rest: 8 (2 done)

| File | Size | Shows | Prompt (subject) |
|------|------|-------|------------------|
| `splash/intro/night_of_the_eclipse.png` | 1920×1080 | Act I: the eclipse breaks the seal over Sulthari | **Done** (the user's piece: Sulthari half whole, half corrupted, under the eclipse) |
| `splash/codex/cover.png` | 1024×1536 | The cover of the Codex screen | **Done** (the user's piece: the Bearers on a cliff before Sulthari) |
| `splash/map/codex_map.png` | 2048×2048 | The illustrated map of Aetheris | Antique illustrated fantasy map on parchment, Sulthari at the center, Nordrath to the north, Parsivan north-east, Khemet south-east, Aureum to the west, a southern sea between Aureum and Khemet, small painted cities, mountains, rivers, no text |
| `splash/hero/cassian.png` | 448×640 | Hero card: the Knight | Portrait card of a Roman-Byzantine knight in blue and gold plate with a scale emblem shield, noble scarred face |
| `splash/hero/ankhareth.png` | 448×640 | Hero card: the Necromancer | Portrait card of an Egyptian necromancer in turquoise and gold with a jackal mask and floating soul lights |
| `splash/hero/shirin.png` | 448×640 | Hero card: the Sorceress | Portrait card of a Persian sorceress in violet and silver with an astrolabe and constellation runes |
| `splash/hero/rurik.png` | 448×640 | Hero card: the Thief | Portrait card of a Norse rogue with a fur hood, twin axes and a single gold coin |
| `splash/hero/azhar.png` | 448×640 | Hero card: the King | Portrait card of an Ottoman sultan in red and gold with a turban crown and a scepter |

(The hero cards may follow the user's sheet `art/concepts/hero_cards_sheet_v2.png`.)

## Which image AI

Any tool works as long as every piece gets the **same style reference**. Three good choices:

| Tool | Cost | Strength | How to keep the style |
|------|------|----------|-----------------------|
| **Midjourney** (v7) | Paid plan from about 10 USD a month | The best at this painted epic look; very consistent sets | `--sref` with `style_reference_bearers.png`, `--ar 16:9` (`--ar 7:10` for hero cards) |
| **ChatGPT** (image generation) | Included in the paid plan | Follows long scene descriptions and "leave the centre empty" well | Attach the reference image and say "in exactly this style" |
| **Stable Diffusion XL or FLUX, local** (Fooocus or ComfyUI) | Free | Unlimited images on the development PC (RTX 4060) | Fooocus "Image Prompt" with the reference image |

Before publishing check the tool's terms for the plan used, and say on the mod's page that the illustrations were made with AI (CurseForge and Modrinth allow it).

## Ready to paste

Every illustration still missing, with its whole message for the image AI (ChatGPT, or any other). For each one:

1. Open a new chat and attach the style reference (`art/concepts/style_reference_bearers.png`).
2. Copy the message in the box and send it.
3. Save the picture as PNG with the name shown, in `src/main/resources/assets/sofe/textures/gui/splash/<folder>/` (or hand it over to be put in place).

This list is written by `scripts/make_art_prompts.py` from the tables above; run it again and the pieces already in the game leave it.

58 to go.

### 1. `fate/sulthari_bazaar.png`

```text
Create a wide landscape illustration in exactly the style of the attached image (dark epic fantasy digital painting, painterly, dramatic rim lighting, volumetric light and mist, rich colors against deep shadows).

Scene: Ottoman brass city at dawn, a crowded night bazaar of lanterns and silk awnings still busy at sunrise, merchants and families, scaffolding on a great observatory dome being repaired with gold tiles in the background, warm gold and red.

Rules: no text, no letters, no logos. Keep the bottom quarter of the image dark and quiet (ground, shadow or mist), because subtitles will go there. Landscape format.
```

### 2. `fate/sulthari_observatory.png`

```text
Create a wide landscape illustration in exactly the style of the attached image (dark epic fantasy digital painting, painterly, dramatic rim lighting, volumetric light and mist, rich colors against deep shadows).

Scene: Ottoman brass city at dawn, a gleaming rebuilt observatory with a huge brass telescope lens pointed at the pale sky, below it a poor market of patched canvas tents among ash and rubble.

Rules: no text, no letters, no logos. Keep the bottom quarter of the image dark and quiet (ground, shadow or mist), because subtitles will go there. Landscape format.
```

### 3. `fate/sulthari_unsettled.png`

```text
Create a wide landscape illustration in exactly the style of the attached image (dark epic fantasy digital painting, painterly, dramatic rim lighting, volumetric light and mist, rich colors against deep shadows).

Scene: Ottoman brass city at grey dawn, half-repaired domes and walls, a brass tramway running again through damaged streets, a council hall with lit windows, muted colors.

Rules: no text, no letters, no logos. Keep the bottom quarter of the image dark and quiet (ground, shadow or mist), because subtitles will go there. This is the version where nothing was settled: greyer and dimmer than a hopeful dawn, the sun weak behind clouds. Landscape format.
```

### 4. `fate/nordrath_peace.png`

```text
Create a wide landscape illustration in exactly the style of the attached image (dark epic fantasy digital painting, painterly, dramatic rim lighting, volumetric light and mist, rich colors against deep shadows).

Scene: Norse clans gathered around one great bonfire in a snowy hall at dawn, axes laid down in a pile, a forge glowing in the background hammering plowshares, ice blue and warm firelight.

Rules: no text, no letters, no logos. Keep the bottom quarter of the image dark and quiet (ground, shadow or mist), because subtitles will go there. Landscape format.
```

### 5. `fate/nordrath_war.png`

```text
Create a wide landscape illustration in exactly the style of the attached image (dark epic fantasy digital painting, painterly, dramatic rim lighting, volumetric light and mist, rich colors against deep shadows).

Scene: Snowy mountain pass at dawn, ghostly armored Norse warriors still fighting in the distance under the mountain, living clansmen watching from the pass with torches and banners, blood red and ice blue.

Rules: no text, no letters, no logos. Keep the bottom quarter of the image dark and quiet (ground, shadow or mist), because subtitles will go there. Landscape format.
```

### 6. `fate/nordrath_unsettled.png`

```text
Create a wide landscape illustration in exactly the style of the attached image (dark epic fantasy digital painting, painterly, dramatic rim lighting, volumetric light and mist, rich colors against deep shadows).

Scene: Abandoned burning citadel now covered in snow, separate Norse holds on distant hills each with its own small fire, grey dawn, lonely and cold.

Rules: no text, no letters, no logos. Keep the bottom quarter of the image dark and quiet (ground, shadow or mist), because subtitles will go there. This is the version where nothing was settled: greyer and dimmer than a hopeful dawn, the sun weak behind clouds. Landscape format.
```

### 7. `fate/parsivan_awakened.png`

```text
Create a wide landscape illustration in exactly the style of the attached image (dark epic fantasy digital painting, painterly, dramatic rim lighting, volumetric light and mist, rich colors against deep shadows).

Scene: Persian palace hanging gardens at dawn, courtiers in purple and turquoise silks waking on cushions, some weeping, gates thrown open, common people walking in among fountains and flowers.

Rules: no text, no letters, no logos. Keep the bottom quarter of the image dark and quiet (ground, shadow or mist), because subtitles will go there. Landscape format.
```

### 8. `fate/parsivan_asleep.png`

```text
Create a wide landscape illustration in exactly the style of the attached image (dark epic fantasy digital painting, painterly, dramatic rim lighting, volumetric light and mist, rich colors against deep shadows).

Scene: Persian palace gardens at dawn, courtiers asleep and smiling on silk cushions overgrown by roses and vines, violet petals drifting, travelers passing quietly on a path.

Rules: no text, no letters, no logos. Keep the bottom quarter of the image dark and quiet (ground, shadow or mist), because subtitles will go there. Landscape format.
```

### 9. `fate/parsivan_unsettled.png`

```text
Create a wide landscape illustration in exactly the style of the attached image (dark epic fantasy digital painting, painterly, dramatic rim lighting, volumetric light and mist, rich colors against deep shadows).

Scene: Persian hanging gardens grown wild at grey dawn, some sleepers, some wandering figures, tangled vines over turquoise domes, uncertain hazy light.

Rules: no text, no letters, no logos. Keep the bottom quarter of the image dark and quiet (ground, shadow or mist), because subtitles will go there. This is the version where nothing was settled: greyer and dimmer than a hopeful dawn, the sun weak behind clouds. Landscape format.
```

### 10. `fate/khemet_guardians.png`

```text
Create a wide landscape illustration in exactly the style of the attached image (dark epic fantasy digital painting, painterly, dramatic rim lighting, volumetric light and mist, rich colors against deep shadows).

Scene: Egyptian desert town at dawn guarded by tall translucent turquoise spirit warriors with jackal-like helms standing at the gates, children sleeping safely inside.

Rules: no text, no letters, no logos. Keep the bottom quarter of the image dark and quiet (ground, shadow or mist), because subtitles will go there. Landscape format.
```

### 11. `fate/khemet_unsettled.png`

```text
Create a wide landscape illustration in exactly the style of the attached image (dark epic fantasy digital painting, painterly, dramatic rim lighting, volumetric light and mist, rich colors against deep shadows).

Scene: Half-dried marsh beside pyramids at grey dawn, faint lost spirits drifting among dead reeds and catacomb entrances, muted sand colors.

Rules: no text, no letters, no logos. Keep the bottom quarter of the image dark and quiet (ground, shadow or mist), because subtitles will go there. This is the version where nothing was settled: greyer and dimmer than a hopeful dawn, the sun weak behind clouds. Landscape format.
```

### 12. `fate/aureum_law.png`

```text
Create a wide landscape illustration in exactly the style of the attached image (dark epic fantasy digital painting, painterly, dramatic rim lighting, volumetric light and mist, rich colors against deep shadows).

Scene: Roman marble forum at dawn, a magistrate reading a scroll aloud from a rostrum to a large crowd of all classes, royal blue banners, gold light on columns.

Rules: no text, no letters, no logos. Keep the bottom quarter of the image dark and quiet (ground, shadow or mist), because subtitles will go there. Landscape format.
```

### 13. `fate/aureum_shared.png`

```text
Create a wide landscape illustration in exactly the style of the attached image (dark epic fantasy digital painting, painterly, dramatic rim lighting, volumetric light and mist, rich colors against deep shadows).

Scene: Roman marble city street at dawn, chests of gold coins being handed out to crowds, bread and market stalls, cheerful noise, slightly worn marble, gold and royal blue.

Rules: no text, no letters, no logos. Keep the bottom quarter of the image dark and quiet (ground, shadow or mist), because subtitles will go there. Landscape format.
```

### 14. `fate/aureum_unsettled.png`

```text
Create a wide landscape illustration in exactly the style of the attached image (dark epic fantasy digital painting, painterly, dramatic rim lighting, volumetric light and mist, rich colors against deep shadows).

Scene: Roman forum at grey dawn busy with traders and orators, a great court building with closed bronze doors, a sealed golden vault door in shadow, muted colors.

Rules: no text, no letters, no logos. Keep the bottom quarter of the image dark and quiet (ground, shadow or mist), because subtitles will go there. This is the version where nothing was settled: greyer and dimmer than a hopeful dawn, the sun weak behind clouds. Landscape format.
```

### 15. `epilogue/cassian_full.png`

```text
Create a wide landscape illustration in exactly the style of the attached image (dark epic fantasy digital painting, painterly, dramatic rim lighting, volumetric light and mist, rich colors against deep shadows).

Scene: Roman marble fortress-chapel at dawn with a great bronze scale emblem over open gates, knights of many different peoples in blue and gold training in the courtyard, an empty sunlit rise in the foreground center.

Rules: no text, no letters, no logos. Keep the bottom quarter of the image dark and quiet (ground, shadow or mist), because subtitles will go there. Leave an empty, lit spot in the center of the lower third: no character standing there. Landscape format.
```

### 16. `epilogue/cassian_unfinished.png`

```text
Create a wide landscape illustration in exactly the style of the attached image (dark epic fantasy digital painting, painterly, dramatic rim lighting, volumetric light and mist, rich colors against deep shadows).

Scene: Ruined Roman marble chapel at grey dawn, broken scale emblem fallen, a single torch at an empty gate, an empty rise in the foreground center.

Rules: no text, no letters, no logos. Keep the bottom quarter of the image dark and quiet (ground, shadow or mist), because subtitles will go there. Leave an empty, lit spot in the center of the lower third: no character standing there. Landscape format.
```

### 17. `epilogue/ankhareth_full.png`

```text
Create a wide landscape illustration in exactly the style of the attached image (dark epic fantasy digital painting, painterly, dramatic rim lighting, volumetric light and mist, rich colors against deep shadows).

Scene: Colossal Egyptian underworld gate opening at dawn, a procession of glowing peaceful souls walking through it into golden light, an empty rise in the foreground center.

Rules: no text, no letters, no logos. Keep the bottom quarter of the image dark and quiet (ground, shadow or mist), because subtitles will go there. Leave an empty, lit spot in the center of the lower third: no character standing there. Landscape format.
```

### 18. `epilogue/ankhareth_unfinished.png`

```text
Create a wide landscape illustration in exactly the style of the attached image (dark epic fantasy digital painting, painterly, dramatic rim lighting, volumetric light and mist, rich colors against deep shadows).

Scene: Egyptian underworld gate at grey dawn, a few souls passing, one empty place in the procession, cold turquoise light, an empty rise in the foreground center.

Rules: no text, no letters, no logos. Keep the bottom quarter of the image dark and quiet (ground, shadow or mist), because subtitles will go there. Leave an empty, lit spot in the center of the lower third: no character standing there. Landscape format.
```

### 19. `epilogue/shirin_full.png`

```text
Create a wide landscape illustration in exactly the style of the attached image (dark epic fantasy digital painting, painterly, dramatic rim lighting, volumetric light and mist, rich colors against deep shadows).

Scene: Persian observatory terrace at dawn under a sky of glowing drawn constellation lines forming a new star map, one bright new star, violet and silver, an empty rise in the foreground center.

Rules: no text, no letters, no logos. Keep the bottom quarter of the image dark and quiet (ground, shadow or mist), because subtitles will go there. Leave an empty, lit spot in the center of the lower third: no character standing there. Landscape format.
```

### 20. `epilogue/shirin_unfinished.png`

```text
Create a wide landscape illustration in exactly the style of the attached image (dark epic fantasy digital painting, painterly, dramatic rim lighting, volumetric light and mist, rich colors against deep shadows).

Scene: Persian gardens at grey dawn, a faint ghostly woman's silhouette among the flowers, a star map in the sky with one dark gap, an empty rise in the foreground center.

Rules: no text, no letters, no logos. Keep the bottom quarter of the image dark and quiet (ground, shadow or mist), because subtitles will go there. Leave an empty, lit spot in the center of the lower third: no character standing there. Landscape format.
```

### 21. `epilogue/rurik_full.png`

```text
Create a wide landscape illustration in exactly the style of the attached image (dark epic fantasy digital painting, painterly, dramatic rim lighting, volumetric light and mist, rich colors against deep shadows).

Scene: Norse village being rebuilt at dawn, new timber longhouses, carts of gold delivered to clan chiefs, smoke from chimneys, an empty rise in the foreground center.

Rules: no text, no letters, no logos. Keep the bottom quarter of the image dark and quiet (ground, shadow or mist), because subtitles will go there. Leave an empty, lit spot in the center of the lower third: no character standing there. Landscape format.
```

### 22. `epilogue/rurik_unfinished.png`

```text
Create a wide landscape illustration in exactly the style of the attached image (dark epic fantasy digital painting, painterly, dramatic rim lighting, volumetric light and mist, rich colors against deep shadows).

Scene: Burned Norse village at grey dawn, ash and charred beams, a lone hall with one candle and a pile of gold inside, an empty rise in the foreground center.

Rules: no text, no letters, no logos. Keep the bottom quarter of the image dark and quiet (ground, shadow or mist), because subtitles will go there. Leave an empty, lit spot in the center of the lower third: no character standing there. Landscape format.
```

### 23. `epilogue/azhar_full.png`

```text
Create a wide landscape illustration in exactly the style of the attached image (dark epic fantasy digital painting, painterly, dramatic rim lighting, volumetric light and mist, rich colors against deep shadows).

Scene: Ottoman throne hall at dawn with an empty throne, a round table of leaders of five different peoples, the sealed glowing book beneath the throne, an empty rise in the foreground center.

Rules: no text, no letters, no logos. Keep the bottom quarter of the image dark and quiet (ground, shadow or mist), because subtitles will go there. Leave an empty, lit spot in the center of the lower third: no character standing there. Landscape format.
```

### 24. `epilogue/azhar_unfinished.png`

```text
Create a wide landscape illustration in exactly the style of the attached image (dark epic fantasy digital painting, painterly, dramatic rim lighting, volumetric light and mist, rich colors against deep shadows).

Scene: Vast empty Ottoman throne hall at grey dawn, empty chairs around a round table, a faint glow from a sealed book beneath the throne, an empty rise in the foreground center.

Rules: no text, no letters, no logos. Keep the bottom quarter of the image dark and quiet (ground, shadow or mist), because subtitles will go there. Leave an empty, lit spot in the center of the lower third: no character standing there. Landscape format.
```

### 25. `crowned/knight.png`

```text
Create a wide landscape illustration in exactly the style of the attached image (dark epic fantasy digital painting, painterly, dramatic rim lighting, volumetric light and mist, rich colors against deep shadows).

Scene: Roman marble city burning under a black eclipse sun with a fiery ring, knights in blue and gold kneeling before an empty dark throne on a dais, melted bronze scales, embers and ash.

Rules: no text, no letters, no logos. Keep the bottom quarter of the image dark and quiet (ground, shadow or mist), because subtitles will go there. Leave an empty, lit spot in the center of the lower third: no character standing there. Landscape format.
```

### 26. `crowned/necromancer.png`

```text
Create a wide landscape illustration in exactly the style of the attached image (dark epic fantasy digital painting, painterly, dramatic rim lighting, volumetric light and mist, rich colors against deep shadows).

Scene: Egyptian necropolis burning under a black eclipse sun, endless rows of mummified dead standing guard with glowing teal eyes before an empty throne on a dais, ash falling.

Rules: no text, no letters, no logos. Keep the bottom quarter of the image dark and quiet (ground, shadow or mist), because subtitles will go there. Leave an empty, lit spot in the center of the lower third: no character standing there. Landscape format.
```

### 27. `crowned/sorceress.png`

```text
Create a wide landscape illustration in exactly the style of the attached image (dark epic fantasy digital painting, painterly, dramatic rim lighting, volumetric light and mist, rich colors against deep shadows).

Scene: Persian palace burning under a black eclipse sun, stars falling burning from the sky, a dreaming court bowing before an empty throne on a dais, violet and fire.

Rules: no text, no letters, no logos. Keep the bottom quarter of the image dark and quiet (ground, shadow or mist), because subtitles will go there. Leave an empty, lit spot in the center of the lower third: no character standing there. Landscape format.
```

### 28. `crowned/thief.png`

```text
Create a wide landscape illustration in exactly the style of the attached image (dark epic fantasy digital painting, painterly, dramatic rim lighting, volumetric light and mist, rich colors against deep shadows).

Scene: Norse mountain hall burning under a black eclipse sun, mountains of gold coins, chained clansmen carrying sacks, an empty throne on a dais, embers.

Rules: no text, no letters, no logos. Keep the bottom quarter of the image dark and quiet (ground, shadow or mist), because subtitles will go there. Leave an empty, lit spot in the center of the lower third: no character standing there. Landscape format.
```

### 29. `crowned/king.png`

```text
Create a wide landscape illustration in exactly the style of the attached image (dark epic fantasy digital painting, painterly, dramatic rim lighting, volumetric light and mist, rich colors against deep shadows).

Scene: Ottoman brass city burning under a black eclipse sun, crowds bowing in ash-covered squares, melted crowns at the foot of an empty golden throne on a dais, the observatory in flames.

Rules: no text, no letters, no logos. Keep the bottom quarter of the image dark and quiet (ground, shadow or mist), because subtitles will go there. Leave an empty, lit spot in the center of the lower third: no character standing there. Landscape format.
```

### 30. `boss/vorath.png`

```text
Create a wide landscape illustration in exactly the style of the attached image (dark epic fantasy digital painting, painterly, dramatic rim lighting, volumetric light and mist, rich colors against deep shadows).

Scene: Colossal demon warlord of wrath with cracked horns and molten armor, red glowing eyes, giant flaming axe, in a burning volcanic citadel full of ghostly endless warriors.

Rules: no text, no letters, no logos. Keep the bottom quarter of the image dark and quiet (ground, shadow or mist), because subtitles will go there. Landscape format.
```

### 31. `boss/luxara.png`

```text
Create a wide landscape illustration in exactly the style of the attached image (dark epic fantasy digital painting, painterly, dramatic rim lighting, volumetric light and mist, rich colors against deep shadows).

Scene: Beautiful terrifying winged sorceress demon of lust in pink and violet silks, bat wings, mirror shards orbiting her, hypnotic pink eyes, in moonlit Persian gardens full of sleepers.

Rules: no text, no letters, no logos. Keep the bottom quarter of the image dark and quiet (ground, shadow or mist), because subtitles will go there. Landscape format.
```

### 32. `boss/morthis.png`

```text
Create a wide landscape illustration in exactly the style of the attached image (dark epic fantasy digital painting, painterly, dramatic rim lighting, volumetric light and mist, rich colors against deep shadows).

Scene: Enormous bloated pharaoh demon of sloth seated in a stagnant swamp, rotting gold mask and wrappings, green glowing eyes, mummies rising from the mud around him.

Rules: no text, no letters, no logos. Keep the bottom quarter of the image dark and quiet (ground, shadow or mist), because subtitles will go there. Landscape format.
```

### 33. `boss/avarok.png`

```text
Create a wide landscape illustration in exactly the style of the attached image (dark epic fantasy digital painting, painterly, dramatic rim lighting, volumetric light and mist, rich colors against deep shadows).

Scene: Huge demon of greed made of gold coins and chains, a gaping sack-like belly, yellow glowing eyes, clawed hands grabbing, in a colossal marble vault overflowing with gold.

Rules: no text, no letters, no logos. Keep the bottom quarter of the image dark and quiet (ground, shadow or mist), because subtitles will go there. Landscape format.
```

### 34. `boss/gularth.png`

```text
Create a wide landscape illustration in exactly the style of the attached image (dark epic fantasy digital painting, painterly, dramatic rim lighting, volumetric light and mist, rich colors against deep shadows).

Scene: Monstrous gluttonous demon with a cleaver and a vast maw, devouring the stone floor, orange glowing eyes, in an underground feast hall of rotten banquet tables.

Rules: no text, no letters, no logos. Keep the bottom quarter of the image dark and quiet (ground, shadow or mist), because subtitles will go there. Landscape format.
```

### 35. `boss/envyris.png`

```text
Create a wide landscape illustration in exactly the style of the attached image (dark epic fantasy digital painting, painterly, dramatic rim lighting, volumetric light and mist, rich colors against deep shadows).

Scene: Gaunt green-eyed demoness of envy on a shadowy throne in ruined Aureum, mirror-like shadow copies of heroes around her, emerald glow, scythe.

Rules: no text, no letters, no logos. Keep the bottom quarter of the image dark and quiet (ground, shadow or mist), because subtitles will go there. Landscape format.
```

### 36. `boss/prython.png`

```text
Create a wide landscape illustration in exactly the style of the attached image (dark epic fantasy digital painting, painterly, dramatic rim lighting, volumetric light and mist, rich colors against deep shadows).

Scene: Radiant fallen angel of pride with golden wings and a cracked halo, a sword of false sunlight, standing atop an upside-down white spire in the sky above a burning Ottoman city.

Rules: no text, no letters, no logos. Keep the bottom quarter of the image dark and quiet (ground, shadow or mist), because subtitles will go there. Landscape format.
```

### 37. `boss/nahrazel.png`

```text
Create a wide landscape illustration in exactly the style of the attached image (dark epic fantasy digital painting, painterly, dramatic rim lighting, volumetric light and mist, rich colors against deep shadows).

Scene: Colossal ash titan, the first fallen devil, seven glowing sin colors in cracks across his body, seated on an inverted throne in a cavern beneath the city, giant open book of light with seven locks before him.

Rules: no text, no letters, no logos. Keep the bottom quarter of the image dark and quiet (ground, shadow or mist), because subtitles will go there. Landscape format.
```

### 38. `boss/kaleth.png`

```text
Create a wide landscape illustration in exactly the style of the attached image (dark epic fantasy digital painting, painterly, dramatic rim lighting, volumetric light and mist, rich colors against deep shadows).

Scene: Corrupted Norse warrior guardian with a burning greatsword, cracked iron armor, torn shackles, in a volcanic forge.

Rules: no text, no letters, no logos. Keep the bottom quarter of the image dark and quiet (ground, shadow or mist), because subtitles will go there. Landscape format.
```

### 39. `boss/serath.png`

```text
Create a wide landscape illustration in exactly the style of the attached image (dark epic fantasy digital painting, painterly, dramatic rim lighting, volumetric light and mist, rich colors against deep shadows).

Scene: Pale blood-drinking warrior maiden in red-stained Norse armor, pools of blood, a snowy arena.

Rules: no text, no letters, no logos. Keep the bottom quarter of the image dark and quiet (ground, shadow or mist), because subtitles will go there. Landscape format.
```

### 40. `boss/mirael.png`

```text
Create a wide landscape illustration in exactly the style of the attached image (dark epic fantasy digital painting, painterly, dramatic rim lighting, volumetric light and mist, rich colors against deep shadows).

Scene: Veiled ghostly whisperer in violet silks half invisible, misty Persian bathhouse with turquoise pools.

Rules: no text, no letters, no logos. Keep the bottom quarter of the image dark and quiet (ground, shadow or mist), because subtitles will go there. Landscape format.
```

### 41. `boss/thessyn.png`

```text
Create a wide landscape illustration in exactly the style of the attached image (dark epic fantasy digital painting, painterly, dramatic rim lighting, volumetric light and mist, rich colors against deep shadows).

Scene: Spider-bodied weaver in silk robes spinning webs across a caravanserai on the Silk Road, poison green glow.

Rules: no text, no letters, no logos. Keep the bottom quarter of the image dark and quiet (ground, shadow or mist), because subtitles will go there. Landscape format.
```

### 42. `boss/dormiel.png`

```text
Create a wide landscape illustration in exactly the style of the attached image (dark epic fantasy digital painting, painterly, dramatic rim lighting, volumetric light and mist, rich colors against deep shadows).

Scene: Sleeping lantern-bearing guardian in Egyptian wrappings, clouds of drowsy dust, nightmare shapes, catacombs.

Rules: no text, no letters, no logos. Keep the bottom quarter of the image dark and quiet (ground, shadow or mist), because subtitles will go there. Landscape format.
```

### 43. `boss/goldarc.png`

```text
Create a wide landscape illustration in exactly the style of the attached image (dark epic fantasy digital painting, painterly, dramatic rim lighting, volumetric light and mist, rich colors against deep shadows).

Scene: Roman guardian in gold-coin armor with a huge golden shield, coins flying, marble treasury.

Rules: no text, no letters, no logos. Keep the bottom quarter of the image dark and quiet (ground, shadow or mist), because subtitles will go there. Landscape format.
```

### 44. `boss/nixara.png`

```text
Create a wide landscape illustration in exactly the style of the attached image (dark epic fantasy digital painting, painterly, dramatic rim lighting, volumetric light and mist, rich colors against deep shadows).

Scene: Hollow masked merchant with an empty coat, false gold and scales, market stalls over trapdoors.

Rules: no text, no letters, no logos. Keep the bottom quarter of the image dark and quiet (ground, shadow or mist), because subtitles will go there. Landscape format.
```

### 45. `boss/fenrath.png`

```text
Create a wide landscape illustration in exactly the style of the attached image (dark epic fantasy digital painting, painterly, dramatic rim lighting, volumetric light and mist, rich colors against deep shadows).

Scene: Wolf-like devourer with a cavernous acid-dripping maw, icy caverns.

Rules: no text, no letters, no logos. Keep the bottom quarter of the image dark and quiet (ground, shadow or mist), because subtitles will go there. Landscape format.
```

### 46. `boss/shadeyn.png`

```text
Create a wide landscape illustration in exactly the style of the attached image (dark epic fantasy digital painting, painterly, dramatic rim lighting, volumetric light and mist, rich colors against deep shadows).

Scene: Gladiator guardian with a shattered mirror blade, mirrored shadow copies, ruined colosseum sand.

Rules: no text, no letters, no logos. Keep the bottom quarter of the image dark and quiet (ground, shadow or mist), because subtitles will go there. Landscape format.
```

### 47. `boss/solrath.png`

```text
Create a wide landscape illustration in exactly the style of the attached image (dark epic fantasy digital painting, painterly, dramatic rim lighting, volumetric light and mist, rich colors against deep shadows).

Scene: False prophet in white and gold robes with a spear of false sunlight, pillars of holy light, an Ottoman temple.

Rules: no text, no letters, no logos. Keep the bottom quarter of the image dark and quiet (ground, shadow or mist), because subtitles will go there. Landscape format.
```

### 48. `empire/sulthari.png`

```text
Create a wide landscape illustration in exactly the style of the attached image (dark epic fantasy digital painting, painterly, dramatic rim lighting, volumetric light and mist, rich colors against deep shadows).

Scene: Ottoman brass city on a plateau behind walls of glowing cyan aetherium, domes and minarets, the Great Observatory, the last free city at dusk.

Rules: no text, no letters, no logos. Keep the bottom quarter of the image dark and quiet (ground, shadow or mist), because subtitles will go there. Landscape format.
```

### 49. `empire/nordrath.png`

```text
Create a wide landscape illustration in exactly the style of the attached image (dark epic fantasy digital painting, painterly, dramatic rim lighting, volumetric light and mist, rich colors against deep shadows).

Scene: Norse tundra and fjords under a red sky, volcanic forges, longhouses and runestones veined with black aetherium.

Rules: no text, no letters, no logos. Keep the bottom quarter of the image dark and quiet (ground, shadow or mist), because subtitles will go there. Landscape format.
```

### 50. `empire/parsivan.png`

```text
Create a wide landscape illustration in exactly the style of the attached image (dark epic fantasy digital painting, painterly, dramatic rim lighting, volumetric light and mist, rich colors against deep shadows).

Scene: Persian hanging gardens and turquoise domes overgrown and dreaming, violet mist, black aetherium cracks.

Rules: no text, no letters, no logos. Keep the bottom quarter of the image dark and quiet (ground, shadow or mist), because subtitles will go there. Landscape format.
```

### 51. `empire/khemet.png`

```text
Create a wide landscape illustration in exactly the style of the attached image (dark epic fantasy digital painting, painterly, dramatic rim lighting, volumetric light and mist, rich colors against deep shadows).

Scene: Egyptian river valley with pyramids and catacombs, a stagnant green marsh, black aetherium veins in the sand.

Rules: no text, no letters, no logos. Keep the bottom quarter of the image dark and quiet (ground, shadow or mist), because subtitles will go there. Landscape format.
```

### 52. `empire/aureum.png`

```text
Create a wide landscape illustration in exactly the style of the attached image (dark epic fantasy digital painting, painterly, dramatic rim lighting, volumetric light and mist, rich colors against deep shadows).

Scene: Roman marble republic in ruins, aqueducts and a colosseum, gold everywhere and violet corruption.

Rules: no text, no letters, no logos. Keep the bottom quarter of the image dark and quiet (ground, shadow or mist), because subtitles will go there. Landscape format.
```

### 53. `map/codex_map.png`

```text
Create a square illustration in exactly the style of the attached image (dark epic fantasy digital painting, painterly, dramatic rim lighting, volumetric light and mist, rich colors against deep shadows).

Scene: Antique illustrated fantasy map on parchment, Sulthari at the center, Nordrath to the north, Parsivan north-east, Khemet south-east, Aureum to the west, a southern sea between Aureum and Khemet, small painted cities, mountains, rivers, no text.

Rules: no text, no letters, no logos. Square format.
```

### 54. `hero/cassian.png`

```text
Create a tall portrait-format illustration in exactly the style of the attached image (dark epic fantasy digital painting, painterly, dramatic rim lighting, volumetric light and mist, rich colors against deep shadows).

Scene: Portrait card of a Roman-Byzantine knight in blue and gold plate with a scale emblem shield, noble scarred face.

Rules: no text, no letters, no logos. Portrait format.
```

### 55. `hero/ankhareth.png`

```text
Create a tall portrait-format illustration in exactly the style of the attached image (dark epic fantasy digital painting, painterly, dramatic rim lighting, volumetric light and mist, rich colors against deep shadows).

Scene: Portrait card of an Egyptian necromancer in turquoise and gold with a jackal mask and floating soul lights.

Rules: no text, no letters, no logos. Portrait format.
```

### 56. `hero/shirin.png`

```text
Create a tall portrait-format illustration in exactly the style of the attached image (dark epic fantasy digital painting, painterly, dramatic rim lighting, volumetric light and mist, rich colors against deep shadows).

Scene: Portrait card of a Persian sorceress in violet and silver with an astrolabe and constellation runes.

Rules: no text, no letters, no logos. Portrait format.
```

### 57. `hero/rurik.png`

```text
Create a tall portrait-format illustration in exactly the style of the attached image (dark epic fantasy digital painting, painterly, dramatic rim lighting, volumetric light and mist, rich colors against deep shadows).

Scene: Portrait card of a Norse rogue with a fur hood, twin axes and a single gold coin.

Rules: no text, no letters, no logos. Portrait format.
```

### 58. `hero/azhar.png`

```text
Create a tall portrait-format illustration in exactly the style of the attached image (dark epic fantasy digital painting, painterly, dramatic rim lighting, volumetric light and mist, rich colors against deep shadows).

Scene: Portrait card of an Ottoman sultan in red and gold with a turban crown and a scepter.

Rules: no text, no letters, no logos. Portrait format.
```
