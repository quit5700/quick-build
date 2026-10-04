package cn.quit5700.buildingwand.logic;

/** Server-authoritative staged confirmation. Old revisions cannot confirm a new selection. */
public final class DeleteSelection {
    public record Point(int x,int y,int z) {}
    private Point first,second;
    private long revision;
    public DeleteSelection(){}
    public DeleteSelection(long initialRevision){revision=initialRevision;}
    public int stage(){return first==null?0:second==null?1:2;}
    public Point first(){return first;} public Point second(){return second;}
    public long revision(){return revision;}
    public boolean pick(Point point,long expected) {
        if(expected!=revision || stage()==2)return false;
        if(first==null)first=point;else second=point;
        revision++;return true;
    }
    public DeleteBox confirm(long expected) {
        if(expected!=revision || stage()!=2)return null;
        DeleteBox box=DeleteBox.between(first.x,first.y,first.z,second.x,second.y,second.z);
        cancel();return box;
    }
    public int cancel(){int previous=stage();first=null;second=null;revision++;return previous;}
}
