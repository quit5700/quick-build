package cn.quit5700.buildingwand.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;

import java.util.List;

public final class MoveWandItem extends Item {
    public MoveWandItem(Properties properties) {
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
        tooltip.add(Component.translatable("text.quick_build.194")
                .withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("text.quick_build.050")
                .withStyle(ChatFormatting.GRAY));
    }
}
