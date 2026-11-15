package cn.quit5700.buildingwand.aux;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AuxRenderPoseTest {
    @BeforeAll static void bootstrap() {
        net.minecraft.SharedConstants.tryDetectVersion();
    }

    private AuxRow row() {
        return new AuxRow(AuxRow.create(new BlockPos(3, 4, 5), new BlockPos(7, 9, 11)));
    }

    @Test void continuousDragMapsCachedCellsToEveryIntermediatePosition() {
        var original=row();
        for(int frame=1;frame<=80;frame++) {
            var current=original.copy();
            current.translate(frame, frame/3, -frame/2);
            var pose=new AuxRenderPose(original,current);
            assertEquals(original.meshKey(),current.meshKey());
            assertNotEquals(original.renderKey(),current.renderKey());
            for(int x=0;x<original.w;x++)for(int y=0;y<original.h;y++)for(int z=0;z<original.d;z++)
                assertEquals(current.world(x,y,z),pose.position(original.world(x,y,z)));
        }
    }

    @Test void cachedRotationAndTranslationUseRelativePoseInEveryAxis() {
        var original=row();
        original.rotate(1,new BlockPos(6,7,8),1);
        for(int axis=0;axis<3;axis++)for(int turns=0;turns<4;turns++) {
            var current=original.copy();
            current.rotate(axis,new BlockPos(1,2,3),turns);
            current.translate(-12,8,19);
            var pose=new AuxRenderPose(original,current);
            assertEquals(current.world(2,3,4),pose.position(original.world(2,3,4)));
            for(var direction:Direction.values()) {
                var from=original.world(2,3,4);
                var to=pose.position(from.relative(direction));
                var center=pose.position(from);
                assertEquals(Direction.getNearest(to.getX()-center.getX(),to.getY()-center.getY(),to.getZ()-center.getZ()),pose.direction(direction));
            }
        }
    }

    @Test void cancelRestoresOriginalLocationWithoutWaitingForRemeshing() {
        var original=row();
        var dragged=original.copy();
        dragged.translate(40,10,-7);
        dragged.rotate(1,BlockPos.ZERO,3);
        var restore=new AuxRenderPose(dragged,original);
        assertEquals(original.world(1,2,3),restore.position(dragged.world(1,2,3)));
    }

    @Test void confirmedRowsKeepMeshIdentityButChangedShapesInvalidateIt() {
        var original=row();
        var current=original.copy();
        current.translate(20,2,3);
        current.rotate(2,BlockPos.ZERO,1);
        assertEquals(original.meshKey(),new AuxRow(current.data.copy()).meshKey());
        var changed=current.data.copy();
        changed.putBoolean("Solid",false);
        assertNotEquals(original.meshKey(),new AuxRow(changed).meshKey());
    }
    @Test void overlappingRowsCanMoveIndependentlyFromTheSameCachedPosition() {
        var first=row();var second=row();
        var moved=first.copy();moved.translate(20,0,0);
        var at=first.world(1,2,3);
        assertEquals(at,new AuxRenderPose(second,second).position(at));
        assertEquals(at.east(20),new AuxRenderPose(first,moved).position(at));
    }

    @Test void smallMovesReuseScanningButDistantMovesRefreshVisibleCells() {
        var original=row();var viewer=new BlockPos(44,48,57);
        var near=original.copy();near.translate(1,0,0);
        assertEquals(original.previewKey(viewer),near.previewKey(viewer));
        var distant=original.copy();distant.translate(32,0,0);
        assertNotEquals(original.previewKey(viewer),distant.previewKey(viewer));
        var rotated=original.copy();rotated.rotate(1,BlockPos.ZERO,1);
        assertNotEquals(original.previewKey(viewer),rotated.previewKey(viewer));
    }

}