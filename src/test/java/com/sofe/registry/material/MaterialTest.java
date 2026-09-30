package com.sofe.registry.material;

import com.sofe.LangFilesTest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class MaterialTest {

    @Test
    void namesFollowTheConvention() {
        assertEquals("deepslate_glacial_iron_ore", Material.GLACIAL_IRON.id(MaterialForm.DEEPSLATE_ORE));
        assertEquals("raw_solar_gold", Material.SOLAR_GOLD.id(MaterialForm.RAW));
        assertEquals("raw_orichalcum_block", Material.ORICHALCUM.id(MaterialForm.RAW_BLOCK));
        assertEquals("star_lapis", Material.STAR_LAPIS.id(MaterialForm.GEM));
        assertEquals("aetherium_shard", Material.AETHERIUM.id(MaterialForm.SHARD));
        assertEquals("sulthari_brass_block", Material.SULTHARI_BRASS.id(MaterialForm.STORAGE_BLOCK));
    }

    @Test
    void everyIdIsUniqueAndValid() {
        Set<String> seen = new HashSet<>();
        for (Material m : Material.values()) {
            for (MaterialForm f : m.forms()) {
                String id = m.id(f);
                assertTrue(id.matches("[a-z0-9_]+"), id);
                assertTrue(seen.add(id), "duplicate id " + id);
            }
        }
        assertEquals(35, seen.size());
    }

    @Test
    void oresDropTheRightForm() {
        assertEquals(MaterialForm.RAW, Material.GLACIAL_IRON.oreDrop());
        assertEquals(MaterialForm.GEM, Material.STAR_LAPIS.oreDrop());
        assertEquals(MaterialForm.SHARD, Material.AETHERIUM.oreDrop());
        assertFalse(Material.BLACK_AETHERIUM.hasOre(), "Black Aetherium is not mined");
    }

    @Test
    void forgeTagsFollowConventions() {
        assertEquals("ingots/glacial_iron", MaterialForm.INGOT.forgeTag("glacial_iron"));
        assertEquals("storage_blocks/raw_glacial_iron", MaterialForm.RAW_BLOCK.forgeTag("glacial_iron"));
        assertEquals("dusts/solar_gold", MaterialForm.POWDER.forgeTag("solar_gold"));
        assertNull(MaterialForm.SHARD.forgeTag("aetherium"));
    }

    @Test
    void askingForAMissingFormFails() {
        assertThrows(IllegalArgumentException.class, () -> Material.ORICHALCUM.id(MaterialForm.ORE));
    }

    @Test
    void everyFormIsTranslated() {
        List<String> keys = new ArrayList<>();
        for (Material m : Material.values()) {
            for (MaterialForm f : m.forms()) {
                keys.add(m.translationKey(f));
            }
        }
        keys.add("itemGroup.sofe");
        LangFilesTest.assertKeysPresent(keys);
    }
}
