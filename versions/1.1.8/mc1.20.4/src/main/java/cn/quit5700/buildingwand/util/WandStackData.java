package cn.quit5700.buildingwand.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public final class WandStackData {
    private static final String SELECTED_BLOCK = "SelectedBlock";

    private WandStackData() {
    }

    public static Block selectedBlock(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return Blocks.AIR;
        }
        if (!tag.contains(SELECTED_BLOCK)) {
            return Blocks.AIR;
        }
        ResourceLocation id = ResourceLocation.tryParse(tag.getString(SELECTED_BLOCK));
        if (id == null) {
            return Blocks.AIR;
        }
        Block block = BuiltInRegistries.BLOCK.get(id);
        return block == null ? Blocks.AIR : block;
    }

    public static void setSelectedBlock(ItemStack stack, Block block) {
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
        stack.getOrCreateTag().putString(SELECTED_BLOCK, id.toString());
    }
}
