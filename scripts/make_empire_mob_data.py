"""The registration of the empires' creatures (EmpireMob): their entity types, spawn eggs and renderers, where
each spawns (a Forge biome modifier per creature, in its own region's biomes only, so each act has its own
enemies), their loot and their names. Their models: scripts/make_empire_mobs.py.

Run from the repository root: python scripts/make_empire_mob_data.py
"""
import json
import os

# kind, width, height, egg colors, region biomes, weight, group, (en, es)
MOBS = [
    ("sand_ghoul", 0.6, 1.9, 0xA8875A, 0xFFAA28, ["sulthari_desert", "ashen_wastes"], 70, 1, 3, ("Sand Ghoul", "Gul de Arena")),
    ("clockwork_scarab", 1.0, 0.8, 0xB07C30, 0x3CD2C8, ["sulthari_desert", "ashen_wastes"], 50, 3, 5, ("Clockwork Scarab", "Escarabajo de Relojería")),
    ("draugr", 0.8, 2.3, 0x4E5A64, 0x96EBFF, ["nordrath_tundra", "nordrath_ice_fields", "nordrath_volcanic_forges"], 70, 1, 3, ("Draugr", "Draugr")),
    ("rime_wolf", 1.1, 1.2, 0xCEDEE8, 0x78C8F0, ["nordrath_tundra", "nordrath_ice_fields"], 60, 2, 4, ("Rime Wolf", "Lobo de Escarcha")),
    ("mirage_dancer", 0.6, 1.9, 0x78329A, 0xFF78DC, ["parsivan_gardens"], 70, 1, 2, ("Mirage Dancer", "Bailarina Espejismo")),
    ("bog_mummy", 0.8, 2.3, 0x625638, 0x8CFF78, ["khemet_valley"], 80, 1, 3, ("Bog Mummy", "Momia del Pantano")),
    ("gilded_legionnaire", 0.7, 2.05, 0xC49232, 0x8C1A1E, ["aureum_hills"], 70, 2, 3, ("Gilded Legionnaire", "Legionario Dorado")),
    ("gladiator_shade", 0.6, 1.9, 0x28202E, 0xD2C8FF, ["aureum_hills"], 50, 1, 2, ("Gladiator Shade", "Sombra de Gladiador")),
]

# --- EntityRegistry: types, attributes, spawn placements
p = "src/main/java/com/sofe/registry/EntityRegistry.java"
s = open(p, encoding="utf-8").read()
lines = ["    // <generated-empire-mobs> the creatures of each empire (EmpireMob, scripts/make_empire_mobs.py)"]
for k, w, h, *_ in MOBS:
    K = k.upper()
    lines.append('    public static final RegistryObject<EntityType<com.sofe.entity.empire.EmpireMob>> %s = ENTITIES.register("%s",' % (K, k))
    lines.append("            () -> EntityType.Builder.<com.sofe.entity.empire.EmpireMob>of((t, l) -> new com.sofe.entity.empire.EmpireMob(t, l, "
                 "com.sofe.entity.empire.EmpireMob.Kind.%s), MobCategory.MONSTER).sized(%sf, %sf).clientTrackingRange(8).build(\"%s\"));" % (K, w, h, k))
lines.append("")
lines.append("    public static java.util.List<RegistryObject<EntityType<com.sofe.entity.empire.EmpireMob>>> empireMobs() {")
lines.append("        return java.util.List.of(%s);" % ", ".join(k.upper() for k, *_ in MOBS))
lines.append("    }")
lines.append("    // </generated-empire-mobs>")
block = "\n".join(lines)
start, end = "    // <generated-empire-mobs>", "    // </generated-empire-mobs>"
if start in s:
    s = s[:s.index(start)] + block + s[s.index(end) + len(end):]
else:
    anchor = "    private EntityRegistry() {"
    s = s.replace(anchor, block + "\n\n" + anchor, 1)
    a = "        event.put(COMPANION.get(), com.sofe.companion.CompanionEntity.createAttributes().build());\n"
    assert a in s
    s = s.replace(a, a + "        for (var mob : empireMobs()) event.put(mob.get(), com.sofe.entity.empire.EmpireMob.Kind.valueOf(mob.getId().getPath().toUpperCase(java.util.Locale.ROOT)).attributes().build());\n")
    a = """        event.register(VOID_STALKER.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                VoidCreature::checkSpawnRules, SpawnPlacementRegisterEvent.Operation.REPLACE);
"""
    assert a in s
    s = s.replace(a, a + """        for (var mob : empireMobs()) {
            event.register(mob.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    net.minecraft.world.entity.monster.Monster::checkMonsterSpawnRules, SpawnPlacementRegisterEvent.Operation.REPLACE);
        }
""")
open(p, "w", encoding="utf-8").write(s)

# --- renderers
p = "src/main/java/com/sofe/client/render/SoFEEntityRenderers.java"
s = open(p, encoding="utf-8").read()
a = """        event.registerEntityRenderer(EntityRegistry.VOID_WRETCH.get(), ctx -> new GeoMobRenderer<>(ctx, "void_wretch", 0.5f));"""
if "empireMobs()" not in s:  # only the first time: later runs find the loop already there
    s = s.replace(a, a + """
        for (var mob : EntityRegistry.empireMobs()) {
            String model = mob.getId().getPath();
            event.registerEntityRenderer(mob.get(), ctx -> new GeoMobRenderer<>(ctx, model, model.equals("clockwork_scarab") ? 0.3f : 0.5f));
        }""")
open(p, "w", encoding="utf-8").write(s)

# --- spawn eggs
p = "src/main/java/com/sofe/registry/ItemRegistry.java"
s = open(p, encoding="utf-8").read()
lines = ["    // <generated-empire-eggs>"]
for k, w, h, c1, c2, *_ in MOBS:
    lines.append('    public static final RegistryObject<Item> %s_SPAWN_EGG = ITEMS.register("%s_spawn_egg",' % (k.upper(), k))
    lines.append("            () -> new ForgeSpawnEggItem(EntityRegistry.%s, 0x%06X, 0x%06X, new Item.Properties()));" % (k.upper(), c1, c2))
lines.append("    // </generated-empire-eggs>")
block = "\n".join(lines)
start, end = "    // <generated-empire-eggs>", "    // </generated-empire-eggs>"
if start in s:
    s = s[:s.index(start)] + block + s[s.index(end) + len(end):]
else:
    anchor = "    public static final RegistryObject<Item> VOID_WRETCH_SPAWN_EGG"
    s = s.replace(anchor, block + "\n" + anchor, 1)
    a = """                KALETH_SPAWN_EGG, SERATH_SPAWN_EGG, VORATH_SPAWN_EGG);"""
    assert a in s
    s = s.replace(a, """                KALETH_SPAWN_EGG, SERATH_SPAWN_EGG, VORATH_SPAWN_EGG, %s);""" % ", ".join(k.upper() + "_SPAWN_EGG" for k, *_ in MOBS))
open(p, "w", encoding="utf-8").write(s)

# --- spawns: one biome modifier per creature, in its own region's biomes only
folder = "src/main/resources/data/sofe/forge/biome_modifier"
os.makedirs(folder, exist_ok=True)
for k, w, h, c1, c2, biomes, weight, lo, hi, _ in MOBS:
    data = {"type": "forge:add_spawns", "biomes": ["sofe:" + b for b in biomes],
            "spawners": [{"type": "sofe:" + k, "weight": weight, "minCount": lo, "maxCount": hi}]}
    open(os.path.join(folder, "spawn_" + k + ".json"), "w", encoding="utf-8").write(json.dumps(data, indent=2) + "\n")

# --- names
L = {}
for k, *_, names in MOBS:
    L["entity.sofe." + k] = names
    L["item.sofe.%s_spawn_egg" % k] = ("%s Spawn Egg" % names[0], "Huevo de %s" % names[1])
for f, i in (("en_us", 0), ("es_es", 1)):
    path = f"src/main/resources/assets/sofe/lang/{f}.json"
    d = json.load(open(path, encoding="utf-8"))
    for key, v in L.items():
        d[key] = v[i]
    open(path, "w", encoding="utf-8").write(json.dumps(d, ensure_ascii=False, indent=2) + "\n")
print("ok")
