package cn.quit5700.buildingwand.aux;
import java.util.ArrayDeque;

/** Retains a press even when its release arrives before the next client tick. */
public final class AuxClickInput {
 public enum Click { LEFT, RIGHT }
 private final ArrayDeque<Click> pending=new ArrayDeque<>();
 public void press(boolean inWorld,boolean attack,boolean use){
  if(!inWorld)return;
  if(attack)pending.addLast(Click.LEFT);else if(use)pending.addLast(Click.RIGHT);
 }
 public Click poll(){return pending.pollFirst();}
 public void clear(){pending.clear();}
}
