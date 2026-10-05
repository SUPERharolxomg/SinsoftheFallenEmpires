package com.sofe.economy;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * One player's economy (docs/Pociones.md, docs/Anexos.md A4): their Dinars, the stock left of each
 * offer today, the last items they sold (buyback), the Blueprints they have learned and the charges of
 * their Bearer's Flask. Everything is per player, so one player buying never takes stock from another.
 */
public final class EconomyData {
    public static final int BUYBACK_SIZE = 5;
    public static final int FLASK_START = 3, FLASK_MAX = 10;

    /** An item sold, kept for buyback at the price it was sold for. */
    public record Sold(String itemNbt, int price) {
    }

    private long dinars;
    private final Map<String, Integer> bought = new HashMap<>();
    private long stockDay = -1;
    private final Deque<Sold> buyback = new ArrayDeque<>();
    private final Set<String> blueprints = new HashSet<>();
    private int flaskCharges = FLASK_START;
    /** Favor with each empire, in points (docs/Anexos.md, "Favor per empire"), by region id. */
    private final Map<String, Integer> favor = new HashMap<>();

    /** The points each Favor rank needs: Stranger, Known, Trusted, Honored, Sworn, Exalted. */
    public static final int[] FAVOR_RANKS = {0, 100, 300, 600, 1000, 1600};
    public static final int MAX_FAVOR_RANK = FAVOR_RANKS.length - 1;
    /** Each rank of Favor takes 2% off that empire's prices (10% at Exalted). */
    public static final double DISCOUNT_PER_RANK = 0.02;

    public int favorPoints(String empire) {
        return favor.getOrDefault(empire, 0);
    }

    public int favorRank(String empire) {
        return rankOf(favorPoints(empire));
    }

    public static int rankOf(int points) {
        int rank = 0;
        for (int i = 0; i < FAVOR_RANKS.length; i++) if (points >= FAVOR_RANKS[i]) rank = i;
        return rank;
    }

    /** Adds Favor; returns the new rank if it went up, else -1. */
    public int addFavor(String empire, int points) {
        if (points <= 0) return -1;
        int before = favorRank(empire);
        favor.merge(empire, points, Integer::sum);
        int after = favorRank(empire);
        return after > before ? after : -1;
    }

    public Map<String, Integer> favor() {
        return Map.copyOf(favor);
    }

    public void loadFavor(Map<String, Integer> points) {
        favor.clear();
        favor.putAll(points);
    }

    /** A price after the Favor discount with its empire. */
    public static int discounted(int price, int rank) {
        return Math.max(1, (int) Math.round(price * (1 - DISCOUNT_PER_RANK * Math.max(0, Math.min(MAX_FAVOR_RANK, rank)))));
    }

    public long dinars() {
        return dinars;
    }

    public void addDinars(long amount) {
        dinars = Math.max(0, dinars + amount);
    }

    /** Spends only if there is enough. */
    public boolean spend(long amount) {
        if (amount < 0 || dinars < amount) return false;
        dinars -= amount;
        return true;
    }

    /** At dawn every offer is full again (docs/Anexos.md, "Restock"). */
    public void restockIfNewDay(long day) {
        if (day != stockDay) {
            bought.clear();
            stockDay = day;
        }
    }

    public int left(MerchantOffer offer) {
        return Math.max(0, offer.stock() - bought.getOrDefault(offer.key(), 0));
    }

    public enum BuyResult { BOUGHT, NO_DINARS, SOLD_OUT, LOCKED }

    /** Checks act, stock and Dinars, then takes the price. The caller gives the item when BOUGHT. */
    public BuyResult buy(MerchantOffer offer, int act, long day) {
        return buy(offer, act, MAX_FAVOR_RANK, offer.price(), day);
    }

    /** As buy, with the Favor the buyer has with the merchant's empire and the price after its discount. */
    public BuyResult buy(MerchantOffer offer, int act, int favorRank, int price, long day) {
        restockIfNewDay(day);
        if (act < offer.minAct() || favorRank < offer.minFavor()) return BuyResult.LOCKED;
        if (left(offer) <= 0) return BuyResult.SOLD_OUT;
        if (!spend(price)) return BuyResult.NO_DINARS;
        bought.merge(offer.key(), 1, Integer::sum);
        return BuyResult.BOUGHT;
    }

    public void sold(Sold item) {
        buyback.addFirst(item);
        while (buyback.size() > BUYBACK_SIZE) buyback.removeLast();
    }

    public List<Sold> buyback() {
        return List.copyOf(buyback);
    }

    /** Buys back the n-th last sold item at the price it was sold for. */
    public Sold takeBuyback(int index) {
        List<Sold> list = buyback();
        if (index < 0 || index >= list.size()) return null;
        Sold sold = list.get(index);
        if (!spend(sold.price())) return null;
        buyback.remove(sold);
        return sold;
    }

    public boolean learn(String blueprint) {
        return blueprints.add(blueprint);
    }

    public boolean knows(String blueprint) {
        return blueprints.contains(blueprint);
    }

    public Set<String> blueprints() {
        return Set.copyOf(blueprints);
    }

    public int flaskCharges() {
        return flaskCharges;
    }

    /** 3 charges at the start, +1 for each Archsin defeated, up to 10. */
    public static int flaskMax(int archsinsDefeated) {
        return Math.min(FLASK_MAX, FLASK_START + Math.max(0, archsinsDefeated));
    }

    public boolean useFlask() {
        if (flaskCharges <= 0) return false;
        flaskCharges--;
        return true;
    }

    public void refillFlask(int max) {
        flaskCharges = max;
    }

    // --- save and load (the Forge capability writes these to NBT)

    public Map<String, Integer> boughtToday() {
        return Map.copyOf(bought);
    }

    public long stockDay() {
        return stockDay;
    }

    public void load(long dinars, Map<String, Integer> bought, long stockDay, List<Sold> buyback, Set<String> blueprints, int flask) {
        this.dinars = Math.max(0, dinars);
        this.bought.clear();
        this.bought.putAll(bought);
        this.stockDay = stockDay;
        this.buyback.clear();
        buyback.stream().limit(BUYBACK_SIZE).forEach(this.buyback::addLast);
        this.blueprints.clear();
        this.blueprints.addAll(blueprints);
        this.flaskCharges = Math.max(0, Math.min(FLASK_MAX, flask));
    }

    public void copyFrom(EconomyData other) {
        load(other.dinars, other.bought, other.stockDay, other.buyback(), other.blueprints, other.flaskCharges);
        loadFavor(other.favor);
    }
}
