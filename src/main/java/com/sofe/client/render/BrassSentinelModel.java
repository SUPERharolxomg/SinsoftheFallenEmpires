package com.sofe.client.render;

import com.sofe.SoFEMod;
import com.sofe.entity.boss.BrassSentinelEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

/** Placeholder model of the Brass Sentinel: a broad clockwork golem with a gear on its chest. */
public class BrassSentinelModel extends HierarchicalModel<BrassSentinelEntity> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(SoFEMod.id("brass_sentinel"), "main");
    private final ModelPart root, head, rightArm, leftArm, rightLeg, leftLeg;

    public BrassSentinelModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        this.rightArm = root.getChild("right_arm");
        this.leftArm = root.getChild("left_arm");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition parts = mesh.getRoot();
        parts.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-5, -10, -5, 10, 10, 10), PartPose.offset(0, -14, 0));
        parts.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 20).addBox(-9, -14, -6, 18, 16, 12)
                .texOffs(60, 0).addBox(-3, -10, -7, 6, 6, 1), PartPose.offset(0, 0, 0));
        parts.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(60, 20).addBox(-4, -2, -3, 5, 26, 6), PartPose.offset(-10, -12, 0));
        parts.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(60, 20).mirror().addBox(-1, -2, -3, 5, 26, 6), PartPose.offset(10, -12, 0));
        parts.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 48).addBox(-3, 0, -3, 6, 22, 6), PartPose.offset(-4.5f, 2, 0));
        parts.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 48).mirror().addBox(-3, 0, -3, 6, 22, 6), PartPose.offset(4.5f, 2, 0));
        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(BrassSentinelEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        head.yRot = netHeadYaw * Mth.DEG_TO_RAD;
        head.xRot = headPitch * Mth.DEG_TO_RAD;
        float swing = Mth.cos(limbSwing * 0.5f) * 1.2f * limbSwingAmount;
        rightLeg.xRot = swing;
        leftLeg.xRot = -swing;
        float attack = entity.getAttackAnim(ageInTicks - entity.tickCount);
        rightArm.xRot = -swing * 0.6f - attack * 2.2f;
        leftArm.xRot = swing * 0.6f - attack * 2.2f;
    }
}
