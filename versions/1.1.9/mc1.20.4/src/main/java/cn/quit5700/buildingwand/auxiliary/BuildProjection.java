package cn.quit5700.buildingwand.aux;
import cn.quit5700.buildingwand.logic.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
import java.util.*;
/** Owner-only task projection; never inserted into editable blueprint rows or exports. */
public final class BuildProjection {
 private BuildProjection(){}
 public static List<AuxRow> renderingOrder(List<AuxRow> rows){var ordered=new ArrayList<AuxRow>();for(var row:rows)if(cn.quit5700.buildingwand.aux.LegacyNbt.booleanOr(row.data,"BuildPreview",false))ordered.add(row);for(var row:rows)if(!(cn.quit5700.buildingwand.aux.LegacyNbt.booleanOr(row.data,"BuildPreview",false)))ordered.add(row);return ordered;}
 public static List<AuxRow> rows(CompoundTag state,String dimension){
  var result=new ArrayList<AuxRow>();for(var raw:cn.quit5700.buildingwand.aux.LegacyNbt.listOrEmpty(state,"Jobs")){
   var job=(CompoundTag)raw;if(!cn.quit5700.buildingwand.aux.LegacyNbt.stringOr(job,"Kind","").equals("Build")||!cn.quit5700.buildingwand.aux.LegacyNbt.stringOr(job,"Dim","").equals(dimension))continue;
   var b=cn.quit5700.buildingwand.aux.LegacyNbt.intArray(job,"PreviewBounds").orElse(new int[0]);if(b.length!=6)continue;
   try{var n=AuxRow.create(new BlockPos(b[0],b[1],b[2]),new BlockPos(b[3],b[4],b[5]));n.putString("Id","build-preview:"+cn.quit5700.buildingwand.aux.LegacyNbt.stringOr(job,"Id",""));n.putString("Dim",dimension);n.putInt("Color",3);n.putBoolean("BuildPreview",true);n.putInt("PreviewMode",cn.quit5700.buildingwand.aux.LegacyNbt.intOr(job,"PreviewMode",1));n.putIntArray("PreviewCursor",cn.quit5700.buildingwand.aux.LegacyNbt.intArray(job,"PreviewCursor").orElse(new int[]{b[0],b[1],b[2]}));n.putIntArray("PreviewDirection",cn.quit5700.buildingwand.aux.LegacyNbt.intArray(job,"PreviewDirection").orElse(new int[]{1,1,1}));result.add(new AuxRow(n));}catch(IllegalArgumentException|ArithmeticException invalid){/* A corrupt preview cannot affect the saved task. */}
  }return result;
 }
 public static boolean visible(AuxRow row,BlockPos point){if(row==null)return false;var local=row.local(point);return row.contains(local[0],local[1],local[2]);}
 public static boolean pending(CompoundTag n,int x,int y,int z,int w,int h,int d){
  if(cn.quit5700.buildingwand.aux.LegacyNbt.intOr(n,"PreviewMode",1)==2&&x>0&&y>0&&z>0&&x<w-1&&y<h-1&&z<d-1)return false;
  int[] c=cn.quit5700.buildingwand.aux.LegacyNbt.intArray(n,"PreviewCursor").orElse(new int[0]),dir=cn.quit5700.buildingwand.aux.LegacyNbt.intArray(n,"PreviewDirection").orElse(new int[0]);if(c.length!=3||dir.length!=3)return true;
  for(int step:dir)if(Math.abs(step)!=1)return true;
  int wx=Math.addExact(cn.quit5700.buildingwand.aux.LegacyNbt.intOr(n,"X",0),x),wy=Math.addExact(cn.quit5700.buildingwand.aux.LegacyNbt.intOr(n,"Y",0),y),wz=Math.addExact(cn.quit5700.buildingwand.aux.LegacyNbt.intOr(n,"Z",0),z);
  int[] lhs={wz>>4,wx>>4,wx,wz,wy},rhs={c[2]>>4,c[0]>>4,c[0],c[2],c[1]},sign={dir[2],dir[0],dir[0],dir[2],dir[1]};
  for(int i=0;i<lhs.length;i++){int order=Integer.compare(lhs[i],rhs[i])*sign[i];if(order!=0)return order>0;}return true;
 }
}
