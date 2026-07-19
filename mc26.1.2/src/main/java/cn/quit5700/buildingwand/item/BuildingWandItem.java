package cn.quit5700.buildingwand.item;

import cn.quit5700.buildingwand.util.WandStackData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.function.Consumer;

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
            TooltipDisplay display,
            Consumer<Component> tooltip,
            TooltipFlag flag
    ) {
        Block selected = WandStackData.selectedBlock(stack);
        if (selected == Blocks.AIR) {
            tooltip.accept(Component.translatable("text.quick_build.173").withStyle(ChatFormatting.GRAY));
            return;
        }
        Identifier id = BuiltInRegistries.BLOCK.getKey(selected);
        tooltip.accept(Component.translatable("text.quick_build.172")
                .append(Component.translatable(selected.getDescriptionId()))
                .withStyle(ChatFormatting.AQUA));
        tooltip.accept(Component.literal("ID: " + id).withStyle(ChatFormatting.DARK_GRAY));
    }
}
