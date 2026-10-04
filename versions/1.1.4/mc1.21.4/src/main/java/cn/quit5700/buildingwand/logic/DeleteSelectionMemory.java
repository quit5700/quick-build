package cn.quit5700.buildingwand.logic;

/** Item visibility and selection lifetime are intentionally independent. */
public final class DeleteSelectionMemory {
    public int stage;public long revision,sequence;
    public DeleteSelection.Point first=new DeleteSelection.Point(0,0,0),second=first;
    public boolean pending,checking,insufficient;
    private String dimension="";private boolean active;
    public void dimension(String next){
        if(!dimension.equals(next)){dimension=next;stage=0;revision=0;pending=false;checking=false;insufficient=false;first=second=new DeleteSelection.Point(0,0,0);active=false;}
    }
    public boolean activate(String next){dimension(next);boolean query=!active;active=true;return query;}
    public void pause(){active=false;}
    public boolean visible(){return active;}
    public boolean flashing(long tick){return stage>0&&tick%40<20;}
    public long request(){pending=true;return ++sequence;}
    public void apply(int stage,DeleteSelection.Point first,DeleteSelection.Point second,long revision,long sequence,boolean checking,boolean insufficient){
        if(sequence!=this.sequence)return;
        this.stage=stage;this.first=first;this.second=second;this.revision=revision;this.checking=checking;this.insufficient=insufficient;pending=false;
    }
    public void cancelLocally(){stage=0;checking=false;insufficient=false;first=second=new DeleteSelection.Point(0,0,0);}
    public void disconnect(){dimension="";active=false;sequence=0;revision=0;pending=false;cancelLocally();}
}
