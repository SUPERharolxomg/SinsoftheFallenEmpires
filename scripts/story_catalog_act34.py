"""The story content of Acts III and IV that is data (Sprint 7): the refugees of Parsivan, Khemet and Aureum who wait
in their empire's camp, the fate side quests of those three regions (with one more quest each), the Bearers' own
quests for Acts III and IV, and what the Bearers say as NPCs in those acts. scripts/make_story.py writes them with
the Sprint 6 content of scripts/story_catalog.py.
"""

# The camps (scripts/make_camps.py) and the places the quests send the Bearer to
CAMP = {"parsivan": (4400, 100), "khemet": (5200, 5600), "aureum": (-5600, 1000)}
PLACES = {
    "baths": ("the Moonlit Baths", "los Baños a la Luz de la Luna", 3600, -500),
    "silk_road": ("the caravanserai on the Silk Road", "el caravasar de la Ruta de la Seda", 5200, 700),
    "gardens": ("the Enchanted Gardens", "los Jardines Encantados", 9000, -5000),
    "catacombs": ("the Catacombs of Khemet", "las Catacumbas de Khemet", 3200, 4000),
    "marsh": ("the Stagnant Marsh", "el Pantano Estancado", 8000, 8500),
    "treasury": ("the Treasury of Aureum", "el Tesoro de Aureum", -3600, -500),
    "market": ("the Forum Market", "el Mercado del Foro", -3600, 1500),
    "vaults": ("the Golden Vaults", "las Bóvedas Doradas", -8000, 500),
    "colosseum": ("the Colosseum", "el Coliseo", -6000, 7000),
}
CAMP_NAMES = {"parsivan": ("the Silk Wells camp", "el campamento de los Pozos de Seda"),
              "khemet": ("the Reed Shore camp", "el campamento de la Orilla de Juncos"),
              "aureum": ("the Broken Aqueduct camp", "el campamento del Acueducto Roto")}

# The refugees in each camp: id, names, lines while the Archsin rules (their default) and once it has fallen
# (the act after). They stand in their camp (EmpireBuilder.camp).
FOLK = {
    "parsivan": {
        "parsivan_poet": (("Poet Feyza", "Feyza la poeta"), 4, [
            ("Luxara's court still sings in the gardens. Every song is sweeter than the last, and every singer thinner.",
             "La corte de Luxara aún canta en los jardines. Cada canción es más dulce que la anterior, y cada cantante más delgado."),
            ("I fled with a single verse in my head: the one she could not make me forget. I will write it down when we are free.",
             "Huí con un solo verso en la cabeza: el que ella no pudo hacerme olvidar. Lo escribiré cuando seamos libres.")], [
            ("The verse I kept? It says: 'Desire is a door, not a home.' I wrote it on the camp's first wall.",
             "¿El verso que guardé? Dice: 'El deseo es una puerta, no un hogar.' Lo escribí en el primer muro del campamento.")]),
        "parsivan_gardener": (("Gardener Omid", "Omid el jardinero"), 4, [
            ("I tended the queen's roses for thirty years. Now they bloom at midnight and smell of honey and blood.",
             "Cuidé las rosas de la reina durante treinta años. Ahora florecen a medianoche y huelen a miel y a sangre."),
            ("The courtiers lie among them, smiling, and do not wake. Some of them were my friends.",
             "Los cortesanos yacen entre ellas, sonriendo, y no despiertan. Algunos eran mis amigos.")], [
            ("The roses bloom by day again. Ordinary roses. I never thought I would cry over ordinary roses.",
             "Las rosas florecen de día otra vez. Rosas comunes. Nunca pensé que lloraría por unas rosas comunes.")]),
        "parsivan_dancer": (("Dancer Neda", "Neda la bailarina"), 4, [
            ("The mirage dancers wear our faces. Mine is out there somewhere, still dancing for her.",
             "Las bailarinas espejismo llevan nuestros rostros. El mío anda por ahí, todavía bailando para ella."),
            ("If you meet a dancer who looks like me, do not hesitate. She is not me. I am here.",
             "Si encuentras una bailarina que se parece a mí, no dudes. No soy yo. Yo estoy aquí.")], [
            ("I danced last night, for nobody, for myself. It is the first dance that belonged to me in years.",
             "Anoche bailé, para nadie, para mí. Es el primer baile que me pertenece en años.")]),
    },
    "khemet": {
        "khemet_embalmer": (("Embalmer Sekhet", "Sekhet la embalsamadora"), 4, [
            ("The dead of Khemet are not resting, they are waiting. Morthis taught them that nothing is worth getting up for.",
             "Los muertos de Khemet no descansan, esperan. Morthis les enseñó que nada vale la pena para levantarse."),
            ("We still wrap them, out of habit. Habit is all that is left of the House of Thresholds.",
             "Todavía los envolvemos, por costumbre. La costumbre es lo único que queda de la Casa de los Umbrales.")], [
            ("The jars are singing again, the old way. The dead have somewhere to go.",
             "Los vasos canopos vuelven a cantar, a la manera antigua. Los muertos tienen adónde ir.")]),
        "khemet_ferryman": (("Ferryman Ani", "Ani el barquero"), 4, [
            ("I rowed the dead across the river for forty years. Now the river does not move, and neither do they.",
             "Remé a los muertos por el río durante cuarenta años. Ahora el río no se mueve, y ellos tampoco."),
            ("My oar is dry. A ferryman with a dry oar is a man with nothing to do but remember.",
             "Mi remo está seco. Un barquero con el remo seco es un hombre sin nada que hacer más que recordar.")], [
            ("The river woke up this morning. I nearly lost my hat. I have never been so happy to nearly lose a hat.",
             "El río despertó esta mañana. Casi pierdo el sombrero. Nunca había sido tan feliz de casi perder un sombrero.")]),
        "khemet_scribe": (("Scribe Meri", "Meri la escriba"), 4, [
            ("I copy the names of the dead onto sunreed papyrus, so someone remembers them. The list grows faster than my reed.",
             "Copio los nombres de los muertos en papiro de junco solar, para que alguien los recuerde. La lista crece más rápido que mi caña."),
            ("The old archive lies under the marsh now. Everything we were, rotting slowly, the way Morthis likes it.",
             "El viejo archivo yace bajo el pantano. Todo lo que fuimos, pudriéndose despacio, como le gusta a Morthis.")], [
            ("I am writing a new list: the names of those who came home. It is shorter. But it is growing.",
             "Estoy escribiendo una lista nueva: los nombres de los que volvieron a casa. Es más corta. Pero crece.")]),
    },
    "aureum": {
        "aureum_senator": (("Senator Varro", "Varro el senador"), 5, [
            ("The Republic had laws for everything, Bearer, except for a Senate that could be bought. Envyris did not need to buy us. We sold ourselves.",
             "La República tenía leyes para todo, Portador, excepto para un Senado que pudiera comprarse. Envyris no necesitó comprarnos. Nos vendimos solos."),
            ("I kept my toga and lost my vote. Here in the camp, at least, nobody pretends otherwise.",
             "Conservé la toga y perdí el voto. Aquí en el campamento, al menos, nadie finge lo contrario.")], [
            ("They are talking about elections in the camp. Elections! I had forgotten the word tasted like that.",
             "En el campamento hablan de elecciones. ¡Elecciones! Había olvidado que la palabra sabía así.")]),
        "aureum_gladiator": (("Gladiator Brennus", "Brennus el gladiador"), 5, [
            ("I won forty fights in the Colosseum. The crowd that cheered me now sits in the stands as shades, and still cheers.",
             "Gané cuarenta peleas en el Coliseo. El público que me aclamaba ahora se sienta en las gradas como sombras, y sigue aclamando."),
            ("They want blood more than they want bread. Envy turns spectators into wolves.",
             "Quieren sangre más que pan. La envidia convierte a los espectadores en lobos.")], [
            ("The sand of the arena is just sand now. I took a handful. I do not know why. Maybe to remember I left alive.",
             "La arena del Coliseo ahora es solo arena. Tomé un puñado. No sé por qué. Quizá para recordar que salí vivo.")]),
        "aureum_widow": (("Widow Livia", "Livia la viuda"), 5, [
            ("My husband minted coins for the Treasury. They took him for a coin he did not strike. Gold has a long memory and no mercy.",
             "Mi esposo acuñaba monedas para el Tesoro. Se lo llevaron por una moneda que no acuñó. El oro tiene buena memoria y ninguna piedad."),
            ("Everyone in Aureum envies someone. I envy the dead. They no longer count anything.",
             "En Aureum todos envidian a alguien. Yo envidio a los muertos. Ya no cuentan nada.")], [
            ("I bought bread today with a coin and the baker gave me change without counting it twice. Aureum is healing.",
             "Hoy compré pan con una moneda y el panadero me dio el cambio sin contarlo dos veces. Aureum está sanando.")]),
    },
}

# The fate side quests of Parsivan, Khemet and Aureum, and one more quest in each (no fate)
SIDE_QUESTS = [
    dict(id="parsivan_dreaming_court", region="parsivan", act=3, giver="parsivan_gardener", reach="gardens", spawn="sofe:mirage_dancer", count=6, xp=2200,
         title=("The Dreaming Court", "La Corte que Sueña"),
         offer=[("The courtiers in the gardens are not dead, Bearer. They are dreaming the dream Luxara gave them: no hunger, no age, no end.",
                  "Los cortesanos de los jardines no están muertos, Portador. Sueñan el sueño que Luxara les dio: sin hambre, sin vejez, sin fin."),
                 ("Her dancers guard them. Break the dancers, and then we must decide whether to wake them.",
                  "Sus bailarinas los guardan. Rompe a las bailarinas, y luego tendremos que decidir si despertarlos.")],
         accept=("I will go to the gardens.", "Iré a los jardines."),
         reminder=("The gardens, far to the north-east. The dancers do not sleep.", "Los jardines, lejos al noreste. Las bailarinas no duermen."),
         choice=[("The dancers are gone. The courtiers are still smiling in their sleep. Woken, they will remember everything they lost.",
                  "Las bailarinas ya no están. Los cortesanos siguen sonriendo en su sueño. Despiertos, recordarán todo lo que perdieron."),
                 ("Do we wake them, and let Parsivan grieve and rebuild? Or let them dream, happy, until the dream ends by itself?",
                  "¿Los despertamos, para que Parsivan llore y reconstruya? ¿O los dejamos soñar, felices, hasta que el sueño termine solo?")],
         answers=[("awakened", ("Wake them. A true life is worth its pain.", "Despiértalos. Una vida verdadera vale su dolor."),
                   ("Then I will sit by each one as they open their eyes. Someone should be there.",
                    "Entonces me sentaré junto a cada uno cuando abran los ojos. Alguien debería estar ahí.")),
                  ("asleep", ("Let them dream. They have suffered enough.", "Déjalos soñar. Ya sufrieron bastante."),
                   ("Then I will keep the roses for them. A garden for sleepers. Perhaps that is mercy.",
                    "Entonces cuidaré las rosas para ellos. Un jardín para durmientes. Quizá eso sea misericordia."))]),
    dict(id="parsivan_silk_caravan", region="parsivan", act=3, giver="parsivan_dancer", reach="silk_road", spawn="sofe:void_stalker", count=6, xp=1800,
         reward_item=("sofe:moonsilk", 8), title=("The Last Silk Caravan", "La Última Caravana de Seda"),
         offer=[("The last caravan of moonsilk never reached the camp. The Void beasts stalk the caravanserai where it stopped.",
                  "La última caravana de seda lunar nunca llegó al campamento. Las bestias del Vacío acechan el caravasar donde se detuvo."),
                 ("Bring back what you can. We need cloth for the wounded, and something beautiful to remember who we were.",
                  "Trae lo que puedas. Necesitamos tela para los heridos, y algo hermoso para recordar quiénes éramos.")],
         accept=("I will bring the silk home.", "Traeré la seda a casa."),
         reminder=("The caravanserai on the Silk Road, east of the Baths.", "El caravasar de la Ruta de la Seda, al este de los Baños."),
         done=[("Moonsilk! It still shines. Look at it, catching the light like nothing happened.",
                "¡Seda lunar! Todavía brilla. Mírala, atrapando la luz como si nada hubiera pasado."),
               ("Keep a share. Silk like this deserves to be worn by someone who fought for it.",
                "Quédate una parte. Una seda así merece vestirla alguien que luchó por ella.")]),
    dict(id="khemet_souls_below", region="khemet", act=3, giver="khemet_ferryman", reach="catacombs", spawn="sofe:bog_mummy", count=6, xp=2200,
         title=("The Souls Below", "Las Almas de Abajo"),
         offer=[("Down in the catacombs, the souls of the old kings sit in the dark with their eyes open. Morthis's mummies keep them there.",
                  "Abajo, en las catacumbas, las almas de los antiguos reyes se sientan en la oscuridad con los ojos abiertos. Las momias de Morthis las retienen."),
                 ("Clear the mummies. Then we must choose what those souls become.", "Despeja las momias. Luego debemos elegir en qué se convierten esas almas.")],
         accept=("Take me to the river's end.", "Llévame al final del río."),
         reminder=("The catacombs, south of the river. The kings are waiting.", "Las catacumbas, al sur del río. Los reyes esperan."),
         choice=[("The mummies are dust. The kings still wait, and the river is ready to carry them. Or they could stay, and stand guard.",
                  "Las momias son polvo. Los reyes siguen esperando, y el río está listo para llevarlos. O podrían quedarse, y montar guardia."),
                 ("Do we guide them across, to rest at last? Or bind them to the tombs, to guard the living against what comes next?",
                  "¿Los guiamos al otro lado, para que descansen por fin? ¿O los atamos a las tumbas, para que protejan a los vivos de lo que venga?")],
         answers=[("rest", ("Guide them across. They have waited enough.", "Guíalos al otro lado. Ya esperaron bastante."),
                   ("Then my oar is wet again. I will row until the last king is home.", "Entonces mi remo vuelve a estar mojado. Remaré hasta que el último rey llegue a casa.")),
                  ("guardians", ("Bind them. Khemet needs its kings to watch over it.", "Átalos. Khemet necesita que sus reyes la vigilen."),
                   ("Then the tombs will never be robbed again. But I will never row them home.",
                    "Entonces las tumbas nunca volverán a ser saqueadas. Pero nunca los llevaré a casa."))]),
    dict(id="khemet_sunken_archive", region="khemet", act=3, giver="khemet_scribe", reach="marsh", spawn="sofe:void_wretch", count=6, xp=1800,
         reward_item=("sofe:sunreed_papyrus", 12), title=("The Sunken Archive", "El Archivo Hundido"),
         offer=[("The archive of Khemet sank into the marsh, with every name, every law, every recipe for embalming the dead.",
                  "El archivo de Khemet se hundió en el pantano, con cada nombre, cada ley, cada receta para embalsamar a los muertos."),
                 ("The Void wretches nest in its ruins. Clear them, and I will dive for whatever papyrus survived.",
                  "Los desdichados del Vacío anidan en sus ruinas. Despéjalos y bucearé por el papiro que haya sobrevivido.")],
         accept=("Khemet will not forget itself.", "Khemet no se olvidará de sí misma."),
         reminder=("The Stagnant Marsh, far to the south-east.", "El Pantano Estancado, lejos al sureste."),
         done=[("Wet, blurred, but readable. The laws of the House of Thresholds, in my teacher's hand.",
                "Mojado, borroso, pero legible. Las leyes de la Casa de los Umbrales, con la letra de mi maestra."),
               ("Take this papyrus. Whatever you write on it will last; sunreed does not forget.",
                "Toma este papiro. Lo que escribas en él perdurará; el junco solar no olvida.")]),
    dict(id="aureum_gold_of_the_courts", region="aureum", act=4, giver="aureum_senator", reach="treasury", spawn="sofe:gilded_legionnaire", count=6, xp=3000,
         title=("The Gold of the Courts", "El Oro de los Tribunales"),
         offer=[("The Treasury still holds the gold that bought every judge in Aureum. Goldarc's legionnaires guard it as if it were sacred.",
                  "El Tesoro aún guarda el oro que compró a cada juez de Aureum. Los legionarios de Goldarc lo custodian como si fuera sagrado."),
                 ("Break the guard. Then the gold is ours, and we must decide what it is for.",
                  "Rompe la guardia. Entonces el oro será nuestro, y tendremos que decidir para qué sirve.")],
         accept=("Let us see this sacred gold.", "Veamos ese oro sagrado."),
         reminder=("The Treasury, west of Sulthari, over the border.", "El Tesoro, al oeste de Sulthari, pasando la frontera."),
         choice=[("The gold is free. Enough to rebuild the courts with honest judges and walls no coin can climb. Or enough to feed every family in Aureum for years.",
                  "El oro es libre. Suficiente para reconstruir los tribunales con jueces honestos y muros que ninguna moneda pueda escalar. O para alimentar a cada familia de Aureum durante años."),
                 ("Do we restore the Law, slowly and properly? Or give the gold back to the people it was stolen from?",
                  "¿Restauramos la Ley, despacio y como se debe? ¿O devolvemos el oro al pueblo al que se lo robaron?")],
         answers=[("law", ("Rebuild the courts. Justice first.", "Reconstruye los tribunales. Primero la justicia."),
                   ("Then I will sit in the Senate again, and this time I will not be for sale.", "Entonces volveré a sentarme en el Senado, y esta vez no estaré en venta.")),
                  ("shared", ("Give it back to the people.", "Devuélveselo al pueblo."),
                   ("Then tonight every hearth in Aureum will be warm. The Senate can wait. Bellies cannot.",
                    "Entonces esta noche cada hogar de Aureum estará caliente. El Senado puede esperar. Los estómagos no."))]),
    dict(id="aureum_bread_and_circuses", region="aureum", act=4, giver="aureum_gladiator", reach="colosseum", spawn="sofe:gladiator_shade", count=8, xp=2600,
         reward_item=("sofe:orichalcum_ingot", 4), title=("Bread and Circuses", "Pan y Circo"),
         offer=[("The shades of the crowd make the dead fight on in the Colosseum: gladiators who never got to retire.",
                  "Las sombras del público obligan a los muertos a seguir peleando en el Coliseo: gladiadores que nunca pudieron retirarse."),
                 ("My brothers are down there. Give them a last fight worth losing.", "Mis hermanos están ahí abajo. Dales una última pelea que valga la pena perder.")],
         accept=("They will have their last fight.", "Tendrán su última pelea."),
         reminder=("The Colosseum, far to the south-west.", "El Coliseo, lejos al suroeste."),
         done=[("The stands are quiet. My brothers are quiet. For the first time, the Colosseum sounds like a grave and not a market.",
                "Las gradas están en silencio. Mis hermanos están en silencio. Por primera vez el Coliseo suena a tumba y no a mercado."),
               ("This orichalcum was the prize they never claimed. Forge something that protects, not something that entertains.",
                "Este oricalco era el premio que nunca reclamaron. Forja algo que proteja, no algo que entretenga.")]),
]

# Who in each camp remembers the region's fate
FATE_WITNESS = {"parsivan": "parsivan_poet", "khemet": "khemet_embalmer", "aureum": "aureum_widow"}
FATE_REACTIONS = {
    "parsivan": {"awakened": ("The court is awake, and weeping, and arguing, and alive. I have never heard anything so beautiful.",
                              "La corte está despierta, y llora, y discute, y vive. Nunca oí nada tan hermoso."),
                 "asleep": ("The gardens are silent and full of smiles. Sometimes I go and read my verse to the sleepers. Nobody answers.",
                            "Los jardines están en silencio y llenos de sonrisas. A veces voy a leerles mi verso a los durmientes. Nadie responde.")},
    "khemet": {"rest": ("Every jar sings, every boat sails. Khemet is a land of the dead again, and so, at last, of the living.",
                        "Cada vaso canta, cada barca navega. Khemet vuelve a ser tierra de muertos, y por fin, de vivos."),
               "guardians": ("The kings stand at the tomb doors with their eyes open. The robbers have stopped coming. So have the mourners.",
                             "Los reyes vigilan las puertas de las tumbas con los ojos abiertos. Los ladrones dejaron de venir. Los dolientes también.")},
    "aureum": {"law": ("The courts open tomorrow. My husband's case will be the first. They say the judge cannot be bought. We shall see.",
                       "Los tribunales abren mañana. El caso de mi esposo será el primero. Dicen que el juez no se puede comprar. Ya veremos."),
               "shared": ("There is bread on every table and gold in every hand. Nobody envies anybody this week. It will not last, but it is sweet.",
                          "Hay pan en cada mesa y oro en cada mano. Nadie envidia a nadie esta semana. No durará, pero es dulce.")},
}

# The Bearers' own quests for Acts III and IV
BEARER_QUESTS_34 = [
    dict(id="cassian_act3", cls="knight", act=3, giver="parsivan_poet", reach="baths", spawn="sofe:void_wretch", count=8, relic="signet_of_the_order", xp=2600,
         title=("The Scale in the Baths", "La Balanza en los Baños"),
         offer=[("A knight of your Order came to the Moonlit Baths the night your brothers were tried. He never left.",
                  "Un caballero de tu Orden llegó a los Baños a la Luz de la Luna la noche en que juzgaron a tus hermanos. Nunca salió."),
                 ("They say his signet still gleams on the bottom of the pool, under the Void things that drink there.",
                  "Dicen que su sello aún brilla en el fondo del estanque, bajo las cosas del Vacío que beben allí.")],
         accept=("Then I will bring him home.", "Entonces lo traeré a casa."),
         reminder=("The Baths, west of the camp. The signet is waiting.", "Los Baños, al oeste del campamento. El sello espera."),
         done=[("His signet. And a letter: 'The judges were paid in Aureum gold. I go to warn the Order. If I fall, carry the Scale.'",
                "Su sello. Y una carta: 'A los jueces les pagaron con oro de Aureum. Voy a avisar a la Orden. Si caigo, lleva la Balanza.'"),
               ("He did not reach them. But you did. Wear it.", "Él no llegó hasta ellos. Pero tú sí. Llévalo.")]),
    dict(id="cassian_act4", cls="knight", act=4, giver="aureum_senator", reach="treasury", spawn="sofe:gilded_legionnaire", count=8, relic="helm_of_the_last_knight", xp=3800,
         title=("The Price of the Scale", "El Precio de la Balanza"),
         offer=[("Knight, I have read the Treasury ledgers. Your Order's trial was paid for in one entry: thirty talents, 'for the Scale'.",
                  "Caballero, he leído los registros del Tesoro. El juicio de tu Orden se pagó en una sola entrada: treinta talentos, 'por la Balanza'."),
                 ("The coins are still in the Treasury vaults, guarded. Take them back, and let the Senate see what it sold.",
                  "Las monedas siguen en las bóvedas del Tesoro, custodiadas. Recupéralas y que el Senado vea lo que vendió.")],
         accept=("Thirty talents. My brothers were worth more.", "Treinta talentos. Mis hermanos valían más."),
         reminder=("The Treasury. Thirty talents, and your brothers' names.", "El Tesoro. Treinta talentos, y los nombres de tus hermanos."),
         done=[("The Senate read the ledger aloud. Nobody spoke for a long time. Then they voted to clear the Order's name.",
                "El Senado leyó el registro en voz alta. Nadie habló durante largo rato. Luego votaron limpiar el nombre de la Orden."),
               ("Among the coins was this helm, pawned by the last knight to stand trial. It is yours now. The last knight is you.",
                "Entre las monedas estaba este yelmo, empeñado por el último caballero en ser juzgado. Ahora es tuyo. El último caballero eres tú.")]),
    dict(id="ankhareth_act3", cls="necromancer", act=3, giver="khemet_embalmer", reach="catacombs", spawn="sofe:bog_mummy", count=8, relic="ring_of_bound_souls", xp=2600,
         title=("The Teacher's Tomb", "La Tumba de la Maestra"),
         offer=[("Ankhareth. Your teacher lies in the catacombs, unwrapped. Morthis's mummies took her bandages for themselves.",
                  "Ankhareth. Tu maestra yace en las catacumbas, sin vendas. Las momias de Morthis se quedaron con ellas."),
                 ("Only an embalmer of the House may wrap her again. That is you. That was always going to be you.",
                  "Solo un embalsamador de la Casa puede volver a envolverla. Ese eres tú. Siempre ibas a ser tú.")],
         accept=("I will wrap her as she wrapped others.", "La envolveré como ella envolvía a otros."),
         reminder=("The catacombs. She is waiting, uncovered.", "Las catacumbas. Ella espera, descubierta."),
         done=[("You wrapped her. I saw your hands shake, and then stop shaking. That is how she taught you.",
                "La envolviste. Vi tus manos temblar, y luego dejar de temblar. Así te enseñó ella."),
               ("She wore this ring to the end. It holds the souls she could not guide. Guide them for her.",
                "Llevó este anillo hasta el final. Guarda las almas que no pudo guiar. Guíalas por ella.")]),
    dict(id="ankhareth_act4", cls="necromancer", act=4, giver="aureum_gladiator", reach="colosseum", spawn="sofe:gladiator_shade", count=8, relic="mask_of_the_ferryman", xp=3800,
         title=("The Unburied Games", "Los Juegos sin Sepultura"),
         offer=[("Priest of Khemet. In Aureum we never buried our gladiators. We dragged them out and sold the sand.",
                  "Sacerdote de Khemet. En Aureum nunca enterramos a nuestros gladiadores. Los sacábamos a rastras y vendíamos la arena."),
                 ("Their shades are in the Colosseum. They have never had a funeral. Can you give them one?",
                  "Sus sombras están en el Coliseo. Nunca tuvieron un funeral. ¿Puedes darles uno?")],
         accept=("Every soul deserves its crossing. Even here.", "Toda alma merece su travesía. Incluso aquí."),
         reminder=("The Colosseum. The shades have waited centuries.", "El Coliseo. Las sombras han esperado siglos."),
         done=[("You said their names, the ones the crowd never learned. I heard the stands go silent for the first time.",
                "Dijiste sus nombres, los que el público nunca aprendió. Oí las gradas callar por primera vez."),
               ("A ferryman's mask was found in the arena's cellar. Someone from Khemet came here before you. Wear it.",
                "En el sótano del Coliseo encontraron una máscara de barquero. Alguien de Khemet vino antes que tú. Llévala.")]),
    dict(id="shirin_act3", cls="sorceress", act=3, giver="parsivan_gardener", reach="gardens", spawn="sofe:mirage_dancer", count=8, relic="ring_of_three_runes", xp=2600,
         title=("The Sleeper by the Fountain", "La Durmiente junto a la Fuente"),
         offer=[("Lady Shirin. Your sister sleeps by the great fountain in the gardens. Luxara keeps her closest of all.",
                  "Señora Shirin. Tu hermana duerme junto a la gran fuente de los jardines. Luxara la mantiene más cerca que a nadie."),
                 ("The dancers will not let anyone near her. Except, perhaps, someone she would want to see.",
                  "Las bailarinas no dejan que nadie se acerque a ella. Excepto, quizá, alguien a quien ella quisiera ver.")],
         accept=("Laleh. I'm coming.", "Laleh. Ya voy."),
         reminder=("The gardens. She is by the fountain.", "Los jardines. Está junto a la fuente."),
         done=[("You reached her. She did not wake, but she smiled when you said her name, a real smile.",
                "Llegaste hasta ella. No despertó, pero sonrió cuando dijiste su nombre, una sonrisa de verdad."),
               ("She held this ring in her fist. Three runes: the ones you learned together. She was waiting for you.",
                "Tenía este anillo en el puño. Tres runas: las que aprendieron juntas. Te estaba esperando.")]),
    dict(id="shirin_act4", cls="sorceress", act=4, giver="aureum_widow", reach="market", spawn="sofe:void_stalker", count=8, relic="veil_of_laleh", xp=3800,
         title=("The Veil in the Market", "El Velo del Mercado"),
         offer=[("A veil of Parsivan silk is for sale in the Forum Market, with a price no one can pay. The merchant says it belonged to a sorceress.",
                  "Un velo de seda de Parsivan está a la venta en el Mercado del Foro, a un precio que nadie puede pagar. El mercader dice que fue de una hechicera."),
                 ("The Void beasts own the market now. Nobody is selling anything. Go and take it.",
                  "Las bestias del Vacío son dueñas del mercado. Nadie vende nada. Ve y tómalo.")],
         accept=("It is hers. I know it.", "Es de ella. Lo sé."),
         reminder=("The Forum Market, west of the camp.", "El Mercado del Foro, al oeste del campamento."),
         done=[("It smells of jasmine and ink. Laleh's smell, you say? Then she passed through Aureum, before the gardens.",
                "Huele a jazmín y a tinta. ¿El olor de Laleh, dices? Entonces pasó por Aureum, antes de los jardines."),
               ("Wear it. Whatever she was running from, she left this behind so you could follow.",
                "Llévalo. Huyera de lo que huyera, lo dejó atrás para que pudieras seguirla.")]),
    dict(id="rurik_act3", cls="thief", act=3, giver="parsivan_dancer", reach="silk_road", spawn="sofe:void_stalker", count=8, relic="hood_of_ash", xp=2600,
         title=("The Guild of the Silk Road", "El Gremio de la Ruta de la Seda"),
         offer=[("Grimsson, isn't it? The thieves of the Silk Road spoke of a northern boy who robbed them once and lived.",
                  "Grimsson, ¿verdad? Los ladrones de la Ruta de la Seda hablaban de un chico del norte que les robó una vez y sobrevivió."),
                 ("Their guildhouse is in the caravanserai, overrun. They kept records of every fence. Every one. Even the one who sold your clan.",
                  "Su casa del gremio está en el caravasar, invadida. Llevaban registro de cada perista. De todos. Incluso del que vendió a tu clan.")],
         accept=("Records. Finally, something worth stealing.", "Registros. Por fin, algo que vale la pena robar."),
         reminder=("The caravanserai. The guild's books are in the cellar.", "El caravasar. Los libros del gremio están en el sótano."),
         done=[("The book says your clan's goods went west, to a fence in Aureum's market. A hooded merchant with no face.",
                "El libro dice que los bienes de tu clan fueron al oeste, a un perista del mercado de Aureum. Un mercader encapuchado sin rostro."),
               ("And the guild left this hood. Ash-grey. They say it was made for the best thief they ever failed to catch.",
                "Y el gremio dejó esta capucha. Gris ceniza. Dicen que la hicieron para el mejor ladrón que nunca lograron atrapar.")]),
    dict(id="rurik_act4", cls="thief", act=4, giver="aureum_widow", reach="vaults", spawn="sofe:gilded_legionnaire", count=8, relic="boots_of_the_fjord_runner", xp=3800,
         title=("What the Vaults Hold", "Lo que Guardan las Bóvedas"),
         offer=[("The faceless merchant's stock went to the Golden Vaults when Avarok took them. Everything stolen in Aureum ends there.",
                  "La mercancía del mercader sin rostro fue a las Bóvedas Doradas cuando Avarok las tomó. Todo lo robado en Aureum termina ahí."),
                 ("If anything of your village survived, it is in those vaults.", "Si algo de tu aldea sobrevivió, está en esas bóvedas.")],
         accept=("Then I'm robbing the greediest vault in the world.", "Entonces voy a robar la bóveda más codiciosa del mundo."),
         reminder=("The Golden Vaults, far to the west.", "Las Bóvedas Doradas, lejos al oeste."),
         done=[("A pair of boots, sealskin, stitched with your clan's knot. Your mother's work, you said?",
                "Un par de botas, de piel de foca, cosidas con el nudo de tu clan. ¿Trabajo de tu madre, dijiste?"),
               ("Then they are the only thing in those vaults that was never stolen. They were waiting to be returned.",
                "Entonces son lo único en esas bóvedas que nunca fue robado. Esperaban ser devueltas.")]),
    dict(id="azhar_act3", cls="king", act=3, giver="khemet_scribe", reach="catacombs", spawn="sofe:bog_mummy", count=8, relic="signet_of_tevfiran", xp=2600,
         title=("The Treaty of Two Crowns", "El Tratado de las Dos Coronas"),
         offer=[("Majesty. Your ancestor Tevfiran signed a treaty with the kings of Khemet, sealed in their catacombs. Nobody has read it since.",
                  "Majestad. Tu antepasado Tevfiran firmó un tratado con los reyes de Khemet, sellado en sus catacumbas. Nadie lo ha leído desde entonces."),
                 ("If Sulthari still honours it, Khemet will stand with you. If not, you should know what was promised.",
                  "Si Sulthari todavía lo honra, Khemet estará contigo. Si no, deberías saber lo que se prometió.")],
         accept=("A sultan should know his debts.", "Un sultán debe conocer sus deudas."),
         reminder=("The catacombs. The treaty lies with the kings.", "Las catacumbas. El tratado yace con los reyes."),
         done=[("The treaty promises that Sulthari would come when Khemet called. Khemet called, Majesty. For a hundred years.",
                "El tratado promete que Sulthari acudiría cuando Khemet llamara. Khemet llamó, Majestad. Durante cien años."),
               ("You came. Late, but you came. Tevfiran's signet was sealed with the treaty. It is yours to keep the promise.",
                "Viniste. Tarde, pero viniste. El sello de Tevfiran estaba con el tratado. Es tuyo para cumplir la promesa.")]),
    dict(id="azhar_act4", cls="king", act=4, giver="aureum_senator", reach="market", spawn="sofe:gilded_legionnaire", count=8, relic="turban_of_the_peacock_throne", xp=3800,
         title=("A Sultan in the Senate", "Un Sultán en el Senado"),
         offer=[("A Republic does not kneel to sultans. But it remembers who stood in its market when the legionnaires turned on the people.",
                  "Una República no se arrodilla ante sultanes. Pero recuerda quién se plantó en su mercado cuando los legionarios se volvieron contra el pueblo."),
                 ("Go to the Forum. Stand there. Let them see you.", "Ve al Foro. Plántate allí. Que te vean.")],
         accept=("Sulthari stands with Aureum.", "Sulthari está con Aureum."),
         reminder=("The Forum Market. Aureum is watching.", "El Mercado del Foro. Aureum está mirando."),
         done=[("The Senate sent this: the turban of the Peacock Throne, taken from your grandfather's embassy in a bad year.",
                "El Senado envió esto: el turbante del Trono del Pavo Real, tomado de la embajada de tu abuelo en un mal año."),
               ("They say it is not a gift. It is a return. Republics, Majesty, are very particular about the difference.",
                "Dicen que no es un regalo. Es una devolución. Las Repúblicas, Majestad, son muy particulares con esa diferencia.")]),
]

# The Bearers as NPCs in Acts III and IV
BEARER_LINES_34 = {
    "knight": {3: ("Luxara offered me a court where no one is ever betrayed. I almost said yes.", "Luxara me ofreció una corte donde nadie es traicionado nunca. Casi dije que sí."),
               4: ("Aureum paid for my brothers' deaths. I came here to judge it, and found widows instead.", "Aureum pagó por la muerte de mis hermanos. Vine a juzgarla y encontré viudas.")},
    "necromancer": {3: ("Khemet is home, and home is asleep. I keep wanting to lie down beside it.", "Khemet es mi hogar, y mi hogar duerme. No dejo de querer acostarme a su lado."),
                    4: ("In Aureum the dead are counted like coins. I count them like people.", "En Aureum a los muertos se los cuenta como monedas. Yo los cuento como personas.")},
    "sorceress": {3: ("Laleh is in those gardens. If Luxara offers me her, whole and happy, I do not know what I will say.",
                      "Laleh está en esos jardines. Si Luxara me la ofrece, entera y feliz, no sé qué diré."),
                  4: ("She passed through here before me. I keep finding her handwriting in the margins of other people's books.",
                      "Pasó por aquí antes que yo. No dejo de encontrar su letra en los márgenes de los libros ajenos.")},
    "thief": {3: ("Silk Road thieves have long memories. Mine is longer.", "Los ladrones de la Ruta de la Seda tienen buena memoria. La mía es mejor."),
              4: ("Aureum has more locks than any city alive. I have never felt more at home.", "Aureum tiene más cerraduras que cualquier ciudad viva. Nunca me sentí más en casa.")},
    "king": {3: ("Khemet called to Sulthari for a hundred years. We had stopped listening.", "Khemet llamó a Sulthari durante cien años. Habíamos dejado de escuchar."),
             4: ("A Republic does not need a king. Perhaps that is the lesson Sulthari needs.", "Una República no necesita un rey. Quizá esa sea la lección que necesita Sulthari.")},
}


# The people of the three capitals (CapitalCity.FOLK): their lines while the Archsin rules (their default) and once
# it has fallen. They live in the houses of Isfaran, Neferet and Aurelion.
CAPITAL_FOLK = {
    "parsivan": {
        "isfaran_perfumer": (("Perfumer Golnar", "Golnar la perfumista"), 4, [
            ("Every perfume I make smells of her now: honey, roses, something sweeter underneath. I cannot make it stop.",
             "Cada perfume que hago huele a ella ahora: miel, rosas, algo más dulce debajo. No puedo hacer que pare."),
            ("If a scent makes you forget why you came, Bearer, walk the other way.",
             "Si un aroma te hace olvidar a qué viniste, Portador, camina en la otra dirección.")], [
            ("I made a perfume of rain and ink this morning. Nobody wanted it more than me.",
             "Esta mañana hice un perfume de lluvia y tinta. Nadie lo quería más que yo.")]),
        "isfaran_guard": (("Gate Guard Arash", "Arash, guardia de la puerta"), 4, [
            ("I keep the gate, but half the court never comes out of the gardens. I am guarding an empty city.",
             "Guardo la puerta, pero la mitad de la corte nunca sale de los jardines. Estoy custodiando una ciudad vacía."),
            ("The ones who leave smile at me as if I were a dream. I am not. I am cold and my feet hurt.",
             "Los que salen me sonríen como si yo fuera un sueño. No lo soy. Tengo frío y me duelen los pies.")], [
            ("People come through the gate again, arguing about prices. I have never loved arguing so much.",
             "La gente vuelve a cruzar la puerta, discutiendo precios. Nunca me habían gustado tanto las discusiones.")]),
        "isfaran_astronomer": (("Astronomer Darius", "Darius el astrónomo"), 4, [
            ("The stars over Isfaran have stopped moving in my charts. Either the sky is asleep, or I am.",
             "Las estrellas sobre Isfaran dejaron de moverse en mis cartas. O el cielo duerme, o duermo yo."),
            ("Laleh of the Observatory came here once. She said the gardens were a door, and someone left it open.",
             "Laleh, la del Observatorio, vino una vez. Dijo que los jardines eran una puerta, y que alguien la dejó abierta.")], [
            ("The sky turns again. I wept over a chart, and the ink ran into a new constellation.",
             "El cielo vuelve a girar. Lloré sobre una carta, y la tinta corrió formando una constelación nueva.")]),
        "isfaran_child": (("Little Parisa", "La pequeña Parisa"), 4, [
            ("My mother sleeps in the garden and won't come home. She says it's nicer there. It isn't. I checked.",
             "Mi mamá duerme en el jardín y no quiere volver a casa. Dice que allá es más bonito. No lo es. Fui a ver."),
            ("Are you the one who wakes people up? Can you wake her first?", "¿Tú eres quien despierta a la gente? ¿Puedes despertarla a ella primero?")], [
            ("Mama is home! She burned the rice. It was the best rice ever.", "¡Mamá está en casa! Quemó el arroz. Fue el mejor arroz del mundo.")]),
        "isfaran_carpet_weaver": (("Weaver Roshan", "Roshan el tejedor"), 4, [
            ("My carpets used to tell stories. Now every pattern I weave turns into the same face.",
             "Mis alfombras contaban historias. Ahora cada dibujo que tejo se convierte en el mismo rostro."),
            ("Bring me moonsilk, if the caravans ever run again. I want to weave something that is mine.",
             "Tráeme seda lunar, si las caravanas vuelven a andar. Quiero tejer algo que sea mío.")], [
            ("The new carpet tells your story, Bearer. I left a knot loose at the end. Stories should not be finished.",
             "La alfombra nueva cuenta tu historia, Portador. Dejé un nudo suelto al final. Las historias no deberían terminarse.")]),
    },
    "khemet": {
        "neferet_priest": (("Priest Khaemwaset", "Khaemwaset el sacerdote"), 4, [
            ("We say the morning prayers to an empty sky. The sun still rises; it simply does not seem to care.",
             "Rezamos las oraciones de la mañana a un cielo vacío. El sol aún sale; simplemente parece que no le importa."),
            ("Morthis sleeps on a throne in the marsh, and all of Khemet yawns with him.",
             "Morthis duerme en un trono del pantano, y todo Khemet bosteza con él.")], [
            ("The temple was full at dawn. People came to pray and stayed to work. That is the best prayer.",
             "El templo estaba lleno al amanecer. La gente vino a rezar y se quedó a trabajar. Esa es la mejor oración.")]),
        "neferet_boatman": (("Boatman Hapi", "Hapi el barquero"), 4, [
            ("The canal barely moves. My boat sits in it like a fly in honey.", "El canal apenas se mueve. Mi barca se queda en él como una mosca en miel."),
            ("My cousin Ani rows the dead. I row the living. Lately our passengers look the same.",
             "Mi primo Ani rema a los muertos. Yo remo a los vivos. Últimamente nuestros pasajeros se parecen.")], [
            ("The water runs! I nearly fell in. Tell Ani the river remembers how to be a river.",
             "¡El agua corre! Casi me caigo. Dile a Ani que el río recuerda cómo ser un río.")]),
        "neferet_potter": (("Potter Tiye", "Tiye la alfarera"), 4, [
            ("I have not finished a jar in a month. I sit at the wheel and watch it turn, and that seems like enough.",
             "No he terminado un cántaro en un mes. Me siento ante el torno y lo miro girar, y eso parece suficiente."),
            ("That is the sin, Bearer. It never feels like a sin. It feels like rest.", "Ese es el pecado, Portador. Nunca se siente como pecado. Se siente como descanso.")], [
            ("Twelve jars since yesterday! My hands hurt. I had forgotten that hurting hands are happy hands.",
             "¡Doce cántaros desde ayer! Me duelen las manos. Había olvidado que las manos que duelen son manos felices.")]),
        "neferet_child": (("Little Amun", "El pequeño Amun"), 4, [
            ("I climbed the small pyramid. From the top you can see the marsh. It looks like a sleeping animal.",
             "Subí a la pirámide pequeña. Desde arriba se ve el pantano. Parece un animal dormido."),
            ("My grandpa says the kings are awake under the sand, waiting. I don't think waiting is fun.",
             "Mi abuelo dice que los reyes están despiertos bajo la arena, esperando. No creo que esperar sea divertido.")], [
            ("The marsh doesn't look like an animal anymore. Just a marsh. A bit smelly. I like it better.",
             "El pantano ya no parece un animal. Solo un pantano. Un poco apestoso. Me gusta más así.")]),
        "neferet_guard": (("Temple Guard Ramose", "Ramose, guardia del templo"), 4, [
            ("The mummies of the marsh walk to our walls at night and stop, as if they forgot why they came.",
             "Las momias del pantano caminan hasta nuestras murallas de noche y se detienen, como si olvidaran a qué venían."),
            ("I do not know if that is a mercy or a warning.", "No sé si eso es una misericordia o una advertencia.")], [
            ("No mummies last night. I slept, and I did not feel guilty about it. That is how I knew it was over.",
             "Anoche no vino ninguna momia. Dormí, y no me sentí culpable. Así supe que había terminado.")]),
    },
    "aureum": {
        "aurelion_magistrate": (("Magistrate Cassia", "Casia la magistrada"), 5, [
            ("I judge cases where both sides envy each other so much that neither wants to win, only to see the other lose.",
             "Juzgo casos donde ambas partes se envidian tanto que ninguna quiere ganar, solo ver perder a la otra."),
            ("The law was written for greed. Nobody wrote one for envy.", "La ley se escribió para la codicia. Nadie escribió una para la envidia.")], [
            ("A man thanked his neighbour in my court today. I adjourned for an hour to recover.",
             "Hoy un hombre le dio las gracias a su vecino en mi tribunal. Suspendí la sesión una hora para recuperarme.")]),
        "aurelion_legionary": (("Legionary Marcus", "Marco el legionario"), 5, [
            ("My cohort was sent to guard the Treasury. Half of them never came back. The other half came back gilded.",
             "Mi cohorte fue enviada a custodiar el Tesoro. La mitad nunca volvió. La otra mitad volvió dorada."),
            ("If you see a golden legionary, Bearer, do not look him in the eye. It was one of us.",
             "Si ves un legionario dorado, Portador, no lo mires a los ojos. Era uno de los nuestros.")], [
            ("I marched past the Treasury today. Only stone and coins. I saluted anyway.",
             "Hoy marché frente al Tesoro. Solo piedra y monedas. Saludé de todas formas.")]),
        "aurelion_baker": (("Baker Fulvia", "Fulvia la panadera"), 5, [
            ("People buy bread only to see if their neighbour's loaf is bigger. I bake them all the same, and still they quarrel.",
             "La gente compra pan solo para ver si la hogaza del vecino es más grande. Las horneo todas iguales, y aun así se pelean."),
            ("Envy is a hunger bread does not fill.", "La envidia es un hambre que el pan no llena.")], [
            ("Today two customers shared a loaf. Shared! I gave them a second one for free.",
             "Hoy dos clientes compartieron una hogaza. ¡La compartieron! Les regalé otra.")]),
        "aurelion_child": (("Little Lucius", "El pequeño Lucio"), 5, [
            ("I wanted to be a gladiator. Now the gladiators are shadows. I want to be a baker.",
             "Quería ser gladiador. Ahora los gladiadores son sombras. Quiero ser panadero."),
            ("Bakers don't turn into shadows, right?", "Los panaderos no se convierten en sombras, ¿verdad?")], [
            ("I'm going to be a baker AND a gladiator. A gladiator who bakes. Nobody has thought of that.",
             "Voy a ser panadero Y gladiador. Un gladiador que hornea. A nadie se le había ocurrido.")]),
        "aurelion_sculptor": (("Sculptor Varus", "Varo el escultor"), 5, [
            ("They paid me to carve the Senate's faces in gold. Every statue I finish, someone pays me to make the next one taller.",
             "Me pagaron para tallar los rostros del Senado en oro. Cada estatua que termino, alguien me paga para que la siguiente sea más alta."),
            ("I am carving a very small statue now, of my daughter. Nobody will envy it. That is the point.",
             "Ahora estoy tallando una estatua muy pequeña, de mi hija. Nadie la envidiará. De eso se trata.")], [
            ("The Senate asked for a statue of you, Bearer. I said I would carve it life-sized, no taller. They agreed!",
             "El Senado pidió una estatua tuya, Portador. Dije que la tallaría a tamaño real, ni un palmo más. ¡Aceptaron!")]),
    },
}
