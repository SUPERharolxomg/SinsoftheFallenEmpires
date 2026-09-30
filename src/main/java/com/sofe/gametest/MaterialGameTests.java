package com.sofe.gametest;

import com.sofe.SoFEMod;
import com.sofe.registry.material.Material;
import com.sofe.registry.material.MaterialForm;
import com.sofe.registry.material.MaterialRegistry;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.Tags;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;

@GameTestHolder(SoFEMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class MaterialGameTests {

    @GameTest(template = "empty")
    public static void everyMaterialFormIsRegistered(GameTestHelper helper) {
        int count = 0;
        for (Material material : Material.values()) {
            for (MaterialForm form : material.forms()) {
                ResourceLocation id = SoFEMod.id(material.id(form));
                helper.assertTrue(ForgeRegistries.ITEMS.containsKey(id), "missing item " + id);
                if (form.isBlock()) {
                    helper.assertTrue(ForgeRegistries.BLOCKS.containsKey(id), "missing block " + id);
                }
                count++;
            }
        }
        helper.assertTrue(count == 35, "expected 35 material entries, found " + count);
        helper.succeed();
    }

    /** Tags come from the data generators; this fails if src/generated was not regenerated. */
    @GameTest(template = "empty")
    public static void generatedTagsAreLoaded(GameTestHelper helper) {
        BlockState glacialOre = MaterialRegistry.block(Material.GLACIAL_IRON, MaterialForm.ORE).defaultBlockState();
        helper.assertTrue(glacialOre.is(BlockTags.MINEABLE_WITH_PICKAXE), "glacial iron ore is not mineable with a pickaxe");
        helper.assertTrue(glacialOre.is(BlockTags.NEEDS_IRON_TOOL), "glacial iron ore should need an iron pickaxe");
        helper.assertTrue(glacialOre.is(Tags.Blocks.ORES), "glacial iron ore is not in forge:ores");

        ItemStack ingot = new ItemStack(MaterialRegistry.item(Material.GLACIAL_IRON, MaterialForm.INGOT));
        helper.assertTrue(ingot.is(ItemTags.create(ResourceLocation.fromNamespaceAndPath("forge", "ingots/glacial_iron"))),
                "missing forge:ingots/glacial_iron");
        helper.assertTrue(ingot.is(Tags.Items.INGOTS), "glacial iron ingot is not in forge:ingots");

        BlockState brassOre = MaterialRegistry.block(Material.SULTHARI_BRASS, MaterialForm.ORE).defaultBlockState();
        helper.assertTrue(brassOre.is(BlockTags.NEEDS_STONE_TOOL), "brass ore should need only a stone pickaxe");
        helper.succeed();
    }
}
