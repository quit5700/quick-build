package cn.quit5700.common.item;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;

public final class DeliberateBlockItem extends BlockItem {
    public static final int PLACEMENT_RECOVERY_TICKS = 6;

    public DeliberateBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        if (player != null && player.getCooldowns().isOnCooldown(stack)) {
            return InteractionResult.FAIL;
        }
        InteractionResult result = super.useOn(context);
        if (player != null && result.consumesAction()) {
            player.getCooldowns().addCooldown(stack, PLACEMENT_RECOVERY_TICKS);
        }
        return result;
    }
}
