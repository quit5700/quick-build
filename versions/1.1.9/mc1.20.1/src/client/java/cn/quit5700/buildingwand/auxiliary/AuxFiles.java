package cn.quit5700.buildingwand.aux;
import cn.quit5700.buildingwand.blueprint.*;
import net.minecraft.nbt.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.Minecraft;
import java.util.*;
public final class AuxFiles {
 public static CompoundTag export(List<AuxRow> rows){
  rows=rows.stream().filter(row->cn.quit5700.buildingwand.aux.LegacyNbt.booleanOr(row.data,"Placed",false)).toList();
  if(rows.isEmpty())throw new IllegalArgumentException("No selected rows");int minX=Integer.MAX_VALUE,minY=minX,minZ=minX,maxX=Integer.MIN_VALUE,maxY=maxX,maxZ=maxX;
  for(var row:rows)for(int x:new int[]{0,row.w-1})for(int y:row.planar()&&!row.data.contains("Blueprint")?new int[]{row.plane}:new int[]{0,row.h-1})for(int z:new int[]{0,row.d-1}){var p=row.world(x,y,z);minX=Math.min(minX,p.getX());minY=Math.min(minY,p.getY());minZ=Math.min(minZ,p.getZ());maxX=Math.max(maxX,p.getX());maxY=Math.max(maxY,p.getY());maxZ=Math.max(maxZ,p.getZ());}
  var saved=new ListTag();var cells=new LinkedHashMap<BlockPos,CompoundTag>();
  for(var row:rows){var copy=new AuxRow(row.data.copy());copy.translate(-minX,-minY,-minZ);copy.data.remove("Dim");saved.add(copy.data);if(cn.quit5700.buildingwand.aux.LegacyNbt.stringOr(copy.data,"Material","").isEmpty()&&!copy.data.contains("Blueprint"))continue;
   for(long index=0;index<copy.volume();index++){int[] p=copy.coordinates(index);if(!copy.contains(p[0],p[1],p[2]))continue;var material=copy.material(p[0],p[1],p[2]);if(material==null)continue;var id=new ResourceLocation(cn.quit5700.buildingwand.aux.LegacyNbt.stringOr(material,"id",""));try{for(var entry:AuxMaterials.unit(copy,p[0],p[1],p[2]).entrySet())cells.putIfAbsent(entry.getKey(),NbtUtils.writeBlockState(entry.getValue()));}catch(IllegalArgumentException invalid){cells.putIfAbsent(copy.world(p[0],p[1],p[2]),material);}}
  }
  var list=new ArrayList<BlueprintNbt.Cell>();for(var entry:cells.entrySet())list.add(new BlueprintNbt.Cell(entry.getKey().getX(),entry.getKey().getY(),entry.getKey().getZ(),entry.getValue()));var editing=new CompoundTag();editing.put("Rows",saved);return BlueprintNbt.structure(maxX-minX+1,maxY-minY+1,maxZ-minZ+1,list,editing);
 }
 public static Set<String> missing(CompoundTag root){var result=new TreeSet<String>();for(var tag:cn.quit5700.buildingwand.aux.LegacyNbt.listOrEmpty(root,"palette")){var state=(CompoundTag)tag;String id=cn.quit5700.buildingwand.aux.LegacyNbt.stringOr(state,"id",cn.quit5700.buildingwand.aux.LegacyNbt.stringOr(state,"Name",""));var key=ResourceLocation.tryParse(id);if(key==null||!BuiltInRegistries.BLOCK.containsKey(key))result.add(id);}for(var raw:cn.quit5700.buildingwand.aux.LegacyNbt.listOrEmpty(cn.quit5700.buildingwand.aux.LegacyNbt.compoundOrEmpty(root,"Q5Aux"),"Rows")){var row=(CompoundTag)raw;String id=cn.quit5700.buildingwand.aux.LegacyNbt.stringOr(row,"Material","");if(!id.isEmpty()){var key=ResourceLocation.tryParse(id);if(key==null||!BuiltInRegistries.BLOCK.containsKey(key))result.add(id);}cn.quit5700.buildingwand.aux.LegacyNbt.compound(row,"Blueprint").ifPresent(blueprint->result.addAll(missing(blueprint)));}return result;}
 public static List<AuxRow> preview(CompoundTag source,BlockPos at){var result=new ArrayList<AuxRow>();var rows=cn.quit5700.buildingwand.aux.LegacyNbt.listOrEmpty(cn.quit5700.buildingwand.aux.LegacyNbt.compoundOrEmpty(source,"Q5Aux"),"Rows");if(rows.isEmpty()){var row=AuxRow.blank();var size=cn.quit5700.buildingwand.aux.LegacyNbt.listOrEmpty(source,"size");row.putInt("W",cn.quit5700.buildingwand.aux.LegacyNbt.numberInt(((IntTag)size.get(0))));row.putInt("H",cn.quit5700.buildingwand.aux.LegacyNbt.numberInt(((IntTag)size.get(1))));row.putInt("D",cn.quit5700.buildingwand.aux.LegacyNbt.numberInt(((IntTag)size.get(2))));row.put("Blueprint",source);row.putBoolean("Placed",true);rows=new ListTag();rows.add(row);}for(var raw:rows){var r=new AuxRow(((CompoundTag)raw).copy());r.translate(at.getX(),at.getY(),at.getZ());r.data.putString("Dim",Minecraft.getInstance().level.dimension().location().toString());result.add(r);}return result;}
}
