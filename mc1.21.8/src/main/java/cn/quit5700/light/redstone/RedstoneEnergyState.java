package cn.quit5700.light.redstone;

import cn.quit5700.light.logic.NetworkLinkRules;
import cn.quit5700.light.logic.SensorLineRules;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import cn.quit5700.persistence.SavedDataFileName;
import net.minecraft.world.level.Level;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

public final class RedstoneEnergyState extends SavedData {
    private static final String SAVE_ID = "light_redstone_networks";
    private static final Codec<RedstoneEnergyState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            EnergyNode.CODEC.listOf().optionalFieldOf("nodes", List.of()).forGetter(state -> new ArrayList<>(state.nodes.values())),
            EnergyEdge.CODEC.listOf().optionalFieldOf("edges", List.of()).forGetter(state -> new ArrayList<>(state.edges))
    ).apply(instance, RedstoneEnergyState::new));
    private static final SavedDataType<RedstoneEnergyState> TYPE = new SavedDataType<>(
            SavedDataFileName.requireSafe(SAVE_ID), RedstoneEnergyState::new, CODEC, DataFixTypes.SAVED_DATA_RANDOM_SEQUENCES);

    private final Map<String, EnergyNode> nodes = new HashMap<>();
    private final Set<EnergyEdge> edges = new HashSet<>();
    private final Map<String, Set<String>> chunkNodes = new HashMap<>();
    private final Map<String, Set<String>> adjacency = new HashMap<>();

    public RedstoneEnergyState() {
    }

    private RedstoneEnergyState(List<EnergyNode> nodes, List<EnergyEdge> edges) {
        nodes.forEach(node -> this.nodes.put(node.key(), node));
        nodes.forEach(this::indexNode);
        this.edges.addAll(edges);
        this.edges.removeIf(edge -> !this.nodes.containsKey(edge.first()) || !this.nodes.containsKey(edge.second()));
        this.edges.forEach(this::indexEdge);
    }

    public static RedstoneEnergyState get(ServerLevel world) {
        return world.getServer().overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    public static String worldId(Level world) {
        return world.dimension().location().toString();
    }

    public EnergyNode register(ServerLevel world, BlockPos pos, EnergyNodeType type, EnergyColor color, boolean powered) {
        String key = EnergyNode.key(worldId(world), pos);
        EnergyNode existing = nodes.get(key);
        EnergyNode node = new EnergyNode(worldId(world), pos.immutable(), type, color,
                existing == null ? powered : existing.powered());
        nodes.put(key, node);
        indexNode(node);
        setDirty();
        return node;
    }

    public void remove(ServerLevel world, BlockPos pos) {
        String key = EnergyNode.key(worldId(world), pos);
        EnergyNode removed = nodes.remove(key);
        if (removed != null) {
            Set<String> indexed = chunkNodes.get(chunkKey(removed.world(), removed.pos()));
            if (indexed != null) indexed.remove(key);
            for (EnergyEdge edge : new ArrayList<>(edges)) {
                if (edge.contains(key)) {
                    edges.remove(edge);
                    unindexEdge(edge);
                }
            }
            adjacency.remove(key);
            setDirty();
            RedstoneEnergyNotifier.syncLoadedEmitters();
        }
    }

    private void indexEdge(EnergyEdge edge) {
        adjacency.computeIfAbsent(edge.first(), ignored -> new HashSet<>()).add(edge.second());
        adjacency.computeIfAbsent(edge.second(), ignored -> new HashSet<>()).add(edge.first());
    }

    private void unindexEdge(EnergyEdge edge) {
        Set<String> first = adjacency.get(edge.first());
        Set<String> second = adjacency.get(edge.second());
        if (first != null) first.remove(edge.second());
        if (second != null) second.remove(edge.first());
    }

    private void indexNode(EnergyNode node) {
        chunkNodes.computeIfAbsent(chunkKey(node.world(), node.pos()), ignored -> new HashSet<>()).add(node.key());
    }

    private static String chunkKey(String world, BlockPos pos) {
        return world + "|" + (pos.getX() >> 4) + "," + (pos.getZ() >> 4);
    }

    public void syncChunk(ServerLevel world, ChunkPos chunkPos) {
        String key = worldId(world) + "|" + chunkPos.x + "," + chunkPos.z;
        Set<String> members = chunkNodes.get(key);
        if (members != null && !members.isEmpty()) {
            RedstoneEnergyNotifier.syncLoadedNetworkNodes(world, this, new HashSet<>(members));
        }
    }

    public Optional<EnergyNode> getNode(ServerLevel world, BlockPos pos) {
        return Optional.ofNullable(nodes.get(EnergyNode.key(worldId(world), pos)));
    }

    EnergyNode node(String key) {
        return nodes.get(key);
    }

    public void setPowered(ServerLevel world, BlockPos pos, boolean powered) {
        getNode(world, pos).ifPresent(node -> {
            if (node.powered() != powered) {
                nodes.put(node.key(), node.withPowered(powered));
                setDirty();
                RedstoneEnergyNotifier.syncLoadedEmitters();
            }
        });
    }

    public void setSwitchNetworkPowered(ServerLevel world, BlockPos pos, boolean powered) {
        String start = EnergyNode.key(worldId(world), pos);
        boolean changed = false;
        for (String key : component(start)) {
            EnergyNode node = nodes.get(key);
            if (node != null && node.type() == EnergyNodeType.SWITCH && node.powered() != powered) {
                nodes.put(key, node.withPowered(powered));
                changed = true;
            }
        }
        if (changed) {
            setDirty();
            RedstoneEnergyNotifier.syncLoadedNetworkNodes(world, this, component(start));
        }
    }

    public LinkResult toggleLink(ServerLevel world, EnergyNode first, EnergyNode second) {
        if (!first.world().equals(second.world())) return LinkResult.DIFFERENT_DIMENSION;
        if (first.key().equals(second.key())) return LinkResult.SAME_NODE;
        EnergyEdge edge = new EnergyEdge(first.key(), second.key());
        Set<String> previouslyConnected = component(first.key());
        if (edges.remove(edge)) {
            unindexEdge(edge);
            setDirty();
            RedstoneEnergyNotifier.syncLoadedNetworkNodes(world, this, previouslyConnected);
            return LinkResult.REMOVED;
        }
        if (first.type() == EnergyNodeType.SENSOR && second.type() == EnergyNodeType.SENSOR
                && !validSensorLine(world, first.pos(), second.pos())) {
            return LinkResult.INVALID_SENSOR_LINE;
        }
        Set<String> merged = component(first.key());
        merged.addAll(component(second.key()));
        Set<EnergyColor> colors = new HashSet<>();
        for (String key : merged) {
            EnergyNode node = nodes.get(key);
            if (node != null && node.type() == EnergyNodeType.SWITCH && node.color() != EnergyColor.NONE) colors.add(node.color());
        }
        boolean touchesSensor = first.type() == EnergyNodeType.SENSOR || second.type() == EnergyNodeType.SENSOR;
        Set<String> colorNames = colors.stream().map(EnergyColor::getSerializedName).collect(java.util.stream.Collectors.toSet());
        if (!NetworkLinkRules.colorsCompatible(colorNames, touchesSensor)) return LinkResult.COLOR_CONFLICT;
        edges.add(edge);
        indexEdge(edge);
        boolean switchPowered = merged.stream().map(nodes::get).filter(Objects::nonNull)
                .anyMatch(node -> node.type() == EnergyNodeType.SWITCH && node.powered());
        if (switchPowered) {
            for (String key : merged) {
                EnergyNode node = nodes.get(key);
                if (node != null && node.type() == EnergyNodeType.SWITCH && !node.powered()) {
                    nodes.put(key, node.withPowered(true));
                }
            }
        }
        setDirty();
        RedstoneEnergyNotifier.syncLoadedNetworkNodes(world, this, merged);
        return LinkResult.ADDED;
    }

    public Set<BlockPos> linkedSensors(ServerLevel world, BlockPos pos) {
        String key = EnergyNode.key(worldId(world), pos);
        Set<BlockPos> result = new HashSet<>();
        for (EnergyEdge edge : edges) {
            if (!edge.contains(key)) continue;
            EnergyNode other = nodes.get(edge.other(key));
            if (other != null && other.type() == EnergyNodeType.SENSOR && other.world().equals(worldId(world))) {
                result.add(other.pos());
            }
        }
        return result;
    }

    public static boolean validSensorLine(ServerLevel world, BlockPos first, BlockPos second) {
        int dx = second.getX() - first.getX();
        int dy = second.getY() - first.getY();
        int dz = second.getZ() - first.getZ();
        Direction direction = dx != 0 ? (dx > 0 ? Direction.EAST : Direction.WEST)
                : dy != 0 ? (dy > 0 ? Direction.UP : Direction.DOWN)
                : (dz > 0 ? Direction.SOUTH : Direction.NORTH);
        return SensorLineRules.validatePath(dx, dy, dz, step -> {
            BlockPos cursor = first.relative(direction, step);
            return world.isLoaded(cursor) && isSensorLineClear(world.getBlockState(cursor));
        }) == SensorLineRules.Result.VALID;
    }

    public static boolean isSensorLineClear(BlockState state) {
        return SensorLineRules.isClear(state.isAir(), state.is(Blocks.LIGHT));
    }

    public NetworkInfo info(ServerLevel world, BlockPos pos) {
        String key = EnergyNode.key(worldId(world), pos);
        Set<String> component = component(key);
        int switches = 0, sensors = 0, emitters = 0;
        boolean powered = false;
        Set<EnergyColor> colors = new HashSet<>();
        for (String member : component) {
            EnergyNode node = nodes.get(member);
            if (node == null) continue;
            switch (node.type()) {
                case SWITCH -> { switches++; colors.add(node.color()); powered |= node.powered(); }
                case SENSOR -> { sensors++; powered |= node.powered(); }
                case EMITTER -> emitters++;
            }
        }
        String networkId = component.stream().min(String::compareTo)
                .map(value -> Integer.toUnsignedString(value.hashCode(), 36).toUpperCase(Locale.ROOT)).orElse("-");
        colors.remove(EnergyColor.NONE);
        EnergyColor color = colors.size() == 1 ? colors.iterator().next() : EnergyColor.NONE;
        return new NetworkInfo(networkId, component.size(), switches, sensors, emitters, powered, color);
    }

    public Set<String> component(String start) {
        if (!nodes.containsKey(start)) return new HashSet<>();
        Set<String> visited = new HashSet<>();
        ArrayDeque<String> queue = new ArrayDeque<>();
        visited.add(start);
        queue.add(start);
        while (!queue.isEmpty()) {
            String current = queue.removeFirst();
            for (String other : adjacency.getOrDefault(current, Set.of())) {
                if (visited.add(other)) queue.addLast(other);
            }
        }
        return visited;
    }

    public boolean emitterPowered(ServerLevel world, BlockPos pos) {
        return info(world, pos).powered();
    }

    public enum LinkResult { ADDED, REMOVED, INVALID_PAIR, INVALID_SENSOR_LINE, COLOR_CONFLICT, DIFFERENT_DIMENSION, SAME_NODE }
    public record NetworkInfo(String networkId, int nodes, int switches, int sensors, int emitters, boolean powered, EnergyColor color) {}
}
