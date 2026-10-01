"""The people of Sulthari who are not part of the main story: citizens who talk about what is happening
(their words change with the act), a storyteller who tells the old tales on request, and the bazaar
merchants with their shops. Writes data/sofe/dialogue/<npc>/*.json, data/sofe/merchant_offers/<npc>.json
and the English and Spanish texts.

Run from the repository root: python scripts/make_citizens.py
"""
import collections
import json
import os

DATA = os.path.join("src", "main", "resources", "data", "sofe")
LANG = os.path.join("src", "main", "resources", "assets", "sofe", "lang")

# id: names (en, es) and lines per act (act 1 is the default; later acts take over once reached)
CITIZENS = {
    "citizen_baker": (("Baker Nesrin", "Nesrin la panadera"), {
        1: [("The festival bread was still warm when the sky went dark. Now I bake for the guards on the walls.",
             "El pan del festival aún estaba caliente cuando el cielo se oscureció. Ahora horneo para los guardias de las murallas."),
            ("Eat something, Bearer. Even a hero walks on bread before walking on legends.",
             "Come algo, Portador. Hasta un héroe camina sobre pan antes de caminar sobre leyendas.")],
        2: [("No ships come from the north anymore. They say the Norse dead rise every dawn to fight again.",
             "Ya no llegan barcos del norte. Dicen que los muertos nórdicos se levantan cada amanecer para volver a luchar."),
            ("If you go to Nordrath, take warm bread. The cold there bites like a moneylender.",
             "Si vas a Nordrath, lleva pan caliente. Allá el frío muerde como un prestamista.")],
        3: [("The bells rang all night when word came that Vorath had fallen. My ovens have never been so busy!",
             "¡Las campanas sonaron toda la noche cuando llegó la noticia de que Vorath había caído! Mis hornos nunca habían trabajado tanto."),
            ("Still... a fragment that thanks you is no gift. My grandmother said: never trust bread that bakes itself.",
             "Aun así... un fragmento que te da las gracias no es un regalo. Mi abuela decía: nunca confíes en el pan que se hornea solo.")],
    }),
    "citizen_water_carrier": (("Water Carrier Halil", "Halil el aguador"), {
        1: [("The fountains of the plaza run on aetherium pumps. On the night of the eclipse they ran black for an hour.",
             "Las fuentes de la plaza funcionan con bombas de aetherium. La noche del eclipse corrieron negras durante una hora."),
            ("I still dream of that water. It whispered.", "Todavía sueño con esa agua. Susurraba.")],
        2: [("The wells of the lower district are clean again, thanks to you Bearers. But the Void creatures still crawl beyond the walls at night.",
             "Los pozos del barrio bajo vuelven a estar limpios, gracias a ustedes, Portadores. Pero las criaturas del Vacío aún se arrastran de noche tras las murallas.")],
        3: [("The water has tasted of iron since the crack opened. The Observatory says it is nothing. The Observatory said that before, too.",
             "El agua sabe a hierro desde que se abrió la grieta. El Observatorio dice que no es nada. El Observatorio también dijo eso antes.")],
    }),
    "citizen_scholar": (("Scholar Imren", "Imren la erudita"), {
        1: [("A thousand years ago five emperors read five pages of a book they should have burned. We call it the Ritual of the Five Crowns.",
             "Hace mil años cinco emperadores leyeron cinco páginas de un libro que debieron quemar. Lo llamamos el Ritual de las Cinco Coronas."),
            ("The Observatory keeps the oldest records. Ask the Council Elder if you want the whole story.",
             "El Observatorio guarda los registros más antiguos. Pregúntale al Anciano del Consejo si quieres la historia completa.")],
        2: [("Seven fragments of the First Fallen escaped the crack: the Seven Archsins. Wrath went north, to Nordrath.",
             "Siete fragmentos del Primer Caído escaparon de la grieta: los Siete Archipecados. La Ira se fue al norte, a Nordrath."),
            ("Each Archsin twisted the guardians of the Ten Laws. Those who broke their law became the Broken Oaths.",
             "Cada Archipecado corrompió a los guardianes de las Diez Leyes. Quienes rompieron su ley se convirtieron en los Juramentos Rotos.")],
        3: [("Lust holds Parsivan and Sloth holds Khemet. Two empires asleep: one in pleasure, one in death.",
             "La Lujuria domina Parsivan y la Pereza domina Khemet. Dos imperios dormidos: uno en el placer, otro en la muerte."),
            ("If you find anything written by the sultan who sealed the crack, bring it to me. Please.",
             "Si encuentras algo escrito por el sultán que selló la grieta, tráemelo. Por favor.")],
    }),
    "citizen_guard": (("Wall Guard Tarik", "Tarik, guardia de la muralla"), {
        1: [("Stay inside the walls after dark, traveler. The aetherium barrier kept us safe for a thousand years, and now it hums like a wasp.",
             "Quédate dentro de las murallas al anochecer, viajero. La barrera de aetherium nos protegió mil años, y ahora zumba como una avispa."),
            ("We drill with the clockwork spears every morning. Much good they did against the Sentinel.",
             "Entrenamos con las lanzas de relojería cada mañana. Para lo que sirvieron contra el Centinela...")],
        2: [("The gates are open again since the Sentinel fell. I count every caravan that leaves. Fewer come back.",
             "Las puertas volvieron a abrirse desde que cayó el Centinela. Cuento cada caravana que sale. Vuelven menos.")],
        3: [("The patrols saw the smoke in the north go out. Whatever you did in Nordrath, the night watch is quieter.",
             "Las patrullas vieron apagarse el humo del norte. Hicieras lo que hicieras en Nordrath, la guardia nocturna está más tranquila.")],
    }),
    "citizen_weaver": (("Weaver Leyla", "Leyla la tejedora"), {
        1: [("My carpets tell the story of the Pact: five crowns, ten laws, one table. I leave a sixth chair empty in every one. Grandmother's custom.",
             "Mis alfombras cuentan la historia del Pacto: cinco coronas, diez leyes, una mesa. En cada una dejo vacía una sexta silla. Costumbre de mi abuela.")],
        2: [("Red thread is dear now. Everyone wants banners for the walls.", "El hilo rojo está caro ahora. Todos quieren estandartes para las murallas.")],
        3: [("A merchant from the east sold me silk that never frays. Beautiful... and it smelled of sleep. I burned it.",
             "Un mercader del este me vendió seda que nunca se deshilacha. Hermosa... y olía a sueño. La quemé.")],
    }),
    "citizen_pilgrim": (("Pilgrim Osman", "Osman el peregrino"), {
        1: [("I walked from the outer oases to watch the eclipse from the Observatory. I watched the sky tear instead.",
             "Caminé desde los oasis lejanos para ver el eclipse desde el Observatorio. En cambio, vi rasgarse el cielo.")],
        2: [("The road north is lined with the cairns of the Norse clans. They bury their dead, and the dead do not stay buried.",
             "El camino al norte está lleno de túmulos de los clanes nórdicos. Entierran a sus muertos, y los muertos no se quedan enterrados.")],
        3: [("I will go east next, to Khemet's river. They say the living there no longer move. Maybe they are only praying.",
             "Ahora iré al este, al río de Khemet. Dicen que allí los vivos ya no se mueven. Quizá solo estén rezando.")],
    }),
    "citizen_widow": (("Widow Hatice", "Hatice la viuda"), {
        1: [("My husband kept the brass gears of the Observatory clean. He was inside when the seal broke.",
             "Mi esposo mantenía limpios los engranajes de latón del Observatorio. Estaba adentro cuando se rompió el sello."),
            ("Do not look at me like that. Find who did this.", "No me mires así. Encuentra a quien hizo esto.")],
        2: [("They say the Broken Oaths were guardians once, sworn to the Laws. My husband swore oaths too. Men are fragile things.",
             "Dicen que los Juramentos Rotos fueron guardianes, jurados a las Leyes. Mi esposo también juró. Los hombres son cosas frágiles.")],
        3: [("You came back from the north alive. Good. Keep doing that.", "Volviste vivo del norte. Bien. Sigue haciéndolo.")],
    }),
    "citizen_clockmaker": (("Clockmaker Bekir", "Bekir el relojero"), {
        1: [("The Brass Sentinel was my master's work. A guardian of the city... and the crack turned it against us.",
             "El Centinela de Latón fue obra de mi maestro. Un guardián de la ciudad... y la grieta lo volvió contra nosotros."),
            ("Aetherium is like a heart. When it turns black, everything it moves goes mad.",
             "El aetherium es como un corazón. Cuando se vuelve negro, todo lo que mueve enloquece.")],
        2: [("I salvaged a gear from the Sentinel. It still turns by itself at night. I keep it in a jar.",
             "Rescaté un engranaje del Centinela. Todavía gira solo por las noches. Lo guardo en un frasco.")],
        3: [("Black aetherium from the Archsins' lands can be purified. The forge masters know how; listen to them.",
             "El aetherium negro de las tierras de los Archipecados puede purificarse. Los maestros forjadores saben cómo; escúchalos.")],
    }),
    "citizen_child": (("Little Emre", "El pequeño Emre"), {
        1: [("Are you a Bearer? Does the shard hurt? Can I touch it? ...No? Okay.",
             "¿Eres un Portador? ¿Duele el fragmento? ¿Puedo tocarlo? ...¿No? Bueno.")],
        2: [("I play Bearers and Archsins with my friends. I'm always Vorath. He's the loudest.",
             "Juego a Portadores y Archipecados con mis amigos. Yo siempre soy Vorath. Es el que más grita.")],
        3: [("Mama says you beat Vorath! I'll play someone else now. Maybe you!",
             "¡Mamá dice que venciste a Vorath! Ahora seré otro. ¡Quizá tú!")],
    }),
    "citizen_tram_keeper": (("Tram Keeper Necati", "Necati, guardián del tranvía"), {
        1: [("The brass tramways ran all night for the festival. Then the Sentinel tore the rails out with its bare hands.",
             "Los tranvías de latón corrieron toda la noche por el festival. Luego el Centinela arrancó los rieles con sus propias manos."),
            ("We will lay them again. Sulthari always rebuilds.", "Volveremos a tenderlos. Sulthari siempre se reconstruye.")],
        2: [("The first line runs again, from the plaza to the bazaar. Slowly. The gears remember the night of the eclipse.",
             "La primera línea vuelve a funcionar, de la plaza al bazar. Despacio. Los engranajes recuerdan la noche del eclipse.")],
        3: [("The Council wants a line to the gates, for the caravans. Victories are good for business, Bearer.",
             "El Consejo quiere una línea hasta las puertas, para las caravanas. Las victorias son buenas para el negocio, Portador.")],
    }),
    "citizen_fisherman": (("Fisherman Yunus", "Yunus el pescador"), {
        1: [("From the cliffs you can see the whole bay. On the night of the eclipse the fish all swam away from the city at once.",
             "Desde los acantilados se ve toda la bahía. La noche del eclipse todos los peces huyeron de la ciudad a la vez.")],
        2: [("The causeways to the gates are safe again. The fish are not back yet. Fish are wiser than people.",
             "Las calzadas hacia las puertas vuelven a ser seguras. Los peces aún no regresan. Los peces son más sabios que la gente.")],
        3: [("The fish came back the week Vorath fell. My wife says it is a sign. I say it is dinner.",
             "Los peces volvieron la semana en que cayó Vorath. Mi esposa dice que es una señal. Yo digo que es la cena.")],
    }),
    "citizen_veteran": (("Old Soldier Kadir", "Kadir, el viejo soldado"), {
        1: [("Forty years on these walls and I never drew my sword in anger. Then the Void came up from below, not over the walls.",
             "Cuarenta años en estas murallas y nunca desenvainé con rabia. Entonces el Vacío llegó desde abajo, no por encima de los muros.")],
        2: [("A guardian who breaks his oath is worse than any monster. A monster never promised you anything.",
             "Un guardián que rompe su juramento es peor que cualquier monstruo. Un monstruo nunca te prometió nada.")],
        3: [("You fought Wrath and kept your head. Good. Wrath is the easiest sin to see. The others hide better.",
             "Luchaste contra la Ira y no perdiste la cabeza. Bien. La Ira es el pecado más fácil de ver. Los otros se esconden mejor.")],
    }),
    "citizen_courier": (("Courier Ayla", "Ayla la mensajera"), {
        1: [("Letters for the Council, letters for the forge, letters for everyone who is still alive. Busy night, Bearer.",
             "Cartas para el Consejo, cartas para la forja, cartas para todos los que siguen vivos. Noche ajetreada, Portador.")],
        2: [("No courier comes back from Nordrath. So now I carry letters to the north gate and leave them there. Silly, I know.",
             "Ningún mensajero vuelve de Nordrath. Así que ahora llevo las cartas hasta la puerta norte y las dejo allí. Tonto, lo sé.")],
        3: [("A letter arrived from Nordrath! Signed by a clan chief. It only says: 'We sleep now. Thank the Bearer.'",
             "¡Llegó una carta de Nordrath! Firmada por un jefe de clan. Solo dice: 'Ahora dormimos. Agradezcan al Portador.'")],
    }),
    "citizen_astronomer": (("Defne, Apprentice Astronomer", "Defne, aprendiz de astrónoma"), {
        1: [("The eclipse was predicted to the minute. What nobody predicted was the seal breaking under the Great Lens.",
             "El eclipse estaba predicho al minuto. Lo que nadie predijo fue que el sello se rompiera bajo la Gran Lente.")],
        2: [("Seven new lights appeared in the northern sky the night of the eclipse. One of them went out. I think you did that.",
             "Siete luces nuevas aparecieron en el cielo del norte la noche del eclipse. Una se apagó. Creo que fuiste tú.")],
        3: [("Six lights left. One burns over Parsivan, one over Khemet. The brightest stands right above us, and it never moves.",
             "Quedan seis luces. Una arde sobre Parsivan, otra sobre Khemet. La más brillante está justo encima de nosotros, y nunca se mueve.")],
    }),
    "citizen_tea_seller": (("Tea Seller Zeynep", "Zeynep la vendedora de té"), {
        1: [("Tea calms the nerves, Bearer. And after last night, the whole city needs a cup.",
             "El té calma los nervios, Portador. Y después de anoche, toda la ciudad necesita una taza.")],
        2: [("People talk over tea. They say the Grand Vizier never sleeps. They say he did not even blink when the seal broke.",
             "La gente habla tomando té. Dicen que el Gran Visir nunca duerme. Dicen que ni parpadeó cuando se rompió el sello.")],
        3: [("More gossip? The Grand Vizier ordered new locks for the Observatory. Locks, Bearer. What is he keeping in?",
             "¿Más chismes? El Gran Visir mandó poner cerraduras nuevas en el Observatorio. Cerraduras, Portador. ¿Qué está encerrando?")],
    }),
}

STORYTELLER = ("citizen_storyteller", ("Rasim the Meddah", "Rasim el meddah"), {
    "ask": ("Gather round, gather round! A meddah never tells the same tale twice... unless you pay him. What shall I tell you?",
            "¡Acérquense, acérquense! Un meddah nunca cuenta dos veces la misma historia... a menos que le paguen. ¿Qué te cuento?"),
    "topics": [
        (("The Pact of the Five Crowns", "El Pacto de las Cinco Coronas"),
         ("Five empires, ten laws, one table: Sulthari of brass, Nordrath of the axe, Parsivan of the gardens, Khemet of the river and Aureum of marble. For a thousand years the Pact held...",
          "Cinco imperios, diez leyes, una mesa: Sulthari del latón, Nordrath del hacha, Parsivan de los jardines, Khemet del río y Aureum del mármol. Durante mil años el Pacto se mantuvo...")),
        (("The Fall", "La Caída"),
         ("...until the emperors found a book beneath our desert. Each read one page aloud. Seven shadows came out of the crack, and our sultan gave his life to close it beneath the Observatory.",
          "...hasta que los emperadores hallaron un libro bajo nuestro desierto. Cada uno leyó una página en voz alta. Siete sombras salieron de la grieta, y nuestro sultán dio su vida para cerrarla bajo el Observatorio.")),
        (("The Bearers", "Los Portadores"),
         ("On the Night of the Eclipse five shards found five hearts. Some say they are heroes. Some say the Codex chose its own messengers. I say: buy a cup of tea and decide for yourself.",
          "En la Noche del Eclipse cinco fragmentos encontraron cinco corazones. Unos dicen que son héroes. Otros, que el Códice eligió a sus propios mensajeros. Yo digo: cómprate un té y decide tú mismo.")),
        (("The Broken Oaths", "Los Juramentos Rotos"),
         ("Ten guardians kept the Ten Laws. The Archsins whispered to each one, and each broke the law they kept. Now they fight with the very sin they swore to stop.",
          "Diez guardianes custodiaban las Diez Leyes. Los Archipecados le susurraron a cada uno, y cada uno rompió la ley que guardaba. Ahora luchan con el mismo pecado que juraron detener.")),
    ],
    "more": ("Another tale", "Otra historia"),
    "bye": ("Farewell", "Adiós"),
})

MERCHANTS = {
    "bazaar_spicer": (("Spice Seller Gulbahar", "Gülbahar la especiera"), "alchemist", [
        ("Saffron, sumac, mountain sage! Everything the Alembic needs, and half of what a good soup needs.",
         "¡Azafrán, zumaque, salvia de montaña! Todo lo que necesita el Alambique, y la mitad de lo que necesita una buena sopa."),
    ], [("Herbs from the north freeze on the road. Grow your own, Bearer: plant pomegranates where the sun is kind.",
         "Las hierbas del norte se congelan en el camino. Cultiva las tuyas, Portador: planta granadas donde el sol es amable.")], {
        "offers": [("sofe:mountain_sage", 2, 3, 10), ("sofe:pomegranate", 2, 4, 10), ("sofe:desert_lotus", 1, 5, 6),
                   ("sofe:mountain_sage_seeds", 2, 4, 8), ("minecraft:glass_bottle", 4, 2, 16), ("minecraft:sugar", 4, 2, 16)],
        "buys": [("sofe:mountain_sage", 1), ("sofe:pomegranate", 1), ("sofe:desert_lotus", 2)]}),
    "bazaar_weaver": (("Carpet Weaver Selin", "Selin la alfombrera"), "quartermaster", [
        ("Wool from the plateau herds, dyed in the colors of the Dominion. Red for courage, gold for the sultan.",
         "Lana de los rebaños de la meseta, teñida con los colores del Dominio. Rojo por el valor, oro por el sultán."),
    ], [("Everyone wants banners now. Even the bakers hang a crescent over their ovens.",
         "Ahora todos quieren estandartes. Hasta los panaderos cuelgan una media luna sobre sus hornos.")], {
        "offers": [("minecraft:red_wool", 4, 3, 16), ("minecraft:yellow_wool", 4, 3, 16), ("minecraft:white_wool", 4, 2, 16),
                   ("minecraft:red_carpet", 4, 3, 16), ("minecraft:red_banner", 1, 6, 4), ("minecraft:string", 8, 2, 16)],
        "buys": [("minecraft:string", 1), ("minecraft:white_wool", 1)]}),
    "bazaar_fruiterer": (("Fruit Seller Kemal", "Kemal el frutero"), "quartermaster", [
        ("Figs, apples and the best pomegranates south of the walls. A Bearer must eat!",
         "¡Higos, manzanas y las mejores granadas al sur de las murallas! ¡Un Portador tiene que comer!"),
    ], [("The caravans from the north are back. Prices are falling, and so is my mood.",
         "Las caravanas del norte volvieron. Los precios bajan, y mi humor también.")], {
        "offers": [("minecraft:apple", 4, 3, 16), ("minecraft:bread", 4, 4, 16), ("minecraft:sweet_berries", 8, 2, 16),
                   ("minecraft:melon_slice", 8, 2, 16), ("minecraft:honey_bottle", 1, 4, 8), ("minecraft:cookie", 8, 3, 16)],
        "buys": [("minecraft:wheat", 1), ("minecraft:apple", 1)]}),
    "bazaar_lampwright": (("Lampwright Yavuz", "Yavuz el lamparero"), "smith", [
        ("Aetherium lamps, brass lanterns, glass blown here in Sulthari. Darkness is bad for business, Bearer.",
         "Lámparas de aetherium, faroles de latón, vidrio soplado aquí en Sulthari. La oscuridad es mala para el negocio, Portador."),
    ], [("Since the crack opened, everyone buys a second lamp. I am not complaining.",
         "Desde que se abrió la grieta, todos compran una segunda lámpara. No me quejo.")], {
        "offers": [("minecraft:lantern", 2, 4, 16), ("sofe:sulthari_aetherium_lamp", 2, 8, 8), ("minecraft:glass", 8, 3, 16),
                   ("minecraft:torch", 16, 2, 16), ("sofe:sulthari_brass_trim", 8, 5, 8)],
        "buys": [("minecraft:copper_ingot", 1), ("minecraft:glass", 1)]}),
}


NORDRATH_CITIZENS = {
    "nordrath_shieldmaiden": (("Shieldmaiden Ylva", "Ylva, la doncella escudera"), {
        2: [("I died on the ice yesterday. And the day before. Every dawn Vorath's fire wakes us and the war starts again.",
             "Ayer morí en el hielo. Y anteayer. Cada amanecer el fuego de Vorath nos despierta y la guerra empieza otra vez."),
            ("You do not wake with us, Bearer. That means you can end it.", "Tú no despiertas con nosotros, Portador. Eso significa que puedes terminarlo.")],
        3: [("This morning I woke and nobody called me to the shieldwall. I did not know what to do with my hands.",
             "Esta mañana desperté y nadie me llamó al muro de escudos. No supe qué hacer con las manos.")],
    }),
    "nordrath_fisher": (("Fisher Bjarke", "Bjarke el pescador"), {
        2: [("The fjord does not freeze anymore. The mountain keeps it warm, and the fish taste of ash.",
             "El fiordo ya no se congela. La montaña lo mantiene tibio, y los peces saben a ceniza.")],
        3: [("The water is cold again! Cold and clean. I never thought I would be glad to lose a finger to the ice.",
             "¡El agua vuelve a estar fría! Fría y limpia. Nunca pensé que me alegraría perder un dedo por el hielo.")],
    }),
    "nordrath_widow": (("Widow Asta", "Asta la viuda"), {
        2: [("My husband falls every evening and stands up every morning. I have buried him a hundred times. Do not ask me to cry again.",
             "Mi esposo cae cada tarde y se levanta cada mañana. Lo he enterrado cien veces. No me pidas que llore otra vez.")],
        3: [("He did not wake today. I sat by him until noon. Then I laughed, Bearer, because he looked rested.",
             "Hoy no despertó. Me senté junto a él hasta el mediodía. Luego me reí, Portador, porque se veía descansado.")],
    }),
    "nordrath_apprentice": (("Apprentice Eirik", "Eirik el aprendiz"), {
        2: [("The forges here drink lava straight from the mountain. Master says Kaleth taught the first smiths before he broke his oath.",
             "Aquí las forjas beben lava directo de la montaña. El maestro dice que Kaleth enseñó a los primeros herreros antes de romper su juramento.")],
        3: [("The lava runs slower since Vorath fell. Master says the mountain is sleeping. I say it is listening.",
             "La lava corre más lenta desde que cayó Vorath. El maestro dice que la montaña duerme. Yo digo que escucha.")],
    }),
    "nordrath_hunter": (("Hunter Sigrun", "Sigrun la cazadora"), {
        2: [("Wolves stay away from the hold. Even beasts know the dead here do not stay dead.",
             "Los lobos no se acercan a la fortaleza. Hasta las bestias saben que aquí los muertos no se quedan muertos.")],
        3: [("The wolves came back to the forest last night. I have never been so happy to hear them howl.",
             "Anoche los lobos volvieron al bosque. Nunca había estado tan feliz de oírlos aullar.")],
    }),
    "nordrath_elder": (("Elder Gunnhild", "Gunnhild la anciana"), {
        2: [("The Pact gave us the Law of the winter bread, and Kaleth the Law of mercy. Now Kaleth burns those who kneel.",
             "El Pacto nos dio la Ley del pan del invierno, y Kaleth la Ley de la piedad. Ahora Kaleth quema a quienes se arrodillan.")],
        3: [("Something sleeps under the Caverns, Bearer. It is hungry. When the time comes, do not go down there with an empty stomach.",
             "Algo duerme bajo las Cavernas, Portador. Tiene hambre. Cuando llegue el momento, no bajes allí con el estómago vacío.")],
    }),
    "nordrath_child": (("Little Tove", "La pequeña Tove"), {
        2: [("Papa says when I grow up I will fight forever too. I do not want to fight forever.",
             "Papá dice que cuando crezca yo también lucharé para siempre. No quiero luchar para siempre.")],
        3: [("Now I can grow up and be a fisher instead! Or a dragon. A small one.",
             "¡Ahora puedo crecer y ser pescadora! O un dragón. Uno pequeño.")],
    }),
    "nordrath_brewer": (("Brewer Halvard", "Halvard el cervecero"), {
        2: [("Mead does not help the dead, but it helps the living watch them march out again.",
             "El hidromiel no ayuda a los muertos, pero ayuda a los vivos a verlos marchar otra vez.")],
        3: [("A feast! The first in a hundred years without a war the next morning. Drink, Bearer, you paid for it.",
             "¡Un festín! El primero en cien años sin guerra a la mañana siguiente. Bebe, Portador, tú lo pagaste.")],
    }),
    "nordrath_raider": (("Old Raider Ketil", "Ketil, el viejo saqueador"), {
        2: [("I sailed to Aureum once, when there were still ships worth robbing. Now the only plunder left is our own graves.",
             "Una vez navegué hasta Aureum, cuando aún había barcos que valía la pena saquear. Ahora el único botín que queda son nuestras tumbas.")],
        3: [("Serath honoured no blood but her own. Thank you for showing her the old law, Bearer.",
             "Serath no honró más sangre que la suya. Gracias por recordarle la vieja ley, Portador.")],
    }),
}

NORDRATH_SKALD = ("nordrath_skald", ("Skald Hrolf", "Hrolf el escaldo"), {
    "ask": ("Sit by the fire, stranger. A skald pays for his mead with sagas. Which one will you hear?",
            "Siéntate junto al fuego, forastero. Un escaldo paga su hidromiel con sagas. ¿Cuál quieres oír?"),
    "topics": [
        (("The clans of Nordrath", "Los clanes de Nordrath"),
         ("Nine clans signed the Pact for Nordrath: the axe and the oar, the forge and the fjord. We kept the Laws of mercy and of the winter bread longer than any.",
          "Nueve clanes firmaron el Pacto por Nordrath: el hacha y el remo, la forja y el fiordo. Guardamos las Leyes de la piedad y del pan del invierno más tiempo que nadie.")),
        (("The endless war", "La guerra sin fin"),
         ("When Wrath came out of the crack, it found the bravest warriors of Aetheris. Vorath gave them what they wanted most: a battle that never ends. Every dawn, his fire raises the fallen.",
          "Cuando la Ira salió de la grieta, encontró a los guerreros más valientes de Aetheris. Vorath les dio lo que más deseaban: una batalla que nunca termina. Cada amanecer, su fuego levanta a los caídos.")),
        (("Kaleth and Serath", "Kaleth y Serath"),
         ("Kaleth guarded the Law of mercy and now finishes the fallen in his Forge. Serath guarded the blood of our fathers and now drinks it in her Arena. Both serve Vorath in the Burning Citadel.",
          "Kaleth custodiaba la Ley de la piedad y ahora remata a los caídos en su Forja. Serath custodiaba la sangre de nuestros padres y ahora la bebe en su Arena. Ambos sirven a Vorath en la Ciudadela Ardiente.")),
        (("The fire under the mountain", "El fuego bajo la montaña"),
         ("Our forges burn with a fire that comes from below. The old ones say there is a deep beneath the deep, and that it opens only when Wrath is silenced.",
          "Nuestras forjas arden con un fuego que viene de abajo. Los viejos dicen que hay un abismo bajo el abismo, y que solo se abre cuando la Ira calla.")),
    ],
    "more": ("Another saga", "Otra saga"),
    "bye": ("Farewell, skald", "Adiós, escaldo"),
})

NORDRATH_MERCHANTS = {
    "nordrath_furrier": (("Furrier Ragna", "Ragna la peletera"), "quartermaster", [
        ("Furs, smoked fish and mead. Nothing keeps a Bearer alive in the north like a good cloak.",
         "Pieles, pescado ahumado e hidromiel. Nada mantiene vivo a un Portador en el norte como una buena capa."),
    ], [("Business is good since the dead stopped buying. They never paid anyway.",
         "El negocio va bien desde que los muertos dejaron de comprar. De todas formas nunca pagaban.")], {
        "offers": [("minecraft:leather", 4, 3, 16), ("minecraft:white_wool", 4, 2, 16), ("minecraft:cooked_cod", 4, 3, 16),
                   ("minecraft:cooked_salmon", 4, 4, 16), ("minecraft:bread", 4, 3, 16), ("minecraft:honey_bottle", 1, 4, 8),
                   ("minecraft:leather_boots", 1, 8, 2), ("minecraft:leather_chestplate", 1, 12, 2)],
        "buys": [("minecraft:rabbit_hide", 1), ("minecraft:leather", 2), ("minecraft:cod", 1)]}),
    "nordrath_runesmith": (("Runesmith Torvald", "Torvald el herrero de runas"), "smith", [
        ("Glacial iron, forged in volcano fire and quenched in the fjord. Nothing else holds an edge in this cold.",
         "Hierro glacial, forjado en fuego de volcán y templado en el fiordo. Nada más conserva el filo con este frío."),
    ], [("Vorath's fall cooled the mountain a little. My forge will miss him. I will not.",
         "La caída de Vorath enfrió un poco la montaña. Mi forja lo extrañará. Yo no.")], {
        "offers": [("sofe:glacial_iron_ingot", 2, 8, 12), ("sofe:glacial_iron_sword", 1, 40, 2), ("sofe:glacial_iron_pickaxe", 1, 36, 2),
                   ("minecraft:iron_ingot", 4, 6, 16), ("minecraft:shield", 1, 10, 4), ("minecraft:arrow", 16, 5, 8)],
        "buys": [("minecraft:iron_ingot", 1), ("sofe:glacial_iron_ingot", 3), ("minecraft:coal", 1)]}),
}

NORDRATH_PLACES = {
    "waystone.sofe.nordrath.city": ("Skarnhold", "Skarnhold"),
    "waystone.sofe.nordrath.forge": ("Nordrath Forge", "Forja de Nordrath"),
    "waystone.sofe.nordrath.arena": ("Nordrath Arena", "Arena de Nordrath"),
    "waystone.sofe.nordrath.burning_citadel": ("Burning Citadel", "Ciudadela Ardiente"),
}


def act_requirement(act):
    return {"type": "act_reached", "act": act}


def write(path, data):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8", newline="\n") as f:
        json.dump(data, f, indent=2, ensure_ascii=False)
        f.write("\n")


en, es = {}, {}


def text(key, pair):
    en[key], es[key] = pair
    return key


def act_dialogues(npc, acts, style="sulthari"):
    for act, lines in acts.items():
        data = {"style": style, "npc": npc}
        if act > 1:
            data["priority"] = act
            data["requires"] = act_requirement(act)
        data["lines"] = [{"speaker": npc, "text": text("dialogue.sofe.%s.act%d.%d" % (npc, act, i + 1), line)} for i, line in enumerate(lines)]
        write(os.path.join(DATA, "dialogue", npc, "act%d.json" % act), data)


for npc, (names, acts) in CITIZENS.items():
    text("npc.sofe." + npc, names)
    act_dialogues(npc, acts)

def storyteller(npc, names, tale, style):
    """Someone who tells the old tales: the player picks a topic, then another or farewell."""
    text("npc.sofe." + npc, names)
    topics = tale["topics"]
    more = text("dialogue.sofe.%s.more" % npc, tale["more"])
    bye = text("dialogue.sofe.%s.bye" % npc, tale["bye"])
    lines = [{"speaker": npc, "text": text("dialogue.sofe.%s.ask" % npc, tale["ask"]),
              "answers": [{"text": text("dialogue.sofe.%s.topic.%d" % (npc, i + 1), title), "next": i + 1} for i, (title, _) in enumerate(topics)]
              + [{"text": bye, "next": -1}]}]
    for i, (_, story) in enumerate(topics):
        lines.append({"speaker": npc, "text": text("dialogue.sofe.%s.tale.%d" % (npc, i + 1), story),
                      "answers": [{"text": more, "next": 0}, {"text": bye, "next": -1}]})
    write(os.path.join(DATA, "dialogue", npc, "tales.json"), {"style": style, "npc": npc, "lines": lines})


def merchants(table, style, first_act=1):
    for npc, (names, role, now, later, shop) in table.items():
        text("npc.sofe." + npc, names)
        act_dialogues(npc, {1: now, first_act + 1: later}, style)
        write(os.path.join(DATA, "merchant_offers", npc + ".json"), {
            "role": role,
            "offers": [dict({"sell": item, "price": price, "stock": stock}, **({"count": count} if count > 1 else {}),
                            **({"min_act": act} if act > 1 else {}))
                       for item, count, price, stock, act in [o if len(o) == 5 else o + (1,) for o in shop["offers"]]],
            "buys": [{"item": item, "price": price} for item, price in shop["buys"]]})


storyteller(*STORYTELLER, "sulthari")
merchants(MERCHANTS, "sulthari")

# --- Nordrath: the hold of the clans (Act II). Their first lines are for Act II, while Vorath's war goes on;
# the second for when he has fallen and the dead can rest.
for npc, (names, acts) in NORDRATH_CITIZENS.items():
    text("npc.sofe." + npc, names)
    act_dialogues(npc, {1 if act == 2 else act: lines for act, lines in acts.items()}, "nordrath")  # Act II lines are their default
storyteller(*NORDRATH_SKALD, "nordrath")
merchants(NORDRATH_MERCHANTS, "nordrath", first_act=2)
for key, pair in NORDRATH_PLACES.items():
    text(key, pair)

text("entity.sofe.citizen", ("Citizen", "Ciudadano"))

for name, entries in (("en_us.json", en), ("es_es.json", es)):
    path = os.path.join(LANG, name)
    with open(path, encoding="utf-8") as f:
        lang = json.load(f, object_pairs_hook=collections.OrderedDict)
    lang.update(entries)
    with open(path, "w", encoding="utf-8", newline="\n") as f:
        json.dump(lang, f, indent=2, ensure_ascii=False)
        f.write("\n")
print(len(en), "texts")
