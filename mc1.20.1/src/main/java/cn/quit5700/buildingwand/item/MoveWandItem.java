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
            net.minecraft.world.level.Level level,
            List<Component> tooltip,
            TooltipFlag flag
    ) {
        tooltip.add(Component.literal("左键选择并确认区域，Ctrl+右键显示投影。")
                .withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.literal("方向键微调，Alt+左右旋转，左键确认移动。")
                .withStyle(ChatFormatting.GRAY));
    }
}
