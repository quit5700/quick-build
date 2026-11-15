package cn.quit5700.buildingwand.blueprint;

import net.minecraft.SharedConstants;
import net.minecraft.nbt.*;
import java.io.*;
import java.util.*;

/** Registry independent structure data. Tile inventories and entities are deliberately excluded. */
public final class BlueprintNbt {
    public record Cell(int x, int y, int z, CompoundTag state) {}
    private BlueprintNbt() {}

    public static CompoundTag structure(int width, int height, int length, List<Cell> cells, CompoundTag editing) {
        if (width < 1 || height < 1 || length < 1) throw new IllegalArgumentException("Empty bounds");
        CompoundTag root = new CompoundTag();
        root.putInt("DataVersion", SharedConstants.getCurrentVersion().dataVersion().version());
        root.put("size", vector(width, height, length));
        ListTag palette = new ListTag(), blocks = new ListTag();
        Map<CompoundTag, Integer> ids = new LinkedHashMap<>();
        for (Cell cell : cells) {
            if (cell.x < 0 || cell.x >= width || cell.y < 0 || cell.y >= height || cell.z < 0 || cell.z >= length)
                throw new IllegalArgumentException("Position outside bounds");
            CompoundTag state = state(cell.state);
            int id = ids.computeIfAbsent(state, s -> { palette.add(s); return palette.size() - 1; });
            CompoundTag block = new CompoundTag();
            block.put("pos", vector(cell.x, cell.y, cell.z)); block.putInt("state", id); blocks.add(block);
        }
        if (palette.isEmpty()) { var air = new CompoundTag(); air.putString("id", "minecraft:air"); palette.add(air); }
        root.put("palette", palette); root.put("blocks", blocks); root.put("entities", new ListTag());
        if (editing != null) { root.put("Q5Aux", editing.copy()); root.getCompoundOrEmpty("Q5Aux").putInt("FormatVersion", 1); }
        return root;
    }

    public static CompoundTag state(CompoundTag input) {
        String id=input.getStringOr("id",input.getStringOr("Name",""));
        if(!id.matches("[a-z0-9_.-]+:[a-z0-9_./-]+"))throw new IllegalArgumentException("Invalid block identifier");
        if(input.contains("id")&&input.contains("Name")&&!id.equals(input.getStringOr("Name","")))throw new IllegalArgumentException("Conflicting block identifiers");
        CompoundTag output=new CompoundTag();output.putString("id",id);
        var properties=input.getCompound("properties").or(()->input.getCompound("Properties"));
        if(input.contains("properties")&&input.contains("Properties")&&!input.getCompoundOrEmpty("properties").equals(input.getCompoundOrEmpty("Properties")))throw new IllegalArgumentException("Conflicting properties");
        properties.ifPresent(p->output.put("properties",p.copy()));return output;
    }

    public static byte[] compress(CompoundTag root) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        NbtIo.writeCompressed(root, output); return output.toByteArray();
    }

    public static ListTag vector(int x, int y, int z) {
        ListTag result = new ListTag(); result.add(IntTag.valueOf(x)); result.add(IntTag.valueOf(y)); result.add(IntTag.valueOf(z)); return result;
    }
}
