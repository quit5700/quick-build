package cn.quit5700.buildingwand.server;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
/** Preserve the accepted complete-structure assembly before placement callbacks. */
public final class QuietPlacement {
 public static final int FLAG=1024;
 private static final ThreadLocal<Boolean> ACTIVE=ThreadLocal.withInitial(()->false);
 public static boolean active(){return ACTIVE.get();}
 public static boolean setBlock(Level level,BlockPos pos,BlockState state,int flags){boolean previous=ACTIVE.get();ACTIVE.set(previous||(flags&FLAG)!=0);try{return level.setBlock(pos,state,flags&~FLAG);}finally{ACTIVE.set(previous);}}
}
