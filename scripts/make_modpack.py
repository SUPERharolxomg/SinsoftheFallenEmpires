"""Builds the CurseForge modpack: build/modpack/<name>-<version>.zip, the format the CurseForge app and site take
(manifest.json, modlist.html and overrides/). Every mod is referenced by its CurseForge project and file id from
pack/curseforge.json; the pack's own config (pack/config) goes in the overrides.

Sins of the Fallen Empires itself must be referenced the same way once it is published on CurseForge: give its ids
with --sofe-project and --sofe-file (or write them into pack/curseforge.json). Without them the mod's jar from
build/libs is put in overrides/mods: fine for importing the zip into the CurseForge app to try it, not for uploading.

Run from the repository root after ./gradlew build:
    python scripts/make_modpack.py
    python scripts/make_modpack.py --sofe-project 123456 --sofe-file 7654321
"""
import argparse
import glob
import html
import io
import json
import os
import re
import zipfile

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(ROOT, "build", "modpack")


def props():
    out = {}
    for line in io.open(os.path.join(ROOT, "gradle.properties"), encoding="utf-8"):
        m = re.match(r"^([\w.]+)=(.*)$", line.strip())
        if m:
            out[m.group(1)] = m.group(2)
    return out


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--sofe-project", type=int)
    ap.add_argument("--sofe-file", type=int)
    args = ap.parse_args()
    p = props()
    pack = json.load(io.open(os.path.join(ROOT, "pack", "curseforge.json"), encoding="utf-8"))
    version = p["mod_version"]
    files, rows, local_jar = [], [], None
    for mod in pack["mods"]:
        project, file = mod["projectID"], mod["fileID"]
        if mod["name"] == p["mod_name"]:
            project, file = args.sofe_project or project, args.sofe_file or file
            if not (project and file):
                jars = [j for j in glob.glob(os.path.join(ROOT, "build", "libs", "*.jar")) if not j.endswith(("-sources.jar", "-slim.jar"))]
                if not jars:
                    raise SystemExit("no mod jar in build/libs (run ./gradlew build)")
                local_jar = max(jars, key=os.path.getmtime)
                rows.append("<li>%s %s (included, not yet on CurseForge)</li>" % (html.escape(mod["name"]), html.escape(version)))
                continue
        files.append({"projectID": project, "fileID": file, "required": True})
        rows.append('<li><a href="https://www.curseforge.com/projects/%d">%s</a> (%s)</li>' % (project, html.escape(mod["name"]), html.escape(mod["note"])))
    manifest = {
        "minecraft": {"version": pack["minecraft"], "modLoaders": [{"id": "forge-" + pack["forge"], "primary": True}]},
        "manifestType": "minecraftModpack",
        "manifestVersion": 1,
        "name": pack["name"],
        "version": version,
        "author": p["mod_authors"],
        "files": files,
        "overrides": "overrides",
    }
    os.makedirs(OUT, exist_ok=True)
    zip_path = os.path.join(OUT, "%s-%s.zip" % (pack["name"].replace(" ", "-"), version))
    with zipfile.ZipFile(zip_path, "w", zipfile.ZIP_DEFLATED) as z:
        z.writestr("manifest.json", json.dumps(manifest, indent=2) + "\n")
        z.writestr("modlist.html", "<ul>\n" + "\n".join(rows) + "\n</ul>\n")
        for f in glob.glob(os.path.join(ROOT, "pack", "config", "**", "*"), recursive=True):
            if os.path.isfile(f):
                z.write(f, "overrides/config/" + os.path.relpath(f, os.path.join(ROOT, "pack", "config")).replace(os.sep, "/"))
        if local_jar:
            z.write(local_jar, "overrides/mods/" + os.path.basename(local_jar))
    print("%s: %d mods from CurseForge%s" % (os.path.relpath(zip_path, ROOT), len(files),
                                             ", the mod's jar included (for the CurseForge app only)" if local_jar else ""))


if __name__ == "__main__":
    main()
