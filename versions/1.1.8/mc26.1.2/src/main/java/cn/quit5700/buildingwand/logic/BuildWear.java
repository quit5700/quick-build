package cn.quit5700.buildingwand.logic;
/** Counts changed, non-air blocks; a complete structure is one atomic payment. */
public final class BuildWear {
 public static final int MAX_DAMAGE=512,BLOCKS_PER_DAMAGE=100;
 public record Payment(int remainder,int damage){}
 public static int remainder(int value){return Math.floorMod(value,BLOCKS_PER_DAMAGE);}
 public static Payment placed(int remainder,int blocks){
  if(blocks<0)throw new IllegalArgumentException("Negative placed count");
  long total=(long)remainder(remainder)+blocks;
  return new Payment((int)(total%BLOCKS_PER_DAMAGE),(int)(total/BLOCKS_PER_DAMAGE));
 }
}
