package com.sofe.gear;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sofe.economy.EconomyData;
import com.sofe.economy.MerchantOffer;
import com.sofe.economy.Prices;
import com.sofe.progression.CharacterAttribute;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class GearAndEconomyTest {
    private static final Path DATA = Path.of("src/main/resources/data/sofe");

    private static List<Affix> realAffixes() throws IOException {
        List<Affix> affixes = new ArrayList<>();
        try (Stream<Path> files = Files.walk(DATA.resolve("affixes"))) {
            for (Path p : files.filter(f -> f.toString().endsWith(".json")).toList()) {
                String name = DATA.resolve("affixes").relativize(p).toString().replace('\\', '/');
                affixes.add(Affix.parse("sofe:" + name.substring(0, name.length() - 5), JsonParser.parseString(Files.readString(p)).getAsJsonObject()));
            }
        }
        return affixes;
    }

    private static List<GearBase> realBases() throws IOException {
        List<GearBase> bases = new ArrayList<>();
        try (Stream<Path> files = Files.walk(DATA.resolve("gear_bases"))) {
            for (Path p : files.filter(f -> f.toString().endsWith(".json")).toList()) {
                bases.add(GearBase.parse(JsonParser.parseString(Files.readString(p)).getAsJsonObject()));
            }
        }
        return bases;
    }

    @Test
    void affixRangesGrowWithItemLevel() throws IOException {
        Affix bull = realAffixes().stream().filter(a -> a.id().equals("sofe:of_the_bull")).findFirst().orElseThrow();
        assertEquals(GearStat.STRENGTH, bull.stat());
        double[] low = bull.range(1), high = bull.range(30);
        assertTrue(high[1] > low[1]);
        Random random = new Random(1);
        for (int i = 0; i < 200; i++) {
            int v = bull.roll(30, random);
            assertTrue(v >= Math.round(high[0]) && v <= Math.round(high[1]), "value " + v + " out of range");
        }
    }

    @Test
    void everyRealAffixAndBaseParses() throws IOException {
        List<Affix> affixes = realAffixes();
        assertTrue(affixes.size() >= 25, "the starter affixes are missing");
        Set<GearStat> stats = new HashSet<>();
        affixes.forEach(a -> stats.add(a.stat()));
        for (GearStat stat : GearStat.values()) assertTrue(stats.contains(stat), "no affix raises " + stat);
        assertFalse(realBases().isEmpty());
    }

    @Test
    void rarityDecidesHowManyAffixesAndNeverMoreThanTwoOfAKind() throws IOException {
        List<Affix> affixes = realAffixes();
        List<GearBase> bases = realBases();
        Random random = new Random(7);
        for (int i = 0; i < 500; i++) {
            var gen = LootGenerator.builder(bases, affixes).itemLevel(1 + i % 30).playerClass("knight").build().generate(random).orElseThrow();
            GearData gear = gen.gear();
            int n = gear.affixes().size();
            assertTrue(n <= gear.rarity().maxAffixes(), gear.rarity() + " rolled " + n + " affixes");
            long prefixes = gear.affixes().stream().filter(r -> affixes.stream().anyMatch(a -> a.id().equals(r.affix()) && a.kind() == Affix.Kind.PREFIX)).count();
            assertTrue(prefixes <= 2 && n - prefixes <= 2, "more than two prefixes or suffixes");
            assertEquals(gear.affixes().size(), gear.affixes().stream().map(r -> r.stat() + ":" + r.parameter()).distinct().count(), "a stat rolled twice");
        }
    }

    @Test
    void theClassBiasKeepsAboutSixtyPercentOfTheLootForTheClass() throws IOException {
        List<GearBase> bases = realBases();
        long suited = bases.stream().filter(b -> b.suits("sorceress")).count();
        assertTrue(suited > 0 && suited < bases.size(), "the test needs both sorceress and other bases");
        Random random = new Random(11);
        int forClass = 0, total = 10_000;
        LootGenerator generator = LootGenerator.builder(bases, realAffixes()).itemLevel(10).playerClass("sorceress").build();
        for (int i = 0; i < total; i++) {
            String item = generator.generate(random).orElseThrow().item();
            if (bases.stream().anyMatch(b -> b.item().equals(item) && b.suits("sorceress"))) forClass++;
        }
        double share = forClass / (double) total;
        double expected = 0.6 + 0.4 * suited / bases.size(); // bias, plus the class pieces in the unbiased draws
        assertEquals(expected, share, 0.03, "share of class loot");
    }

    @Test
    void eliteLootNeverDropsBelowItsRarity() throws IOException {
        LootGenerator generator = LootGenerator.builder(realBases(), realAffixes()).itemLevel(5).minRarity(Rarity.IMPERIAL).build();
        Random random = new Random(3);
        for (int i = 0; i < 200; i++) assertEquals(Rarity.IMPERIAL, generator.generate(random).orElseThrow().gear().rarity());
    }

    @Test
    void gearCountsOnlyWhenItsRequirementsAreMet() {
        GearData axe = new GearData(10, Rarity.TEMPERED, List.of(new GearData.Roll("sofe:of_the_bull", GearStat.STRENGTH, null, 5)),
                CharacterAttribute.STRENGTH, 30, null, List.of());
        GearData ring = new GearData(2, Rarity.TEMPERED, List.of(new GearData.Roll("sofe:of_the_five_crowns", GearStat.ALL_ATTRIBUTES, null, 2),
                new GearData.Roll("sofe:of_the_knight", GearStat.CLASS_SKILL_RANKS, "knight", 2)), null, 0, null, List.of());
        GearBonuses weak = GearBonuses.of(List.of(axe, ring), 9, a -> 20);
        assertEquals(2, weak.attribute(CharacterAttribute.STRENGTH), "the axe needs 30 Strength");
        GearBonuses strong = GearBonuses.of(List.of(axe, ring), 9, a -> 30);
        assertEquals(7, strong.attribute(CharacterAttribute.STRENGTH));
        assertEquals(2, strong.attribute(CharacterAttribute.VITALITY));
        assertTrue(GearBonuses.of(List.of(axe), 6, a -> 99).isEmpty(), "level 6 is below item level 10 − 3");
        assertEquals(2, strong.bonusRanks("shield_bash", "knight"));
        assertEquals(7, GearBonuses.effectiveRank(5, 2));
        assertEquals(8, GearBonuses.effectiveRank(5, 9), "gear ranks stop at 8");
        assertEquals(0, GearBonuses.effectiveRank(0, 3), "gear never teaches a skill");
    }

    @Test
    void resistancesStopAtSeventyFivePercent() {
        GearData a = new GearData(30, Rarity.IMPERIAL, List.of(new GearData.Roll("x", GearStat.FIRE_RESISTANCE, null, 40)), null, 0, null, List.of());
        GearData b = new GearData(30, Rarity.IMPERIAL, List.of(new GearData.Roll("y", GearStat.FIRE_RESISTANCE, null, 40)), null, 0, null, List.of());
        assertEquals(0.75, GearBonuses.of(List.of(a, b), 30, x -> 10).fraction(GearStat.FIRE_RESISTANCE), 1e-9);
    }

    @Test
    void stockIsPerPlayerAndRestocksAtDawn() {
        MerchantOffer offer = new MerchantOffer("ferid#0", "sofe:minor_pomegranate_elixir", 1, 15, 1, 0, 2);
        EconomyData a = new EconomyData(), b = new EconomyData();
        a.addDinars(100);
        b.addDinars(100);
        assertEquals(EconomyData.BuyResult.BOUGHT, a.buy(offer, 1, 5));
        assertEquals(EconomyData.BuyResult.BOUGHT, a.buy(offer, 1, 5));
        assertEquals(EconomyData.BuyResult.SOLD_OUT, a.buy(offer, 1, 5));
        assertEquals(EconomyData.BuyResult.BOUGHT, b.buy(offer, 1, 5), "player A buying does not change player B's stock");
        assertEquals(EconomyData.BuyResult.BOUGHT, a.buy(offer, 1, 6), "a new day restocks");
        assertEquals(55, a.dinars());
        EconomyData poor = new EconomyData();
        assertEquals(EconomyData.BuyResult.NO_DINARS, poor.buy(offer, 1, 1));
        MerchantOffer later = new MerchantOffer("dilara#1", "sofe:glacial_iron_sword", 1, 180, 2, 0, 1);
        b.addDinars(1000);
        assertEquals(EconomyData.BuyResult.LOCKED, b.buy(later, 1, 1), "an offer never appears below its act");
    }

    @Test
    void buybackKeepsTheLastFiveAtTheirPrice() {
        EconomyData data = new EconomyData();
        for (int i = 0; i < 7; i++) data.sold(new EconomyData.Sold("{id:" + i + "}", 10 + i));
        assertEquals(5, data.buyback().size());
        assertEquals("{id:6}", data.buyback().get(0).itemNbt());
        assertNull(data.takeBuyback(0), "no Dinars, no buyback");
        data.addDinars(16);
        assertEquals("{id:6}", data.takeBuyback(0).itemNbt());
        assertEquals(0, data.dinars());
        assertEquals(4, data.buyback().size());
    }

    @Test
    void relicsCannotBeSoldAndPricesRiseWithLevel() {
        GearData relic = new GearData(10, Rarity.RELIC, List.of(), null, 0, "kaleth_blade", List.of());
        assertTrue(Prices.sellPrice(relic).isEmpty());
        assertTrue(Prices.sellPrice(GearData.common(20)).getAsInt() > Prices.sellPrice(GearData.common(1)).getAsInt());
        assertTrue(Prices.sellPrice(new GearData(5, Rarity.IMPERIAL, List.of(), null, 0, null, List.of())).getAsInt()
                > Prices.sellPrice(new GearData(5, Rarity.TEMPERED, List.of(), null, 0, null, List.of())).getAsInt());
    }

    @Test
    void theFlaskGrowsWithEachArchsin() {
        assertEquals(3, EconomyData.flaskMax(0));
        assertEquals(4, EconomyData.flaskMax(1));
        assertEquals(10, EconomyData.flaskMax(9));
        EconomyData data = new EconomyData();
        for (int i = 0; i < 3; i++) assertTrue(data.useFlask());
        assertFalse(data.useFlask(), "the Flask never goes below 0");
        data.refillFlask(EconomyData.flaskMax(1));
        assertEquals(4, data.flaskCharges());
    }

    @Test
    void everyMerchantFileParses() throws IOException {
        try (Stream<Path> files = Files.list(DATA.resolve("merchant_offers"))) {
            for (Path p : files.toList()) {
                JsonObject json = JsonParser.parseString(Files.readString(p)).getAsJsonObject();
                MerchantOffer.Catalog catalog = MerchantOffer.parse(p.getFileName().toString().replace(".json", ""), json);
                assertFalse(catalog.offers().isEmpty() && catalog.buys().isEmpty(), p + " sells and buys nothing");
            }
        }
    }
}
