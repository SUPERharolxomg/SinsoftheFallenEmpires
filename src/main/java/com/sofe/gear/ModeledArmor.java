package com.sofe.gear;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;

/**
 * SoFE armor worn as a 3D model (GeckoLib), as in Armor of the Ages: horns, crests, pauldrons, robes. The
 * model and texture are scripts/make_armor_models.py's, one per set: geo/armor/&lt;set&gt;.geo.json and
 * textures/models/armor/geo/&lt;set&gt;.png.
 */
public class ModeledArmor extends GearItems.Armor implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public ModeledArmor(ArmorMaterial material, Type type, Properties properties) {
        super(material, type, properties);
    }

    /** The set's name: the model and texture file names. */
    public String setName() {
        return getMaterial().getName().replace("sofe:", "");
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private com.sofe.client.render.SoFEArmorRenderer renderer;

            @Override
            public HumanoidModel<?> getHumanoidArmorModel(LivingEntity entity, ItemStack stack, EquipmentSlot slot, HumanoidModel<?> original) {
                if (renderer == null) renderer = new com.sofe.client.render.SoFEArmorRenderer();
                renderer.prepForRender(entity, stack, slot, original);
                return renderer;
            }
        });
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // the armor does not animate on its own: it follows the body
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
