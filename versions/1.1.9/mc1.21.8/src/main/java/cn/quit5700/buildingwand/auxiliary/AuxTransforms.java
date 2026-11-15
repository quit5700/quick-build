package cn.quit5700.buildingwand.aux;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
public final class AuxTransforms {
 private static Direction direction(int[] m,Direction d){int x=d.getStepX(),y=d.getStepY(),z=d.getStepZ();return Direction.getApproximateNearest(m[0]*x+m[1]*y+m[2]*z,m[3]*x+m[4]*y+m[5]*z,m[6]*x+m[7]*y+m[8]*z);}
 public static BlockState state(AuxRow row,BlockState state){int[] m=row.data.getIntArray("Matrix").orElse(new int[]{1,0,0,0,1,0,0,0,1});for(var property:state.getProperties())state=transform(state,property,m);return state;}
 @SuppressWarnings({"unchecked","rawtypes"}) private static BlockState transform(BlockState state,Property property,int[] m){Comparable value=state.getValue(property);Comparable next=value;
  if(value instanceof Direction d)next=direction(m,d);
  else if(value instanceof Direction.Axis axis){Direction mapped=direction(m,axis==Direction.Axis.X?Direction.EAST:axis==Direction.Axis.Y?Direction.UP:Direction.SOUTH);next=mapped.getAxis();}
  else if(value instanceof DoubleBlockHalf half){var up=direction(m,Direction.UP);if(up.getAxis()!=Direction.Axis.Y)throw new IllegalArgumentException("Unsupported sideways structure");if(up==Direction.DOWN)next=half==DoubleBlockHalf.LOWER?DoubleBlockHalf.UPPER:DoubleBlockHalf.LOWER;}
  else if(value instanceof Half half){var up=direction(m,Direction.UP);if(up.getAxis()!=Direction.Axis.Y)throw new IllegalArgumentException("Unsupported sideways half block");if(up==Direction.DOWN)next=half==Half.TOP?Half.BOTTOM:Half.TOP;}
  else if(value instanceof SlabType slab&&slab!=SlabType.DOUBLE){var up=direction(m,Direction.UP);if(up.getAxis()!=Direction.Axis.Y)throw new IllegalArgumentException("Unsupported sideways slab");if(up==Direction.DOWN)next=slab==SlabType.TOP?SlabType.BOTTOM:SlabType.TOP;}
  if(!property.getPossibleValues().contains(next))throw new IllegalArgumentException("Unsupported block facing");return state.setValue(property,next);
 }
}
