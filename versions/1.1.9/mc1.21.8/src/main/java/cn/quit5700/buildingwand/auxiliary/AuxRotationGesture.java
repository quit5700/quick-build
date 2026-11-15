package cn.quit5700.buildingwand.aux;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
/** Quantized gesture shared by input and regression checks; no block-state changes. */
public final class AuxRotationGesture {
 private AuxRotationGesture(){}
 public static int turns(int axis,double yaw,double pitch,double orbit){return (int)Math.round(orbit/(Math.PI/2))+(int)Math.round((Math.floorMod(axis,3)==1?yaw:pitch)/90);}
 public static void apply(AuxRow row,ListTag steps,BlockPos pivot){if(steps.size()>256)throw new IllegalArgumentException("Too many rotation steps");for(var raw:steps){var step=(CompoundTag)raw;row.rotate(Math.floorMod(step.getIntOr("Axis",1),3),pivot,step.getIntOr("Turns",0));}}
}
