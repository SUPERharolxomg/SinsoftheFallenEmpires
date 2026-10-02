"""The arsenal, batch 3, and the class items: what each item is (its Java constructor), its names, who it is for,
its level, how it is held (model) and how it is drawn (sprite), and its recipe if it has one.
scripts/make_arsenal3_data.py writes the registry block, lang, gear bases and recipes from here;
scripts/make_arsenal3_sprites.py draws the sprites.

model: handheld, large (held bigger), gun (barrel forward), flat, bow, crossbow.
"""

P = "new Item.Properties()"


def item(id, java, en, es, model, sprite, cls=None, level=1, gear=True, recipe=None, tab=True):
    return dict(id=id, java=java, en=en, es=es, model=model, sprite=sprite, cls=cls, level=level, gear=gear, recipe=recipe, tab=tab)


def tw(tier, dmg, speed, reach, traits, cls):
    t = ", ".join("com.sofe.gear.WeaponTrait." + x for x in traits)
    return 'new com.sofe.gear.TraitWeapon(SoFETiers.%s, %d, %sf, %s, java.util.EnumSet.of(%s), "%s", %s)' % (tier, dmg, speed, reach, t, cls, P)


def pick_recipe(ing):
    return {"type": "shaped", "pattern": ["GGG", " S ", " S "], "key": {"G": ing, "S": "minecraft:stick"}}


def hammer_recipe(block, ing):
    return {"type": "shaped", "pattern": ["GBG", " S ", " S "], "key": {"G": ing, "B": block, "S": "minecraft:stick"}}


ITEMS = [
    # ------------------------------------------------------------------ tools
    item("star_lapis_pickaxe", "new GearItems.Pickaxe(SoFETiers.STAR_LAPIS, 1, -2.8f, %s)" % P, "Star Lapis Pickaxe", "Pico de Lapislázuli Estelar",
         "handheld", ("pickaxe", "star_lapis"), gear=False, recipe=pick_recipe("sofe:star_lapis")),
    item("solar_gold_pickaxe", "new GearItems.Pickaxe(SoFETiers.SOLAR_GOLD, 1, -2.8f, %s)" % P, "Solar Gold Pickaxe", "Pico de Oro Solar",
         "handheld", ("pickaxe", "solar_gold"), gear=False, recipe=pick_recipe("sofe:solar_gold_ingot")),
    item("orichalcum_pickaxe", "new GearItems.Pickaxe(SoFETiers.ORICHALCUM, 1, -2.8f, %s.fireResistant())" % P, "Orichalcum Pickaxe", "Pico de Oricalco",
         "handheld", ("pickaxe", "orichalcum"), gear=False, recipe=pick_recipe("sofe:orichalcum_ingot")),
    item("aetherium_pickaxe", "new GearItems.Pickaxe(SoFETiers.AETHERIUM, 1, -2.8f, %s.fireResistant())" % P, "Aetherium Pickaxe", "Pico de Aeterio",
         "handheld", ("pickaxe", "aetherium"), gear=False, recipe=pick_recipe("sofe:aetherium_shard")),
    item("brass_mining_hammer", "new com.sofe.gear.Tools.MiningHammer(SoFETiers.BRASS, 5, -3.4f, %s)" % P, "Brass Mining Hammer",
         "Martillo de Minería de Latón", "large", ("mining_hammer", "brass"), gear=False,
         recipe=hammer_recipe("sofe:sulthari_brass_block", "sofe:sulthari_brass_ingot")),
    item("glacial_mining_hammer", "new com.sofe.gear.Tools.MiningHammer(SoFETiers.GLACIAL_IRON, 6, -3.4f, %s)" % P, "Glacial Mining Hammer",
         "Martillo de Minería Glacial", "large", ("mining_hammer", "glacial"), gear=False,
         recipe=hammer_recipe("sofe:glacial_iron_block", "sofe:glacial_iron_ingot")),
    item("gearwork_drill", "new com.sofe.gear.Tools.Drill(SoFETiers.STAR_LAPIS, 1, -2.6f, %s)" % P, "Gearwork Drill", "Taladro de Engranajes",
         "gun", ("drill",), gear=False,
         recipe={"type": "shaped", "pattern": [" GL", "GRG", "BG "], "key": {"G": "sofe:sulthari_brass_ingot", "L": "sofe:star_lapis",
                                                                          "R": "minecraft:redstone_block", "B": "sofe:sulthari_brass_block"}}),
    # ------------------------------------------------------------------ thrown
    item("brass_javelin", "new com.sofe.gear.ranged.RangedItems.Javelin(7, Spell.NONE, false, %s.durability(200))" % P, "Brass Javelin",
         "Jabalina de Latón", "large", ("javelin", "brass"), cls=("knight", "thief"), level=2,
         recipe={"type": "shaped", "pattern": ["  G", " S ", "S  "], "key": {"G": "sofe:sulthari_brass_ingot", "S": "minecraft:stick"}}),
    item("glacial_javelin", "new com.sofe.gear.ranged.RangedItems.Javelin(9, Spell.FROST, false, %s.durability(320))" % P, "Glacial Javelin",
         "Jabalina Glacial", "large", ("javelin", "glacial"), cls=("knight", "thief"), level=6,
         recipe={"type": "shaped", "pattern": ["  G", " S ", "S  "], "key": {"G": "sofe:glacial_iron_ingot", "S": "minecraft:stick"}}),
    item("aetherium_javelin", "new com.sofe.gear.ranged.RangedItems.Javelin(11, Spell.ARCANE, true, %s.durability(600))" % P, "Aetherium Javelin",
         "Jabalina de Aeterio", "large", ("javelin", "aetherium"), cls=("knight", "thief"), level=11),
    item("brass_throwing_knife", "new com.sofe.gear.ranged.RangedItems.ThrowingKnife(4, Spell.NONE, 1, null, %s.stacksTo(16))" % P,
         "Brass Throwing Knife", "Cuchillo Arrojadizo de Latón", "handheld", ("knife", "brass"), cls=("thief",), level=1,
         recipe={"type": "shaped", "pattern": ["G", "S"], "key": {"G": "sofe:sulthari_brass_ingot", "S": "minecraft:stick"}, "count": 4}),
    item("glacial_throwing_knife", "new com.sofe.gear.ranged.RangedItems.ThrowingKnife(5, Spell.FROST, 1, null, %s.stacksTo(16))" % P,
         "Glacial Throwing Knife", "Cuchillo Arrojadizo Glacial", "handheld", ("knife", "glacial"), cls=("thief",), level=6,
         recipe={"type": "shaped", "pattern": ["G", "S"], "key": {"G": "sofe:glacial_iron_ingot", "S": "minecraft:stick"}, "count": 4}),
    item("venom_throwing_knife", "new com.sofe.gear.ranged.RangedItems.ThrowingKnife(4, Spell.POISON, 1, null, %s.stacksTo(16))" % P,
         "Venom Knife", "Cuchillo Envenenado", "handheld", ("knife", "venom"), cls=("thief",), level=4),
    # ------------------------------------------------------------------ bows and crossbows
    item("sulthari_recurve_bow", "new com.sofe.gear.ranged.RangedItems.SoFEBow(14, 1.0f, 1.0f, Spell.NONE, %s.durability(420))" % P,
         "Sulthari Recurve Bow", "Arco Recurvo de Sulthari", "bow", ("bow", "recurve"), cls=("thief", "king"), level=2),
    item("nordrath_longbow", "new com.sofe.gear.ranged.RangedItems.SoFEBow(30, 1.5f, 1.2f, Spell.NONE, %s.durability(500))" % P,
         "Nordrath Longbow", "Arco Largo de Nordrath", "bow", ("bow", "longbow"), cls=("thief", "knight"), level=6),
    item("frostbite_bow", "new com.sofe.gear.ranged.RangedItems.SoFEBow(20, 1.1f, 1.0f, Spell.FROST, %s.durability(520))" % P,
         "Frostbite Bow", "Arco Mordisco de Escarcha", "bow", ("bow", "frost"), cls=("thief", "sorceress"), level=7),
    item("solar_bow", "new com.sofe.gear.ranged.RangedItems.SoFEBow(18, 1.2f, 1.1f, Spell.EMBER, %s.durability(700))" % P,
         "Solar Bow", "Arco Solar", "bow", ("bow", "solar"), cls=("thief", "king"), level=10),
    item("void_bow", "new com.sofe.gear.ranged.RangedItems.SoFEBow(22, 1.3f, 1.1f, Spell.VOID, %s.durability(800))" % P,
         "Void Bow", "Arco del Vacío", "bow", ("bow", "void"), cls=("thief", "necromancer"), level=12),
    item("brass_arbalest", "new com.sofe.gear.ranged.RangedItems.SoFECrossbow(1.6f, Spell.NONE, 1, %s.durability(500))" % P,
         "Brass Arbalest", "Arbalesta de Latón", "crossbow", ("crossbow", "brass"), cls=("knight", "thief"), level=4),
    item("repeating_crossbow", "new com.sofe.gear.ranged.RangedItems.SoFECrossbow(0.9f, Spell.NONE, 3, %s.durability(600))" % P,
         "Repeating Crossbow", "Ballesta de Repetición", "crossbow", ("crossbow", "repeater"), cls=("thief",), level=8),
    item("glacial_crossbow", "new com.sofe.gear.ranged.RangedItems.SoFECrossbow(1.3f, Spell.FROST, 1, %s.durability(650))" % P,
         "Glacial Crossbow", "Ballesta Glacial", "crossbow", ("crossbow", "glacial"), cls=("knight", "thief"), level=7),
    # ------------------------------------------------------------------ firearms
    item("brass_pistol", "new com.sofe.gear.ranged.RangedItems.Firearm(8, 1, 1.0f, 25, null, %s.durability(400))" % P,
         "Brass Pistol", "Pistola de Latón", "gun", ("pistol", "brass"), cls=("king", "thief"), level=4),
    item("gearwork_musket", "new com.sofe.gear.ranged.RangedItems.Firearm(15, 1, 0.2f, 45, null, %s.durability(500))" % P,
         "Gearwork Musket", "Mosquete de Engranajes", "gun", ("musket",), cls=("king", "knight"), level=8),
    item("blunderbuss", "new com.sofe.gear.ranged.RangedItems.Firearm(3.5f, 6, 8.0f, 50, null, %s.durability(400))" % P,
         "Blunderbuss", "Trabuco", "gun", ("blunderbuss",), cls=("king", "thief"), level=6),
    item("brass_cartridge", "new Item(%s)" % P, "Brass Cartridge", "Cartucho de Latón", "flat", ("cartridge",), gear=False,
         recipe={"type": "shapeless", "ingredients": ["sofe:sulthari_brass_ingot", "minecraft:gunpowder"], "count": 8}),
    # ------------------------------------------------------------------ staves
    item("ember_staff", "new com.sofe.gear.ranged.RangedItems.SpellCaster(Spell.EMBER, CastMode.BOLT, 6, 20, null, %s.durability(300))" % P,
         "Ember Staff", "Báculo de Ascuas", "large", ("staff", "ember"), cls=("sorceress",), level=3),
    item("frost_staff", "new com.sofe.gear.ranged.RangedItems.SpellCaster(Spell.FROST, CastMode.BOLT, 5, 20, null, %s.durability(300))" % P,
         "Frost Staff", "Báculo de Escarcha", "large", ("staff", "frost"), cls=("sorceress",), level=4),
    item("storm_staff", "new com.sofe.gear.ranged.RangedItems.SpellCaster(Spell.STORM, CastMode.LIGHTNING, 9, 50, null, %s.durability(300))" % P,
         "Storm Staff", "Báculo de la Tormenta", "large", ("staff", "storm"), cls=("sorceress",), level=9),
    item("void_staff", "new com.sofe.gear.ranged.RangedItems.SpellCaster(Spell.VOID, CastMode.BOLT, 8, 30, null, %s.durability(400))" % P,
         "Void Staff", "Báculo del Vacío", "large", ("staff", "void"), cls=("sorceress", "necromancer"), level=11),
    item("soul_staff", "new com.sofe.gear.ranged.RangedItems.SpellCaster(Spell.SOUL, CastMode.BEAM, 6, 30, null, %s.durability(350))" % P,
         "Soul Staff", "Báculo de Almas", "large", ("staff", "soul"), cls=("necromancer",), level=8),
    # ------------------------------------------------------------------ tomes
    item("tome_of_embers", "new com.sofe.gear.ranged.RangedItems.Tome(TomeKind.EMBERS, 6, 200, null, %s.durability(80))" % P,
         "Tome of Embers", "Tomo de Ascuas", "flat", ("tome", "ember"), cls=("sorceress",), level=5),
    item("tome_of_frost", "new com.sofe.gear.ranged.RangedItems.Tome(TomeKind.FROST, 5, 200, null, %s.durability(80))" % P,
         "Tome of Frost", "Tomo de Escarcha", "flat", ("tome", "frost"), cls=("sorceress",), level=5),
    item("tome_of_wards", "new com.sofe.gear.ranged.RangedItems.Tome(TomeKind.WARDS, 1, 600, null, %s.durability(60))" % P,
         "Tome of Wards", "Tomo de Protección", "flat", ("tome", "wards"), cls=("knight", "king"), level=7),
    item("tome_of_the_gale", "new com.sofe.gear.ranged.RangedItems.Tome(TomeKind.GALE, 1.6f, 240, null, %s.durability(80))" % P,
         "Tome of the Gale", "Tomo del Vendaval", "flat", ("tome", "gale"), cls=("sorceress", "thief"), level=6),
    item("book_of_souls", "new com.sofe.gear.ranged.RangedItems.Tome(TomeKind.SOULS, 4, 300, null, %s.durability(80))" % P,
         "Book of Souls", "Libro de las Almas", "flat", ("tome", "souls"), cls=("necromancer",), level=9),
    # ------------------------------------------------------------------ gadgets
    item("clockwork_bomb", "new com.sofe.gear.ranged.RangedItems.Gadget(com.sofe.entity.projectile.Bomb.Kind.CLOCKWORK, %s)" % P,
         "Clockwork Bomb", "Bomba de Relojería", "flat", ("bomb", "clockwork"), gear=False,
         recipe={"type": "shapeless", "ingredients": ["sofe:sulthari_brass_ingot", "minecraft:gunpowder", "minecraft:redstone"], "count": 2}),
    item("smoke_bomb", "new com.sofe.gear.ranged.RangedItems.Gadget(com.sofe.entity.projectile.Bomb.Kind.SMOKE, %s)" % P,
         "Smoke Bomb", "Bomba de Humo", "flat", ("bomb", "smoke"), gear=False,
         recipe={"type": "shapeless", "ingredients": ["minecraft:gunpowder", "minecraft:charcoal", "minecraft:paper"], "count": 2}),
    item("fire_bomb", "new com.sofe.gear.ranged.RangedItems.Gadget(com.sofe.entity.projectile.Bomb.Kind.FIRE, %s)" % P,
         "Fire Bomb", "Bomba Incendiaria", "flat", ("bomb", "fire"), gear=False,
         recipe={"type": "shapeless", "ingredients": ["minecraft:gunpowder", "sofe:infernal_ember", "minecraft:glass_bottle"], "count": 2}),
    # ------------------------------------------------------------------ class items (Diablo II style: only their Bearer uses them)
    item("oathblade", tw("GLACIAL_IRON", 5, -2.4, 0.0, ["SWEEP", "HOLY"], "knight"), "Oathblade", "Hoja del Juramento",
         "handheld", ("class_sword", "oath"), cls=("knight",), level=5),
    item("aureum_warmace", tw("GLACIAL_IRON", 6, -3.0, 0.0, ["STUN", "HOLY"], "knight"), "Aureum Warmace", "Maza de Guerra de Aureum",
         "large", ("class_mace", "aureum"), cls=("knight",), level=9),
    item("lance_of_the_scale", tw("GLACIAL_IRON", 6, -3.1, 2.0, ["REACH", "CHARGE", "HOLY"], "knight"), "Lance of the Scale", "Lanza de la Balanza",
         "large", ("class_lance",), cls=("knight",), level=12),
    item("bone_wand", 'new com.sofe.gear.ranged.RangedItems.SpellCaster(Spell.BONE, CastMode.BOLT, 6, 14, "necromancer", %s.durability(250))' % P,
         "Bone Wand", "Varita de Hueso", "handheld", ("wand", "bone"), cls=("necromancer",), level=2),
    item("soul_wand", 'new com.sofe.gear.ranged.RangedItems.SpellCaster(Spell.SOUL, CastMode.BEAM, 5, 20, "necromancer", %s.durability(300))' % P,
         "Soul Wand", "Varita de Almas", "handheld", ("wand", "soul"), cls=("necromancer",), level=7),
    item("reaper_sickle", tw("GLACIAL_IRON", 4, -2.2, 0.5, ["REACH", "LIFE_STEAL", "WEAKEN"], "necromancer"), "Reaper's Sickle", "Hoz del Segador",
         "handheld", ("class_sickle",), cls=("necromancer",), level=10),
    item("ember_orb", 'new com.sofe.gear.ranged.RangedItems.SpellCaster(Spell.EMBER, CastMode.BOLT, 7, 12, "sorceress", %s.durability(300))' % P,
         "Ember Orb", "Orbe de Ascuas", "handheld", ("orb", "ember"), cls=("sorceress",), level=2),
    item("frost_orb", 'new com.sofe.gear.ranged.RangedItems.SpellCaster(Spell.FROST, CastMode.BOLT, 6, 12, "sorceress", %s.durability(300))' % P,
         "Frost Orb", "Orbe de Escarcha", "handheld", ("orb", "frost"), cls=("sorceress",), level=6),
    item("storm_orb", 'new com.sofe.gear.ranged.RangedItems.SpellCaster(Spell.STORM, CastMode.LIGHTNING, 10, 36, "sorceress", %s.durability(350))' % P,
         "Storm Orb", "Orbe de la Tormenta", "handheld", ("orb", "storm"), cls=("sorceress",), level=11),
    item("shadow_claws", tw("GLACIAL_IRON", 3, -1.8, 0.0, ["MULTI_HIT", "BLEED"], "thief"), "Shadow Claws", "Garras de Sombra",
         "handheld", ("claws", "shadow"), cls=("thief",), level=4),
    item("viper_claws", tw("GLACIAL_IRON", 4, -1.8, 0.0, ["MULTI_HIT", "POISON"], "thief"), "Viper Claws", "Garras de Víbora",
         "handheld", ("claws", "viper"), cls=("thief",), level=9),
    item("throwing_stars", 'new com.sofe.gear.ranged.RangedItems.ThrowingKnife(3, Spell.BLEED, 3, "thief", %s.stacksTo(32))' % P,
         "Throwing Stars", "Estrellas Arrojadizas", "flat", ("star",), cls=("thief",), level=3),
    item("royal_scepter", 'new com.sofe.gear.ranged.RangedItems.Tome(TomeKind.DECREE, 0, 600, "king", %s.durability(100))' % P,
         "Royal Scepter", "Cetro Real", "handheld", ("scepter",), cls=("king",), level=3),
    item("sultan_saber", tw("GLACIAL_IRON", 5, -2.3, 0.0, ["SWEEP", "BLEED"], "king"), "Sultan's Saber", "Sable del Sultán",
         "handheld", ("class_saber",), cls=("king",), level=7),
    item("royal_flintlock", 'new com.sofe.gear.ranged.RangedItems.Firearm(12, 1, 0.3f, 30, "king", %s.durability(500))' % P,
         "Royal Flintlock", "Pistola Real", "gun", ("pistol", "royal"), cls=("king",), level=10),
    # ------------------------------------------------------------------ what projectiles look like in flight (not in a tab)
] + [item("spell_" + k, "new Item(%s)" % P, en, es, "flat", ("spell", k), gear=False, tab=False)
     for k, en, es in (("ember", "Ember Bolt", "Rayo de Ascuas"), ("frost", "Frost Bolt", "Rayo de Escarcha"), ("void", "Void Orb", "Orbe del Vacío"),
                       ("soul", "Soul Bolt", "Rayo de Almas"), ("bone", "Bone Spear", "Lanza de Hueso"), ("holy", "Holy Bolt", "Rayo Sagrado"),
                       ("storm", "Storm Bolt", "Rayo de Tormenta"), ("arcane", "Arcane Bolt", "Rayo Arcano"))] + \
    [item("lead_shot", "new Item(%s)" % P, "Lead Shot", "Perdigón de Plomo", "flat", ("shot",), gear=False, tab=False)]

# what the tooltips say
LANG = {
    "item.sofe.mining_hammer.tooltip": ("Breaks a 3x3 face; crouch to mine one block", "Rompe una cara de 3x3; agáchate para picar un solo bloque"),
    "item.sofe.drill.tooltip": ("Digs stone and earth alike, and fast", "Excava piedra y tierra por igual, y rápido"),
    "item.sofe.javelin.tooltip": ("Hold and release to throw: %s damage", "Mantén y suelta para lanzar: %s de daño"),
    "item.sofe.javelin.returning": ("Hold and release to throw: %s damage; flies back to you", "Mantén y suelta para lanzar: %s de daño; vuelve a ti"),
    "item.sofe.throwing_knife.tooltip": ("Right click to throw: %s damage", "Clic derecho para lanzar: %s de daño"),
    "item.sofe.throwing_star.tooltip": ("Right click to throw %2$s at once: %1$s damage each", "Clic derecho para lanzar %2$s a la vez: %1$s de daño cada una"),
    "item.sofe.bow.tooltip": ("Arrow damage %s%%, full draw in %ss", "Daño de flecha %s%%, tensado completo en %ss"),
    "item.sofe.crossbow.tooltip": ("Bolt damage %s%%", "Daño del virote %s%%"),
    "item.sofe.crossbow.volley": ("Bolt damage %s%%, %s bolts at once", "Daño del virote %s%%, %s virotes a la vez"),
    "item.sofe.firearm.tooltip": ("%s damage a shot, reloads in %3$ss; uses Brass Cartridges", "%s de daño por disparo, recarga en %3$ss; usa Cartuchos de Latón"),
    "item.sofe.firearm.pellets": ("%2$s pellets of %1$s damage, reloads in %3$ss; uses Brass Cartridges",
                                  "%2$s perdigones de %1$s de daño, recarga en %3$ss; usa Cartuchos de Latón"),
    "item.sofe.firearm.no_ammo": ("No Brass Cartridges", "No tienes Cartuchos de Latón"),
    "item.sofe.caster.bolt": ("Right click: a bolt of %s damage, every %ss", "Clic derecho: un rayo de %s de daño, cada %ss"),
    "item.sofe.caster.beam": ("Right click: a draining beam of %s damage, every %ss", "Clic derecho: un haz que drena %s de daño, cada %ss"),
    "item.sofe.caster.lightning": ("Right click: lightning of %s damage where you look, every %ss",
                                   "Clic derecho: un rayo de %s de daño donde miras, cada %ss"),
    "tome.sofe.embers": ("Read: a ring of fire, %s damage to every foe near you (every %ss)", "Leer: un anillo de fuego, %s de daño a cada enemigo cercano (cada %ss)"),
    "tome.sofe.frost": ("Read: a frost nova, %s damage and slows every foe near you (every %ss)",
                        "Leer: una nova de escarcha, %s de daño y ralentiza a los enemigos cercanos (cada %ss)"),
    "tome.sofe.wards": ("Read: wards of absorption and resistance (every %2$ss)", "Leer: protecciones de absorción y resistencia (cada %2$ss)"),
    "tome.sofe.gale": ("Read: a gale that throws back every foe near you (every %2$ss)", "Leer: un vendaval que aleja a los enemigos cercanos (cada %2$ss)"),
    "tome.sofe.souls": ("Read: drains %s from every foe near you and heals you (every %ss)", "Leer: drena %s de cada enemigo cercano y te cura (cada %ss)"),
    "tome.sofe.decree": ("Decree: you and your allies near you fight harder, resist and run (every %2$ss)",
                         "Decreto: tú y tus aliados cercanos pegan más fuerte, resisten y corren (cada %2$ss)"),
    "gadget.sofe.clockwork": ("Throw: bursts and hurts everything near, without breaking blocks", "Lanzar: estalla y hiere a todo lo cercano, sin romper bloques"),
    "gadget.sofe.smoke": ("Throw: a cloud that blinds and slows", "Lanzar: una nube que ciega y ralentiza"),
    "gadget.sofe.fire": ("Throw: sets everything around it ablaze", "Lanzar: incendia todo a su alrededor"),
    "spell.sofe.ember": ("Element: fire (sets ablaze)", "Elemento: fuego (incendia)"),
    "spell.sofe.frost": ("Element: frost (slows and chills)", "Elemento: escarcha (ralentiza y congela)"),
    "spell.sofe.storm": ("Element: storm", "Elemento: tormenta"),
    "spell.sofe.void": ("Element: Void (withers)", "Elemento: Vacío (marchita)"),
    "spell.sofe.soul": ("Element: souls (heals you for part of the damage)", "Elemento: almas (te cura parte del daño)"),
    "spell.sofe.bone": ("Element: bone (weakens)", "Elemento: hueso (debilita)"),
    "spell.sofe.arcane": ("Element: arcane", "Elemento: arcano"),
    "spell.sofe.holy": ("Element: holy (more against the undead, heals you)", "Elemento: sagrado (más contra los no muertos, te cura)"),
    "spell.sofe.poison": ("Element: poison", "Elemento: veneno"),
    "spell.sofe.bleed": ("Element: bleeding", "Elemento: sangrado"),
    "gear.sofe.class_only": ("%s only", "Solo %s"),
    "gear.sofe.class_only.refused": ("Only the %s can use this", "Solo el %s puede usar esto"),
    "trait.sofe.holy": ("Holy: half again against the undead, heals you a little", "Sagrado: la mitad más contra los no muertos, te cura un poco"),
    "trait.sofe.poison": ("Venom: poisons the target", "Veneno: envenena al objetivo"),
    "entity.sofe.thrown_weapon": ("Thrown Weapon", "Arma Lanzada"),
    "entity.sofe.spell_bolt": ("Spell Bolt", "Rayo de Hechizo"),
    "entity.sofe.bomb": ("Bomb", "Bomba"),
}
