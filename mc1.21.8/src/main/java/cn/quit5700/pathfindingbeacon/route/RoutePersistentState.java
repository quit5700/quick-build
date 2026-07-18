package cn.quit5700.pathfindingbeacon.route;

import cn.quit5700.pathfindingbeacon.PathfindingBeaconMod;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

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
    public static final SavedDataType<RoutePersistentState> TYPE = new SavedDataType<>(
            PathfindingBeaconMod.id(ID).toString(),
            RoutePersistentState::new,
            CODEC,
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

    public static RoutePersistentState fromNbt(CompoundTag nbt) {
        Map<Integer, UUID> owners = new HashMap<>();
        ListTag ownerList = nbt.getListOrEmpty("Owners");
        for (int i = 0; i < ownerList.size(); i++) {
            CompoundTag entry = ownerList.getCompoundOrEmpty(i);
            owners.put(entry.getIntOr("Number", 0), readUuid(entry, "Owner"));
        }

        List<RouteNode> nodes = new ArrayList<>();
        ListTag nodeList = nbt.getListOrEmpty("Nodes");
        for (int i = 0; i < nodeList.size(); i++) {
            CompoundTag entry = nodeList.getCompoundOrEmpty(i);
            nodes.add(new RouteNode(
                    entry.getIntOr("Number", 0),
                    readUuid(entry, "PlacedBy"),
                    readPosition(entry),
                    entry.getBooleanOr("Active", false)
            ));
        }

        Map<Integer, List<RoutePosition>> orders = new HashMap<>();
        ListTag orderList = nbt.getListOrEmpty("Orders");
        for (int i = 0; i < orderList.size(); i++) {
            CompoundTag entry = orderList.getCompoundOrEmpty(i);
            ListTag values = entry.getListOrEmpty("Positions");
            List<RoutePosition> positions = new ArrayList<>();
            for (int j = 0; j < values.size(); j++) {
                positions.add(readPosition(values.getCompoundOrEmpty(j)));
            }
            orders.put(entry.getIntOr("Number", 0), positions);
        }

        List<RouteEdge> edges = new ArrayList<>();
        ListTag edgeList = nbt.getListOrEmpty("Edges");
        for (int i = 0; i < edgeList.size(); i++) {
            CompoundTag entry = edgeList.getCompoundOrEmpty(i);
            edges.add(new RouteEdge(
                    entry.getIntOr("Number", 0),
                    readPosition(entry.getCompoundOrEmpty("First")),
                    readPosition(entry.getCompoundOrEmpty("Second")),
                    entry.getLongOr("CreatedOrder", 0L)
            ));
        }

        RouteSnapshot snapshot = new RouteSnapshot(
                owners,
                nodes,
                orders,
                edges,
                Math.max(1L, nbt.getLongOr("NextEdgeOrder", 1L))
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
        return new RoutePosition(nbt.getIntOr("X", 0), nbt.getIntOr("Y", 0), nbt.getIntOr("Z", 0));
    }

    private static UUID readUuid(CompoundTag nbt, String key) {
        return nbt.getIntArray(key)
                .map(UUIDUtil::uuidFromIntArray)
                .orElse(new UUID(0L, 0L));
    }
}
