package cn.quit5700.buildingwand.logic;

/** Each successful removal contributes exactly once, including across jobs. */
public final class DeleteWear {
    public static final int MAX_DAMAGE=512, BLOCKS_PER_DAMAGE=100;
    public record Payment(int remainder,int damage){}
    public static int remainder(int value){return Math.floorMod(value,BLOCKS_PER_DAMAGE);}
    public static long capacity(int maxDamage,int damage,int remainder){
        return Math.max(0L,(long)Math.max(0,maxDamage-damage)*BLOCKS_PER_DAMAGE-remainder(remainder));
    }
    public static Payment removed(int remainder){
        int next=remainder(remainder)+1;
        return new Payment(next%BLOCKS_PER_DAMAGE,next/BLOCKS_PER_DAMAGE);
    }
    public static boolean removable(boolean air,boolean bedrock,boolean creative){return !air && (!bedrock || creative);}
}
