package com.sofe.entity.npc;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;

import java.util.Optional;

/**
 * A SoFE merchant (Ferid, Dilara, Yusuf, Selim...), never a vanilla villager. Each has a
 * {@link MerchantRole}; its offers, stock and Dinars arrive in Sprint 5.5.
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
