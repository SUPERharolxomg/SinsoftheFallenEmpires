package com.sofe.economy;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * One thing a merchant sells, from data/sofe/merchant_offers/&lt;npc&gt;.json (docs/Anexos.md, A4).
 *
 * @param stock how many each player can buy per day
 */
public record MerchantOffer(String key, String item, int count, int price, int minAct, int minFavor, int stock) {

    /** What a merchant buys outside gear, with its price per item. */
    public record Buy(String item, int price) {
    }

    /** All of one merchant's file. */
    public record Catalog(String npc, String role, List<MerchantOffer> offers, List<Buy> buys) {
        public Catalog {
            offers = List.copyOf(offers);
            buys = List.copyOf(buys);
        }

        /** The offers a player in this act sees (Favor comes with Sprint 7). */
        public List<MerchantOffer> available(int act) {
            return offers.stream().filter(o -> act >= o.minAct()).toList();
        }
    }

    public static Catalog parse(String npc, JsonObject json) {
        List<MerchantOffer> offers = new ArrayList<>();
        int i = 0;
        if (json.has("offers")) {
            for (JsonElement e : json.getAsJsonArray("offers")) {
                JsonObject o = e.getAsJsonObject();
                int price = o.get("price").getAsInt();
                if (price < 0) throw new IllegalArgumentException(npc + ": negative price");
                offers.add(new MerchantOffer(npc + "#" + i++, o.get("sell").getAsString(), o.has("count") ? o.get("count").getAsInt() : 1,
                        price, o.has("min_act") ? o.get("min_act").getAsInt() : 1, o.has("min_favor") ? o.get("min_favor").getAsInt() : 0,
                        o.has("stock") ? o.get("stock").getAsInt() : 8));
            }
        }
        List<Buy> buys = new ArrayList<>();
        if (json.has("buys")) {
            for (JsonElement e : json.getAsJsonArray("buys")) {
                JsonObject b = e.getAsJsonObject();
                buys.add(new Buy(b.get("item").getAsString(), b.get("price").getAsInt()));
            }
        }
        return new Catalog(npc, json.has("role") ? json.get("role").getAsString() : "quartermaster", offers, buys);
    }
}
