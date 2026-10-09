package cn.quit5700.light.mixin;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
/** Intercept the callback itself so other mods can retain their chunk call-site redirects. */
@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class QuietPlacementMixin {
 @Inject(method="onRemove",at=@At("HEAD"),cancellable=true)
 private void q5$remove(Level level,BlockPos pos,BlockState next,boolean moved,CallbackInfo ci){
  if(!cn.quit5700.buildingwand.server.QuietPlacement.active())return;
  BlockState state=(BlockState)(Object)this;
  if(state.hasBlockEntity()&&!state.is(next.getBlock()))level.removeBlockEntity(pos);
  ci.cancel();
 }
 @Inject(method="onPlace",at=@At("HEAD"),cancellable=true)
 private void q5$place(Level level,BlockPos pos,BlockState previous,boolean moved,CallbackInfo ci){
  if(cn.quit5700.buildingwand.server.QuietPlacement.active())ci.cancel();
 }
}
