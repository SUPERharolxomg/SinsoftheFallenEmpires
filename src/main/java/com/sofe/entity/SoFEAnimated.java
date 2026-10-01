package com.sofe.entity;

import net.minecraft.world.entity.LivingEntity;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A mob drawn with a GeckoLib model (scripts/make_mob_models.py): it plays
 * animation.&lt;model&gt;.attack while swinging, .walk while moving and .idle otherwise.
 */
public interface SoFEAnimated extends GeoEntity {

    /** The model's name: geo/entity/&lt;model&gt;.geo.json, textures/entity/&lt;model&gt;.png and its animations. */
    String modelName();

    @Override
    default void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main", 5, state -> {
            if (((LivingEntity) this).swinging) return state.setAndContinue(Animations.of(modelName(), "attack", false));
            return state.setAndContinue(Animations.of(modelName(), state.isMoving() ? "walk" : "idle", true));
        }));
    }

    /** Built once per model and animation, not every frame. */
    final class Animations {
        private static final Map<String, RawAnimation> CACHE = new ConcurrentHashMap<>();

        private Animations() {
        }

        static RawAnimation of(String model, String name, boolean loop) {
            String id = "animation." + model + "." + name;
            return CACHE.computeIfAbsent(id, key -> loop ? RawAnimation.begin().thenLoop(key) : RawAnimation.begin().thenPlay(key));
        }
    }
}
