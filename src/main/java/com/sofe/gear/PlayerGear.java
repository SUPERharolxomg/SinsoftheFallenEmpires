package com.sofe.gear;

import com.sofe.player.PlayerClass;
import com.sofe.player.PlayerClassCapability;
import com.sofe.player.PlayerClassData;
import com.sofe.progression.AttributeSheet;
import com.sofe.progression.CharacterAttribute;
import com.sofe.progression.ProgressionCapability;
import com.sofe.progression.ProgressionData;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.event.CurioChangeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The gear a Bearer wears and what it adds up to (docs/Pociones.md): armor, the weapon in the main
 * hand, and through Curios the amulet, the two rings and the Talisman Pouch. Cached per player and
 * recomputed when anything they wear changes.
 */
public final class PlayerGear {
    public static final String NECKLACE = "necklace", RING = "ring", TALISMAN = "talisman", POTION_BELT = "potion_belt";
    private static final Map<UUID, GearBonuses> CACHE = new ConcurrentHashMap<>();

    private PlayerGear() {
    }

    /** Every gear piece that counts: armor slots, the main hand weapon, and the Curios slots. */
    public static List<ItemStack> equipped(Player player) {
        List<ItemStack> stacks = new ArrayList<>();
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            ItemStack stack = player.getItemBySlot(slot);
            if (stack.getItem() instanceof SoFEGear gear && gear.gearSlot() == GearSlot.ARMOR) stacks.add(stack);
        }
        ItemStack main = player.getMainHandItem();
        if (main.getItem() instanceof SoFEGear gear && gear.gearSlot() == GearSlot.WEAPON) stacks.add(main);
        CuriosApi.getCuriosInventory(player).ifPresent(curios -> {
            for (var result : curios.findCurios(NECKLACE, RING, TALISMAN)) {
                ItemStack stack = result.stack();
                if (stack.getItem() instanceof SoFEGear gear && gear.gearSlot() != GearSlot.ARMOR && gear.gearSlot() != GearSlot.WEAPON) {
                    stacks.add(stack);
                }
            }
        });
        return stacks;
    }

    /** One stat of everything the player wears (for tests and tooltips). */
    public static double get(Player player, GearStat stat) {
        return bonuses(player).get(stat);
    }

    public static GearBonuses bonuses(Player player) {
        return CACHE.computeIfAbsent(player.getUUID(), id -> compute(player));
    }

    /** The player's attribute values without gear: requirements never count gear itself. */
    public static int baseAttribute(Player player, CharacterAttribute attribute) {
        Optional<PlayerClass> playerClass = PlayerClassCapability.get(player).flatMap(PlayerClassData::get);
        Optional<ProgressionData> progress = ProgressionCapability.get(player);
        if (playerClass.isEmpty() || progress.isEmpty()) return AttributeSheet.BASE;
        return progress.get().attributes().value(attribute, playerClass.get());
    }

    public static int level(Player player) {
        return ProgressionCapability.get(player).map(ProgressionData::level).orElse(1);
    }

    /** Whether this player meets the item's level and attribute requirements. */
    public static boolean meets(Player player, GearData gear) {
        return GearBonuses.meetsRequirements(gear, level(player), a -> baseAttribute(player, a));
    }

    private static GearBonuses compute(Player player) {
        List<GearData> gear = new ArrayList<>();
        String cls = com.sofe.skill.ClassState.classOf(player).map(com.sofe.player.PlayerClass::id).orElse(null);
        for (ItemStack stack : equipped(player)) {
            GearNbt.read(stack).filter(g -> g.relic() == null || GearDataManager.relic(g.relic()).map(r -> r.suits(cls)).orElse(true)).ifPresent(gear::add);
        }
        if (gear.isEmpty()) return GearBonuses.NONE;
        return GearBonuses.of(gear, level(player), a -> baseAttribute(player, a));
    }

    /** Forget the cached sum; the next read adds the gear up again. */
    public static void invalidate(Player player) {
        CACHE.remove(player.getUUID());
    }

    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        if (event.getEntity() instanceof Player player && !player.level().isClientSide()) changed(player);
    }

    public static void onCurioChange(CurioChangeEvent event) {
        if (event.getEntity() instanceof Player player && !player.level().isClientSide()) changed(player);
    }

    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        CACHE.remove(event.getEntity().getUUID());
    }

    /** Recompute and push the results: health, resource maximum, the character sheet. */
    public static void changed(Player player) {
        invalidate(player);
        if (player instanceof net.minecraft.server.level.ServerPlayer server) {
            com.sofe.combat.CombatHandler.refresh(server);
            com.sofe.progression.ProgressionHandler.sync(server);
        }
    }
}
