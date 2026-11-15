package cn.quit5700.buildingwand.util;
import cn.quit5700.buildingwand.logic.BuildWear;

import net.minecraft.world.item.ItemStack;

import net.minecraft.server.level.ServerPlayer;
public final class BuildWandWear {
 public static final String REMAINDER="Q5BuildRemainder";
 public static int remainder(ItemStack s){return BuildWear.remainder(s.getOrCreateTag().getInt(REMAINDER));}
 public static boolean charge(ItemStack s,ServerPlayer p,int changed){
  if(p.isCreative()||p.getAbilities().instabuild||changed==0)return false;
  var payment=BuildWear.placed(remainder(s),changed);
  WandTagData.update(s,t->t.putInt(REMAINDER,payment.remainder()));
  if(payment.damage()>0)s.hurtAndBreak(payment.damage(),p,owner->owner.broadcastBreakEvent(net.minecraft.world.InteractionHand.MAIN_HAND));
  p.inventoryMenu.broadcastChanges();return s.isEmpty();
 }
}
