package cn.quit5700.buildingwand.util;
import cn.quit5700.buildingwand.logic.BuildWear;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.server.level.ServerPlayer;
public final class BuildWandWear {
 public static final String REMAINDER="Q5BuildRemainder";
 public static int remainder(ItemStack s){return BuildWear.remainder(s.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getIntOr(REMAINDER,0));}
 public static boolean charge(ItemStack s,ServerPlayer p,int changed){
  if(p.isCreative()||p.getAbilities().instabuild||changed==0)return false;
  var payment=BuildWear.placed(remainder(s),changed);
  CustomData.update(DataComponents.CUSTOM_DATA,s,t->t.putInt(REMAINDER,payment.remainder()));
  if(payment.damage()>0)s.hurtAndBreak(payment.damage(),(net.minecraft.server.level.ServerLevel)p.level(),p,item->{});
  p.inventoryMenu.broadcastChanges();return s.isEmpty();
 }
}
