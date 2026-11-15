package cn.quit5700.buildingwand.blueprint;

import net.minecraft.SharedConstants;
import net.minecraft.nbt.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class BlueprintReaderTest {
    @TempDir Path temp;
    @BeforeAll static void version(){SharedConstants.tryDetectVersion();}
    private static CompoundTag state(String name){var s=new CompoundTag();s.putString("id",name);return s;}
    @Test void legacyUppercaseStatesConvertTo263NativeFieldsWithoutLosingProperties() {
        var legacy=new CompoundTag();legacy.putString("Name","minecraft:oak_log");
        var properties=new CompoundTag();properties.putString("axis","x");legacy.put("Properties",properties);
        var normalized=BlueprintNbt.state(legacy);
        assertEquals("minecraft:oak_log",normalized.getStringOr("id",""));
        assertEquals("x",normalized.getCompoundOrEmpty("properties").getStringOr("axis",""));
        assertFalse(normalized.contains("Name"));assertTrue(legacy.contains("Name"));
        legacy.putString("id","minecraft:stone");
        assertThrows(IllegalArgumentException.class,()->BlueprintNbt.state(legacy));
    }
    @Test void nbtRoundTripRetainsStatesAndEditingButStripsInventoriesAndEntities() throws Exception {
        var chest=state("minecraft:chest");var props=new CompoundTag();props.putString("facing","west");chest.put("properties",props);
        var editing=new CompoundTag();editing.putBoolean("Visible",false);editing.putInt("Color",14);
        var root=BlueprintNbt.structure(1,1,1,List.of(new BlueprintNbt.Cell(0,0,0,chest)),editing);
        ((CompoundTag)root.getListOrEmpty("blocks").get(0)).put("nbt",new CompoundTag());
        var entities=new ListTag();entities.add(new CompoundTag());root.put("entities",entities);
        Path file=BlueprintFiles.save(temp,"\u7bb1\u5b50\u671d\u5411",BlueprintNbt.compress(root),false);
        var loaded=BlueprintReader.read(file);
        assertEquals(chest,loaded.getListOrEmpty("palette").get(0));
        assertFalse(((CompoundTag)loaded.getListOrEmpty("blocks").get(0)).contains("nbt"));
        assertTrue(loaded.getListOrEmpty("entities").isEmpty());assertEquals(14,loaded.getCompoundOrEmpty("Q5Aux").getIntOr("Color",0));
    }
    @Test void missingModIdentifiersAreKeptForAnExplicitMissingMaterialsError() {
        var root=BlueprintNbt.structure(1,1,1,List.of(new BlueprintNbt.Cell(0,0,0,state("missing_mod:machine"))),null);
        assertEquals("missing_mod:machine",((CompoundTag)BlueprintReader.vanilla(root).getListOrEmpty("palette").get(0)).getStringOr("id",""));
    }
    @Test void realCastleFilesMatchAll70870NonAirBlocksAcrossBothFormats() throws Exception {
        Map<String,CompoundTag> expected=null;
        for(String extension:List.of("nbt","schem")) {
            Path file=temp.resolve("\u57ce\u5821."+extension);
            try(var input=getClass().getResourceAsStream("/blueprints/q5_grand_castle."+extension)) {
                assertNotNull(input);Files.copy(input,file);
            }
            var root=BlueprintReader.read(file);
            assertEquals(BlueprintNbt.vector(96,45,80),root.getListOrEmpty("size"));
            Map<String,CompoundTag> states=new HashMap<>();var palette=root.getListOrEmpty("palette");
            for(Tag raw:root.getListOrEmpty("blocks")) {
                var cell=(CompoundTag)raw;var state=(CompoundTag)palette.get(cell.getInt("state").orElseThrow());
                if(!state.getStringOr("id","").equals("minecraft:air"))states.put(cell.getListOrEmpty("pos").toString(),state);
            }
            assertEquals(70870,states.size());
            if(expected==null)expected=states;else assertEquals(expected,states);
        }
    }
    @Test void malformedPaletteAndCoordinatesAreRejected() {
        var root=BlueprintNbt.structure(1,1,1,List.of(new BlueprintNbt.Cell(0,0,0,state("minecraft:stone"))),null);
        ((CompoundTag)root.getListOrEmpty("blocks").get(0)).putInt("state",9);
        assertThrows(IllegalArgumentException.class,()->BlueprintReader.vanilla(root));
        assertThrows(IllegalArgumentException.class,()->BlueprintNbt.structure(1,1,1,List.of(new BlueprintNbt.Cell(1,0,0,state("minecraft:stone"))),null));
    }
    @Test void spongeV2AndV3DecodeVarintsAndAxisOrder() {
        for(int version:new int[]{2,3}) {
            var root=new CompoundTag();root.putInt("Version",version);root.putShort("Width",(short)2);root.putShort("Height",(short)2);root.putShort("Length",(short)2);
            var data=new CompoundTag();var palette=new CompoundTag();palette.putInt("minecraft:air",0);palette.putInt("minecraft:oak_log[axis=x]",130);data.put("Palette",palette);
            data.putByteArray(version==3?"Data":"BlockData",new byte[]{0,0,0,0,0,0,0,(byte)0x82,1});
            if(version==3)root.put("Blocks",data);else{root.put("Palette",palette);root.putByteArray("BlockData",data.getByteArray("BlockData").orElseThrow());}
            var out=BlueprintReader.schematic(root);var cell=(CompoundTag)out.getListOrEmpty("blocks").get(7);
            assertEquals(BlueprintNbt.vector(1,1,1),cell.getListOrEmpty("pos"));
            assertEquals("x",((CompoundTag)out.getListOrEmpty("palette").get(1)).getCompoundOrEmpty("properties").getStringOr("axis",""));
        }
    }
    @Test void litematicContinuousBitPackingCrossesWordBoundaries() {
        int bits=5,count=70;long[] data=new long[(count*bits+63)/64];
        for(int i=0;i<count;i++){long bit=(long)i*bits;int word=(int)(bit>>>6),offset=(int)(bit&63);long v=i%19;data[word]|=v<<offset;if(offset+bits>64)data[word+1]|=v>>>(64-offset);}
        for(int i=0;i<count;i++)assertEquals(i%19,BlueprintReader.unpack(data,i,bits));
    }
    @Test void litematicNegativeSizesAndMultipleRegionsAreNormalized() {
        var root=new CompoundTag();root.putInt("Version",6);var regions=new CompoundTag();
        regions.put("first",region(5,-2,2));regions.put("second",region(8,1,1));root.put("Regions",regions);
        var out=BlueprintReader.litematic(root);
        assertEquals(BlueprintNbt.vector(5,1,1),out.getListOrEmpty("size"));
        assertEquals(3,out.getListOrEmpty("blocks").size());
        assertEquals(BlueprintNbt.vector(0,0,0),((CompoundTag)out.getListOrEmpty("blocks").get(0)).getListOrEmpty("pos"));
        assertEquals(BlueprintNbt.vector(4,0,0),((CompoundTag)out.getListOrEmpty("blocks").get(2)).getListOrEmpty("pos"));
    }
    private static CompoundTag region(int x,int sx,int count) {
        var r=new CompoundTag();var p=new CompoundTag();p.putInt("x",x);p.putInt("y",0);p.putInt("z",0);r.put("Position",p);
        var size=new CompoundTag();size.putInt("x",sx);size.putInt("y",1);size.putInt("z",1);r.put("Size",size);
        var palette=new ListTag();palette.add(state("minecraft:stone"));r.put("BlockStatePalette",palette);r.putLongArray("BlockStates",new long[]{0});return r;
    }
}
