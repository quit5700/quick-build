package cn.quit5700.buildingwand.aux;
import cn.quit5700.buildingwand.logic.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import java.util.*;

/** Shared material layout for preview, file export and generation. */
public final class AuxMaterials {
 public static LinkedHashMap<BlockPos,BlockState> unit(AuxRow row,int x,int y,int z){
  var result=new LinkedHashMap<BlockPos,BlockState>();var tag=row.material(x,y,z);if(tag==null){if(row.contains(x,y,z))throw new IllegalArgumentException("Pure color has no material");return result;}
  var id=Identifier.parse(tag.getStringOr("id",""));if(!BuiltInRegistries.BLOCK.containsKey(id))throw new IllegalArgumentException("Missing material");var state=NbtUtils.readBlockState(BuiltInRegistries.BLOCK,tag);if(state.getBlock() instanceof ChestBlock&&state.hasProperty(ChestBlock.TYPE))state=state.setValue(ChestBlock.TYPE,ChestType.SINGLE);
  boolean override=!row.data.getStringOr("Material","").isEmpty();
  if(override&&cn.quit5700.buildingwand.server.StructurePlacement.multiple(state.getBlock())){
   var box=DeleteBox.between(0,0,0,row.w-1,row.h-1,row.d-1);boolean vertical=!state.hasProperty(BedBlock.PART);int mode=row.data.getBooleanOr("Solid",true)?1:2;
   int axis=StructureLayout.axis(box,vertical,mode);int direction=row.data.getIntOr(axis==0?"AX":axis==1?"AY":"AZ",0)==0?1:-1;
   var pair=StructureLayout.pair(box,x,y,z,axis,mode,direction);int ax=pair.x(),ay=pair.y(),az=pair.z(),bx=pair.mx(),by=pair.my(),bz=pair.mz();
   boolean includeA=row.contains(ax,ay,az),includeB=row.contains(bx,by,bz);if(!includeA&&!includeB)return result;
   if(pair.remainder()||!row.fits(ax,ay,az)||!row.fits(bx,by,bz)){if(includeA)result.put(row.world(ax,ay,az),Blocks.AIR.defaultBlockState());if(includeB)result.put(row.world(bx,by,bz),Blocks.AIR.defaultBlockState());return result;}
   BlockState first=state,second=state;
   if(state.hasProperty(BedBlock.PART)){Direction facing=Direction.getApproximateNearest(bx-ax,by-ay,bz-az);first=state.setValue(BedBlock.PART,BedPart.FOOT).setValue(BedBlock.FACING,facing);second=state.setValue(BedBlock.PART,BedPart.HEAD).setValue(BedBlock.FACING,facing);}
   else{first=state.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF,ay<by?DoubleBlockHalf.LOWER:DoubleBlockHalf.UPPER);second=state.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF,ay<by?DoubleBlockHalf.UPPER:DoubleBlockHalf.LOWER);}
   result.put(row.world(ax,ay,az),AuxTransforms.state(row,first));result.put(row.world(bx,by,bz),AuxTransforms.state(row,second));return result;
  }
  if(!row.contains(x,y,z))return result;
  result.put(row.world(x,y,z),AuxTransforms.state(row,state));
  if(!override&&cn.quit5700.buildingwand.server.StructurePlacement.multiple(state.getBlock())){
   Direction d=state.hasProperty(BedBlock.PART)?(state.getValue(BedBlock.PART)==BedPart.FOOT?state.getValue(BedBlock.FACING):state.getValue(BedBlock.FACING).getOpposite()):state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF)==DoubleBlockHalf.LOWER?Direction.UP:Direction.DOWN;
   int bx=x+d.getStepX(),by=y+d.getStepY(),bz=z+d.getStepZ();var partner=row.material(bx,by,bz);if(partner==null||!row.fits(bx,by,bz))throw new IllegalArgumentException("Incomplete multi block");var other=NbtUtils.readBlockState(BuiltInRegistries.BLOCK,partner);if(other.getBlock()!=state.getBlock())throw new IllegalArgumentException("Incomplete multi block");result.put(row.world(bx,by,bz),AuxTransforms.state(row,other));
  }
  return result;
 }
}
