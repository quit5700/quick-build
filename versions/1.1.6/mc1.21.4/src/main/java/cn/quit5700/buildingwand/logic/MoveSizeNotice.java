package cn.quit5700.buildingwand.logic;
import java.math.BigInteger;
public final class MoveSizeNotice {
 public static final BigInteger WARNING=BigInteger.valueOf(32768);
 public static boolean warn(DeleteBox b){return b.volume().compareTo(WARNING)>0;}
}
