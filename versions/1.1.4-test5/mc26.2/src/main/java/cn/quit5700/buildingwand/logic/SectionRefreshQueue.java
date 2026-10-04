package cn.quit5700.buildingwand.logic;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.List;

/** Coalesces final mesh invalidation after the corresponding block updates settle. */
public final class SectionRefreshQueue {
    public record Key(int x,int z){}
    public record Refresh(int x,int z,int minY,int maxY,long due){}
    private final LinkedHashMap<Key,Refresh> pending=new LinkedHashMap<>();
    public void offer(int x,int z,int minY,int maxY,long now){
        Key key=new Key(x,z);Refresh old=pending.get(key);
        if(old!=null){minY=Math.min(minY,old.minY);maxY=Math.max(maxY,old.maxY);}
        pending.put(key,new Refresh(x,z,minY,maxY,now+4));
    }
    public List<Refresh> drain(long now,int budget){
        List<Refresh> ready=new ArrayList<>();
        for(var i=pending.values().iterator();i.hasNext()&&ready.size()<budget;){
            Refresh r=i.next();if(now>=r.due || pending.size()>256){ready.add(r);i.remove();}
        }
        return List.copyOf(ready);
    }
    public int size(){return pending.size();}
    public void clear(){pending.clear();}
}
