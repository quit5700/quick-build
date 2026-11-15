package cn.quit5700.buildingwand.aux;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import java.util.HashMap;
import java.util.Map;

/** Map retained mesh cells and material facing to the current preview pose. */
public final class AuxRenderPose {
    private final AuxRow source;
    private final AuxRow target;
    private final int[] matrix = new int[9];
    private final Map<BlockState, BlockState> states = new HashMap<>();
    private AuxRow rotation;
    private final boolean translationOnly;
    private final long dx,dy,dz;

    public AuxRenderPose(AuxRow source, AuxRow target) {
        this.source = source;
        this.target = target;
        int[] from = LegacyNbt.intArray(source.data, "Matrix").orElse(new int[]{1,0,0,0,1,0,0,0,1});
        int[] to = LegacyNbt.intArray(target.data, "Matrix").orElse(new int[]{1,0,0,0,1,0,0,0,1});
        for (int row = 0; row < 3; row++)
            for (int column = 0; column < 3; column++)
                for (int axis = 0; axis < 3; axis++)
                    matrix[row*3+column] += to[row*3+axis] * from[column*3+axis];
        translationOnly=java.util.Arrays.equals(matrix,new int[]{1,0,0,0,1,0,0,0,1});
        var fromOrigin=source.world(0,0,0);var toOrigin=target.world(0,0,0);
        dx=(long)toOrigin.getX()-fromOrigin.getX();dy=(long)toOrigin.getY()-fromOrigin.getY();dz=(long)toOrigin.getZ()-fromOrigin.getZ();
    }

    public BlockPos position(BlockPos position) {
        if(translationOnly)return new BlockPos(Math.toIntExact(position.getX()+dx),Math.toIntExact(position.getY()+dy),Math.toIntExact(position.getZ()+dz));
        int[] local = source.local(position);
        return target.world(local[0], local[1], local[2]);
    }

    public boolean translationOnly(){return translationOnly;}

    public Direction direction(Direction direction) {
        if(translationOnly)return direction;
        int x = direction.getStepX(), y = direction.getStepY(), z = direction.getStepZ();
        return Direction.getNearest(matrix[0]*x+matrix[1]*y+matrix[2]*z,
                matrix[3]*x+matrix[4]*y+matrix[5]*z, matrix[6]*x+matrix[7]*y+matrix[8]*z);
    }

    public BlockState state(BlockState state) {
        if(translationOnly)return state;
        return states.computeIfAbsent(state, original -> {
            if (rotation == null) {
                var tag = AuxRow.blank();
                tag.putIntArray("Matrix", matrix);
                rotation = new AuxRow(tag);
            }
            return AuxTransforms.state(rotation, original);
        });
    }
}