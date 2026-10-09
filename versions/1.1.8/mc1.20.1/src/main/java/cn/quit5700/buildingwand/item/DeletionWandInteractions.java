package cn.quit5700.buildingwand.item;
import cn.quit5700.buildingwand.registry.ModItems;
import net.fabricmc.fabric.api.event.player.*;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;

/** Common-side callbacks block vanilla left/right actions in the deletion wand. */
public final class DeletionWandInteractions {
    private static InteractionResult result(Player player){return (player.getMainHandItem().is(ModItems.DELETE_WAND)||player.getMainHandItem().is(ModItems.BUILDING_WAND))?InteractionResult.FAIL:InteractionResult.PASS;}
    public static void register(){
        AttackBlockCallback.EVENT.register((player,world,hand,pos,face)->result(player));
        AttackEntityCallback.EVENT.register((player,world,hand,entity,hit)->result(player));
        UseBlockCallback.EVENT.register((player,world,hand,hit)->result(player));
        UseEntityCallback.EVENT.register((player,world,hand,entity,hit)->result(player));
        UseItemCallback.EVENT.register((player,world,hand)->new net.minecraft.world.InteractionResultHolder<>(result(player),player.getItemInHand(hand)));
    }
}
