"""Starts the mod on a real (production) Forge dedicated server, as docs/Servidor.md tells a server owner to: the mod's
jar, the server side of the pack (no client-only mods), a new journey (level-type=sofe:aetheris), then a few commands
from the console; it stops the server and says whether it came up and what went wrong.

Needs the Forge server installed in build/packcheck/server (java -jar forge-installer.jar --installServer there) and
the mod's jar (./gradlew jar). Run from the repository root:
    python scripts/server_check.py                 # the pack's server side
    python scripts/server_check.py --extra ftb     # with FTB Essentials too
The server's log stays in build/packcheck/server/logs/latest.log.
"""
import argparse
import os
import shutil
import subprocess
import sys
import threading
import time
from pathlib import Path

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import pack_check  # noqa: E402

SERVER = pack_check.PACK / "server"
CLIENT_ONLY = ("embeddium", "entityculling", "immediatelyfast", "dynamic-fps", "xaeros-minimap")
COMMANDS = ["sofe pacts", "list", "forge tps"]


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--extra", default="")
    ap.add_argument("--memory", default="4G")
    ap.add_argument("--keep-world", action="store_true")
    ap.add_argument("--commands", default="", help="more console commands, separated by ;")
    ap.add_argument("--hold", type=int, default=0, help="keep the server up this many seconds (for a client to join)")
    args = ap.parse_args()
    p = pack_check.props()
    mods = SERVER / "mods"
    shutil.rmtree(mods, ignore_errors=True)
    mods.mkdir(parents=True)
    jars = [j for j in pack_check.mods(p, True) if not any(c in j.name for c in CLIENT_ONLY)] + pack_check.extra_jars(args.extra)
    for j in jars:
        shutil.copy2(j, mods / j.name)
    shutil.copytree(pack_check.ROOT / "pack" / "config", SERVER / "config", dirs_exist_ok=True)
    if not args.keep_world:
        shutil.rmtree(SERVER / "world", ignore_errors=True)
    (SERVER / "eula.txt").write_text("eula=true\n")
    (SERVER / "server.properties").write_text("level-type=sofe\\:aetheris\nonline-mode=false\nspawn-protection=0\nview-distance=8\n"
                                              "simulation-distance=6\nmotd=Sins of the Fallen Empires\n")
    (SERVER / "user_jvm_args.txt").write_text("-Xms%s -Xmx%s -XX:+UseG1GC -XX:+ParallelRefProcEnabled -XX:MaxGCPauseMillis=200\n"
                                              % (args.memory, args.memory))
    java = next((pack_check.PACK / "mc" / "jvm").glob("*/bin/java.exe"))
    win_args = SERVER / "libraries" / "net" / "minecraftforge" / "forge" / ("%s-%s" % (p["minecraft_version"], p["forge_version"])) / "win_args.txt"
    print("%d mods; starting the server" % len(jars))
    proc = subprocess.Popen([str(java), "@user_jvm_args.txt", "@" + str(win_args.relative_to(SERVER)), "nogui"], cwd=SERVER,
                            stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True, encoding="utf-8", errors="replace")
    lines, done, start = [], threading.Event(), time.time()

    def read():
        for line in proc.stdout:
            lines.append(line.rstrip())
            if "]: Done (" in line:
                done.set()
    threading.Thread(target=read, daemon=True).start()
    up = done.wait(900)
    took = time.time() - start
    if up:
        for c in COMMANDS + [c.strip() for c in args.commands.split(";") if c.strip()]:
            proc.stdin.write(c + "\n")
            proc.stdin.flush()
            time.sleep(2)
        time.sleep(args.hold)
        proc.stdin.write("stop\n")
        proc.stdin.flush()
    else:
        proc.kill()
    proc.wait(300)
    errors = [l for l in lines if ("/ERROR]" in l or "/FATAL]" in l or "Exception" in l) and "VersionCheck" not in l]
    print("server up: %s in %.0f s" % (up, took))
    for l in lines:
        if "joined the game" in l or "left the game" in l or "lost connection" in l or "[Chunky]" in l:
            print("  " + l[-180:])
        if "Journey integrity" in l or "free mode" in l or "Loaded" in l and "boss lairs" in l or "]: Done (" in l or "TPS" in l or "Pacts" in l.title():
            print("  " + l[-180:])
    print("errors and exceptions: %d" % len(errors))
    for l in errors[:15]:
        print("  " + l[-220:])


if __name__ == "__main__":
    main()
