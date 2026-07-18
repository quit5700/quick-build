package cn.quit5700.buildingwand.item;

import cn.quit5700.buildingwand.util.WandStackData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.List;

public final class BuildingWandItem extends Item {
    public BuildingWandItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return context.getLevel().isClientSide() ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            List<Component> tooltip,
            TooltipFlag flag
    ) {
        Block selected = WandStackData.selectedBlock(stack);
        if (selected == Blocks.AIR) {
            tooltip.add(Component.literal("已选方块: 未选择").withStyle(ChatFormatting.GRAY));
            return;
        }
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(selected);
        tooltip.add(Component.literal("已选方块: ")
                .append(Component.translatable(selected.getDescriptionId()))
                .withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.literal("ID: " + id).withStyle(ChatFormatting.DARK_GRAY));
    }
}
