package cn.quit5700.buildingwand.logic;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.math.BigInteger;
import java.util.List;
import java.util.UUID;

/** Persisted job payload, independent of game registries and world state. */
public record DeleteTaskData(String owner,List<Integer> bounds,List<Integer> cursor,String visited,String removed,List<Integer> directions,String wand) {
    public static final Codec<DeleteTaskData> CODEC=RecordCodecBuilder.create(i->i.group(
            Codec.STRING.fieldOf("owner").forGetter(DeleteTaskData::owner),
            Codec.INT.listOf().fieldOf("bounds").forGetter(DeleteTaskData::bounds),
            Codec.INT.listOf().fieldOf("cursor").forGetter(DeleteTaskData::cursor),
            Codec.STRING.fieldOf("visited").forGetter(DeleteTaskData::visited),
            Codec.STRING.fieldOf("removed").forGetter(DeleteTaskData::removed),
            Codec.INT.listOf().optionalFieldOf("directions",List.of(1,1,1)).forGetter(DeleteTaskData::directions),
            Codec.STRING.optionalFieldOf("wand","").forGetter(DeleteTaskData::wand)
    ).apply(i,DeleteTaskData::new));
    public DeleteTaskData(String owner,List<Integer> bounds,List<Integer> cursor,String visited,String removed){this(owner,bounds,cursor,visited,removed,List.of(1,1,1),"");}
    public DeleteTaskData {
        UUID.fromString(owner);
        bounds=List.copyOf(bounds);cursor=List.copyOf(cursor);directions=List.copyOf(directions);
        if(directions.size()!=3||directions.stream().anyMatch(v->Math.abs(v)!=1))throw new IllegalArgumentException("Invalid deletion directions");
        if(!wand.isEmpty())UUID.fromString(wand);
        if(bounds.size()!=6 || cursor.size()!=3)throw new IllegalArgumentException("Invalid deletion coordinates");
        DeleteBox box=new DeleteBox(bounds.get(0),bounds.get(1),bounds.get(2),bounds.get(3),bounds.get(4),bounds.get(5));
        if(!box.contains(cursor.get(0),cursor.get(1),cursor.get(2)))throw new IllegalArgumentException("Cursor outside deletion bounds");
        BigInteger v=new BigInteger(visited),r=new BigInteger(removed);
        if(v.signum()<0 || v.compareTo(box.volume())>=0 || r.signum()<0 || r.compareTo(v)>0)
            throw new IllegalArgumentException("Invalid deletion progress");
    }
    public DeleteBox box(){return new DeleteBox(bounds.get(0),bounds.get(1),bounds.get(2),bounds.get(3),bounds.get(4),bounds.get(5));}
    public DeleteCursor restoreCursor(){return new DeleteCursor(box(),cursor.get(0),cursor.get(1),cursor.get(2),false);}
    public DirectedDeleteCursor restoreDirectedCursor(){return new DirectedDeleteCursor(box(),directions.get(0),directions.get(1),directions.get(2),cursor.get(0),cursor.get(1),cursor.get(2));}
    public BigInteger visitedCount(){return new BigInteger(visited);}
    public BigInteger removedCount(){return new BigInteger(removed);}
}
