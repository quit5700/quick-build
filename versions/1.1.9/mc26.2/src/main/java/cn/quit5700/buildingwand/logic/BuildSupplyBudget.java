package cn.quit5700.buildingwand.logic;
import java.math.BigInteger;
/** Constant-space upper estimate; payment remains tied to successful placement. */
public final class BuildSupplyBudget {
 private BuildSupplyBudget(){}
 public static long remainingItems(BigInteger physical,BigInteger placed,boolean paired){
  if(physical.signum()<0||placed.signum()<0)throw new IllegalArgumentException("Negative build progress");
  var remaining=physical.subtract(placed).max(BigInteger.ZERO);
  if(paired)remaining=remaining.add(BigInteger.ONE).divide(BigInteger.TWO);
  return remaining.longValueExact();
 }
}
