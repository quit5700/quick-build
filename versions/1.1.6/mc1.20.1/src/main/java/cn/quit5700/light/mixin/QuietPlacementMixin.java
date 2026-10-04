package cn.quit5700.light.mixin;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
@Mixin(LevelChunk.class)
public abstract class QuietPlacementMixin {
 @Redirect(method="setBlockState",at=@At(value="INVOKE",target="Lnet/minecraft/world/level/block/state/BlockState;onRemove(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Z)V"))
 private void q5$remove(BlockState state,Level level,BlockPos pos,BlockState next,boolean moved){if(!cn.quit5700.buildingwand.server.QuietPlacement.active())state.onRemove(level,pos,next,moved);else if(state.hasBlockEntity()&&!state.is(next.getBlock()))level.removeBlockEntity(pos);}
 @Redirect(method="setBlockState",at=@At(value="INVOKE",target="Lnet/minecraft/world/level/block/state/BlockState;onPlace(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Z)V"))
 private void q5$place(BlockState state,Level level,BlockPos pos,BlockState previous,boolean moved){if(!cn.quit5700.buildingwand.server.QuietPlacement.active())state.onPlace(level,pos,previous,moved);}
}
