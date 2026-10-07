# The Armies of the Empires

Each of the five empires keeps an army in its city. Its soldiers fight beside the Bearers against the Void and the monsters, and a Bearer can hire some of them to follow. They help, but they never take the Bearer's place: the kills a quest asks for are still the Bearer's (but in the Void's invasions, where the whole city fights), and the garrisons leave the bosses to the Bearers.

## Troops

Every army has three ranks, each with its own skin in the empire's colors (`textures/entity/npc/<empire>_<rank>.png`, painted by `scripts/make_npc_skins.py`):

| Rank | Health | Blow | Fights with | Look |
| --- | --- | --- | --- | --- |
| Soldier | 30 | 4 | the empire's blade | the empire's armor and headgear |
| Archer | 24 | 3 (arrows, 60% of it) | a bow | light clothes and a quiver strap |
| Captain | 50 | 6 | the empire's finest blade | a commander's helm and cape; a little taller |

| Empire | Colors and dress | Weapons (soldier / captain) |
| --- | --- | --- |
| Sulthari | Red and gold, the guard's felt cap, scale armor | brass longsword / brass scimitar |
| Nordrath | Blue and iron, furs and braids | iron axe / glacial iron sword |
| Parsivan | Violet and teal, plumed helms | iron sword / brass scimitar |
| Khemet | Linen and gold, the nemes, a gold collar | brass scimitar / khopesh |
| Aureum | The legion's red, crested helms | iron sword / brass longsword |

## Garrisons

Every district of a city has a garrison of seven: a captain, three soldiers and three archers (`Army.GARRISON`, `Army.posts`). Sulthari's districts are the plaza, the lower district, the low bazaar, the training grounds, the forge and the avenue up to the Observatory (42 soldiers); each other city has one at its center and one in each quarter (35). A garrison is placed with the story places (`StoryPlacements.placeNear`), on the open street nearest its post, never on a roof or a dome.

A garrison soldier keeps within 16 blocks of their post, and fights every Void creature and monster that comes within 16 blocks, but not a boss. A soldier who falls is replaced within two minutes, when no player is within 32 blocks to see it; a world made before the garrisons grew fills them up the same way.

When the Void attacks a city in waves (`VoidInvasion`), the rifts open by the garrisons of the districts on that side, and every creature of the invasion that falls counts for the Bearer it came for, whoever strikes it.

## Hiring a company

Talking to a captain opens their dialogue (`data/sofe/dialogue/army/<empire>_captain.json`, written by `scripts/make_army.py`):

- **Hire a soldier:** 40 dinars.
- **Hire an archer:** 50 dinars.
- **Bring back the fallen:** 25 dinars for each.

Up to **three** soldiers follow a Bearer (their company), from any empire. A hired soldier follows the Bearer, fights what the Bearer fights or what fights the Bearer, and obeys the King's Command. A soldier who falls keeps their place in the company: a captain can bring them back, or the Bearer can hire another to take that place. The company is kept with the player through death and logging out, and stands with them again when they log in, come back to life or pass through a portal.

Soldiers never harm a Bearer, a citizen or another soldier, and a Bearer's blows do not hurt them.

## Tests

`ArmyGameTests`: hiring a company and its limit, the prices, the fallen brought back or replaced; a soldier never fights a Bearer, and turns on a Void creature.
