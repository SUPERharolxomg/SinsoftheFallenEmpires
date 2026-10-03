"""The uniques of batch 4: class uniques (22 per Bearer, usable only by that class, as Diablo II class items),
uniques for any class (charms, jewelry, weapons, armor) and the Grand Talisman. scripts/make_uniques.py turns
this into items, Relic data, names, effect texts, sprites and 3D armor models.

Every unique: id, kind, class (None for anyone), level, look, affixes, effects, (en name, es name).
Kinds: weapon (base: which weapon it is built on), armor (piece), ring, amulet, charm.
Effects: H(...) on hit (weapon in hand), K(...) on kill, U(...) when hurt, W(...) while worn.
"""


def H(do, **kw):
    return dict(on="hit", do=do, **kw)


def K(do, **kw):
    return dict(on="kill", do=do, **kw)


def U(do, **kw):
    return dict(on="hurt", do=do, **kw)


def W(do, **kw):
    return dict(on="worn", do=do, **kw)


def a(stat, value, parameter=None):
    d = {"stat": stat, "value": value}
    if parameter:
        d["parameter"] = parameter
    return d


def ranks(cls, n=1):
    return a("class_skill_ranks", n, cls)


def skill(id, n=2):
    return a("skill_ranks", n, id)


def weapon(id, cls, level, base, hue, affixes, effects, names, glow=None):
    return dict(id=id, kind="weapon", cls=cls, level=level, base=base, hue=hue, glow=glow, affixes=affixes, effects=effects, names=names)


def armor(id, cls, level, piece, style, pal, head, affixes, effects, names):
    return dict(id=id, kind="armor", cls=cls, level=level, piece=piece, style=style, pal=pal, head=head, affixes=affixes, effects=effects, names=names)


def jewel(id, kind, cls, level, metal, stone, affixes, effects, names, pendant=None):
    return dict(id=id, kind=kind, cls=cls, level=level, metal=metal, stone=stone, pendant=pendant, affixes=affixes, effects=effects, names=names)


def charm(id, cls, level, size, shape, color, affixes, effects, names):
    return dict(id=id, kind="charm", cls=cls, level=level, size=size, shape=shape, color=color, affixes=affixes, effects=effects, names=names)


GOLD, SILVER, BRONZE, OBSIDIAN, ICE, BONE_M = "gold", "silver", "bronze", "obsidian", "ice", "bone"
RUBY, SAPPHIRE, EMERALD, TOPAZ, AMETHYST, TURQUOISE, DIAMOND, ONYX = ((230, 40, 50), (50, 90, 230), (60, 200, 90), (250, 200, 60),
                                                                     (170, 80, 240), (50, 210, 200), (235, 245, 255), (40, 30, 50))

UNIQUES = [
    # ======================================================================================= the Knight
    weapon("sunder_of_the_order", "knight", 6, "oathblade", 0.12, [a("strength", 6), a("physical_damage", 12), ranks("knight")],
           [H("vs_undead", mult=1.6), H("lifesteal", frac=0.05)], ("Sunder of the Order", "Hendedora de la Orden")),
    weapon("verdict_of_cassian", "knight", 12, "scale_blade", 0.14, [a("strength", 9), skill("verdict", 2), a("crit_chance", 5)],
           [H("stun", chance=0.15, s=1.0)], ("Cassian's Verdict", "Veredicto de Cassian")),
    weapon("broken_banner_lance", "knight", 16, "lance_of_the_scale", 0.6, [a("strength", 10), skill("legionary_charge", 2), a("physical_damage", 18)],
           [H("knockup", chance=0.2, v=0.6), H("bleed", s=3)], ("Lance of the Broken Banner", "Lanza del Estandarte Roto")),
    weapon("anvil_of_aureum", "knight", 20, "justicar_maul", 0.08, [a("strength", 12), a("armor", 6), ranks("knight")],
           [H("stun", chance=0.2, s=1.5), H("chain", targets=2, frac=0.4)], ("Anvil of Aureum", "Yunque de Aureum")),
    weapon("oathkeeper", "knight", 24, "battle_greatsword", 0.55, [a("strength", 14), a("max_health", 10), skill("last_one_standing", 1)],
           [H("lifesteal", frac=0.08), K("heal", amount=3)], ("Oathkeeper", "Guardajuramentos")),
    weapon("wrath_unbound", "knight", 28, "bearded_axe", 0.0, [a("strength", 16), a("fire_damage", 8), a("crit_damage", 25)],
           [H("ignite", s=4), K("explode", damage=8, radius=3)], ("Wrath Unbound", "Ira Desatada")),
    armor("helm_of_the_last_knight", "knight", 8, "helmet", "heavy", {"plate": (200, 205, 215), "trim": (40, 70, 170), "gem": (90, 140, 255), "cloth2": (40, 70, 170)},
          "great_plume", [a("vitality", 6), skill("shield_wall", 2)], [U("reduce", frac=0.06)], ("Helm of the Last Knight", "Yelmo del Último Caballero")),
    armor("crown_of_the_scale", "knight", 22, "helmet", "heavy", {"plate": (232, 186, 60), "trim": (255, 236, 160), "gem": (40, 90, 210)},
          "crown_helm", [ranks("knight"), a("all_attributes", 4)], [W("absorption", every_s=25)], ("Crown of the Scale", "Corona de la Balanza")),
    armor("bulwark_cuirass", "knight", 10, "chestplate", "heavy", {"plate": (120, 126, 140), "trim": (180, 140, 60), "gem": (220, 40, 40)},
          "great", [a("armor", 8), skill("living_rampart", 2)], [U("thorns", frac=0.15)], ("Bulwark Cuirass", "Coraza del Baluarte")),
    armor("heart_of_the_legion", "knight", 26, "chestplate", "heavy", {"plate": (180, 30, 30), "trim": (240, 200, 90), "gem": (255, 230, 120), "cloth": (150, 20, 20)},
          "plume", [a("strength", 12), a("max_health", 14)], [W("effect", effect="minecraft:strength", when="low_health"), U("knockback", v=0.8)],
          ("Heart of the Legion", "Corazón de la Legión")),
    armor("greaves_of_the_vigil", "knight", 12, "leggings", "heavy", {"plate": (150, 156, 170), "trim": (90, 60, 30)}, "great",
          [a("vitality", 8), a("frost_resistance", 20)], [W("effect", effect="minecraft:resistance", when="crouching")], ("Greaves of the Vigil", "Grebas de la Vigilia")),
    armor("legs_of_the_unyielding", "knight", 25, "leggings", "heavy", {"plate": (60, 64, 80), "trim": (200, 40, 40)}, "great",
          [a("armor", 6), a("strength", 8)], [U("reduce", frac=0.1)], ("Legs of the Unyielding", "Piernas del Inquebrantable")),
    armor("march_of_the_paladin", "knight", 9, "boots", "heavy", {"plate": (220, 220, 230), "trim": (232, 186, 70)}, "great",
          [a("agility", 5), skill("legionary_charge", 1)], [W("effect", effect="minecraft:speed")], ("March of the Paladin", "Marcha del Paladín")),
    armor("iron_roots", "knight", 23, "boots", "heavy", {"plate": (80, 70, 60), "trim": (60, 150, 80)}, "great",
          [a("vitality", 10), a("armor", 5)], [W("effect", effect="minecraft:resistance")], ("Iron Roots", "Raíces de Hierro")),
    jewel("signet_of_the_order", "ring", "knight", 7, SILVER, SAPPHIRE, [a("strength", 5), skill("banner_cry", 2)], [K("resource", amount=8)],
          ("Signet of the Order", "Sello de la Orden")),
    jewel("band_of_unbroken_oaths", "ring", "knight", 19, GOLD, RUBY, [ranks("knight"), a("life_steal", 4)], [H("lifesteal", frac=0.05)],
          ("Band of Unbroken Oaths", "Sortija de los Juramentos Intactos")),
    jewel("medal_of_the_scale", "amulet", "knight", 11, GOLD, DIAMOND, [a("vitality", 8), skill("protectors_oath", 2)], [W("heal", every_s=6, amount=1)],
          ("Medal of the Scale", "Medalla de la Balanza"), pendant="scale"),
    jewel("tear_of_aureum", "amulet", "knight", 27, SILVER, SAPPHIRE, [ranks("knight", 2), a("all_attributes", 5)], [U("cheat_death", cooldown_s=240)],
          ("Tear of Aureum", "Lágrima de Aureum"), pendant="drop"),
    charm("knight_shield_charm", "knight", 5, "small", "shield", (90, 140, 230), [a("armor", 3)], [U("reduce", frac=0.03)], ("Charm of the Shieldbearer", "Talismán del Escudero")),
    charm("knight_banner_charm", "knight", 13, "large", "banner", (200, 40, 40), [skill("banner_cry", 1), a("strength", 4)], [], ("Charm of the Banner", "Talismán del Estandarte")),
    charm("knight_wrath_charm", "knight", 18, "large", "flame", (255, 120, 40), [skill("contained_wrath", 2)], [K("effect", effect="minecraft:strength", s=4)],
          ("Charm of Held Wrath", "Talismán de la Ira Contenida")),
    charm("knight_grand_charm", "knight", 26, "grand", "scale", (232, 186, 70), [ranks("knight"), a("max_health", 10)], [], ("Grand Charm of the Scale", "Gran Talismán de la Balanza")),

    # ======================================================================================= the Necromancer
    weapon("wand_of_the_ferryman", "necromancer", 5, "bone_wand", 0.5, [a("will", 6), skill("raise_the_embalmed", 2)], [K("souls", n=1, chance=0.3)],
           ("Wand of the Ferryman", "Varita del Barquero")),
    weapon("thoth_reed", "necromancer", 11, "soul_wand", 0.45, [a("will", 8), skill("threshold_touch", 2), a("magic_damage", 12)], [H("lifesteal", frac=0.08)],
           ("Reed of Thoth", "Caña de Thoth")),
    weapon("staff_of_the_nine_gates", "necromancer", 17, "soul_staff", 0.47, [a("will", 10), skill("the_great_judgment", 1), a("max_resource", 20)],
           [K("raise", s=12, health=18, damage=4, chance=0.25)], ("Staff of the Nine Gates", "Báculo de las Nueve Puertas")),
    weapon("sickle_of_morthis", "necromancer", 21, "reaper_sickle", 0.75, [a("will", 10), a("life_steal", 6)], [H("wither", s=3), H("execute", below=0.12)],
           ("Sickle of Morthis", "Hoz de Morthis")),
    weapon("khopesh_of_the_embalmer", "necromancer", 9, "khopesh", 0.1, [a("will", 7), skill("burial_wraps", 2)], [H("slow", s=2, lvl=1), H("soul", n=1, chance=0.1)],
           ("Khopesh of the Embalmer", "Khopesh del Embalsamador")),
    weapon("anubets_judgement", "necromancer", 27, "jackal_glaive", 0.5, [ranks("necromancer", 2), a("magic_damage", 20)], [H("execute", below=0.2), K("souls", n=1)],
           ("Anubet's Final Judgement", "Juicio Final de Anubet")),
    armor("mask_of_the_ferryman", "necromancer", 7, "helmet", "robe", {"cloth": (30, 32, 44), "trim": (232, 186, 70), "dark": (24, 24, 34), "gem": (60, 230, 210)},
          "jackal", [a("will", 6), skill("clay_warden", 2)], [W("reveal", radius=12)], ("Mask of the Ferryman", "Máscara del Barquero")),
    armor("crown_of_the_pale_king", "necromancer", 24, "helmet", "robe", {"cloth": (60, 30, 90), "bone": (220, 214, 196), "gem": (110, 250, 230), "dark": (20, 10, 30)},
          "lich", [ranks("necromancer"), a("max_resource", 25)], [W("resource", per_s=1)], ("Crown of the Pale King", "Corona del Rey Pálido")),
    armor("shroud_of_khemet", "necromancer", 11, "chestplate", "robe", {"cloth": (206, 196, 168), "trim": (30, 150, 140), "gem": (50, 220, 200)},
          "wraps", [a("vitality", 8), skill("canopic_jars", 2)], [K("heal", amount=2)], ("Shroud of Khemet", "Sudario de Khemet")),
    armor("ribcage_of_the_lich", "necromancer", 26, "chestplate", "robe", {"cloth": (40, 18, 60), "bone": (226, 214, 186), "gem": (120, 255, 240), "trim": (200, 196, 180)},
          "lich", [a("will", 12), skill("rite_of_passage", 2)], [U("wither", s=3)], ("Ribcage of the Lich", "Costillar del Liche")),
    armor("wraps_of_the_long_sleep", "necromancer", 13, "leggings", "robe", {"cloth": (180, 170, 140), "trim": (40, 40, 50)}, "wraps",
          [a("vitality", 8), a("void_resistance", 20)], [W("cleanse", effects=["minecraft:slowness", "minecraft:weakness"])], ("Wraps of the Long Sleep", "Vendas del Largo Sueño")),
    armor("grave_dust_trousers", "necromancer", 22, "leggings", "robe", {"cloth": (70, 64, 60), "bone": (214, 204, 178), "gem": (110, 240, 220)}, "skull",
          [a("will", 9), skill("raise_the_embalmed", 2)], [K("souls", n=1, chance=0.2)], ("Grave Dust Trousers", "Calzas de Polvo de Tumba")),
    armor("sandals_of_the_threshold", "necromancer", 8, "boots", "robe", {"cloth": (150, 120, 70), "trim": (232, 186, 70)}, "wraps",
          [a("agility", 5), a("max_resource", 10)], [W("effect", effect="minecraft:speed", when="night")], ("Sandals of the Threshold", "Sandalias del Umbral")),
    armor("treads_of_the_underworld", "necromancer", 25, "boots", "robe", {"cloth": (30, 20, 40), "gem": (110, 250, 230), "dark": (10, 6, 16)}, "lich",
          [a("will", 8), a("dodge", 5)], [W("effect", effect="minecraft:invisibility", when="crouching")], ("Treads of the Underworld", "Pasos del Inframundo")),
    jewel("ring_of_bound_souls", "ring", "necromancer", 6, OBSIDIAN, TURQUOISE, [a("will", 5), skill("clay_warden", 1)], [K("souls", n=1, chance=0.15)],
          ("Ring of Bound Souls", "Anillo de las Almas Atadas")),
    jewel("seal_of_the_house_of_thresholds", "ring", "necromancer", 18, GOLD, ONYX, [ranks("necromancer"), a("magic_damage", 10)], [W("resource", per_s=0.5)],
          ("Seal of the House of Thresholds", "Sello de la Casa de los Umbrales")),
    jewel("heart_scarab", "amulet", "necromancer", 12, GOLD, TURQUOISE, [a("vitality", 8), skill("scarab_plague", 2)], [U("cheat_death", cooldown_s=300)],
          ("Heart Scarab", "Escarabajo del Corazón"), pendant="scarab"),
    jewel("eye_of_morthis", "amulet", "necromancer", 28, OBSIDIAN, AMETHYST, [ranks("necromancer", 2), a("will", 10)], [W("reveal", radius=18)],
          ("Eye of Morthis", "Ojo de Morthis"), pendant="eye"),
    charm("necro_scarab_charm", "necromancer", 5, "small", "scarab", (50, 210, 200), [a("will", 3)], [K("souls", n=1, chance=0.05)], ("Charm of the Scarab", "Talismán del Escarabajo")),
    charm("necro_ankh_charm", "necromancer", 12, "large", "ankh", (232, 186, 70), [skill("raise_the_embalmed", 1), a("max_resource", 8)], [], ("Charm of the Ankh", "Talismán del Ankh")),
    charm("necro_jar_charm", "necromancer", 19, "large", "jar", (180, 120, 70), [skill("canopic_jars", 2)], [K("heal", amount=1)], ("Charm of the Canopic Jar", "Talismán del Vaso Canopo")),
    charm("necro_grand_charm", "necromancer", 26, "grand", "skull", (110, 240, 220), [ranks("necromancer"), a("will", 6)], [], ("Grand Charm of Thresholds", "Gran Talismán de los Umbrales")),

    # ======================================================================================= the Sorceress
    weapon("laleh_lantern", "sorceress", 6, "ember_orb", 0.05, [a("intellect", 6), skill("ember_verse", 2)], [H("ignite", s=4)], ("Laleh's Lantern", "Linterna de Laleh")),
    weapon("tear_of_the_moon", "sorceress", 10, "frost_orb", 0.58, [a("intellect", 8), skill("frost_lance", 2), a("frost_damage", 4)], [H("freeze", chance=0.15, s=2)],
           ("Tear of the Moon", "Lágrima de la Luna")),
    weapon("astrolabe_of_shirin", "sorceress", 16, "storm_orb", 0.15, [a("intellect", 10), skill("wandering_spark", 2), a("cooldown_reduction", 8)],
           [H("chain", targets=2, frac=0.5)], ("Astrolabe of Shirin", "Astrolabio de Shirin")),
    weapon("pen_of_the_poet_king", "sorceress", 20, "ember_staff", 0.9, [a("intellect", 12), skill("burning_calligraphy", 2)], [K("resource", amount=12)],
           ("Pen of the Poet King", "Pluma del Rey Poeta")),
    weapon("staff_of_winter_gardens", "sorceress", 24, "frost_staff", 0.48, [a("intellect", 13), skill("water_mirror", 2), a("frost_resistance", 25)],
           [H("slow", s=3, lvl=2), W("aura", damage=1, radius=4, kind="frost")], ("Staff of the Winter Gardens", "Báculo de los Jardines de Invierno")),
    weapon("skyfall", "sorceress", 28, "storm_staff", 0.72, [ranks("sorceress", 2), a("magic_damage", 22)], [H("lightning", chance=0.15, mult=1.8)],
           ("Skyfall", "Caída del Cielo")),
    armor("veil_of_laleh", "sorceress", 7, "helmet", "robe", {"cloth": (40, 170, 170), "cloth2": (230, 200, 230), "trim": (236, 190, 80), "gem": (240, 80, 160)},
          "veil", [a("intellect", 6), skill("petal_tempest", 2)], [W("effect", effect="minecraft:night_vision")], ("Veil of Laleh", "Velo de Laleh")),
    armor("hat_of_the_circle", "sorceress", 22, "helmet", "robe", {"cloth": (30, 40, 120), "trim": (240, 210, 110), "gem": (255, 240, 160)},
          "wizard", [ranks("sorceress"), a("max_resource", 25)], [W("resource", per_s=1)], ("Hat of the Circle of the Astrolabe", "Sombrero del Círculo del Astrolabio")),
    armor("robe_of_written_stars", "sorceress", 12, "chestplate", "robe", {"cloth": (64, 42, 136), "trim": (226, 186, 80), "gem": (120, 230, 255)},
          "wizard", [a("intellect", 9), skill("starfall", 2)], [K("resource", amount=6)], ("Robe of Written Stars", "Túnica de las Estrellas Escritas")),
    armor("mantle_of_the_eclipse", "sorceress", 27, "chestplate", "robe", {"cloth": (20, 14, 30), "trim": (200, 160, 255), "gem": (230, 160, 255)},
          "witch", [a("intellect", 14), skill("written_eclipse", 1)], [U("slow", s=3, lvl=1)], ("Mantle of the Eclipse", "Manto del Eclipse")),
    armor("silks_of_parsivan", "sorceress", 9, "leggings", "robe", {"cloth": (170, 40, 90), "trim": (240, 210, 120)}, "veil",
          [a("agility", 5), a("dodge", 4)], [W("effect", effect="minecraft:slow_falling", when="always")], ("Silks of Parsivan", "Sedas de Parsivan")),
    armor("leggings_of_the_storm_court", "sorceress", 21, "leggings", "robe", {"cloth": (60, 70, 100), "trim": (255, 230, 90)}, "wizard",
          [a("intellect", 9), a("storm_resistance", 25)], [U("slow", s=2, lvl=1)], ("Leggings of the Storm Court", "Calzas de la Corte de la Tormenta")),
    armor("slippers_of_the_waterline", "sorceress", 8, "boots", "robe", {"cloth": (60, 140, 200), "trim": (220, 240, 255)}, "veil",
          [a("agility", 4), skill("water_mirror", 1)], [W("effect", effect="minecraft:dolphins_grace", when="water")], ("Slippers of the Waterline", "Zapatillas de la Línea del Agua")),
    armor("steps_of_the_comet", "sorceress", 25, "boots", "robe", {"cloth": (240, 230, 220), "trim": (255, 170, 60), "gem": (255, 220, 90)}, "witch",
          [a("intellect", 8), a("cooldown_reduction", 6)], [W("effect", effect="minecraft:speed", amp=1)], ("Steps of the Comet", "Pasos del Cometa")),
    jewel("ring_of_three_runes", "ring", "sorceress", 6, SILVER, AMETHYST, [a("intellect", 5), skill("sky_map", 2)], [K("resource", amount=5)],
          ("Ring of Three Runes", "Anillo de las Tres Runas")),
    jewel("band_of_the_constellation", "ring", "sorceress", 19, GOLD, SAPPHIRE, [ranks("sorceress"), a("magic_damage", 10)], [], ("Band of the Constellation", "Sortija de la Constelación")),
    jewel("star_of_parsivan", "amulet", "sorceress", 11, GOLD, TOPAZ, [a("intellect", 8), skill("arcane_poetry", 2)], [W("resource", per_s=0.5)],
          ("Star of Parsivan", "Estrella de Parsivan"), pendant="star"),
    jewel("lalehs_locket", "amulet", "sorceress", 27, SILVER, RUBY, [ranks("sorceress", 2), a("all_attributes", 4)], [U("cheat_death", cooldown_s=300)],
          ("Laleh's Locket", "Relicario de Laleh"), pendant="drop"),
    charm("sorc_ember_charm", "sorceress", 5, "small", "flame", (255, 120, 40), [a("fire_damage", 2)], [], ("Charm of Embers", "Talismán de Ascuas")),
    charm("sorc_frost_charm", "sorceress", 11, "large", "snowflake", (150, 220, 255), [skill("frost_lance", 1), a("frost_damage", 2)], [], ("Charm of Frost", "Talismán de Escarcha")),
    charm("sorc_storm_charm", "sorceress", 17, "large", "bolt", (255, 236, 110), [skill("wandering_spark", 1), a("storm_damage", 3)], [], ("Charm of the Storm", "Talismán de la Tormenta")),
    charm("sorc_grand_charm", "sorceress", 26, "grand", "star", (170, 120, 255), [ranks("sorceress"), a("intellect", 6)], [], ("Grand Charm of the Astrolabe", "Gran Talismán del Astrolabio")),

    # ======================================================================================= the Thief
    weapon("ash_and_ember", "thief", 5, "shadow_claws", 0.04, [a("agility", 6), skill("double_edge", 2)], [H("ignite", s=2), H("marks", n=1, chance=0.25)],
           ("Ash and Ember", "Ceniza y Brasa")),
    weapon("fangs_of_fenrath", "thief", 13, "viper_claws", 0.95, [a("agility", 9), skill("cutthroat", 2), a("crit_chance", 6)], [H("bleed", s=4), H("poison", s=3)],
           ("Fangs of Fenrath", "Colmillos de Fenrath")),
    weapon("smugglers_seax", "thief", 9, "seax", 0.62, [a("agility", 7), skill("light_fingers", 2)], [K("dinars", n=2)], ("Smuggler's Seax", "Seax del Contrabandista")),
    weapon("last_breath", "thief", 18, "serrated_dagger", 0.85, [a("agility", 11), a("crit_damage", 25)], [H("execute", below=0.15), H("marks", n=1)],
           ("Last Breath", "Último Aliento")),
    weapon("fjord_wolf_hook", "thief", 22, "hook_blade", 0.55, [a("agility", 12), skill("fjord_snare", 2)], [H("slow", s=2, lvl=2), H("bleed", s=3)],
           ("Hook of the Fjord Wolf", "Gancho del Lobo del Fiordo")),
    weapon("greed_incarnate", "thief", 28, "explorer_machete", 0.13, [ranks("thief", 2), a("physical_damage", 22)], [K("dinars", n=3), H("lifesteal", frac=0.06)],
           ("Greed Incarnate", "Codicia Encarnada")),
    armor("hood_of_ash", "thief", 6, "helmet", "leather", {"leather": (34, 32, 40), "cloth": (28, 26, 34), "trim": (180, 60, 40), "dark": (14, 12, 18)},
          "deep_hood", [a("agility", 6), skill("smoke_step", 2)], [W("effect", effect="minecraft:night_vision")], ("Hood of Ash", "Capucha de Ceniza")),
    armor("mask_of_the_heist", "thief", 23, "helmet", "leather", {"leather": (40, 30, 60), "cloth": (30, 20, 50), "trim": (220, 220, 230)},
          "deep_hood", [ranks("thief"), a("crit_chance", 6)], [W("effect", effect="minecraft:invisibility", when="crouching")], ("Mask of the Great Heist", "Máscara del Gran Golpe")),
    armor("jerkin_of_many_pockets", "thief", 10, "chestplate", "leather", {"leather": (110, 74, 42), "trim": (200, 160, 70)}, "hood",
          [a("agility", 7), skill("deep_pockets", 2)], [K("dinars", n=1, chance=0.3)], ("Jerkin of Many Pockets", "Jubón de Mil Bolsillos")),
    armor("wolfskin_of_the_raider", "thief", 25, "chestplate", "leather", {"fur": (150, 146, 140), "leather": (80, 54, 34), "trim": (140, 140, 150)},
          "wolf", [a("agility", 12), a("max_health", 10)], [K("heal", amount=2), W("effect", effect="minecraft:speed")], ("Wolfskin of the Raider", "Piel de Lobo del Saqueador")),
    armor("breeches_of_the_cutpurse", "thief", 9, "leggings", "leather", {"leather": (60, 50, 40), "trim": (150, 110, 60)}, "hood",
          [a("agility", 6), a("dodge", 5)], [], ("Breeches of the Cutpurse", "Calzones del Cortabolsas")),
    armor("shadowstep_legwraps", "thief", 21, "leggings", "leather", {"leather": (20, 18, 26), "trim": (100, 60, 160)}, "deep_hood",
          [a("agility", 10), skill("thousand_cuts", 2)], [W("effect", effect="minecraft:jump_boost")], ("Shadowstep Legwraps", "Vendas del Paso Sombrío")),
    armor("boots_of_the_fjord_runner", "thief", 7, "boots", "leather", {"leather": (90, 70, 50), "fur": (200, 190, 180), "trim": (110, 170, 220)}, "hood",
          [a("agility", 5)], [W("effect", effect="minecraft:speed"), W("unfreeze")], ("Boots of the Fjord Runner", "Botas del Corredor del Fiordo")),
    armor("silent_soles", "thief", 24, "boots", "leather", {"leather": (30, 30, 34), "trim": (180, 30, 30)}, "deep_hood",
          [a("agility", 9), a("dodge", 6)], [W("effect", effect="minecraft:speed", amp=1, when="night")], ("Silent Soles", "Suelas Silenciosas")),
    jewel("ring_of_the_fence", "ring", "thief", 6, BRONZE, TOPAZ, [a("agility", 5), skill("light_fingers", 1)], [K("dinars", n=1, chance=0.25)],
          ("Ring of the Fence", "Anillo del Perista")),
    jewel("knot_of_the_grimsson_clan", "ring", "thief", 18, SILVER, RUBY, [ranks("thief"), a("crit_chance", 5)], [H("marks", n=1, chance=0.2)],
          ("Knot of the Grimsson Clan", "Nudo del Clan Grimsson")),
    jewel("wolf_tooth_necklace", "amulet", "thief", 12, BONE_M, ONYX, [a("agility", 8), skill("scavengers_instinct", 2)], [K("effect", effect="minecraft:speed", s=4)],
          ("Wolf Tooth Necklace", "Collar de Dientes de Lobo"), pendant="fangs"),
    jewel("ember_of_the_burned_village", "amulet", "thief", 27, OBSIDIAN, RUBY, [ranks("thief", 2), a("fire_resistance", 30)], [U("ignite", s=3)],
          ("Ember of the Burned Village", "Brasa de la Aldea Quemada"), pendant="drop"),
    charm("thief_coin_charm", "thief", 5, "small", "coin", (232, 186, 60), [a("agility", 2)], [K("dinars", n=1, chance=0.08)], ("Charm of the Lucky Coin", "Talismán de la Moneda de la Suerte")),
    charm("thief_key_charm", "thief", 12, "large", "key", (200, 200, 210), [skill("light_fingers", 1), a("dodge", 2)], [], ("Charm of the Skeleton Key", "Talismán de la Ganzúa")),
    charm("thief_fang_charm", "thief", 18, "large", "fang", (230, 220, 200), [skill("double_edge", 1), a("crit_damage", 8)], [], ("Charm of the Wolf Fang", "Talismán del Colmillo de Lobo")),
    charm("thief_grand_charm", "thief", 26, "grand", "smoke", (90, 90, 110), [ranks("thief"), a("agility", 6)], [], ("Grand Charm of Ash", "Gran Talismán de Ceniza")),

    # ======================================================================================= the King
    weapon("scepter_of_the_first_sultan", "king", 6, "royal_scepter", 0.12, [a("charisma", 6), skill("decree_of_steadfastness", 2)], [],
           ("Scepter of the First Sultan", "Cetro del Primer Sultán")),
    weapon("azhar_crescent", "king", 12, "sultan_saber", 0.55, [a("charisma", 8), skill("scepter_slash", 2), a("physical_damage", 12)], [H("resource", amount=2), H("bleed", s=3)],
           ("Azhar's Crescent", "Media Luna de Azhar")),
    weapon("voice_of_the_cannon", "king", 16, "royal_flintlock", 0.08, [a("charisma", 10), skill("bronze_cannon", 2)], [H("knockup", chance=0.15, v=0.4)],
           ("Voice of the Cannon", "Voz del Cañón")),
    weapon("janissary_oath", "king", 10, "brass_scimitar", 0.6, [a("charisma", 7), skill("janissary_guard", 2)], [K("resource", amount=6)], ("The Janissary's Oath", "Juramento del Jenízaro")),
    weapon("musket_of_the_siege", "king", 21, "gearwork_musket", 0.1, [a("charisma", 11), skill("siege_decree", 2), a("crit_chance", 5)], [H("slow", s=3, lvl=2)],
           ("Musket of the Siege", "Mosquete del Asedio")),
    weapon("halberd_of_five_lands", "king", 28, "imperial_halberd", 0.14, [ranks("king", 2), a("charisma", 14)], [H("chain", targets=3, frac=0.4), K("effect", effect="minecraft:strength", s=5)],
           ("Halberd of the Five Lands", "Alabarda de las Cinco Tierras")),
    armor("turban_of_the_peacock_throne", "king", 7, "helmet", "royal", {"cloth": (30, 110, 140), "cloth2": (40, 150, 90), "trim": (230, 190, 80), "gem": (60, 90, 220)},
          "turban", [a("charisma", 6), skill("command", 2)], [W("effect", effect="minecraft:hero_of_the_village")], ("Turban of the Peacock Throne", "Turbante del Trono del Pavo Real")),
    armor("crown_of_azhar", "king", 24, "helmet", "royal", {"cloth": (150, 20, 30), "trim": (255, 220, 120), "gem": (220, 30, 50), "fur": (250, 250, 246)},
          "crown", [ranks("king"), a("all_attributes", 5)], [W("absorption", every_s=20)], ("Crown of Azhar", "Corona de Azhar")),
    armor("kaftan_of_the_divan", "king", 11, "chestplate", "royal", {"cloth": (170, 30, 40), "cloth2": (240, 236, 224), "trim": (236, 186, 60)}, "turban",
          [a("charisma", 8), skill("royal_treasury", 2)], [K("dinars", n=1, chance=0.3)], ("Kaftan of the Divan", "Caftán del Diván")),
    armor("robes_of_the_golden_age", "king", 26, "chestplate", "royal", {"cloth": (232, 186, 60), "cloth2": (150, 20, 30), "trim": (255, 236, 160), "fur": (250, 250, 246)},
          "crown", [a("charisma", 14), skill("crown_of_the_five_lands", 1)], [W("aura", damage=1, radius=4, kind="holy")], ("Robes of the Golden Age", "Ropajes de la Edad Dorada")),
    armor("trousers_of_the_vizier", "king", 9, "leggings", "royal", {"cloth": (90, 30, 110), "trim": (230, 186, 80)}, "janissary",
          [a("charisma", 6), a("will", 4)], [W("effect", effect="minecraft:luck")], ("Trousers of the Vizier", "Calzas del Visir")),
    armor("legs_of_the_conqueror", "king", 22, "leggings", "royal", {"cloth": (40, 40, 60), "trim": (232, 186, 70)}, "crown",
          [a("charisma", 10), a("armor", 5)], [U("knockback", v=0.6)], ("Legs of the Conqueror", "Piernas del Conquistador")),
    armor("slippers_of_the_harem_road", "king", 8, "boots", "royal", {"cloth": (240, 200, 150), "trim": (240, 210, 120)}, "turban",
          [a("agility", 5), a("charisma", 4)], [W("effect", effect="minecraft:speed")], ("Slippers of the Caravan Road", "Babuchas del Camino de las Caravanas")),
    armor("tread_of_kings", "king", 25, "boots", "royal", {"cloth": (232, 186, 60), "trim": (255, 240, 180)}, "crown",
          [a("charisma", 9), skill("voice_of_the_throne", 2)], [W("effect", effect="minecraft:jump_boost")], ("Tread of Kings", "Paso de los Reyes")),
    jewel("signet_of_tevfiran", "ring", "king", 6, GOLD, RUBY, [a("charisma", 5), skill("janissary_guard", 1)], [K("resource", amount=5)],
          ("Signet of Tevfirán", "Sello de Tevfirán")),
    jewel("ring_of_the_grand_vizier", "ring", "king", 18, GOLD, EMERALD, [ranks("king"), a("cooldown_reduction", 6)], [W("reveal", radius=12)],
          ("Ring of the Grand Vizier", "Anillo del Gran Visir")),
    jewel("crescent_of_sulthari", "amulet", "king", 12, GOLD, TURQUOISE, [a("charisma", 8), skill("imperial_lineage", 2)], [W("heal", every_s=5, amount=1)],
          ("Crescent of Sulthari", "Media Luna de Sulthari"), pendant="crescent"),
    jewel("heart_of_the_throne", "amulet", "king", 27, GOLD, DIAMOND, [ranks("king", 2), a("max_health", 12)], [U("cheat_death", cooldown_s=300)],
          ("Heart of the Throne", "Corazón del Trono"), pendant="sun"),
    charm("king_coin_charm", "king", 5, "small", "coin", (232, 186, 60), [a("charisma", 2)], [], ("Charm of the Royal Mint", "Talismán de la Ceca Real")),
    charm("king_decree_charm", "king", 12, "large", "scroll", (236, 226, 200), [skill("siege_decree", 1), a("charisma", 3)], [], ("Charm of the Decree", "Talismán del Decreto")),
    charm("king_cannon_charm", "king", 18, "large", "cannonball", (90, 80, 70), [skill("bronze_cannon", 1), a("physical_damage", 6)], [], ("Charm of the Bronze", "Talismán del Bronce")),
    charm("king_grand_charm", "king", 26, "grand", "crown", (255, 220, 120), [ranks("king"), a("charisma", 6)], [], ("Grand Charm of the Throne", "Gran Talismán del Trono")),

    # ======================================================================================= for any Bearer: charms
    charm("void_shard_charm", None, 4, "small", "shard", (180, 90, 250), [a("void_resistance", 8)], [], ("Splinter of the Void", "Astilla del Vacío")),
    charm("desert_rose_charm", None, 3, "small", "rose", (230, 120, 110), [a("fire_resistance", 8)], [], ("Desert Rose", "Rosa del Desierto")),
    charm("frost_tooth_charm", None, 6, "small", "fang", (170, 230, 255), [a("frost_resistance", 9)], [], ("Frost Tooth", "Diente de Escarcha")),
    charm("storm_glass_charm", None, 7, "small", "bolt", (255, 236, 110), [a("storm_resistance", 9)], [], ("Storm Glass", "Vidrio de Tormenta")),
    charm("lucky_feather_charm", None, 5, "small", "feather", (240, 240, 240), [a("dodge", 2)], [W("effect", effect="minecraft:slow_falling", when="always")], ("Lucky Feather", "Pluma de la Suerte")),
    charm("traveler_charm", None, 4, "small", "compass", (200, 160, 80), [a("vitality", 3)], [W("effect", effect="minecraft:speed")], ("Traveler's Charm", "Talismán del Viajero")),
    charm("gheed_coin", None, 9, "large", "coin", (255, 210, 60), [a("charisma", 4)], [K("dinars", n=1, chance=0.15)], ("Gheed's Lost Coin", "La Moneda Perdida de Gheed")),
    charm("lantern_charm", None, 8, "large", "lantern", (255, 200, 90), [a("all_attributes", 1)], [W("effect", effect="minecraft:night_vision")], ("Lamplighter's Charm", "Talismán del Farolero")),
    charm("bloodstone_charm", None, 10, "large", "drop", (180, 20, 30), [a("life_steal", 3)], [], ("Bloodstone", "Piedra de Sangre")),
    charm("hourglass_charm", None, 14, "large", "hourglass", (230, 200, 120), [a("cooldown_reduction", 4)], [], ("Hourglass of the Observatory", "Reloj de Arena del Observatorio")),
    charm("compass_of_the_codex", None, 12, "large", "compass", (120, 200, 255), [a("all_attributes", 2)], [W("reveal", radius=10)], ("Compass of the Codex", "Brújula del Códice")),
    charm("ember_heart_charm", None, 15, "large", "flame", (255, 110, 40), [a("fire_damage", 3), a("fire_resistance", 10)], [], ("Ember Heart", "Corazón de Ascua")),
    charm("frostbound_charm", None, 15, "large", "snowflake", (150, 220, 255), [a("frost_damage", 3), a("frost_resistance", 10)], [W("unfreeze")], ("Frostbound", "Atado a la Escarcha")),
    charm("thunder_idol_charm", None, 16, "large", "idol", (255, 236, 110), [a("storm_damage", 3), a("storm_resistance", 10)], [], ("Thunder Idol", "Ídolo del Trueno")),
    charm("pilgrim_charm", None, 11, "large", "rose", (240, 220, 200), [a("vitality", 5)], [W("heal", every_s=8, amount=1)], ("Pilgrim's Charm", "Talismán del Peregrino")),
    charm("annihilus", None, 28, "small", "eye", (230, 120, 40), [a("all_attributes", 8), a("fire_resistance", 12), a("frost_resistance", 12), a("storm_resistance", 12)], [],
          ("Annihilus", "Annihilus")),
    charm("torch_of_the_bearers", None, 26, "large", "torch", (255, 140, 40), [a("all_attributes", 6), a("void_resistance", 15)], [], ("Torch of the Bearers", "Antorcha de los Portadores")),
    charm("seal_of_the_eighth_lock", None, 29, "grand", "lock", (200, 160, 255), [a("all_attributes", 6), a("max_health", 12), a("cooldown_reduction", 6)],
          [W("absorption", every_s=30)], ("Seal of the Eighth Lock", "Sello de la Octava Cerradura")),
    charm("codex_page_charm", None, 22, "grand", "scroll", (236, 226, 200), [a("magic_damage", 10), a("max_resource", 15)], [W("resource", per_s=0.5)],
          ("Torn Page of the Codex", "Página Arrancada del Códice")),
    charm("vorath_cinder", None, 20, "grand", "flame", (255, 80, 30), [a("physical_damage", 10), a("fire_damage", 4)], [H("ignite", s=2)], ("Cinder of Vorath", "Ceniza de Vorath")),

    # ======================================================================================= for any Bearer: jewelry
    jewel("ring_of_the_caravanserai", "ring", None, 4, BRONZE, TURQUOISE, [a("vitality", 4), a("charisma", 3)], [W("heal", every_s=10, amount=1)],
          ("Ring of the Caravanserai", "Anillo del Caravasar")),
    jewel("serpent_eye_ring", "ring", None, 9, GOLD, EMERALD, [a("agility", 5), a("crit_chance", 4)], [H("poison", s=2)], ("Serpent's Eye", "Ojo de la Serpiente")),
    jewel("frost_wyrm_band", "ring", None, 14, ICE, DIAMOND, [a("frost_resistance", 20), a("frost_damage", 3)], [U("slow", s=2, lvl=1)], ("Band of the Frost Wyrm", "Sortija de la Sierpe de Escarcha")),
    jewel("void_touched_ring", "ring", None, 23, OBSIDIAN, AMETHYST, [a("all_attributes", 4), a("void_resistance", 20)], [H("wither", s=2)], ("Void-touched Ring", "Anillo Tocado por el Vacío")),
    jewel("amulet_of_the_dawn", "amulet", None, 7, GOLD, TOPAZ, [a("vitality", 6), a("fire_resistance", 12)], [W("effect", effect="minecraft:regeneration", when="day")],
          ("Amulet of the Dawn", "Amuleto del Alba"), pendant="sun"),
    jewel("tidecaller_pendant", "amulet", None, 13, SILVER, SAPPHIRE, [a("vitality", 6), a("dodge", 4)], [W("effect", effect="minecraft:water_breathing"),
          W("effect", effect="minecraft:dolphins_grace", when="water")], ("Tidecaller's Pendant", "Colgante del Llamamareas"), pendant="drop"),
    jewel("bone_of_the_first_fallen", "amulet", None, 21, BONE_M, ONYX, [a("all_attributes", 5), a("life_steal", 4)], [K("heal", amount=2)],
          ("Bone of the First Fallen", "Hueso del Primer Caído"), pendant="fangs"),
    jewel("prythons_mirror", "amulet", None, 29, SILVER, DIAMOND, [a("all_attributes", 8), a("crit_damage", 20)], [U("thorns", frac=0.2)],
          ("Prython's Mirror", "Espejo de Prython"), pendant="eye"),

    # ======================================================================================= for any Bearer: weapons and armor
    weapon("caravan_breaker", None, 4, "war_hammer", 0.08, [a("strength", 4), a("physical_damage", 10)], [H("stun", chance=0.1, s=1)], ("Caravan Breaker", "Rompecaravanas")),
    weapon("jungle_of_glass", None, 8, "explorer_machete", 0.38, [a("agility", 5), a("crit_chance", 4)], [H("bleed", s=3), H("vs_beast", mult=1.5)], ("Glass Jungle", "Selva de Cristal")),
    weapon("tide_of_teeth", None, 12, "crystal_trident", 0.55, [a("strength", 7), a("frost_damage", 3)], [H("freeze", chance=0.1, s=2)], ("Tide of Teeth", "Marea de Dientes")),
    weapon("chainmaster", None, 15, "chain_sword", 0.1, [a("strength", 8), a("physical_damage", 14)], [H("chain", targets=2, frac=0.35)], ("Chainmaster", "Señor de las Cadenas")),
    weapon("harvest_of_shadows", None, 19, "shadow_scythe", 0.78, [a("strength", 9), a("life_steal", 5)], [H("wither", s=3), K("heal", amount=2)], ("Harvest of Shadows", "Cosecha de Sombras")),
    weapon("flail_of_the_penitent", None, 23, "double_flail", 0.02, [a("strength", 11), a("max_health", 10)], [H("vs_monster", mult=1.25), H("stun", chance=0.12, s=1)],
           ("Flail of the Penitent", "Mangual del Penitente")),
    weapon("echo_of_kaleth", None, 27, "kaleth_blade", 0.04, [a("strength", 13), a("fire_damage", 7), a("crit_chance", 6)], [H("ignite", s=5), H("lightning", chance=0.08, mult=1.6)],
           ("Echo of Kaleth", "Eco de Kaleth")),
    armor("helm_of_the_wanderer", None, 5, "helmet", "leather", {"leather": (120, 90, 60), "cloth": (190, 170, 130), "trim": (60, 200, 190)}, "hood",
          [a("vitality", 4), a("all_attributes", 1)], [W("effect", effect="minecraft:night_vision")], ("Hood of the Wanderer", "Capucha del Errante")),
    armor("eclipse_plate", None, 20, "chestplate", "heavy", {"plate": (30, 20, 50), "trim": (200, 120, 255), "gem": (230, 160, 255)}, "great",
          [a("armor", 6), a("void_resistance", 25)], [U("wither", s=2)], ("Plate of the Eclipse", "Placa del Eclipse")),
    armor("sandstriders", None, 10, "boots", "leather", {"leather": (200, 160, 100), "trim": (150, 60, 40)}, "hood",
          [a("agility", 6), a("fire_resistance", 15)], [W("effect", effect="minecraft:speed", when="day")], ("Sandstriders", "Andarenas")),
    armor("legs_of_the_seventh_sin", None, 27, "leggings", "heavy", {"plate": (100, 20, 20), "trim": (40, 30, 30), "gem": (255, 60, 40)}, "great",
          [a("all_attributes", 6), a("crit_damage", 15)], [W("effect", effect="minecraft:strength", when="low_health")], ("Legs of the Seventh Sin", "Piernas del Séptimo Pecado")),
]

# a third size of random charm, as in Diablo II
GRAND_TALISMAN = ("grand_talisman", ("Grand Talisman", "Gran Talismán"), 50)

# the top of the game: level 100 (the highest a Bearer reaches, in Act V), the strongest of each class and of all
ASCENDED = {
    "knight": ["wrath_unbound", "tear_of_aureum", "heart_of_the_legion", "knight_grand_charm"],
    "necromancer": ["anubets_judgement", "eye_of_morthis", "crown_of_the_pale_king", "necro_grand_charm"],
    "sorceress": ["skyfall", "lalehs_locket", "mantle_of_the_eclipse", "sorc_grand_charm"],
    "thief": ["greed_incarnate", "ember_of_the_burned_village", "wolfskin_of_the_raider", "thief_grand_charm"],
    "king": ["halberd_of_five_lands", "heart_of_the_throne", "robes_of_the_golden_age", "king_grand_charm"],
    None: ["annihilus", "seal_of_the_eighth_lock", "prythons_mirror", "echo_of_kaleth"],
}
MAX_LEVEL = 100
_top = {i for ids in ASCENDED.values() for i in ids}
for _u in UNIQUES:  # the catalog's levels were written for a cap of 30; the cap is 100
    _u["level"] = max(1, min(MAX_LEVEL, round(_u["level"] * 10 / 3)))
for _u in UNIQUES:
    if _u["id"] in _top:
        _u["level"] = MAX_LEVEL
        for _a in _u["affixes"]:
            if _a["stat"] == "class_skill_ranks":
                _a["value"] = max(_a["value"], 2)
            elif _a["stat"] != "skill_ranks":
                _a["value"] = round(_a["value"] * 1.3)
        _u["ascended"] = True
