"""The places of Acts III and IV in data/sofe/structure_positions.json (dungeons, their arenas, Sealed Gates and
Waystones), their gate conditions and hints, and the lairs of their bosses in data/sofe/boss_lairs.json. Arenas
and lairs are given relative to their place, so moving a place moves all of it. Built by EmpireBuilder.

Run from the repository root: python scripts/act34_places.py
"""
import json
import os

DATA = "src/main/resources/data/sofe"
pos_path = os.path.join(DATA, "structure_positions.json")
pos = json.load(open(pos_path, encoding="utf-8"))

# id: (x, z, size_x, size_z, door) — door "west" (Parsivan, Khemet), "east" (Aureum), or None (placed by hand)
PLACES = {
    "parsivan/baths": (3600, -500, 48, 48, "west"),
    "parsivan/silk_road": (5200, 700, 56, 56, "west"),
    "parsivan/enchanted_gardens": (9000, -5000, 96, 96, "west"),
    "khemet/catacombs": (3200, 4000, 64, 40, "west"),
    "khemet/stagnant_marsh": (8000, 8500, 96, 96, "west"),
    "aureum/treasury": (-3600, -500, 48, 48, "east"),
    "aureum/market": (-3600, 1500, 64, 64, "east"),
    "aureum/golden_vaults": (-8000, 500, 72, 72, "east"),
    "aureum/colosseum": (-6000, 7000, 96, 80, "colosseum"),
    "aureum/shadow_throne": (-9000, -8000, 64, 64, "east"),
    "nordrath/caverns": (-600, -2484, 48, 48, None),
    "nordrath/feast_halls": (-600, -2550, 56, 40, None),
}
P = {k: (v[0], v[1]) for k, v in PLACES.items()}
# arena: (place, dx, dz, size_x, size_z); lair: (boss, place, dx, dz, radius)
ARENA_OF = {
    "parsivan/baths_pool": ("parsivan/baths", 0, 0, 18, 18),
    "parsivan/caravanserai_court": ("parsivan/silk_road", 0, 0, 36, 36),
    "parsivan/gardens_heart": ("parsivan/enchanted_gardens", 0, 0, 26, 26),
    "khemet/catacombs_crypt": ("khemet/catacombs", 8, 0, 44, 30),
    "khemet/marsh_pyramid": ("khemet/stagnant_marsh", -6, 0, 30, 30),
    "aureum/treasury_hall": ("aureum/treasury", 0, 0, 30, 30),
    "aureum/market_forum": ("aureum/market", 0, 0, 30, 30),
    "aureum/vault_floor": ("aureum/golden_vaults", 0, 0, 30, 30),
    "aureum/colosseum_floor": ("aureum/colosseum", 0, 0, 50, 40),
    "aureum/throne_room": ("aureum/shadow_throne", 0, 0, 40, 40),
    "nordrath/caverns_floor": ("nordrath/caverns", 0, 0, 30, 30),
    "nordrath/feast_hall": ("nordrath/feast_halls", 0, 0, 40, 30),
}
ARENAS = {k: (P[v[0]][0] + v[1], P[v[0]][1] + v[2], v[3], v[4]) for k, v in ARENA_OF.items()}
LAIR_OF = {
    "sofe:mirael": ("parsivan/baths", 0, 0, 12), "sofe:thessyn": ("parsivan/silk_road", 0, 0, 16),
    "sofe:luxara": ("parsivan/enchanted_gardens", 0, 0, 12), "sofe:dormiel": ("khemet/catacombs", 8, 0, 16),
    "sofe:morthis": ("khemet/stagnant_marsh", -6, 0, 14), "sofe:goldarc": ("aureum/treasury", 0, 0, 14),
    "sofe:nixara": ("aureum/market", 0, 0, 14), "sofe:avarok": ("aureum/golden_vaults", 0, 0, 16),
    "sofe:fenrath": ("nordrath/caverns", 0, 0, 18), "sofe:gularth": ("nordrath/feast_halls", 0, 0, 18),
    "sofe:shadeyn": ("aureum/colosseum", 0, 0, 22), "sofe:envyris": ("aureum/shadow_throne", 0, 0, 18),
}
LAIRS = {k: (P[v[0]][0] + v[1], P[v[0]][1] + v[2], v[3]) for k, v in LAIR_OF.items()}
# gate id: condition id, its parts, hint (en, es)
CONDITIONS = {
    "parsivan_baths": ({"type": "act_reached", "act": 3}, ("The Baths open in Act III", "Los Baños se abren en el Acto III")),
    "silk_road": ({"type": "act_reached", "act": 3}, ("The caravanserai opens in Act III", "El caravasar se abre en el Acto III")),
    "enchanted_gardens": ({"type": "all_of", "conditions": [{"type": "boss_defeated", "boss": "sofe:mirael"}, {"type": "boss_defeated", "boss": "sofe:thessyn"}]},
                          ("Defeat Mirael and Thessyn to enter the Enchanted Gardens", "Derrota a Mirael y a Thessyn para entrar en los Jardines Encantados")),
    "stagnant_marsh": ({"type": "boss_defeated", "boss": "sofe:dormiel"}, ("Defeat Dormiel to reach the Stagnant Marsh", "Derrota a Dormiel para llegar al Pantano Estancado")),
    "aureum_treasury": ({"type": "act_reached", "act": 4}, ("The Treasury opens in Act IV", "El Tesoro se abre en el Acto IV")),
    "aureum_market": ({"type": "act_reached", "act": 4}, ("The Market opens in Act IV", "El Mercado se abre en el Acto IV")),
    "golden_vaults": ({"type": "all_of", "conditions": [{"type": "boss_defeated", "boss": "sofe:goldarc"}, {"type": "boss_defeated", "boss": "sofe:nixara"}]},
                      ("Defeat Goldarc and Nixara to open the Golden Vaults", "Derrota a Goldarc y a Nixara para abrir las Bóvedas Doradas")),
    "feast_halls": ({"type": "boss_defeated", "boss": "sofe:fenrath"}, ("Defeat Fenrath to enter the Feast Halls", "Derrota a Fenrath para entrar en los Salones del Festín")),
    "colosseum": ({"type": "boss_defeated", "boss": "sofe:gularth"}, ("Defeat Gularth to open the Colosseum", "Derrota a Gularth para abrir el Coliseo")),
    "shadow_throne": ({"type": "boss_defeated", "boss": "sofe:shadeyn"}, ("Defeat Shadeyn to reach the Shadow Throne", "Derrota a Shadeyn para llegar al Trono de las Sombras")),
}
GATE_OF = {
    "parsivan/baths": "parsivan_baths", "parsivan/silk_road": "silk_road", "parsivan/enchanted_gardens": "enchanted_gardens",
    "khemet/catacombs": "khemet_catacombs", "khemet/stagnant_marsh": "stagnant_marsh",
    "aureum/treasury": "aureum_treasury", "aureum/market": "aureum_market", "aureum/golden_vaults": "golden_vaults",
    "aureum/colosseum": "colosseum", "aureum/shadow_throne": "shadow_throne", "nordrath/feast_halls": "feast_halls",
}

for id, (x, z, sx, sz, door) in PLACES.items():
    pos["structures"]["sofe:" + id] = {"x": x, "z": z, "size_x": sx, "size_z": sz, "zone": "dungeon"}
    gate = GATE_OF.get(id)
    hint = "message.sofe.gate." + (gate or "")
    if door == "west":
        gx, gz, axis = x - sx // 2, z, "x"
        ws = (gx - 5, z)
    elif door == "east":
        gx, gz, axis = x + sx // 2 - 1, z, "x"
        ws = (gx + 5, z)
    elif door == "colosseum":
        gx, gz, axis = x + sx // 2 - 1, z, "x"
        ws = (gx + 6, z)
    elif id == "nordrath/feast_halls":
        gx, gz, axis = x, z + sz // 2 - 1, "z"
        ws = None
    else:
        gx = None
        ws = None
    if gate and gx is not None:
        g = {"x": gx, "z": gz, "condition": "sofe:" + gate, "hint": hint}
        if axis == "x":
            g["axis"] = "x"
        pos["gates"]["sofe:" + id] = g
    if ws:
        pos["waystones"]["sofe:" + id] = {"x": ws[0], "z": ws[1]}
pos["waystones"]["sofe:nordrath/caverns"] = {"x": -606, "z": -2394}
for id, (x, z, sx, sz) in ARENAS.items():
    pos["structures"]["sofe:" + id] = {"x": x, "z": z, "size_x": sx, "size_z": sz, "zone": "arena"}
open(pos_path, "w", encoding="utf-8").write(json.dumps(pos, ensure_ascii=False, indent=2) + "\n")

for cid, (cond, _) in CONDITIONS.items():
    open(os.path.join(DATA, "conditions", cid + ".json"), "w", encoding="utf-8").write(json.dumps(cond, indent=2) + "\n")

lairs_path = os.path.join(DATA, "boss_lairs.json")
lairs = json.load(open(lairs_path, encoding="utf-8"))
for boss, (x, z, r) in LAIRS.items():
    lairs["lairs"][boss] = {"x": x, "z": z, "radius": r}
open(lairs_path, "w", encoding="utf-8").write(json.dumps(lairs, ensure_ascii=False, indent=2) + "\n")

entries = {"message.sofe.gate." + cid: hint for cid, (_, hint) in CONDITIONS.items()}
for f, i in (("en_us", 0), ("es_es", 1)):
    path = f"src/main/resources/assets/sofe/lang/{f}.json"
    data = json.load(open(path, encoding="utf-8"))
    for k, v in entries.items():
        data[k] = v[i]
    open(path, "w", encoding="utf-8").write(json.dumps(data, ensure_ascii=False, indent=2) + "\n")
print("places", len(PLACES), "arenas", len(ARENAS), "lairs", len(LAIRS))
