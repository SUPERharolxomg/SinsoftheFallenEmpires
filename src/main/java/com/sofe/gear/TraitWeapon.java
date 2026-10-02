package com.sofe.gear;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeMod;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** A weapon of the arsenal: SoFE gear (it rolls affixes) with its traits and, for polearms, more reach. */
public class TraitWeapon extends GearItems.Sword {
    private static final UUID REACH_ID = UUID.fromString("5b2d9a61-7e3c-4f1a-9b84-2c6e1d0f7a35");
    private final Set<WeaponTrait> traits;
    private final double reach;
    private final String requiredClass;

    public TraitWeapon(Tier tier, int damage, float speed, double reach, Set<WeaponTrait> traits, Properties properties) {
        this(tier, damage, speed, reach, traits, null, properties);
    }

    /** A class weapon (Diablo II style): only the Bearer of that class strikes with it in full. */
    public TraitWeapon(Tier tier, int damage, float speed, double reach, Set<WeaponTrait> traits, String requiredClass, Properties properties) {
        super(tier, damage, speed, properties);
        this.traits = traits.isEmpty() ? EnumSet.noneOf(WeaponTrait.class) : EnumSet.copyOf(traits);
        this.reach = reach;
        this.requiredClass = requiredClass;
    }

    /** The class this weapon belongs to, or null when anyone can wield it. */
    public String requiredClass() {
        return requiredClass;
    }

    public Set<WeaponTrait> traits() {
        return traits;
    }

    public boolean has(WeaponTrait trait) {
        return traits.contains(trait);
    }

    public double reach() {
        return reach;
    }

    @Override
    @SuppressWarnings("deprecation")
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        Multimap<Attribute, AttributeModifier> base = super.getDefaultAttributeModifiers(slot);
        if (slot != EquipmentSlot.MAINHAND || reach == 0) return base;
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.putAll(base);
        builder.put(ForgeMod.ENTITY_REACH.get(), new AttributeModifier(REACH_ID, "Weapon reach", reach, AttributeModifier.Operation.ADDITION));
        return builder.build();
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        if (has(WeaponTrait.PLANTS) && (state.is(BlockTags.LEAVES) || state.is(BlockTags.REPLACEABLE) || state.is(BlockTags.FLOWERS)
                || state.is(BlockTags.CROPS) || state.is(BlockTags.SAPLINGS))) {
            return 15f;
        }
        return super.getDestroySpeed(stack, state);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        for (WeaponTrait trait : traits) {
            if (trait == WeaponTrait.REACH) continue; // the reach shows as an attribute
            tooltip.add(Component.translatable(trait.translationKey()).withStyle(ChatFormatting.DARK_AQUA));
        }
        com.sofe.gear.ranged.ClassBound.tooltip(tooltip, requiredClass);
    }
}
