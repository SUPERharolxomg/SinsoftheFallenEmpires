"""The Codex (docs/Anexos.md, "The Codex"): its chapters of lore and its bestiary, in English and Spanish, written into
the lang files. CodexScreen shows them; a chapter opens with its act, a creature once it has fallen to the Bearer.

Run from the repository root: python scripts/make_codex.py
"""
import collections
import json
import os

LANG = os.path.join("src", "main", "resources", "assets", "sofe", "lang")

# chapter: (act it opens at, 0 = always; or "nahrazel" once he has fallen), title, text
CHAPTERS = [
    (0, ("The Pact of the Five Crowns", "El Pacto de las Cinco Coronas"),
     ("A thousand years ago five empires signed the Pact of the Five Crowns: Sulthari, Nordrath, Parsivan, Khemet and Aureum. "
      "Under ten shared Laws they ruled Aetheris in peace, and each empire set its best to guard one of them.",
      "Hace mil años cinco imperios firmaron el Pacto de las Cinco Coronas: Sulthari, Nordrath, Parsivan, Khemet y Aureum. "
      "Bajo diez Leyes compartidas gobernaron Aetheris en paz, y cada imperio puso a sus mejores a guardar una de ellas.")),
    (0, ("The Void Codex", "El Códice del Vacío"),
     ("The Codex is no book of power. It is a prison, and in it sleeps Nahrazel, the First Fallen, older than the gods of Aetheris, "
      "who feeds on mortal pride. The five emperors found it beneath the Sulthari desert. Its pages promised the knowledge they lacked to become gods.",
      "El Códice no es un libro de poder. Es una prisión, y en ella duerme Nahrazel, el Primer Caído, más antiguo que los dioses de Aetheris, "
      "que se alimenta del orgullo de los mortales. Los cinco emperadores lo hallaron bajo el desierto de Sulthari. Sus páginas prometían el saber que les faltaba para ser dioses.")),
    (0, ("The Ritual and the Fall", "El Ritual y la Caída"),
     ("On one night each emperor read one page aloud. The seal cracked, and seven fragments of Nahrazel came out of the crack: the Seven Archsins. "
      "Each took a realm and twisted the guardians of its Laws; by breaking the Law they kept, they became the Ten Broken Oaths.",
      "En una misma noche cada emperador leyó en voz alta una página. El sello se agrietó, y de la grieta salieron siete fragmentos de Nahrazel: los Siete Archipecados. "
      "Cada uno tomó un reino y torció a los guardianes de sus Leyes; al romper la Ley que custodiaban, se volvieron los Diez Juramentos Rotos.")),
    (0, ("The Last Seal", "El Último Sello"),
     ("The sultan of Sulthari, repentant, gave his life to close the crack beneath the Great Observatory. "
      "Sulthari survived behind walls of aetherium. The other four empires fell, and where an Archsin reigns, the aetherium runs black.",
      "El sultán de Sulthari, arrepentido, dio su vida para cerrar la grieta bajo el Gran Observatorio. "
      "Sulthari sobrevivió tras murallas de aetherio. Los otros cuatro imperios cayeron, y donde reina un Archipecado, el aetherio se vuelve negro.")),
    (1, ("The Night of the Eclipse", "La Noche del Eclipse"),
     ("A thousand years later an eclipse weakened the seal. The crack opened again, the Codex split, and five shards pierced five people in the city: "
      "the Bearers. Grand Vizier Ozhan sent them to recover the fragments before the Archsins could.",
      "Mil años después un eclipse debilitó el sello. La grieta volvió a abrirse, el Códice se partió, y cinco fragmentos atravesaron a cinco personas de la ciudad: "
      "los Portadores. El Gran Visir Ozhan los envió a recuperar los fragmentos antes que los Archipecados.")),
    (2, ("The Northern Campaign", "La Campaña del Norte"),
     ("In Nordrath the clans fought an endless war: Vorath raised their dead every dawn to fight again. When Wrath fell, its fragment spoke with another voice: "
      "\"Thank you, Bearer.\"",
      "En Nordrath los clanes libraban una guerra sin fin: Vorath alzaba a sus muertos cada amanecer para volver a luchar. Cuando cayó la Ira, su fragmento habló con otra voz: "
      "\"Gracias, Portador.\"")),
    (3, ("The Eastern Shadows", "Las Sombras del Este"),
     ("Parsivan dreamed and would not wake; Khemet was a necropolis where the living would not move. In Khemet the sultan's diary told the truth: "
      "the Archsins need a Bearer to hand the fragments over willingly.",
      "Parsivan soñaba y no quería despertar; Khemet era una necrópolis donde los vivos no querían moverse. En Khemet el diario del sultán contó la verdad: "
      "los Archipecados necesitan que un Portador les entregue los fragmentos por voluntad propia.")),
    (4, ("The Western Ruins", "Las Ruinas del Oeste"),
     ("Aureum, the marble republic, had become a market of souls, its colosseum a place where the dead fought for coins. "
      "When Envy fell, a message came: Sulthari was under siege.",
      "Aureum, la república de mármol, se había vuelto un mercado de almas, y su coliseo un lugar donde los muertos luchaban por monedas. "
      "Cuando cayó la Envidia, llegó un mensaje: Sulthari estaba sitiada.")),
    (5, ("The Ascension", "La Ascensión"),
     ("Prython raised the Celestial Spire above the city, and Ozhan was his servant all along. Prython offered the rule of Aetheris for the fragments. "
      "When he fell, the Spire sank beneath the city to the Inverted Throne, where Nahrazel waited.",
      "Prython alzó la Aguja Celestial sobre la ciudad, y Ozhan había sido su siervo desde siempre. Prython ofreció el dominio de Aetheris a cambio de los fragmentos. "
      "Cuando cayó, la Aguja se hundió bajo la ciudad hasta el Trono Invertido, donde esperaba Nahrazel.")),
    ("nahrazel", ("The Eighth Lock", "La Octava Cerradura"),
     ("The seal was rewritten and the First Fallen sealed again. But the seal has eight locks, not seven, and on the last page one lock is still dark. "
      "No one knows which sin it holds.",
      "El sello fue reescrito y el Primer Caído quedó sellado de nuevo. Pero el sello tiene ocho cerraduras, no siete, y en la última página una sigue a oscuras. "
      "Nadie sabe qué pecado guarda.")),
]

# the bestiary: id, title, lore
BEASTS = [
    ("brass_sentinel", ("Corrupted automaton of Sulthari", "Autómata corrompido de Sulthari"),
     ("A guardian of the city's clockwork, turned by the crack on the Night of the Eclipse. Its pistons still keep the old patrol.",
      "Un guardián del mecanismo de la ciudad, torcido por la grieta en la Noche del Eclipse. Sus pistones aún siguen la vieja ronda.")),
    ("kaleth", ("The Burning Blade, Broken Oath of Law I", "La Hoja Ardiente, Juramento Roto de la Ley I"),
     ("He swore never to raise his sword against one who surrenders. Now he finishes off the fallen, in the fires of the Nordrath Forge.",
      "Juró no alzar jamás su espada contra quien se rinde. Ahora remata a los caídos, en los fuegos de la Forja de Nordrath.")),
    ("serath", ("The Blood Maiden, Broken Oath of Law II", "La Doncella de Sangre, Juramento Roto de la Ley II"),
     ("She guarded the honour of the fathers' blood. In the Nordrath Arena she drinks it.",
      "Guardaba el honor de la sangre de los padres. En la Arena de Nordrath, la bebe.")),
    ("vorath", ("Archsin of Wrath", "Archipecado de la Ira"),
     ("From the Burning Citadel he raised Nordrath's dead every dawn, so that the war would never end.",
      "Desde la Ciudadela Ardiente alzaba a los muertos de Nordrath cada amanecer, para que la guerra no terminara nunca.")),
    ("mirael", ("The Whisperer, Broken Oath of Law III", "La Susurrante, Juramento Roto de la Ley III"),
     ("She swore not to take a heart not given to her. In the Parsivan Baths she steals minds with a whisper.",
      "Juró no tomar un corazón que no le fuera dado. En los Baños de Parsivan roba mentes con un susurro.")),
    ("thessyn", ("The Silk Weaver, Broken Oath of Law IV", "El Tejedor de Seda, Juramento Roto de la Ley IV"),
     ("He spoke the truth before the throne. Now he weaves lies of silk across the Silk Road.",
      "Decía la verdad ante el trono. Ahora teje mentiras de seda sobre la Ruta de la Seda.")),
    ("luxara", ("Archsin of Lust", "Archipecado de la Lujuria"),
     ("In the Enchanted Gardens she charms and divides, and the court of Parsivan dreams of her.",
      "En los Jardines Encantados encanta y divide, y la corte de Parsivan sueña con ella.")),
    ("dormiel", ("The Dreamer, Broken Oath of Law V", "El Soñador, Juramento Roto de la Ley V"),
     ("He kept watch while others slept. In the Khemet Catacombs he lulls the living into nightmares.",
      "Velaba mientras otros dormían. En las Catacumbas de Khemet arrulla a los vivos hacia las pesadillas.")),
    ("morthis", ("Archsin of Sloth", "Archipecado de la Pereza"),
     ("He never moves. In the Stagnant Marsh the mummies rise for him, and everything near him slows to a stop.",
      "Nunca se mueve. En la Ciénaga Estancada las momias se alzan por él, y todo lo que se le acerca se detiene.")),
    ("goldarc", ("The Coinlord, Broken Oath of Law VI", "El Señor de las Monedas, Juramento Roto de la Ley VI"),
     ("He swore not to hoard what another needs. The Aureum Treasury is his, and so is every coin in it.",
      "Juró no acaparar lo que otro necesita. El Tesoro de Aureum es suyo, y también cada moneda que guarda.")),
    ("nixara", ("The Hollow Merchant, Broken Oath of Law VII", "El Mercader Hueco, Juramento Roto de la Ley VII"),
     ("She swore not to rob one who trusts her. In the Aureum Market she sells false gold over open trapdoors.",
      "Juró no robar a quien confía en ella. En el Mercado de Aureum vende oro falso sobre trampillas abiertas.")),
    ("avarok", ("Archsin of Greed", "Archipecado de la Avaricia"),
     ("In the Golden Vaults he steals from every Bearer, and grows with every theft.",
      "En las Cámaras Doradas roba a cada Portador, y crece con cada robo.")),
    ("fenrath", ("The Devourer, Broken Oath of Law VIII", "El Devorador, Juramento Roto de la Ley VIII"),
     ("He shared the winter bread. In the Nordrath Caverns he swallows whoever comes near.",
      "Compartía el pan del invierno. En las Cavernas de Nordrath se traga a quien se acerca.")),
    ("gularth", ("Archsin of Gluttony", "Archipecado de la Gula"),
     ("In the Feast Halls beneath Nordrath he eats the very floor, and grows.",
      "En las Salas del Festín bajo Nordrath se come hasta el suelo, y crece.")),
    ("shadeyn", ("The Mirror, Broken Oath of Law IX", "El Espejo, Juramento Roto de la Ley IX"),
     ("He swore not to covet another's crown. In the Aureum Colosseum he wears copies of yours.",
      "Juró no codiciar la corona de otro. En el Coliseo de Aureum lleva copias de la tuya.")),
    ("envyris", ("Archsin of Envy", "Archipecado de la Envidia"),
     ("On the Shadow Throne she becomes the Bearers who hurt her most, and answers each with their own skills.",
      "En el Trono de las Sombras se convierte en los Portadores que más la hieren, y responde a cada uno con sus propias habilidades.")),
    ("solrath", ("The False Prophet, Broken Oath of Law X", "El Falso Profeta, Juramento Roto de la Ley X"),
     ("No one shall stand above the Law, he swore, as Grand Vizier Ozhan. In the Temple of Sulthari he raises the fallen Oaths again.",
      "Nadie estará por encima de la Ley, juró, como Gran Visir Ozhan. En el Templo de Sulthari vuelve a alzar a los Juramentos caídos.")),
    ("prython", ("Archsin of Pride", "Archipecado de la Soberbia"),
     ("On the Celestial Spire he wields the sins of all six before him, and offers the Bearer the rule of Aetheris.",
      "En la Aguja Celestial empuña los pecados de los seis anteriores, y ofrece al Portador el dominio de Aetheris.")),
    ("nahrazel", ("The First Fallen", "El Primer Caído"),
     ("A colossus of ash, all seven sins at once, on the Inverted Throne beneath Sulthari. The Codex was made to hold him.",
      "Un coloso de ceniza, los siete pecados a la vez, en el Trono Invertido bajo Sulthari. El Códice se hizo para contenerlo.")),
]

# the line under each name: the Law a Broken Oath kept, the realm an Archsin rules (README, 2.4 and 2.5)
LAWS = {"kaleth": ("I", "You shall not raise your sword against one who surrenders", "No alzarás tu espada contra quien se rinde"),
        "serath": ("II", "You shall honor the blood of your fathers", "Honrarás la sangre de tus padres"),
        "mirael": ("III", "You shall not take a heart that is not given to you", "No tomarás un corazón que no te sea dado"),
        "thessyn": ("IV", "You shall speak the truth before the throne", "Dirás la verdad ante el trono"),
        "dormiel": ("V", "You shall keep watch while others sleep", "Velarás mientras otros duermen"),
        "goldarc": ("VI", "You shall not hoard what another needs", "No acapararás lo que otro necesita"),
        "nixara": ("VII", "You shall not rob one who trusts you", "No robarás a quien confía en ti"),
        "fenrath": ("VIII", "You shall share the winter bread", "Compartirás el pan del invierno"),
        "shadeyn": ("IX", "You shall not covet another's crown", "No codiciarás la corona de otro"),
        "solrath": ("X", "No one shall stand above the Law", "Nadie estará por encima de la Ley")}
REALMS = {"vorath": ("Lord of the Burning Citadel, Nordrath", "Señor de la Ciudadela Ardiente, Nordrath"),
          "luxara": ("Lady of the Enchanted Gardens, Parsivan", "Señora de los Jardines Encantados, Parsivan"),
          "morthis": ("Lord of the Stagnant Marsh, Khemet", "Señor de la Ciénaga Estancada, Khemet"),
          "avarok": ("Lord of the Golden Vaults, Aureum", "Señor de las Cámaras Doradas, Aureum"),
          "gularth": ("Lord of the Feast Halls, beneath Nordrath", "Señor de las Salas del Festín, bajo Nordrath"),
          "envyris": ("Lady of the Shadow Throne, Aureum", "Señora del Trono de las Sombras, Aureum"),
          "prython": ("Lord of the Celestial Spire, above Sulthari", "Señor de la Aguja Celestial, sobre Sulthari"),
          "nahrazel": ("Sealed in the Void Codex, beneath Sulthari", "Sellado en el Códice del Vacío, bajo Sulthari")}


def beast_title(beast, fallback):
    if beast in LAWS:
        n, en, es = LAWS[beast]
        return ("Broken Oath of Law %s: \"%s\"" % (n, en), "Juramento Roto de la Ley %s: \"%s\"" % (n, es))
    return REALMS.get(beast, fallback)


UI = {
    "codex.sofe.title": ("The Codex", "El Códice"),
    "codex.sofe.tab.story": ("Story", "Historia"),
    "codex.sofe.tab.bestiary": ("Bestiary", "Bestiario"),
    "codex.sofe.tab.gallery": ("Gallery", "Galería"),
    "codex.sofe.locked": ("Not yet written", "Aún sin escribir"),
    "codex.sofe.locked_beast": ("Still unknown: defeat it to learn its story", "Aún desconocido: derrótalo para conocer su historia"),
    "codex.sofe.empty_gallery": ("No illustration unlocked yet", "Aún no hay ilustraciones desbloqueadas"),
    "codex.sofe.progress": ("Act %s · %s of %s great foes defeated", "Acto %s · %s de %s grandes enemigos vencidos"),
    "codex.sofe.view": ("View illustration", "Ver ilustración"),
    "menu.sofe.codex": ("The Codex", "El Códice"),
}


def main():
    lang = dict(UI)
    for i, (_, title, text) in enumerate(CHAPTERS, 1):
        lang["codex.sofe.chapter.%d" % i] = title
        lang["codex.sofe.chapter.%d.text" % i] = text
    for beast, heading, lore in BEASTS:
        lang["codex.sofe.beast.%s.title" % beast] = beast_title(beast, heading)
        lang["codex.sofe.beast.%s.lore" % beast] = lore
    for code, k in (("en_us", 0), ("es_es", 1)):
        path = os.path.join(LANG, code + ".json")
        data = json.load(open(path, encoding="utf-8"), object_pairs_hook=collections.OrderedDict)
        for key, value in lang.items():
            data[key] = value[k]
        open(path, "w", encoding="utf-8").write(json.dumps(data, ensure_ascii=False, indent=2) + "\n")
    print("codex: %d chapters, %d creatures" % (len(CHAPTERS), len(BEASTS)))


if __name__ == "__main__":
    main()
