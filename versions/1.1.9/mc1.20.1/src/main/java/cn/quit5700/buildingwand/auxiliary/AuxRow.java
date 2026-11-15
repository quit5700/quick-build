package cn.quit5700.buildingwand.aux;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
import java.util.*;

/** Parameterized integer voxel shape; imported non-air cells are indexed once. */
public final class AuxRow {
 public static final String[] SHAPES={"cuboid","sphere","cylinder","pyramid","rectangle","circle","prism_triangle","triangle","prism_star","star","prism_crescent","crescent","beacon_tower"};
 public static final int[] SHAPE_ORDER={0,1,2,3,12,4,5,6,7,8,9,10,11};
 public static final int[] COLORS={0xffffff,0xffa000,0xcf50df,0x55baff,0xffff33,0x80ef32,0xff75ac,0x666666,0xbbbbbb,0x00cccc,0x9933ff,0x2255ff,0x926044,0x118833,0xee2222,0x222222};
 public final CompoundTag data;
 public final int w,h,d,plane;
 private final int[] m;
 private Set<Long> removed=new HashSet<>();
 private Map<Long,CompoundTag> imported=new HashMap<>();
 private int fingerprint;private long[] importedOrder=new long[0];private int sx,sy,sz,iw,ih,idim;
 public AuxRow(CompoundTag data){
  this.data=data;w=cn.quit5700.buildingwand.aux.LegacyNbt.intOr(data,"W",1);h=cn.quit5700.buildingwand.aux.LegacyNbt.intOr(data,"H",1);d=cn.quit5700.buildingwand.aux.LegacyNbt.intOr(data,"D",1);plane=cn.quit5700.buildingwand.aux.LegacyNbt.intOr(data,"Plane",0);
  if(w<1||h<1||d<1||plane<0||plane>=h)throw new IllegalArgumentException("Invalid auxiliary bounds");
  Math.multiplyExact(Math.multiplyExact((long)w,h),d);int shape=cn.quit5700.buildingwand.aux.LegacyNbt.intOr(data,"Shape",0);if(shape<0||shape>=SHAPES.length)throw new IllegalArgumentException("Unknown shape");
  if(beacon()&&cn.quit5700.buildingwand.aux.LegacyNbt.booleanOr(data,"Placed",false)&&AuxBeacon.levels(w,h,d)==0)throw new IllegalArgumentException("Beacon bounds too small");
  m=cn.quit5700.buildingwand.aux.LegacyNbt.intArray(data,"Matrix").orElse(new int[]{1,0,0,0,1,0,0,0,1});validateMatrix(m);
  for(long k:cn.quit5700.buildingwand.aux.LegacyNbt.longArray(data,"Removed").orElse(new long[0]))removed.add(k);
  var source=cn.quit5700.buildingwand.aux.LegacyNbt.compound(data,"Blueprint");if(source.isPresent()){
   var root=source.get();var palette=cn.quit5700.buildingwand.aux.LegacyNbt.listOrEmpty(root,"palette");
   for(var raw:cn.quit5700.buildingwand.aux.LegacyNbt.listOrEmpty(root,"blocks")){var cell=(CompoundTag)raw;var p=cn.quit5700.buildingwand.aux.LegacyNbt.listOrEmpty(cell,"pos");var state=cn.quit5700.buildingwand.aux.LegacyNbt.compoundOrEmpty(palette,cn.quit5700.buildingwand.aux.LegacyNbt.integer(cell,"state").orElseThrow());
    String id=cn.quit5700.buildingwand.aux.LegacyNbt.stringOr(state,"id",cn.quit5700.buildingwand.aux.LegacyNbt.stringOr(state,"Name",""));if(id.equals("minecraft:air")||id.equals("minecraft:cave_air")||id.equals("minecraft:void_air"))continue;
    int x=cn.quit5700.buildingwand.aux.LegacyNbt.numberInt(((IntTag)p.get(0))),y=cn.quit5700.buildingwand.aux.LegacyNbt.numberInt(((IntTag)p.get(1))),z=cn.quit5700.buildingwand.aux.LegacyNbt.numberInt(((IntTag)p.get(2)));
    if(x<0||x>=w||y<0||y>=h||z<0||z>=d)throw new IllegalArgumentException("Imported cell outside bounds");
    imported.put(index(x,y,z),cn.quit5700.buildingwand.blueprint.BlueprintNbt.state(state));
   }
  }
  importedOrder=cn.quit5700.buildingwand.aux.LegacyNbt.keys(imported).stream().mapToLong(Long::longValue).sorted().toArray();iterationBounds();fingerprint=data.hashCode();
 }
 private AuxRow(AuxRow source){data=new CompoundTag();cn.quit5700.buildingwand.aux.LegacyNbt.each(source.data,(k,v)->data.put(k,v));w=source.w;h=source.h;d=source.d;plane=source.plane;m=source.m.clone();removed=source.removed;imported=source.imported;fingerprint=source.fingerprint;importedOrder=source.importedOrder;sx=source.sx;sy=source.sy;sz=source.sz;iw=source.iw;ih=source.ih;idim=source.idim;}
 public AuxRow copy(){return new AuxRow(this);}
 public int renderKey(){return fingerprint*31+31*cn.quit5700.buildingwand.aux.LegacyNbt.intOr(data,"X",0)+997*cn.quit5700.buildingwand.aux.LegacyNbt.intOr(data,"Y",0)+131*cn.quit5700.buildingwand.aux.LegacyNbt.intOr(data,"Z",0)+Arrays.hashCode(m);}
 public static CompoundTag create(BlockPos first,BlockPos second){var n=blank();n.putInt("X",Math.min(first.getX(),second.getX()));n.putInt("Y",Math.min(first.getY(),second.getY()));n.putInt("Z",Math.min(first.getZ(),second.getZ()));n.putInt("W",Math.absExact(Math.subtractExact(first.getX(),second.getX()))+1);n.putInt("H",Math.absExact(Math.subtractExact(first.getY(),second.getY()))+1);n.putInt("D",Math.absExact(Math.subtractExact(first.getZ(),second.getZ()))+1);n.putInt("Plane",first.getY()-Math.min(first.getY(),second.getY()));n.putInt("AX",first.getX()-Math.min(first.getX(),second.getX()));n.putInt("AY",first.getY()-Math.min(first.getY(),second.getY()));n.putInt("AZ",first.getZ()-Math.min(first.getZ(),second.getZ()));n.putBoolean("Down",second.getY()<first.getY());n.putBoolean("Placed",true);return n;}
 public static CompoundTag blank(){var n=new CompoundTag();n.putString("Id",UUID.randomUUID().toString());n.putInt("W",1);n.putInt("H",1);n.putInt("D",1);n.putInt("Shape",0);n.putInt("Color",0);n.putBoolean("Visible",true);n.putBoolean("Solid",true);n.putIntArray("Matrix",new int[]{1,0,0,0,1,0,0,0,1});return n;}
 public String id(){return cn.quit5700.buildingwand.aux.LegacyNbt.stringOr(data,"Id","");}
 public long volume(){return data.contains("Blueprint")?importedOrder.length:(long)iw*ih*idim;}
 public long index(int x,int y,int z){return x+(long)w*(z+(long)d*y);}
 public int[] coordinates(long i){if(data.contains("Blueprint")){long n=importedOrder[Math.toIntExact(i)];return new int[]{(int)(n%w),(int)(n/(w*(long)d)),(int)(n/w%d)};}return new int[]{sx+(int)(i%iw),sy+(int)(i/(iw*(long)idim)),sz+(int)(i/iw%idim)};}
 private void iterationBounds(){iw=w;ih=h;idim=d;int shape=cn.quit5700.buildingwand.aux.LegacyNbt.intOr(data,"Shape",0),size;if(planar()){sy=plane;ih=1;}switch(shape){case 12->{int levels=AuxBeacon.levels(w,h,d);iw=AuxBeacon.side(levels);ih=levels+1;idim=iw;sx=(w-iw)/2;sz=(d-idim)/2;}case 1-> {size=Math.min(w,Math.min(h,d));sx=(w-size)/2;sy=(h-size)/2;sz=(d-size)/2;iw=size;ih=size;idim=size;}case 2,5,7,9,11->{size=Math.min(w,d);sx=(w-size)/2;sz=(d-size)/2;iw=size;idim=size;}case 3->{size=Math.min(w,d);sx=cn.quit5700.buildingwand.aux.LegacyNbt.intOr(data,"AX",0)==0?0:w-size;sz=cn.quit5700.buildingwand.aux.LegacyNbt.intOr(data,"AZ",0)==0?0:d-size;iw=size;idim=size;}case 6,8,10->{size=Math.min(w,h);sx=(w-size)/2;sy=(h-size)/2;iw=size;ih=size;}default->{}}}
 public boolean beacon(){return cn.quit5700.buildingwand.aux.LegacyNbt.intOr(data,"Shape",0)==AuxBeacon.SHAPE&&!data.contains("Blueprint");}
 public boolean planar(){int s=cn.quit5700.buildingwand.aux.LegacyNbt.intOr(data,"Shape",0);return s==4||s==5||s==7||s==9||s==11;}
 public boolean fits(int x,int y,int z){return base(x,y,z)&&!removed.contains(index(x,y,z));}
 public boolean contains(int x,int y,int z){
  if(!base(x,y,z)||removed.contains(index(x,y,z)))return false;
  if(beacon()||cn.quit5700.buildingwand.aux.LegacyNbt.booleanOr(data,"Solid",true))return true;
  if(planar())return !base(x-1,y,z)||!base(x+1,y,z)||!base(x,y,z-1)||!base(x,y,z+1);
  return !base(x-1,y,z)||!base(x+1,y,z)||!base(x,y-1,z)||!base(x,y+1,z)||!base(x,y,z-1)||!base(x,y,z+1);
 }
 private boolean base(int x,int y,int z){
  if(x<0||y<0||z<0||x>=w||y>=h||z>=d||!cn.quit5700.buildingwand.aux.LegacyNbt.booleanOr(data,"Placed",false))return false;
  if(cn.quit5700.buildingwand.aux.LegacyNbt.booleanOr(data,"BuildPreview",false))return BuildProjection.pending(data,x,y,z,w,h,d);
  if(data.contains("Blueprint"))return imported.containsKey(index(x,y,z));
  int s=cn.quit5700.buildingwand.aux.LegacyNbt.intOr(data,"Shape",0);if(s<0||s>=SHAPES.length)throw new IllegalArgumentException("Unknown shape");
  if(beacon())return AuxBeacon.contains(w,h,d,x,y,z);
  if(planar()&&y!=plane)return false;
  double cx=(w-1)/2.0,cy=(h-1)/2.0,cz=(d-1)/2.0;
  if(s==0||s==4)return true;
  if(s==1){int size=Math.min(w,Math.min(h,d));cx=(w-size)/2+(size-1)/2.0;cy=(h-size)/2+(size-1)/2.0;cz=(d-size)/2+(size-1)/2.0;double r=size/2.0;return sq(x-cx)+sq(y-cy)+sq(z-cz)<=r*r;}
  if(s==2||s==5){int size=Math.min(w,d);cx=(w-size)/2+(size-1)/2.0;cz=(d-size)/2+(size-1)/2.0;double r=size/2.0;return sq(x-cx)+sq(z-cz)<=r*r;}
  if(s==3){int size=Math.min(w,d);cx=(cn.quit5700.buildingwand.aux.LegacyNbt.intOr(data,"AX",0)==0?0:w-size)+(size-1)/2.0;cz=(cn.quit5700.buildingwand.aux.LegacyNbt.intOr(data,"AZ",0)==0?0:d-size)+(size-1)/2.0;double yy=cn.quit5700.buildingwand.aux.LegacyNbt.booleanOr(data,"Down",false)?h-1-y:y;double radius=Math.min(w,d)/2.0*(h-yy)/(double)h;return Math.abs(x-cx)<radius&&Math.abs(z-cz)<radius;}
  int size=Math.min(w,(s==6||s==8||s==10)?h:d);cx=(w-size)/2+(size-1)/2.0;cy=(h-size)/2+(size-1)/2.0;cz=(d-size)/2+(size-1)/2.0;double a=x-cx,b=(s==6||s==8||s==10)?y-cy:z-cz;
  double radius=size/2.0;
  if(s==6||s==7)return Math.abs(a)<radius&&b>=-radius&&b<=radius&&Math.abs(a)<=((radius-b)/2.0)+.001;
  if((s==10||s==11)&&size==1)return a==0&&b==0;
  if(s==10||s==11)return AuxProfile.crescent(a,b,radius);
  return AuxProfile.star(a,s==8?b:-b,radius);
 }
 private static double sq(double x){return x*x;}
 public BlockPos world(int x,int y,int z){return new BlockPos(Math.addExact(cn.quit5700.buildingwand.aux.LegacyNbt.intOr(data,"X",0),m[0]*x+m[1]*y+m[2]*z),Math.addExact(cn.quit5700.buildingwand.aux.LegacyNbt.intOr(data,"Y",0),m[3]*x+m[4]*y+m[5]*z),Math.addExact(cn.quit5700.buildingwand.aux.LegacyNbt.intOr(data,"Z",0),m[6]*x+m[7]*y+m[8]*z));}
 public int[] local(BlockPos p){int x=p.getX()-cn.quit5700.buildingwand.aux.LegacyNbt.intOr(data,"X",0),y=p.getY()-cn.quit5700.buildingwand.aux.LegacyNbt.intOr(data,"Y",0),z=p.getZ()-cn.quit5700.buildingwand.aux.LegacyNbt.intOr(data,"Z",0);return new int[]{m[0]*x+m[3]*y+m[6]*z,m[1]*x+m[4]*y+m[7]*z,m[2]*x+m[5]*y+m[8]*z};}
 public CompoundTag material(int x,int y,int z){if(beacon()){String material=AuxBeacon.material(w,h,d,x,y,z,cn.quit5700.buildingwand.aux.LegacyNbt.stringOr(data,"Material",""));if(material==null)return null;var tag=new CompoundTag();tag.putString("id",material);return tag;}String chosen=cn.quit5700.buildingwand.aux.LegacyNbt.stringOr(data,"Material","");if(!chosen.isEmpty()){var s=new CompoundTag();s.putString("id",chosen);return s;}return imported.get(index(x,y,z));}
 public void remove(BlockPos p){int[] l=local(p);if(contains(l[0],l[1],l[2])){removed=new HashSet<>(removed);removed.add(index(l[0],l[1],l[2]));data.putLongArray("Removed",removed.stream().mapToLong(Long::longValue).toArray());}}
 public void translate(int x,int y,int z){data.putInt("X",Math.addExact(cn.quit5700.buildingwand.aux.LegacyNbt.intOr(data,"X",0),x));data.putInt("Y",Math.addExact(cn.quit5700.buildingwand.aux.LegacyNbt.intOr(data,"Y",0),y));data.putInt("Z",Math.addExact(cn.quit5700.buildingwand.aux.LegacyNbt.intOr(data,"Z",0),z));}
 public void rotate(int axis,BlockPos pivot,int turns){
  int[] r=axis==0?new int[]{1,0,0,0,0,-1,0,1,0}:axis==1?new int[]{0,0,1,0,1,0,-1,0,0}:new int[]{0,-1,0,1,0,0,0,0,1};
  for(int t=0;t<Math.floorMod(turns,4);t++){int[] a=m.clone();for(int i=0;i<3;i++)for(int j=0;j<3;j++)m[i*3+j]=r[i*3]*a[j]+r[i*3+1]*a[3+j]+r[i*3+2]*a[6+j];int x=cn.quit5700.buildingwand.aux.LegacyNbt.intOr(data,"X",0)-pivot.getX(),y=cn.quit5700.buildingwand.aux.LegacyNbt.intOr(data,"Y",0)-pivot.getY(),z=cn.quit5700.buildingwand.aux.LegacyNbt.intOr(data,"Z",0)-pivot.getZ();data.putInt("X",pivot.getX()+r[0]*x+r[1]*y+r[2]*z);data.putInt("Y",pivot.getY()+r[3]*x+r[4]*y+r[5]*z);data.putInt("Z",pivot.getZ()+r[6]*x+r[7]*y+r[8]*z);}
  data.putIntArray("Matrix",m.clone());
 }
 private static void validateMatrix(int[] m){if(m.length!=9)throw new IllegalArgumentException("Invalid matrix");for(int value:m)if(value< -1||value>1)throw new IllegalArgumentException("Invalid matrix entry");for(int i=0;i<3;i++)for(int j=0;j<3;j++){int dot=0;for(int k=0;k<3;k++)dot+=m[i*3+k]*m[j*3+k];if(dot!=(i==j?1:0))throw new IllegalArgumentException("Non-orthogonal transform");}int det=m[0]*(m[4]*m[8]-m[5]*m[7])-m[1]*(m[3]*m[8]-m[5]*m[6])+m[2]*(m[3]*m[7]-m[4]*m[6]);if(det!=1)throw new IllegalArgumentException("Reflection is not a rotation");}
}
