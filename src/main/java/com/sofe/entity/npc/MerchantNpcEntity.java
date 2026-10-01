package com.sofe.entity.npc;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;

import java.util.Optional;

/**
 * A SoFE merchant (Ferid, Dilara, Yusuf, Selim...), never a vanilla villager. Each has a
 * {@link MerchantRole} and a shop from data/sofe/merchant_offers/&lt;npc&gt;.json.
 */
public class MerchantNpcEntity extends StoryNpcEntity {
    private MerchantRole role = MerchantRole.QUARTERMASTER;

    public MerchantNpcEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    public MerchantRole role() {
        return role;
    }

    public void setMerchant(String npc, MerchantRole role) {
        this.role = role;
        setNpcId(npc);
    }

    /**
     * Using a merchant opens their shop; crouching and using them talks instead. Opening the shop also
     * counts as talking to them for quests.
     */
    @Override
    protected net.minecraft.world.InteractionResult mobInteract(net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand hand) {
        if (hand != net.minecraft.world.InteractionHand.MAIN_HAND || player.isShiftKeyDown()
                || com.sofe.economy.MerchantService.catalog(npcId()).isEmpty()) {
            return super.mobInteract(player, hand);
        }
        if (player instanceof net.minecraft.server.level.ServerPlayer server) {
            getLookControl().setLookAt(player);
            com.sofe.quest.QuestEngine.event(server, new com.sofe.quest.QuestEvent.Talked(npcId()));
            com.sofe.economy.MerchantService.open(server, this, npcId());
        }
        return net.minecraft.world.InteractionResult.sidedSuccess(level().isClientSide());
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("role", role.id());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        Optional<MerchantRole> saved = MerchantRole.byId(tag.getString("role"));
        saved.ifPresent(r -> this.role = r);
    }
}
