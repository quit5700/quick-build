package cn.quit5700.pathfindingbeacon.route;

import cn.quit5700.pathfindingbeacon.PathfindingBeaconMod;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class RoutePersistentState extends SavedData {
    public static final String ID = "pathfinding_beacon_routes";
    public static final Codec<RoutePersistentState> CODEC = Codec.PASSTHROUGH.xmap(
            RoutePersistentState::fromDynamic,
            RoutePersistentState::toDynamic
    );
    public static final SavedData.Factory<RoutePersistentState> TYPE = new SavedData.Factory<>(
            RoutePersistentState::new,
            (tag, registries) -> fromNbt(tag),
            null
    );

    private final RouteData data;
    private final RouteService service;

    public RoutePersistentState() {
        this(new RouteData());
    }

    private RoutePersistentState(RouteData data) {
        this.data = data;
        this.service = new RouteService(data);
        if (service.removeOwnershipRestrictions()) {
            setDirty();
        }
    }

    public RouteData data() {
        return data;
    }

    public RouteService service() {
        return service;
    }

    private static RoutePersistentState fromDynamic(Dynamic<?> dynamic) {
        return fromNbt((CompoundTag) dynamic.convert(NbtOps.INSTANCE).getValue());
    }

    private Dynamic<?> toDynamic() {
        return new Dynamic<>(NbtOps.INSTANCE, writeNbt(new CompoundTag()));
    }

    public CompoundTag writeNbt(CompoundTag nbt) {
        RouteSnapshot snapshot = data.snapshot();
        ListTag owners = new ListTag();
        snapshot.owners().forEach((number, owner) -> {
            CompoundTag entry = new CompoundTag();
            entry.putInt("Number", number);
            entry.putIntArray("Owner", UUIDUtil.uuidToIntArray(owner));
            owners.add(entry);
        });
        nbt.put("Owners", owners);

        ListTag nodes = new ListTag();
        snapshot.nodes().forEach(node -> {
            CompoundTag entry = writePosition(node.position());
            entry.putInt("Number", node.number());
            entry.putIntArray("PlacedBy", UUIDUtil.uuidToIntArray(node.placedBy()));
            entry.putBoolean("Active", node.active());
            nodes.add(entry);
        });
        nbt.put("Nodes", nodes);

        ListTag orders = new ListTag();
        snapshot.orders().forEach((number, positions) -> {
            CompoundTag entry = new CompoundTag();
            entry.putInt("Number", number);
            ListTag values = new ListTag();
            positions.forEach(position -> values.add(writePosition(position)));
            entry.put("Positions", values);
            orders.add(entry);
        });
        nbt.put("Orders", orders);

        ListTag edges = new ListTag();
        snapshot.edges().forEach(edge -> {
            CompoundTag entry = new CompoundTag();
            entry.putInt("Number", edge.number());
            entry.put("First", writePosition(edge.first()));
            entry.put("Second", writePosition(edge.second()));
            entry.putLong("CreatedOrder", edge.createdOrder());
            edges.add(entry);
        });
        nbt.put("Edges", edges);
        nbt.putLong("NextEdgeOrder", snapshot.nextEdgeOrder());
        return nbt;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        return writeNbt(tag);
    }

    public static RoutePersistentState fromNbt(CompoundTag nbt) {
        Map<Integer, UUID> owners = new HashMap<>();
        ListTag ownerList = nbt.getList("Owners", Tag.TAG_COMPOUND);
        for (int i = 0; i < ownerList.size(); i++) {
            CompoundTag entry = ownerList.getCompound(i);
            owners.put(entry.getInt("Number"), readUuid(entry, "Owner"));
        }

        List<RouteNode> nodes = new ArrayList<>();
        ListTag nodeList = nbt.getList("Nodes", Tag.TAG_COMPOUND);
        for (int i = 0; i < nodeList.size(); i++) {
            CompoundTag entry = nodeList.getCompound(i);
            nodes.add(new RouteNode(
                    entry.getInt("Number"),
                    readUuid(entry, "PlacedBy"),
                    readPosition(entry),
                    entry.getBoolean("Active")
            ));
        }

        Map<Integer, List<RoutePosition>> orders = new HashMap<>();
        ListTag orderList = nbt.getList("Orders", Tag.TAG_COMPOUND);
        for (int i = 0; i < orderList.size(); i++) {
            CompoundTag entry = orderList.getCompound(i);
            ListTag values = entry.getList("Positions", Tag.TAG_COMPOUND);
            List<RoutePosition> positions = new ArrayList<>();
            for (int j = 0; j < values.size(); j++) {
                positions.add(readPosition(values.getCompound(j)));
            }
            orders.put(entry.getInt("Number"), positions);
        }

        List<RouteEdge> edges = new ArrayList<>();
        ListTag edgeList = nbt.getList("Edges", Tag.TAG_COMPOUND);
        for (int i = 0; i < edgeList.size(); i++) {
            CompoundTag entry = edgeList.getCompound(i);
            edges.add(new RouteEdge(
                    entry.getInt("Number"),
                    readPosition(entry.getCompound("First")),
                    readPosition(entry.getCompound("Second")),
                    entry.getLong("CreatedOrder")
            ));
        }

        RouteSnapshot snapshot = new RouteSnapshot(
                owners,
                nodes,
                orders,
                edges,
                Math.max(1L, nbt.getLong("NextEdgeOrder"))
        );
        return new RoutePersistentState(RouteData.fromSnapshot(snapshot));
    }

    private static CompoundTag writePosition(RoutePosition position) {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("X", position.x());
        nbt.putInt("Y", position.y());
        nbt.putInt("Z", position.z());
        return nbt;
    }

    private static RoutePosition readPosition(CompoundTag nbt) {
        return new RoutePosition(nbt.getInt("X"), nbt.getInt("Y"), nbt.getInt("Z"));
    }

    private static UUID readUuid(CompoundTag nbt, String key) {
        int[] value = nbt.getIntArray(key);
        return value.length == 4 ? UUIDUtil.uuidFromIntArray(value) : new UUID(0L, 0L);
    }
}
