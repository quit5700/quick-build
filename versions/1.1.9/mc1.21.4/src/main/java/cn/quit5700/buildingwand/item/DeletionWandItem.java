package cn.quit5700.buildingwand.item;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import net.minecraft.world.item.context.UseOnContext;
import java.util.List;

/** Interaction is handled by the staged server confirmation protocol. */
public final class DeletionWandItem extends Item {
    public DeletionWandItem(Properties properties){super(properties);}
    @Override public InteractionResult useOn(UseOnContext context){return InteractionResult.FAIL;}
    @Override public void appendHoverText(ItemStack stack,TooltipContext context,List<Component> tooltip,TooltipFlag flag){
        tooltip.add(Component.translatable("item.building_wand.delete_wand.tooltip").withStyle(ChatFormatting.RED));
    }
}
