"""The registration of the bosses of Acts III and IV (the Broken Oaths III to IX and the Archsins Luxara, Morthis,
Avarok, Gularth and Envyris, and Envyris's copies): entity types, attributes, renderers, spawn eggs, names, loot
tables (their materials, a Blueprint chance and their own Relic in each Bearer's Reward Coffer) and the story
advancements. Their code: com.sofe.entity.boss; their models: scripts/make_boss_models.py.

Run from the repository root: python scripts/make_boss_data.py
"""
import json
import os

# id, class, width, height, egg colours, (en, es), material drop, relic, kind ("oath"/"archsin"), advancement parent
BOSSES = [
    ("mirael", "MiraelEntity", 1.0, 3.2, 0x5A2A7A, 0xE070FF, ("Mirael, the Whisperer", "Mirael, la Susurrante"), "sofe:star_lapis_powder", "whisper_of_mirael", "oath", "story/enter_parsivan"),
    ("thessyn", "ThessynEntity", 1.6, 3.2, 0x2A3A30, 0xC0E0B0, ("Thessyn, the Silk Weaver", "Thessyn, la Tejedora de Seda"), "minecraft:string", "thessyns_needle", "oath", "story/enter_parsivan"),
    ("dormiel", "DormielEntity", 1.0, 3.0, 0x2A3050, 0x90C0FF, ("Dormiel, the Dreamer", "Dormiel, el Soñador"), "sofe:solar_gold_nugget", "ring_of_the_long_watch", "oath", "story/enter_khemet"),
    ("luxara", "LuxaraEntity", 1.2, 3.4, 0x7A1A5A, 0xFF80C0, ("Luxara, Archsin of Lust", "Luxara, Archipecado de la Lujuria"), "sofe:star_lapis", "heart_of_luxara", "archsin", "story/enter_parsivan"),
    ("morthis", "MorthisEntity", 1.8, 3.8, 0x3A4A2A, 0x90FF80, ("Morthis, Archsin of Sloth", "Morthis, Archipecado de la Pereza"), "sofe:solar_gold_ingot", "crown_of_the_stagnant_king", "archsin", "story/enter_khemet"),
    ("goldarc", "GoldarcEntity", 1.2, 3.3, 0xC49232, 0x3A2A10, ("Goldarc, the Coinlord", "Goldarc, el Señor de las Monedas"), "minecraft:gold_ingot", "goldarcs_scales", "oath", "story/enter_aureum"),
    ("nixara", "NixaraEntity", 1.0, 3.0, 0x3A3020, 0xE0B040, ("Nixara, the Hollow Merchant", "Nixara, la Mercader Hueca"), "sofe:orichalcum_nugget", "nixaras_false_ring", "oath", "story/enter_aureum"),
    ("avarok", "AvarokEntity", 1.6, 4.0, 0xE0B030, 0x8A1A1A, ("Avarok, Archsin of Greed", "Avarok, Archipecado de la Avaricia"), "sofe:orichalcum_ingot", "hoard_of_avarok", "archsin", "story/enter_aureum"),
    ("fenrath", "FenrathEntity", 2.0, 3.4, 0x4A4038, 0x90FF60, ("Fenrath, the Devourer", "Fenrath, el Devorador"), "sofe:frostpelt", "maw_of_fenrath", "oath", "story/act4"),
    ("gularth", "GularthEntity", 2.0, 3.8, 0x6A4A3A, 0xC04030, ("Gularth, Archsin of Gluttony", "Gularth, Archipecado de la Gula"), "sofe:infernal_ember", "gularths_cleaver", "archsin", "story/act4"),
    ("shadeyn", "ShadeynEntity", 1.1, 3.4, 0xC0C8D8, 0x2A2A3A, ("Shadeyn, the Mirror", "Shadeyn, el Espejo"), "sofe:orichalcum_nugget", "mask_of_shadeyn", "oath", "story/enter_aureum"),
    ("solrath", "SolrathEntity", 1.1, 3.4, 0x4A1A6A, 0xFFD050, ("Solrath, the False Prophet", "Solrath, el Falso Profeta"), "sofe:aetherium_shard", "sun_halo_of_solrath", "oath", "story/act5"),
    ("prython", "PrythonEntity", 1.4, 4.2, 0x2A2010, 0xFFD060, ("Prython, Archsin of Pride", "Prython, Archipecado de la Soberbia"), "sofe:aetherium_shard", "prythons_mirror", "archsin", "story/boss_solrath"),
    ("nahrazel", "NahrazelEntity", 2.6, 6.0, 0x1A0A2A, 0xB050FF, ("Nahrazel, the First Fallen", "Nahrazel, el Primer Caído"), "minecraft:nether_star", "ashbringer_of_the_first_fallen", "archsin", "story/boss_prython"),
    ("envyris", "EnvyrisEntity", 1.2, 3.6, 0x1A3A20, 0x60FF90, ("Envyris, Archsin of Envy", "Envyris, Archipecado de la Envidia"), "sofe:aetherium_shard", "envyris_borrowed_scythe", "archsin", "story/enter_aureum"),
]


def replace_block(path, start, end, block, anchor):
    s = open(path, encoding="utf-8").read()
    if start in s:
        s = s[:s.index(start)] + block + s[s.index(end) + len(end):]
    else:
        assert anchor in s, (path, anchor)
        s = s.replace(anchor, block + "\n" + anchor, 1)
    open(path, "w", encoding="utf-8").write(s)
    return s


def java():
    reg = "src/main/java/com/sofe/registry/EntityRegistry.java"
    lines = ["    // <generated-bosses> by scripts/make_boss_data.py: the bosses of Acts III and IV"]
    for id, cls, w, h, *_ in BOSSES:
        lines.append('    public static final RegistryObject<EntityType<com.sofe.entity.boss.%s>> %s = ENTITIES.register("%s",' % (cls, id.upper(), id))
        lines.append("            () -> EntityType.Builder.of(com.sofe.entity.boss.%s::new, MobCategory.MONSTER).sized(%sf, %sf).fireImmune()"
                     ".clientTrackingRange(10).build(\"%s\"));" % (cls, w, h, id))
    lines.append('    public static final RegistryObject<EntityType<com.sofe.entity.boss.SealGlyph>> SEAL_GLYPH = ENTITIES.register("seal_glyph",')
    lines.append("            () -> EntityType.Builder.of(com.sofe.entity.boss.SealGlyph::new, MobCategory.MISC).sized(1.2f, 1.8f).fireImmune().clientTrackingRange(10).build(\"seal_glyph\"));")
    lines.append('    public static final RegistryObject<EntityType<com.sofe.entity.boss.EnvyCopy>> ENVY_COPY = ENTITIES.register("envy_copy",')
    lines.append("            () -> EntityType.Builder.of(com.sofe.entity.boss.EnvyCopy::new, MobCategory.MONSTER).sized(0.6f, 1.8f).clientTrackingRange(8).build(\"envy_copy\"));")
    lines.append("    // </generated-bosses>")
    s = replace_block(reg, "    // <generated-bosses>", "    // </generated-bosses>", "\n".join(lines), "    // <generated-empire-mobs>")
    attrs = "".join("        event.put(%s.get(), com.sofe.entity.boss.%s.attributes().build());\n" % (id.upper(), cls) for id, cls, *_ in BOSSES)
    attrs += "        event.put(ENVY_COPY.get(), com.sofe.entity.boss.EnvyCopy.attributes().build());\n"
    attrs += "        event.put(SEAL_GLYPH.get(), com.sofe.entity.boss.SealGlyph.attributes().build());\n"
    start, end = "        // <generated-boss-attributes>\n", "        // </generated-boss-attributes>\n"
    block = start + attrs + end
    if start in s:
        s = s[:s.index(start)] + block + s[s.index(end) + len(end):]
    else:
        anchor = "        event.put(VORATH.get(), VorathEntity.attributes().build());\n"
        assert anchor in s
        s = s.replace(anchor, anchor + block, 1)
    open(reg, "w", encoding="utf-8").write(s)

    rend = "src/main/java/com/sofe/client/render/SoFEEntityRenderers.java"
    s = open(rend, encoding="utf-8").read()
    start, end = "        // <generated-boss-renderers>\n", "        // </generated-boss-renderers>\n"
    body = "".join(('        event.registerEntityRenderer(EntityRegistry.NAHRAZEL.get(), ctx -> new GeoMobRenderer<>(ctx, new NahrazelModel(), %sf));\n' % round(w * 0.8, 2))
                   if id == "nahrazel" else
                   ('        event.registerEntityRenderer(EntityRegistry.%s.get(), ctx -> new GeoMobRenderer<>(ctx, "%s", %sf));\n' % (id.upper(), id, round(w * 0.8, 2)))
                   for id, cls, w, *_ in BOSSES)
    body += "        event.registerEntityRenderer(EntityRegistry.SEAL_GLYPH.get(), ctx -> new GeoMobRenderer<>(ctx, \"seal_glyph\", 0.4f));\n"
    body += "        event.registerEntityRenderer(EntityRegistry.ENVY_COPY.get(), EnvyCopyRenderer::new);\n"
    block = start + body + end
    if start in s:
        s = s[:s.index(start)] + block + s[s.index(end) + len(end):]
    else:
        anchor = '        event.registerEntityRenderer(EntityRegistry.VORATH.get(), ctx -> new GeoMobRenderer<>(ctx, "vorath", 1.2f));\n'
        assert anchor in s
        s = s.replace(anchor, anchor + block, 1)
    open(rend, "w", encoding="utf-8").write(s)

    items = "src/main/java/com/sofe/registry/ItemRegistry.java"
    lines = ["    // <generated-boss-eggs>"]
    for id, cls, w, h, c1, c2, *_ in BOSSES:
        lines.append('    public static final RegistryObject<Item> %s_SPAWN_EGG = ITEMS.register("%s_spawn_egg",' % (id.upper(), id))
        lines.append("            () -> new ForgeSpawnEggItem(EntityRegistry.%s, 0x%06X, 0x%06X, new Item.Properties()));" % (id.upper(), c1, c2))
    lines.append("")
    lines.append("    public static List<RegistryObject<Item>> bossEggs() {")
    lines.append("        return List.of(%s);" % ", ".join(id.upper() + "_SPAWN_EGG" for id, *_ in BOSSES))
    lines.append("    }")
    lines.append("    // </generated-boss-eggs>")
    s = replace_block(items, "    // <generated-boss-eggs>", "    // </generated-boss-eggs>", "\n".join(lines), "    // <generated-empire-eggs>")
    old = "        return List.of(VOID_WRETCH_SPAWN_EGG, VOID_STALKER_SPAWN_EGG, BRASS_SENTINEL_SPAWN_EGG,"
    if "bossEggs()" not in s.split("public static List<RegistryObject<Item>> spawnEggs()")[1].split("}")[0]:
        a = s.index("    public static List<RegistryObject<Item>> spawnEggs() {")
        b = s.index("    }", a) + len("    }")
        s = s[:a] + """    public static List<RegistryObject<Item>> spawnEggs() {
        List<RegistryObject<Item>> eggs = new ArrayList<>(List.of(VOID_WRETCH_SPAWN_EGG, VOID_STALKER_SPAWN_EGG, BRASS_SENTINEL_SPAWN_EGG,
                KALETH_SPAWN_EGG, SERATH_SPAWN_EGG, VORATH_SPAWN_EGG, SAND_GHOUL_SPAWN_EGG, CLOCKWORK_SCARAB_SPAWN_EGG, DRAUGR_SPAWN_EGG,
                RIME_WOLF_SPAWN_EGG, MIRAGE_DANCER_SPAWN_EGG, BOG_MUMMY_SPAWN_EGG, GILDED_LEGIONNAIRE_SPAWN_EGG, GLADIATOR_SHADE_SPAWN_EGG));
        eggs.addAll(bossEggs());
        return eggs;
    }""" + s[b:]
    open(items, "w", encoding="utf-8").write(s)


def data():
    lang = {}
    for id, cls, w, h, c1, c2, names, material, relic, kind, parent in BOSSES:
        lang["entity.sofe." + id] = names
        lang["item.sofe.%s_spawn_egg" % id] = ("%s Spawn Egg" % names[0].split(",")[0], "Huevo de %s" % names[1].split(",")[0])
        lang["advancement.sofe.boss_" + id] = (names[0], names[1])
        lang["advancement.sofe.boss_%s.desc" % id] = ("Defeat %s" % names[0].split(",")[0], "Derrota a %s" % names[1].split(",")[0])
        loot = {"type": "minecraft:entity", "pools": [
            {"rolls": 1, "entries": [{"type": "minecraft:item", "name": material, "functions": [
                {"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": 3 if kind == "oath" else 5, "max": 6 if kind == "oath" else 10}}]}]},
            {"rolls": {"type": "minecraft:uniform", "min": 2, "max": 3}, "entries": [
                {"type": "minecraft:item", "name": "minecraft:diamond", "weight": 2},
                {"type": "minecraft:item", "name": "minecraft:emerald", "weight": 3},
                {"type": "minecraft:item", "name": "sofe:dinar", "weight": 4, "functions": [
                    {"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": 10, "max": 30}}]}]},
            {"rolls": 1, "conditions": [{"condition": "minecraft:random_chance", "chance": 0.35 if kind == "oath" else 0.6}],
             "entries": [{"type": "minecraft:item", "name": "sofe:blueprint", "functions": [{"function": "sofe:set_blueprint"}]}]},
            {"rolls": 1, "entries": [{"type": "minecraft:item", "name": "sofe:" + relic, "functions": [{"function": "sofe:make_relic", "relic": relic}]}],
             "sofe_extra": True},
        ]}
        with open("src/main/resources/data/sofe/loot_tables/entities/%s.json" % id, "w", encoding="utf-8") as f:
            f.write(json.dumps(loot, indent=2) + "\n")
        adv = {"display": {"icon": {"item": "sofe:" + relic}, "title": {"translate": "advancement.sofe.boss_" + id},
                           "description": {"translate": "advancement.sofe.boss_%s.desc" % id}, "frame": "goal" if kind == "archsin" else "task",
                           "show_toast": True, "announce_to_chat": True, "hidden": False},
               "criteria": {"done": {"trigger": "minecraft:impossible"}}, "parent": "sofe:" + parent}
        path = "src/main/resources/data/sofe/advancements/story/boss_%s.json" % id
        with open(path, "w", encoding="utf-8") as f:
            f.write(json.dumps(adv, indent=2) + "\n")
    # the story's own steps of Acts III and IV: the act and the regions entered (awarded by QuestEngine and RegionTitleHandler)
    steps = [("act4", "minecraft:gold_ingot", "story/boss_morthis", ("The Western Ruins", "Las Ruinas del Oeste"), ("Begin Act IV", "Comienza el Acto IV")),
             ("act5", "minecraft:end_crystal", "story/boss_envyris", ("The Ascension", "La Ascensión"), ("Begin Act V", "Comienza el Acto V")),
             ("enter_parsivan", "minecraft:pink_petals", "story/act3", ("The Court of Illusions", "La Corte de las Ilusiones"), ("Enter Parsivan", "Entra en Parsivan")),
             ("enter_khemet", "minecraft:sandstone", "story/act3", ("The Valley of the Still", "El Valle de los Inmóviles"), ("Enter Khemet", "Entra en Khemet")),
             ("enter_aureum", "minecraft:quartz_pillar", "story/act4", ("The Marble Republic", "La República de Mármol"), ("Enter Aureum", "Entra en Aureum"))]
    for name, icon, parent, title, desc in steps:
        adv = {"display": {"icon": {"item": icon}, "title": {"translate": "advancement.sofe." + name},
                           "description": {"translate": "advancement.sofe.%s.desc" % name}, "frame": "task",
                           "show_toast": True, "announce_to_chat": True, "hidden": False},
               "criteria": {"done": {"trigger": "minecraft:impossible"}}, "parent": "sofe:" + parent}
        with open("src/main/resources/data/sofe/advancements/story/%s.json" % name, "w", encoding="utf-8") as f:
            f.write(json.dumps(adv, indent=2) + "\n")
        lang["advancement.sofe." + name] = title
        lang["advancement.sofe.%s.desc" % name] = desc
    lang["entity.sofe.envy_copy"] = ("Envious Copy", "Copia Envidiosa")
    lang["entity.sofe.seal_glyph"] = ("Seal of the Codex", "Sello del Códice")
    lang["entity.sofe.envy_copy.named"] = ("Envious %s", "%s Envidioso")
    msgs = {
        "message.sofe.mirael.whisper": ("Mirael whispers... the world tilts", "Mirael susurra... el mundo se inclina"),
        "message.sofe.thessyn.chests": ("Chests appear in the court. Trust none of them", "Aparecen cofres en el patio. No confíes en ninguno"),
        "message.sofe.thessyn.lie": ("A lie of silk: spiders and venom!", "Una mentira de seda: ¡arañas y veneno!"),
        "message.sofe.dormiel.slumber": ("Your eyes grow heavy...", "Los párpados te pesan..."),
        "message.sofe.goldarc.ward": ("Goldarc raises a ward of gold", "Goldarc alza un escudo de oro"),
        "message.sofe.nixara.floor": ("The floor of the forum gives way!", "¡El suelo del foro se abre!"),
        "message.sofe.nixara.false_gold": ("False gold: it bursts into venom", "Oro falso: estalla en veneno"),
        "message.sofe.fenrath.swallowed": ("Fenrath swallows you! Your allies must strike him", "¡Fenrath te traga! Tus aliados deben golpearlo"),
        "message.sofe.shadeyn.covets": ("Shadeyn covets what you wear", "Shadeyn codicia lo que llevas"),
        "message.sofe.shadeyn.reflection": ("Reflection of %s", "Reflejo de %s"),
        "message.sofe.luxara.charmed": ("Charmed: you walk to her, and cannot raise a hand against your own", "Encantado: caminas hacia ella y no puedes alzar la mano contra los tuyos"),
        "message.sofe.luxara.mirrors": ("Luxara's mirrors rise: blows come back", "Se alzan los espejos de Luxara: los golpes regresan"),
        "message.sofe.morthis.risen": ("The marsh gives up its dead for Morthis", "El pantano entrega a sus muertos a Morthis"),
        "message.sofe.avarok.stole": ("Avarok took your %s", "Avarok tomó tu %s"),
        "message.sofe.avarok.returned": ("What Avarok stole is yours again", "Lo que Avarok robó vuelve a ser tuyo"),
        "message.sofe.gularth.devours": ("Gularth devours the floor beneath you!", "¡Gularth devora el suelo bajo tus pies!"),
        "message.sofe.envyris.copy": ("Envyris raises a copy of %s", "Envyris alza una copia de %s"),
        "message.sofe.envyris.echo": ("Envyris answers with your %s", "Envyris responde con tu %s"),
        "message.sofe.solrath.raises": ("Solrath raises %s from the dead", "Solrath alza a %s de entre los muertos"),
        "message.sofe.nahrazel.raises": ("Nahrazel calls %s back from the Codex", "Nahrazel llama a %s de vuelta desde el Códice"),
        "message.sofe.nahrazel.sins": ("The seven sins wake in him at once", "Los siete pecados despiertan en él a la vez"),
        "message.sofe.nahrazel.codex": ("You are inside the Codex. Break the seven Seals and rewrite it", "Estás dentro del Códice. Rompe los siete Sellos y reescríbelo"),
        "message.sofe.nahrazel.seal": ("Seal rewritten: %s of %s", "Sello reescrito: %s de %s"),
        "message.sofe.nahrazel.rewritten": ("The seal is rewritten (%s of %s): strike him now!", "El sello está reescrito (%s de %s): ¡golpéalo ahora!"),
        "message.sofe.nahrazel.fallen": ("The First Fallen falls. The Codex closes.", "El Primer Caído cae. El Códice se cierra."),
        "message.sofe.sin_power.wrath": ("Wrath: a ring of fire!", "Ira: ¡un anillo de fuego!"),
        "message.sofe.sin_power.lust": ("Lust: you are drawn to him", "Lujuria: te ves atraído hacia él"),
        "message.sofe.sin_power.sloth": ("Sloth: your limbs grow heavy", "Pereza: tus miembros se vuelven pesados"),
        "message.sofe.sin_power.greed": ("Greed: a rain of coins", "Avaricia: una lluvia de monedas"),
        "message.sofe.sin_power.gluttony": ("Gluttony: the floor is devoured", "Gula: el suelo es devorado"),
        "message.sofe.sin_power.envy": ("Envy: a copy of you rises", "Envidia: se alza una copia de ti"),
        "message.sofe.sin_power.pride": ("Pride", "Soberbia"),
    }
    lang.update(msgs)
    for f, i in (("en_us", 0), ("es_es", 1)):
        path = "src/main/resources/assets/sofe/lang/%s.json" % f
        d = json.load(open(path, encoding="utf-8"))
        for k, v in lang.items():
            d[k] = v[i]
        open(path, "w", encoding="utf-8").write(json.dumps(d, ensure_ascii=False, indent=2) + "\n")


if __name__ == "__main__":
    java()
    data()
    print("bosses: %d" % len(BOSSES))
