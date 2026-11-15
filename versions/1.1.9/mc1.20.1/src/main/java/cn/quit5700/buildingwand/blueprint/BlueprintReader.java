package cn.quit5700.buildingwand.blueprint;

import net.minecraft.nbt.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

/** Converts supported formats to inventory-free vanilla structure NBT without resolving missing blocks to air. */
public final class BlueprintReader {
    private BlueprintReader() {}
    public static CompoundTag read(Path path) throws IOException {
        if (Files.isSymbolicLink(path) || !Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)) throw new IOException("Not a regular structure file");
        String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
        if (!name.endsWith(".nbt") && !name.endsWith(".schem") && !name.endsWith(".litematic")) throw new IOException("Unsupported structure format");
        CompoundTag root = cn.quit5700.buildingwand.blueprint.LegacyCompressedNbt.read(path, new NbtAccounter(512L * 1024 * 1024));
        try {
            return name.endsWith(".litematic") ? litematic(root) : name.endsWith(".schem") ? schematic(root) : vanilla(root);
        } catch (IllegalArgumentException | ArithmeticException | NoSuchElementException | IndexOutOfBoundsException ex) { throw new IOException("Invalid structure data", ex); }
    }

    public static CompoundTag vanilla(CompoundTag root) {
        ListTag size = cn.quit5700.buildingwand.aux.LegacyNbt.listOrEmpty(root,"size");
        int w = number(size,0), h = number(size,1), l = number(size,2); volume(w,h,l);
        ListTag palette = cn.quit5700.buildingwand.aux.LegacyNbt.listOrEmpty(root,"palette");
        if (palette.isEmpty()) throw new IllegalArgumentException("Missing palette");
        List<BlueprintNbt.Cell> cells = new ArrayList<>();
        Set<Position> seen = new HashSet<>();
        for (Tag t : cn.quit5700.buildingwand.aux.LegacyNbt.listOrEmpty(root,"blocks")) {
            if (!(t instanceof CompoundTag b)) throw new IllegalArgumentException("Invalid block");
            int id = cn.quit5700.buildingwand.aux.LegacyNbt.integer(b,"state").orElseThrow();
            ListTag pos = cn.quit5700.buildingwand.aux.LegacyNbt.listOrEmpty(b,"pos");
            int x=number(pos,0),y=number(pos,1),z=number(pos,2);
            if (!seen.add(new Position(x,y,z))) throw new IllegalArgumentException("Duplicate block position");
            cells.add(new BlueprintNbt.Cell(x,y,z,paletteState(palette,id)));
        }
        return version(BlueprintNbt.structure(w,h,l,cells,cn.quit5700.buildingwand.aux.LegacyNbt.compound(root,"Q5Aux").orElse(null)), cn.quit5700.buildingwand.aux.LegacyNbt.intOr(root,"DataVersion",0));
    }

    public static CompoundTag schematic(CompoundTag input) {
        CompoundTag root = cn.quit5700.buildingwand.aux.LegacyNbt.compound(input,"Schematic").orElse(input);
        int version=cn.quit5700.buildingwand.aux.LegacyNbt.intOr(root,"Version",0);
        if (version != 2 && version != 3) throw new IllegalArgumentException("Unsupported Sponge version");
        int w=cn.quit5700.buildingwand.aux.LegacyNbt.intOr(root,"Width",0),h=cn.quit5700.buildingwand.aux.LegacyNbt.intOr(root,"Height",0),l=cn.quit5700.buildingwand.aux.LegacyNbt.intOr(root,"Length",0);
        int volume=volume(w,h,l);
        CompoundTag blocks=version==3?cn.quit5700.buildingwand.aux.LegacyNbt.compoundOrEmpty(root,"Blocks"):root;
        CompoundTag palette=cn.quit5700.buildingwand.aux.LegacyNbt.compoundOrEmpty(blocks,"Palette");
        Map<Integer,CompoundTag> states=new HashMap<>();
        for(String key:cn.quit5700.buildingwand.aux.LegacyNbt.keys(palette)) {
            int id=cn.quit5700.buildingwand.aux.LegacyNbt.integer(palette,key).orElseThrow();
            if(id<0 || states.put(id,stateString(key))!=null) throw new IllegalArgumentException("Duplicate palette index");
        }
        byte[] data=cn.quit5700.buildingwand.aux.LegacyNbt.byteArray(blocks,version==3?"Data":"BlockData").orElseThrow();
        List<BlueprintNbt.Cell> cells=new ArrayList<>(); int cursor=0;
        for(int index=0;index<volume;index++) {
            int value=0,shift=0,b;
            do {
                if(cursor>=data.length || shift>=35) throw new IllegalArgumentException("Invalid varint");
                b=data[cursor++]&255;
                if(shift==28 && (b&0xF8)!=0) throw new IllegalArgumentException("Overflowing palette index");
                value|=(b&127)<<shift;shift+=7;
            } while((b&128)!=0);
            CompoundTag state=states.get(value);if(state==null)throw new IllegalArgumentException("Missing palette entry");
            cells.add(new BlueprintNbt.Cell(index%w,index/(w*l),index/w%l,state));
        }
        if(cursor!=data.length)throw new IllegalArgumentException("Trailing block data");
        return version(BlueprintNbt.structure(w,h,l,cells,null),cn.quit5700.buildingwand.aux.LegacyNbt.intOr(root,"DataVersion",0));
    }

    private record Region(int x,int y,int z,int w,int h,int l,CompoundTag data) {}
    private record Position(int x,int y,int z) {}
    public static CompoundTag litematic(CompoundTag root) {
        int version=cn.quit5700.buildingwand.aux.LegacyNbt.intOr(root,"Version",0);
        if(version<4 || version>7) throw new IllegalArgumentException("Unsupported Litematica version");
        CompoundTag raw=cn.quit5700.buildingwand.aux.LegacyNbt.compoundOrEmpty(root,"Regions");
        List<Region> regions=new ArrayList<>();
        int minX=Integer.MAX_VALUE,minY=minX,minZ=minX,maxX=Integer.MIN_VALUE,maxY=maxX,maxZ=maxX;
        for(String key:cn.quit5700.buildingwand.aux.LegacyNbt.keys(raw)) {
            CompoundTag r=cn.quit5700.buildingwand.aux.LegacyNbt.compoundOrEmpty(raw,key),p=cn.quit5700.buildingwand.aux.LegacyNbt.compoundOrEmpty(r,"Position"),s=cn.quit5700.buildingwand.aux.LegacyNbt.compoundOrEmpty(r,"Size");
            int sx=cn.quit5700.buildingwand.aux.LegacyNbt.integer(s,"x").orElseThrow(),sy=cn.quit5700.buildingwand.aux.LegacyNbt.integer(s,"y").orElseThrow(),sz=cn.quit5700.buildingwand.aux.LegacyNbt.integer(s,"z").orElseThrow();
            int w=Math.absExact(sx),h=Math.absExact(sy),l=Math.absExact(sz);volume(w,h,l);
            int x=Math.addExact(cn.quit5700.buildingwand.aux.LegacyNbt.integer(p,"x").orElseThrow(),Math.min(0,sx+1));
            int y=Math.addExact(cn.quit5700.buildingwand.aux.LegacyNbt.integer(p,"y").orElseThrow(),Math.min(0,sy+1));
            int z=Math.addExact(cn.quit5700.buildingwand.aux.LegacyNbt.integer(p,"z").orElseThrow(),Math.min(0,sz+1));
            minX=Math.min(minX,x);minY=Math.min(minY,y);minZ=Math.min(minZ,z);
            maxX=Math.max(maxX,Math.addExact(x,w));maxY=Math.max(maxY,Math.addExact(y,h));maxZ=Math.max(maxZ,Math.addExact(z,l));
            regions.add(new Region(x,y,z,w,h,l,r));
        }
        if(regions.isEmpty())throw new IllegalArgumentException("Empty regions");
        Map<Position,CompoundTag> combined=new LinkedHashMap<>();
        for(Region r:regions) {
            ListTag palette=cn.quit5700.buildingwand.aux.LegacyNbt.listOrEmpty(r.data,"BlockStatePalette");
            if(palette.isEmpty())throw new IllegalArgumentException("Empty palette");
            int bits=Math.max(2,32-Integer.numberOfLeadingZeros(palette.size()-1));
            int count=volume(r.w,r.h,r.l);
            long[] data=cn.quit5700.buildingwand.aux.LegacyNbt.longArray(r.data,"BlockStates").orElseThrow();
            if(data.length!=((long)count*bits+63)/64)throw new IllegalArgumentException("Invalid packed array length");
            for(int i=0;i<count;i++) {
                CompoundTag state=paletteState(palette,unpack(data,i,bits));
                // Empty cells of one region must not erase blocks of another region.
                String name=cn.quit5700.buildingwand.aux.LegacyNbt.stringOr(state,"id","");
                if(name.equals("minecraft:air")||name.equals("minecraft:cave_air")||name.equals("minecraft:void_air"))continue;
                Position p=new Position(Math.subtractExact(r.x,minX)+i%r.w,
                    Math.subtractExact(r.y,minY)+i/(r.w*r.l),Math.subtractExact(r.z,minZ)+i/r.w%r.l);
                CompoundTag previous=combined.putIfAbsent(p,state);
                if(previous!=null&&!previous.equals(state))throw new IllegalArgumentException("Conflicting overlapping regions");
            }
        }
        List<BlueprintNbt.Cell> cells=new ArrayList<>();
        cn.quit5700.buildingwand.aux.LegacyNbt.each(combined,(p,s)->cells.add(new BlueprintNbt.Cell(p.x,p.y,p.z,s)));
        return version(BlueprintNbt.structure(Math.subtractExact(maxX,minX),Math.subtractExact(maxY,minY),Math.subtractExact(maxZ,minZ),cells,null),cn.quit5700.buildingwand.aux.LegacyNbt.intOr(root,"MinecraftDataVersion",0));
    }

    private static CompoundTag version(CompoundTag root, int version) { root.putInt("DataVersion",version); return root; }

    public static int unpack(long[] data,int index,int bits) {
        long start=(long)index*bits;int word=(int)(start>>>6),offset=(int)(start&63);
        long value=data[word]>>>offset;
        if(offset+bits>64)value|=data[word+1]<<(64-offset);
        return (int)(value&((1L<<bits)-1));
    }
    private static int volume(int w,int h,int l) {
        if(w<1||h<1||l<1)throw new IllegalArgumentException("Invalid bounds");
        return Math.multiplyExact(Math.multiplyExact(w,h),l);
    }
    private static int number(ListTag list,int i) {
        if(list.size()!=3 || !(list.get(i) instanceof IntTag t))throw new IllegalArgumentException("Invalid vector");
        return cn.quit5700.buildingwand.aux.LegacyNbt.numberInt(t);
    }
    private static CompoundTag paletteState(ListTag palette,int id) {
        if(id<0||id>=palette.size()||!(palette.get(id)instanceof CompoundTag state))throw new IllegalArgumentException("Invalid palette index");
        return BlueprintNbt.state(state);
    }
    private static CompoundTag stateString(String value) {
        int bracket=value.indexOf('[');String id=bracket<0?value:value.substring(0,bracket);
        if(!id.matches("[a-z0-9_.-]+:[a-z0-9_./-]+"))throw new IllegalArgumentException("Invalid block identifier");
        CompoundTag state=new CompoundTag();state.putString("id",id);
        if(bracket>=0) {
            if(!value.endsWith("]"))throw new IllegalArgumentException("Invalid state");
            CompoundTag properties=new CompoundTag();
            for(String pair:value.substring(bracket+1,value.length()-1).split(",")) {
                String[] kv=pair.split("=",-1);if(kv.length!=2||kv[0].isEmpty()||kv[1].isEmpty()||properties.contains(kv[0]))throw new IllegalArgumentException("Invalid property");
                properties.putString(kv[0],kv[1]);
            }
            state.put("properties",properties);
        }
        return state;
    }
}
