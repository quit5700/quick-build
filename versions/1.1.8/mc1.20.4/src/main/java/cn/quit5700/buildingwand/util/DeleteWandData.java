package cn.quit5700.buildingwand.util;
import cn.quit5700.buildingwand.logic.DeleteWear;
import cn.quit5700.buildingwand.registry.ModItems;

import net.minecraft.world.item.ItemStack;

import net.minecraft.server.level.ServerPlayer;
import java.util.UUID;

public final class DeleteWandData {
    public static final String ID="QuickBuildDeleteWandId",REMAINDER="QuickBuildDeleteRemainder";
    public static String id(ItemStack stack){return stack.getOrCreateTag().getString(ID);}
    public static String ensureId(ItemStack stack){
        String id=id(stack);if(id.isEmpty()) {id=UUID.randomUUID().toString();String next=id;WandTagData.update(stack,t->t.putString(ID,next));}return id;
    }
    public static int remainder(ItemStack stack){return DeleteWear.remainder(stack.getOrCreateTag().getInt(REMAINDER));}
    public static long capacity(ItemStack stack){return stack.isEmpty()?0:DeleteWear.capacity(stack.getMaxDamage(),stack.getDamageValue(),remainder(stack));}
    public static boolean charge(ItemStack stack,ServerPlayer owner){
        if(owner.isCreative())return false;
        DeleteWear.Payment payment=DeleteWear.removed(remainder(stack));
        WandTagData.update(stack,t->t.putInt(REMAINDER,payment.remainder()));
        if(payment.damage()>0)stack.hurtAndBreak(payment.damage(),owner,p->p.broadcastBreakEvent(net.minecraft.world.InteractionHand.MAIN_HAND));
        owner.inventoryMenu.broadcastChanges();return stack.isEmpty();
    }
    public static ItemStack find(ServerPlayer player,String id){
        for(int n=0;n<player.getInventory().getContainerSize();n++){
            ItemStack stack=player.getInventory().getItem(n);if(stack.is(ModItems.DELETE_WAND)&&id.equals(id(stack)))return stack;
        }
        ItemStack off=player.getOffhandItem();return off.is(ModItems.DELETE_WAND)&&id.equals(id(off))?off:ItemStack.EMPTY;
    }
}
