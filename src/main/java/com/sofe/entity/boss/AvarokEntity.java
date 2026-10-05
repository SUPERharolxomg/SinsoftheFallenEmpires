package com.sofe.entity.boss;

import com.sofe.gear.GearNbt;
import com.sofe.gear.Rarity;
import com.sofe.gear.ranged.Spell;
import com.sofe.registry.ItemRegistry;
import com.sofe.story.Sin;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.event.entity.player.PlayerEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Avarok, the Archsin of Greed, in the Golden Vaults of Aureum: he steals items from the inventory during the
 * fight. He steals from each Bearer separately (never story items, the Flask, Relics or anything bound), grows a
 * little stronger with every theft, and gives everything back when he dies or the fight resets (docs/Jugabilidad.md,
 * G1 and G14); a Bearer who left the world gets it back when they return. The hoard is kept in the world.
 */
public class AvarokEntity extends ArchsinEntity {
    public static final String BOSS_ID = "sofe:avarok";
    public static final double STRENGTH_PER_THEFT = 0.04, MAX_THEFT_STRENGTH = 0.6;
    private static final UUID GREED_ID = UUID.fromString("7d0a3c2e-5b6f-4f7e-9a1d-2c3b4e5f6a80");
    private int thefts;

    public AvarokEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 520.0).add(Attributes.ATTACK_DAMAGE, 13.0)
                .add(Attributes.MOVEMENT_SPEED, 0.27).add(Attributes.ARMOR, 16.0).add(Attributes.KNOCKBACK_RESISTANCE, 0.9)
                .add(Attributes.FOLLOW_RANGE, 48.0);
    }

    @Override
    public String modelName() {
        return "avarok";
    }

    @Override
    public String bossId() {
        return BOSS_ID;
    }

    @Override
    public Sin sin() {
        return Sin.GREED;
    }

    @Override
    protected String introDialogue() {
        return "sofe:act4/avarok_temptation";
    }

    @Override
    protected double arenaRadius() {
        return 20;
    }

    public int thefts() {
        return thefts;
    }

    @Override
    protected void fightTick(ServerLevel level, List<ServerPlayer> fighters) {
        int every = phase() >= 2 ? 140 : 220;
        if (this.tickCount % every == 0) {
            List<ServerPlayer> robbed = phase() >= 2 ? fighters : List.of(fighters.get(random.nextInt(fighters.size())));
            for (ServerPlayer p : robbed) steal(level, p);
        }
        if (this.tickCount % (phase() >= 2 ? 60 : 90) == 30) {
            for (ServerPlayer p : fighters) BossKit.bolt(level, this, p, Spell.HOLY, phase() >= 2 ? 7 : 5, 1.3f, 8);
            level.playSound(null, blockPosition(), SoundEvents.CHAIN_PLACE, SoundSource.HOSTILE, 1.4f, 1.8f);
        }
    }

    /** Whether Avarok may take this: nothing of the story, the Flask, Relics or anything bound. */
    public static boolean stealable(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (stack.is(ItemRegistry.BEARERS_FLASK.get()) || stack.is(ItemRegistry.CODEX_SHARD.get()) || stack.is(ItemRegistry.RETURN_SCROLL.get())) return false;
        if (GearNbt.read(stack).map(g -> g.rarity() == Rarity.RELIC).orElse(false)) return false;
        return stack.getTag() == null || !stack.getTag().contains("sofe_bound");
    }

    /** One stack out of a Bearer's pack, into his hoard. */
    public boolean steal(ServerLevel level, ServerPlayer player) {
        var inventory = player.getInventory();
        List<Integer> slots = new ArrayList<>();
        for (int i = 0; i < inventory.items.size(); i++) {
            if (i == inventory.selected) continue; // not what is in hand
            if (stealable(inventory.items.get(i))) slots.add(i);
        }
        if (slots.isEmpty()) return false;
        int slot = slots.get(random.nextInt(slots.size()));
        ItemStack taken = inventory.items.get(slot);
        inventory.items.set(slot, ItemStack.EMPTY);
        Hoard.get(level.getServer()).add(player.getUUID(), taken);
        thefts++;
        greed();
        level.sendParticles(ParticleTypes.WAX_ON, player.getX(), player.getY() + 1, player.getZ(), 12, 0.3, 0.5, 0.3, 0.05);
        level.playSound(null, player.blockPosition(), SoundEvents.ITEM_PICKUP, SoundSource.HOSTILE, 1.5f, 0.6f);
        player.displayClientMessage(Component.translatable("message.sofe.avarok.stole", taken.getHoverName()).withStyle(ChatFormatting.GOLD), true);
        return true;
    }

    private void greed() {
        var damage = getAttribute(Attributes.ATTACK_DAMAGE);
        if (damage == null) return;
        damage.removeModifier(GREED_ID);
        double bonus = Math.min(MAX_THEFT_STRENGTH, thefts * STRENGTH_PER_THEFT);
        if (bonus > 0) damage.addTransientModifier(new AttributeModifier(GREED_ID, "SoFE Avarok's greed", bonus, AttributeModifier.Operation.MULTIPLY_TOTAL));
    }

    /** Dead or reset, he gives everything back to everyone; those away get it when they return. */
    @Override
    protected void onFightOver(ServerLevel level, boolean defeated) {
        Hoard hoard = Hoard.get(level.getServer());
        for (UUID id : new ArrayList<>(hoard.owners())) {
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(id);
            if (player != null) hoard.returnTo(player);
        }
        thefts = 0;
        greed();
    }

    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            Hoard hoard = Hoard.get(player.server);
            // only once Avarok is no longer fighting: no Avarok alive with this player as a fighter means the fight is over
            boolean fighting = player.serverLevel().getEntitiesOfClass(AvarokEntity.class, player.getBoundingBox().inflate(96), AvarokEntity::isFighting).size() > 0;
            if (!fighting) hoard.returnTo(player);
        }
    }

    /** What Avarok holds, by owner, kept in the world. */
    public static class Hoard extends SavedData {
        private static final String NAME = "sofe_avarok_hoard";
        private final Map<UUID, List<ItemStack>> held = new HashMap<>();

        public static Hoard get(MinecraftServer server) {
            return server.overworld().getDataStorage().computeIfAbsent(Hoard::load, Hoard::new, NAME);
        }

        public List<UUID> owners() {
            return new ArrayList<>(held.keySet());
        }

        public List<ItemStack> of(UUID owner) {
            return held.getOrDefault(owner, List.of());
        }

        void add(UUID owner, ItemStack stack) {
            held.computeIfAbsent(owner, k -> new ArrayList<>()).add(stack.copy());
            setDirty();
        }

        public void returnTo(ServerPlayer player) {
            List<ItemStack> mine = held.remove(player.getUUID());
            if (mine == null || mine.isEmpty()) return;
            for (ItemStack stack : mine) {
                if (!player.getInventory().add(stack)) player.drop(stack, false);
            }
            player.displayClientMessage(Component.translatable("message.sofe.avarok.returned").withStyle(ChatFormatting.GOLD), false);
            setDirty();
        }

        static Hoard load(CompoundTag tag) {
            Hoard hoard = new Hoard();
            for (String key : tag.getAllKeys()) {
                List<ItemStack> list = new ArrayList<>();
                for (Tag t : tag.getList(key, Tag.TAG_COMPOUND)) list.add(ItemStack.of((CompoundTag) t));
                hoard.held.put(UUID.fromString(key), list);
            }
            return hoard;
        }

        @Override
        public CompoundTag save(CompoundTag tag) {
            held.forEach((id, list) -> {
                ListTag items = new ListTag();
                for (ItemStack s : list) items.add(s.save(new CompoundTag()));
                tag.put(id.toString(), items);
            });
            return tag;
        }
    }

    // --- signature attack (SoFEBossEntity.Signature): he swings his sack of gold in a wide arc: coins in the eyes, bones broken

    private net.minecraft.world.phys.Vec3 signatureAim = net.minecraft.world.phys.Vec3.ZERO;

    @Override
    protected Signature signature() {
        return new Signature("avarok_sack", 28, 240, 7);
    }

    @Override
    protected void signatureWindup(net.minecraft.server.level.ServerLevel level, net.minecraft.world.entity.LivingEntity target, int tick,
                                   java.util.List<net.minecraft.server.level.ServerPlayer> fighters) {
        if (tick == 1) signatureAim = Signatures.toward(this, target);
        if (tick % 4 == 0) Signatures.drawArc(level, this, signatureAim, 7, 180, net.minecraft.core.particles.ParticleTypes.WAX_ON);
    }

    @Override
    protected void signatureStrike(net.minecraft.server.level.ServerLevel level, net.minecraft.world.entity.LivingEntity target,
                                   java.util.List<net.minecraft.server.level.ServerPlayer> fighters) {
        for (var p : Signatures.arc(this, signatureAim, 7, 180, fighters)) {
            Signatures.strike(this, p, signatureDamage(2.0), position(), 1.8, 0.4);
            p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.BLINDNESS, 40, 0), this);
        }
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.WAX_ON, getX(), getY() + 1.5, getZ(), 40, 3, 1, 3, 0.2);
        level.playSound(null, blockPosition(), net.minecraft.sounds.SoundEvents.CHAIN_BREAK, net.minecraft.sounds.SoundSource.HOSTILE, 2.0f, 0.5f);
    }
}
