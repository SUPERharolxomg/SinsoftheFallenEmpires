package com.sofe.crafting;

import com.sofe.gear.Affix;
import com.sofe.gear.GearData;
import com.sofe.gear.GearDataManager;
import com.sofe.gear.GearNbt;
import com.sofe.gear.Rarity;
import com.sofe.gear.Sockets;
import com.sofe.registry.material.Material;
import com.sofe.registry.material.MaterialForm;
import com.sofe.registry.material.MaterialRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The Tempering Anvil (docs/Pociones.md, "Forges and crafting": Dilara): it works the gear in the Bearer's hand with
 * aetherium. Using it tempers the item (one affix, chosen at random, grows by a tenth, at least one point), up to
 * three times per item; sneaking rerolls the weakest affix's value within what its item level allows. Gems and Relics
 * are left alone. Each work costs aetherium shards.
 */
public class TemperingAnvilBlock extends Block {
    public static final int MAX_TEMPERS = 3, TEMPER_COST = 1, REROLL_COST = 2;
    private static final String TEMPERS = "tempers";

    public TemperingAnvilBlock(Properties properties) {
        super(properties);
    }

    public static Item aetherium() {
        return MaterialRegistry.item(Material.AETHERIUM, MaterialForm.SHARD);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        if (player instanceof ServerPlayer server) {
            ItemStack gear = player.getMainHandItem();
            Result result = player.isShiftKeyDown() ? reroll(server, gear, server.getRandom()) : temper(server, gear, server.getRandom());
            tell(server, result, gear);
            if (result == Result.TEMPERED || result == Result.REROLLED) {
                level.playSound(null, pos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 0.8f, result == Result.TEMPERED ? 1.2f : 0.8f);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    public enum Result { TEMPERED, REROLLED, NOT_GEAR, NOTHING_TO_WORK, WORN_OUT, NO_AETHERIUM }

    /** The affixes the anvil may work: not gems, and nothing on a Relic. */
    private static List<Integer> workable(GearData gear) {
        List<Integer> out = new ArrayList<>();
        if (gear.rarity() == Rarity.RELIC) return out;
        for (int i = 0; i < gear.affixes().size(); i++) if (!Sockets.isGem(gear.affixes().get(i))) out.add(i);
        return out;
    }

    public static int tempers(ItemStack stack) {
        var tag = stack.getTagElement("sofe_gear");
        return tag == null ? 0 : tag.getInt(TEMPERS);
    }

    public static Result temper(ServerPlayer player, ItemStack stack, RandomSource random) {
        Optional<GearData> read = GearNbt.read(stack);
        if (read.isEmpty()) return Result.NOT_GEAR;
        GearData gear = read.get();
        List<Integer> workable = workable(gear);
        if (workable.isEmpty()) return Result.NOTHING_TO_WORK;
        if (tempers(stack) >= MAX_TEMPERS) return Result.WORN_OUT;
        if (!pay(player, TEMPER_COST)) return Result.NO_AETHERIUM;
        int i = workable.get(random.nextInt(workable.size()));
        List<GearData.Roll> rolls = new ArrayList<>(gear.affixes());
        GearData.Roll r = rolls.get(i);
        rolls.set(i, new GearData.Roll(r.affix(), r.stat(), r.parameter(), r.value() + Math.max(1, Math.round(r.value() * 0.1f))));
        GearNbt.write(stack, gear.withAffixes(gear.rarity(), rolls));
        stack.getOrCreateTagElement("sofe_gear").putInt(TEMPERS, tempers(stack) + 1);
        return Result.TEMPERED;
    }

    public static Result reroll(ServerPlayer player, ItemStack stack, RandomSource random) {
        Optional<GearData> read = GearNbt.read(stack);
        if (read.isEmpty()) return Result.NOT_GEAR;
        GearData gear = read.get();
        // the weakest affix: the one furthest down its own range
        int weakest = -1;
        double lowest = Double.MAX_VALUE;
        Affix weakestAffix = null;
        for (int i : workable(gear)) {
            GearData.Roll r = gear.affixes().get(i);
            Affix affix = GearDataManager.affixes().stream().filter(a -> a.id().equals(r.affix())).findFirst().orElse(null);
            if (affix == null) continue;
            double[] range = affix.range(gear.itemLevel());
            double where = range[1] > range[0] ? (r.value() - range[0]) / (range[1] - range[0]) : 1;
            if (where < lowest) {
                lowest = where;
                weakest = i;
                weakestAffix = affix;
            }
        }
        if (weakest < 0) return Result.NOTHING_TO_WORK;
        if (!pay(player, REROLL_COST)) return Result.NO_AETHERIUM;
        List<GearData.Roll> rolls = new ArrayList<>(gear.affixes());
        GearData.Roll r = rolls.get(weakest);
        int value = weakestAffix.roll(gear.itemLevel(), new java.util.Random(random.nextLong()));
        rolls.set(weakest, new GearData.Roll(r.affix(), r.stat(), r.parameter(), value));
        GearNbt.write(stack, gear.withAffixes(gear.rarity(), rolls));
        return Result.REROLLED;
    }

    private static boolean pay(ServerPlayer player, int shards) {
        if (player.isCreative()) return true;
        var inventory = player.getInventory();
        if (inventory.countItem(aetherium()) < shards) return false;
        int left = shards;
        for (int i = 0; i < inventory.getContainerSize() && left > 0; i++) {
            ItemStack s = inventory.getItem(i);
            if (!s.is(aetherium())) continue;
            int take = Math.min(left, s.getCount());
            s.shrink(take);
            left -= take;
        }
        return true;
    }

    private static void tell(ServerPlayer player, Result result, ItemStack gear) {
        Component message = switch (result) {
            case TEMPERED -> Component.translatable("message.sofe.anvil.tempered", tempers(gear), MAX_TEMPERS).withStyle(ChatFormatting.GOLD);
            case REROLLED -> Component.translatable("message.sofe.anvil.rerolled").withStyle(ChatFormatting.GOLD);
            case NOT_GEAR -> Component.translatable("message.sofe.anvil.how").withStyle(ChatFormatting.GRAY);
            case NOTHING_TO_WORK -> Component.translatable("message.sofe.anvil.nothing").withStyle(ChatFormatting.RED);
            case WORN_OUT -> Component.translatable("message.sofe.anvil.worn_out", MAX_TEMPERS).withStyle(ChatFormatting.RED);
            case NO_AETHERIUM -> Component.translatable("message.sofe.anvil.no_aetherium",
                    player.isShiftKeyDown() ? REROLL_COST : TEMPER_COST).withStyle(ChatFormatting.RED);
        };
        player.displayClientMessage(message, true);
    }
}
