package com.sofe.entity.npc;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

/**
 * What a merchant sells (docs/Anexos.md, "The new merchant system"). The shop itself comes with the
 * Wallet and the offers JSON in Sprint 5.5; for now merchants talk.
 */
public enum MerchantRole {
    ALCHEMIST("ferid"),
    SMITH("dilara"),
    QUARTERMASTER("yusuf"),
    MONEY_CHANGER("selim");

    private final String defaultNpc;

    MerchantRole(String defaultNpc) {
        this.defaultNpc = defaultNpc;
    }

    /** The merchant who has this role in Sulthari. */
    public String defaultNpc() {
        return defaultNpc;
    }

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    public String translationKey() {
        return "merchant.sofe.role." + id();
    }

    public static Optional<MerchantRole> byId(String id) {
        return Arrays.stream(values()).filter(r -> r.id().equals(id)).findFirst();
    }

    public static Optional<MerchantRole> byNpc(String npc) {
        return Arrays.stream(values()).filter(r -> r.defaultNpc.equals(npc)).findFirst();
    }
}
