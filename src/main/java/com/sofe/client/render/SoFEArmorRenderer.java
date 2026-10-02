package com.sofe.client.render;

import com.sofe.SoFEMod;
import com.sofe.gear.ModeledArmor;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

/** Draws a {@link ModeledArmor} piece with its set's 3D model; GeckoLib shows the bones of the slot it is worn in. */
public class SoFEArmorRenderer extends GeoArmorRenderer<ModeledArmor> {
    private static final ResourceLocation NO_ANIMATIONS = SoFEMod.id("animations/armor/static.animation.json");

    public SoFEArmorRenderer() {
        super(new GeoModel<>() {
            @Override
            public ResourceLocation getModelResource(ModeledArmor armor) {
                return SoFEMod.id("geo/armor/" + armor.setName() + ".geo.json");
            }

            @Override
            public ResourceLocation getTextureResource(ModeledArmor armor) {
                return SoFEMod.id("textures/models/armor/geo/" + armor.setName() + ".png");
            }

            @Override
            public ResourceLocation getAnimationResource(ModeledArmor armor) {
                return NO_ANIMATIONS;
            }
        });
    }
}
