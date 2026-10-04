package cn.quit5700.light.mixin;

import cn.quit5700.light.light.DaylightOcclusion;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelChunk.class)
public abstract class DaylightChunkMixin {
    @Inject(method = "setBlockState", at = @At("RETURN"))
    private void quickBuild$invalidateVisibility(BlockPos pos, BlockState updated, boolean moved,
                                                 CallbackInfoReturnable<BlockState> callback) {
        BlockState previous = callback.getReturnValue();
        if (previous == null || previous == updated) return;
        var world = ((LevelChunk) (Object) this).getLevel();
        // Air, invisible light and fluids have no collision: do not invalidate for light installation.
        var oldShape = previous.getCollisionShape(world, pos);
        var newShape = updated.getCollisionShape(world, pos);
        if ((oldShape != newShape && (!oldShape.isEmpty() || !newShape.isEmpty()))
                || previous.isSolidRender(world,pos) != updated.isSolidRender(world,pos)
                || previous.canOcclude() != updated.canOcclude()
                || (previous.canOcclude() && previous.getOcclusionShape(world,pos) != updated.getOcclusionShape(world,pos)))
            DaylightOcclusion.changed(world, pos);
    }
}
