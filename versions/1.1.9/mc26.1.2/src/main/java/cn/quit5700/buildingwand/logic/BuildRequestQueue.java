package cn.quit5700.buildingwand.logic;
import java.util.ArrayDeque;
/** One acknowledged action at a time. Timeouts query state; they never repeat mutations. */
public final class BuildRequestQueue<T> {
 public record Dispatch<T>(long sequence,boolean query,T action) {}
 private final ArrayDeque<T> clicks=new ArrayDeque<>();
 private long sequence,waiting,sentAt;private int retries;private boolean active,queryNeeded;
 public void activate(){deactivate();active=true;queryNeeded=true;retries=0;}
 public void deactivate(){clicks.clear();waiting=0;active=false;queryNeeded=false;}
 public void reset(){deactivate();sequence=0;}
 public void enqueue(T click){if(active)clicks.addLast(click);}
 public Dispatch<T> next(long tick){
  if(!active)return null;
  if(waiting!=0){if(tick-sentAt<(20L<<Math.min(retries,4)))return null;waiting=0;queryNeeded=true;retries++;}
  boolean query=queryNeeded;
  T action=query?null:clicks.pollFirst();if(!query&&action==null)return null;
  queryNeeded=false;waiting=++sequence;sentAt=tick;
  return new Dispatch<>(waiting,query,action);
 }
 public boolean acknowledge(long received){if(!active||waiting==0||received!=waiting)return false;waiting=0;retries=0;return true;}
 public long waiting(){return waiting;}
}
