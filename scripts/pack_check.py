"""Plays the mod the way a player gets it: a real (production) Forge 1.20.1 client with the mod's jar and every mod in
the pack, the performance mods included (docs/Rendimiento.md), with the memory a modest PC would give it. It opens
the PlaceShots world, screenshots the places asked for and logs frames per second and memory per shot.

The development client (./gradlew runClient) cannot load Entity Culling, ImmediatelyFast or Dynamic FPS (they are
built against the production names with no refmap), so this is where those are checked.

Needs portablemc (pip install portablemc) and the mod's jar (./gradlew jar). Run from the repository root:
    python scripts/pack_check.py                      # aureum/city,parsivan/city,bosses with 4 GB
    python scripts/pack_check.py --memory 3G --shots khemet/city,npcs:0
    python scripts/pack_check.py --no-perf-mods       # the same without the performance mods, to compare
    python scripts/pack_check.py --check compat --extra essential,ftb   # sealed regions against /home, /rtp...
    python scripts/pack_check.py --check title --extra essential        # a screenshot of the title screen
The instance lives in build/packcheck/instance: screenshots/, placeshots_perf.log and logs/latest.log.
"""
import argparse
import glob
import os
import re
import shutil
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
PACK = ROOT / "build" / "packcheck"
CACHE = Path.home() / ".gradle" / "caches" / "modules-2" / "files-2.1"


def props():
    out = {}
    for line in (ROOT / "gradle.properties").read_text(encoding="utf-8").splitlines():
        m = re.match(r"^([\w.]+)=(.*)$", line.strip())
        if m:
            out[m.group(1)] = m.group(2)
    return out


def cached(group, name, version, pattern="*.jar"):
    hits = [p for p in glob.glob(str(CACHE / group / name / version / "*" / pattern)) if not p.endswith("-sources.jar")]
    if not hits:
        sys.exit("not in the Gradle cache: %s:%s:%s (run ./gradlew runClient once)" % (group, name, version))
    return Path(hits[0])


def mods(p, perf):
    """The jars of the pack: the libraries and companions in build.gradle, and the performance mods."""
    jars = [
        cached("software.bernie.geckolib", "geckolib-forge-" + p["minecraft_version"], p["geckolib_version"]),
        cached("top.theillusivec4.curios", "curios-forge", p["curios_version"], "curios-forge-%s.jar" % p["curios_version"]),
        cached("mezz.jei", "jei-%s-forge" % p["minecraft_version"], p["jei_version"]),
        cached("curse.maven", "refurbished-furniture-897116", p["furniture_file"]),
        cached("curse.maven", "framework-549225", p["framework_file"]),
        cached("curse.maven", "supplementaries-412082", p["supplementaries_file"]),
        cached("curse.maven", "moonlight-499980", p["moonlight_file"]),
        cached("curse.maven", "small-ships-450659", p["smallships_file"]),
        cached("curse.maven", "xaeros-minimap-263420", p["xaero_minimap_file"]),
    ]
    if perf:
        for m in ("embeddium", "immediatelyfast", "entityculling", "modernfix", "ferrite-core", "canary", "saturn",
                  "memoryleakfix", "dynamic-fps"):
            jars.append(cached("maven.modrinth", m, p[m.replace("-", "") + "_version"]))
    built = [j for j in glob.glob(str(ROOT / "build" / "libs" / "*.jar")) if not j.endswith(("-sources.jar", "-slim.jar"))]
    if not built:
        sys.exit("no mod jar in build/libs (run ./gradlew jar)")
    jars.append(Path(max(built, key=os.path.getmtime)))
    return jars


# What docs/Rendimiento.md recommends for a Ryzen 3 3200G / GTX 1650 / 8 GB: the measures are taken with it
OPTIONS = {"renderDistance": "10", "simulationDistance": "6", "graphicsMode": "1", "maxFps": "260", "enableVsync": "false",
           "entityDistanceScaling": "0.75", "biomeBlendRadius": "1", "particles": "1", "mipmapLevels": "2",
           "pauseOnLostFocus": "false", "onboardAccessibility": "false", "skipMultiplayerWarning": "true"}


def prepare(instance, jars):
    mods_dir = instance / "mods"
    shutil.rmtree(mods_dir, ignore_errors=True)
    mods_dir.mkdir(parents=True)
    for j in jars:
        shutil.copy2(j, mods_dir / j.name)
    shutil.copytree(ROOT / "pack" / "config", instance / "config", dirs_exist_ok=True)
    (instance / "placeshots_perf.log").unlink(missing_ok=True)
    opts = instance / "options.txt"
    lines = dict(l.split(":", 1) for l in opts.read_text(encoding="utf-8").splitlines() if ":" in l) if opts.exists() else {}
    lines.update(OPTIONS)
    opts.write_text("".join("%s:%s\n" % kv for kv in lines.items()), encoding="utf-8")


# mods that players often add, to check the pack with them (--extra): where their jars come from
EXTRA = {
    "essential": ["https://cdn.modrinth.com/data/k2ZPuTBm/versions/soTLEi9f/Essential_1-5-0-1_forge_1-20-1.jar"],
    "ftb": ["https://maven.ftb.dev/releases/dev/ftb/mods/ftb-essentials-forge/2001.2.4/ftb-essentials-forge-2001.2.4.jar",
            "https://maven.ftb.dev/releases/dev/ftb/mods/ftb-library-forge/2001.2.13/ftb-library-forge-2001.2.13.jar",
            "https://cdn.modrinth.com/data/lhGA9TYQ/versions/1MKTLiiG/architectury-9.2.14-forge.jar"],
}


def extra_jars(names):
    import urllib.request
    out = []
    for name in [n for n in names.split(",") if n]:
        for url in EXTRA[name]:
            path = PACK / "extra" / url.rsplit("/", 1)[1]
            if not path.exists():
                path.parent.mkdir(parents=True, exist_ok=True)
                req = urllib.request.Request(url, headers={"User-Agent": "SUPERharolxomg/SinsoftheFallenEmpires pack_check"})
                path.write_bytes(urllib.request.urlopen(req).read())
            out.append(path)
    return out


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--memory", default="4G", help="what the game gets (-Xmx); 4G on a PC with 8 GB")
    ap.add_argument("--shots", default="aureum/city,parsivan/city,bosses")
    ap.add_argument("--no-perf-mods", action="store_true")
    ap.add_argument("--check", default="places", choices=["places", "compat", "title"])
    ap.add_argument("--extra", default="", help="other mods to add: " + ", ".join(EXTRA))
    args = ap.parse_args()
    from portablemc.forge import ForgeVersion
    from portablemc.standard import Context

    p = props()
    instance = PACK / ("instance_noperf" if args.no_perf_mods else "instance")
    jars = mods(p, not args.no_perf_mods) + extra_jars(args.extra)
    prepare(instance, jars)
    print("%d mods in %s" % (len(jars), instance / "mods"))

    version = ForgeVersion("%s-%s" % (p["minecraft_version"], p["forge_version"]), context=Context(PACK / "mc", instance))
    version.set_auth_offline("SoFEPackCheck", None)
    if args.check != "title":
        version.set_quick_play_singleplayer("shots_aetheris")
    version.resolution = (1280, 720)
    env = version.install()
    # the JVM path comes first; the pack's memory and garbage collector right after it (docs/Rendimiento.md)
    env.jvm_args[1:1] = ["-Xms%s" % args.memory, "-Xmx%s" % args.memory, "-XX:+UseG1GC", "-XX:+ParallelRefProcEnabled",
                         "-XX:MaxGCPauseMillis=200",
                         {"places": "-Dsofe.placeShots=" + args.shots, "compat": "-Dsofe.compatCheck=true", "title": "-Dsofe.titleShot=true"}[args.check]]
    env.run()
    log = instance / {"places": "placeshots_perf.log", "compat": "compatcheck.log", "title": "titleshot.log"}[args.check]
    print(log.read_text(encoding="utf-8") if log.exists() else "no placeshots_perf.log: see %s" % (instance / "logs" / "latest.log"))


if __name__ == "__main__":
    main()
