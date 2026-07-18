package cn.quit5700.light.block;

import cn.quit5700.light.redstone.EnergyNodeType;
import cn.quit5700.light.redstone.RedstoneEnergyState;
import cn.quit5700.light.redstone.EnergyColor;
import cn.quit5700.light.registry.LightBlockEntities;
import cn.quit5700.light.logic.RedstoneRelayPower;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.Direction;

import java.util.List;
import java.util.EnumMap;
import java.util.Map;

public final class RedstoneEnergySensorBlockEntity extends BlockEntity {
    private final Map<Direction, BlockPos> partners = new EnumMap<>(Direction.class);
    private boolean initialized;
    private boolean entityDetected;
    private int signalStrength;
    private boolean suppressOutput;
    public RedstoneEnergySensorBlockEntity(BlockPos pos, BlockState state) {
        super(LightBlockEntities.REDSTONE_ENERGY_SENSOR, pos, state);
    }

    public static void tick(ServerLevel world, BlockPos pos, BlockState state, RedstoneEnergySensorBlockEntity blockEntity) {
        if (world.getGameTime() % 2 != 0) return;
        if (!blockEntity.initialized) {
            RedstoneEnergyState energy = RedstoneEnergyState.get(world);
            if (energy.getNode(world, pos).isEmpty()) {
                energy.register(world, pos, EnergyNodeType.SENSOR, EnergyColor.NONE, false);
            }
            blockEntity.rebuildSegments(world);
            refreshNearby(world, pos);
        }
        if (world.getGameTime() % 10 == 0) {
            blockEntity.entityDetected = blockEntity.detectsEntity(world);
        }
        int power = blockEntity.calculatePower(world);
        boolean powered = power > 0;
        if (blockEntity.signalStrength != power || state.getValue(RedstoneEnergySensorBlock.POWERED) != powered) {
            blockEntity.signalStrength = power;
            world.setBlock(pos, state.setValue(RedstoneEnergySensorBlock.POWERED, powered), 3);
            world.updateNeighborsAt(pos, state.getBlock());
            RedstoneEnergyState energy = RedstoneEnergyState.get(world);
            if (energy.getNode(world, pos).isEmpty()) {
                energy.register(world, pos, EnergyNodeType.SENSOR, EnergyColor.NONE, powered);
            }
            energy.setPowered(world, pos, powered);
        }
    }

    public int outputSignalStrength() {
        return suppressOutput ? 0 : signalStrength;
    }

    private int calculatePower(ServerLevel world) {
        int directSignal = 0;
        int wirePower = 0;
        suppressOutput = true;
        try {
            for (Direction direction : Direction.values()) {
                BlockPos neighbourPos = worldPosition.relative(direction);
                BlockState neighbour = world.getBlockState(neighbourPos);
                if (neighbour.getBlock() instanceof RedstoneEnergySensorBlock) continue;
                if (neighbour.getBlock() instanceof RedStoneWireBlock && neighbour.hasProperty(RedStoneWireBlock.POWER)) {
                    wirePower = Math.max(wirePower, neighbour.getValue(RedStoneWireBlock.POWER));
                } else {
                    directSignal = Math.max(directSignal, world.getSignal(neighbourPos, direction));
                }
            }
        } finally {
            suppressOutput = false;
        }
        return RedstoneRelayPower.calculate(
                entityDetected, directSignal, wirePower, signalStrength);
    }

    public static void refreshNearby(ServerLevel world, BlockPos changedPos) {
        for (Direction direction : Direction.values()) {
            for (int distance = 1; distance <= 11; distance++) {
                BlockPos cursor = changedPos.relative(direction, distance);
                if (!world.isLoaded(cursor)) break;
                if (world.getBlockEntity(cursor) instanceof RedstoneEnergySensorBlockEntity sensor) {
                    sensor.rebuildSegments(world);
                    break;
                }
                if (!RedstoneEnergyState.isSensorLineClear(world.getBlockState(cursor))) break;
            }
        }
        if (world.getBlockEntity(changedPos) instanceof RedstoneEnergySensorBlockEntity sensor) sensor.rebuildSegments(world);
    }

    private void rebuildSegments(ServerLevel world) {
        partners.clear();
        for (BlockPos partner : RedstoneEnergyState.get(world).linkedSensors(world, worldPosition)) {
            Direction direction = directionTo(worldPosition, partner);
            if (direction != null && RedstoneEnergyState.validSensorLine(world, worldPosition, partner)) {
                partners.put(direction, partner.immutable());
            }
        }
        initialized = true;
        BlockState state = world.getBlockState(worldPosition);
        if (state.getBlock() instanceof RedstoneEnergySensorBlock) {
            BlockState visual = state.setValue(RedstoneEnergySensorBlock.NORTH, partners.containsKey(Direction.NORTH))
                    .setValue(RedstoneEnergySensorBlock.SOUTH, partners.containsKey(Direction.SOUTH))
                    .setValue(RedstoneEnergySensorBlock.EAST, partners.containsKey(Direction.EAST))
                    .setValue(RedstoneEnergySensorBlock.WEST, partners.containsKey(Direction.WEST))
                    .setValue(RedstoneEnergySensorBlock.UP, partners.containsKey(Direction.UP))
                    .setValue(RedstoneEnergySensorBlock.DOWN, partners.containsKey(Direction.DOWN));
            if (visual != state) world.setBlock(worldPosition, visual, 2);
        }
    }

    private static Direction directionTo(BlockPos first, BlockPos second) {
        int dx = second.getX() - first.getX();
        int dy = second.getY() - first.getY();
        int dz = second.getZ() - first.getZ();
        if (dy == 0 && dz == 0 && dx != 0) return dx > 0 ? Direction.EAST : Direction.WEST;
        if (dx == 0 && dz == 0 && dy != 0) return dy > 0 ? Direction.UP : Direction.DOWN;
        if (dx == 0 && dy == 0 && dz != 0) return dz > 0 ? Direction.SOUTH : Direction.NORTH;
        return null;
    }

    private boolean detectsEntity(ServerLevel world) {
        for (Map.Entry<Direction, BlockPos> entry : partners.entrySet()) {
            Direction direction = entry.getKey();
            BlockPos partner = entry.getValue();
            int distance = Math.abs(partner.getX() - worldPosition.getX()) + Math.abs(partner.getY() - worldPosition.getY()) + Math.abs(partner.getZ() - worldPosition.getZ());
            if (distance <= 1 || distance > 11 || !(world.getBlockState(partner).getBlock() instanceof RedstoneEnergySensorBlock)) continue;
            boolean clear = true;
            for (int step = 1; step < distance; step++) {
                if (!RedstoneEnergyState.isSensorLineClear(
                        world.getBlockState(worldPosition.relative(direction, step)))) {
                    clear = false;
                    break;
                }
            }
            if (!clear) continue;
            BlockPos innerStart = worldPosition.relative(direction);
            BlockPos innerEnd = partner.relative(direction.getOpposite());
            AABB beam = new AABB(innerStart).minmax(new AABB(innerEnd));
            List<Entity> entities = world.getEntities((Entity) null, beam, entity -> !entity.isSpectator());
            if (!entities.isEmpty()) return true;
        }
        return false;
    }
}
