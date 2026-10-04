from pathlib import Path
import shutil
r=Path.cwd();base=r/'versions/1.1.4-test5/mc26.2';v=r/'versions/1.1.4-test6/mc26.2';assert not v.exists();shutil.copytree(base,v,ignore=shutil.ignore_patterns('build','.gradle','run','dist','__pycache__'))
p=v/'gradle.properties';p.write_text(p.read_text(encoding='utf-8').replace('1.1.4-test5','1.1.4-test6'),encoding='utf-8')
p=v/'src/main/java/cn/quit5700/buildingwand/server/WandActions.java';s=p.read_text(encoding='utf-8');s=s.replace('import java.util.ArrayList;','import org.slf4j.Logger;\nimport org.slf4j.LoggerFactory;\n\nimport java.util.ArrayList;');s=s.replace('public final class WandActions {','public final class WandActions {\n    private static final Logger MOVE_LOGGER = LoggerFactory.getLogger("quick_build.move");')
s=s.replace('''        try {
            clearPositions(level, sourcePositions);
            placeSnapshot(level, targetBlocks);
            verifySnapshot(level, targetBlocks);
            if (updatesRoutes) {''','''        String moveStage = "clear_source";
        try {
            clearPositions(level, sourcePositions);
            moveStage = "place_target";
            placeSnapshot(level, targetBlocks);
            moveStage = "verify_target";
            verifySnapshot(level, targetBlocks);
            if (updatesRoutes) {
                moveStage = "update_routes";''')
s=s.replace('''        } catch (RuntimeException exception) {
            restoreSnapshot(level, updatePositions, rollback);
            if (updatesRoutes) {
                WorldRouteManager.restoreSnapshot(level, routeSnapshot);
            }
            fail(player, "text.quick_build.097");''','''        } catch (RuntimeException exception) {
            MOVE_LOGGER.error("Move failed: stage={}, dimension={}, player={}, first={}, targetFirst={}, rotation={}, sourceCount={}, targetCount={}",
                    moveStage, level.dimension(), player.getUUID(), session.first, targetFirst,
                    rotation & 3, sourcePositions.size(), targetBlocks.size(), exception);
            try {
                restoreSnapshot(level, updatePositions, rollback);
                if (updatesRoutes) {
                    WorldRouteManager.restoreSnapshot(level, routeSnapshot);
                }
            } catch (RuntimeException rollbackFailure) {
                MOVE_LOGGER.error("Move rollback failed: dimension={}, player={}",
                        level.dimension(), player.getUUID(), rollbackFailure);
                throw rollbackFailure;
            }
            MOVE_LOGGER.info("Move rollback completed: dimension={}, player={}",
                    level.dimension(), player.getUUID());
            fail(player, "text.quick_build.097");''')
a=s.index('    private static void clearPositions(');b=s.index('    private static void restoreSnapshot(',a)
s=s[:a]+'''    private static IllegalStateException moveBlockFailure(
            ServerLevel level, BlockPos pos, BlockState expected, String operation,
            RuntimeException cause) {
        BlockState actual = level.getBlockState(pos);
        BlockEntity entity = level.getBlockEntity(pos);
        return new IllegalStateException("Move block failure: operation=" + operation
                + ", pos=" + pos + ", expectedBlock=" + BuiltInRegistries.BLOCK.getKey(expected.getBlock())
                + ", expectedState=" + expected + ", actualBlock=" + BuiltInRegistries.BLOCK.getKey(actual.getBlock())
                + ", actualState=" + actual + ", blockEntity="
                + (entity == null ? "none" : entity.getClass().getName()), cause);
    }

    private static void clearPositions(ServerLevel level, Set<BlockPos> positions) {
        for (BlockPos pos : positions) {
            BlockState original = level.getBlockState(pos);
            try {
                level.removeBlockEntity(pos);
                if (!level.getBlockState(pos).isAir()
                        && !level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS)) {
                    throw new IllegalStateException("Cannot clear move source");
                }
            } catch (RuntimeException exception) {
                throw moveBlockFailure(level, pos, original, "clear_source", exception);
            }
        }
    }

    private static void placeSnapshot(ServerLevel level, Map<BlockPos, MoveBlock> blocks) {
        for (Map.Entry<BlockPos, MoveBlock> entry : blocks.entrySet()) {
            try {
                level.removeBlockEntity(entry.getKey());
                if (!level.getBlockState(entry.getKey()).equals(entry.getValue().state)
                        && !level.setBlock(entry.getKey(), entry.getValue().state, Block.UPDATE_CLIENTS)) {
                    throw new IllegalStateException("Cannot place move target");
                }
            } catch (RuntimeException exception) {
                throw moveBlockFailure(level, entry.getKey(), entry.getValue().state, "place_block", exception);
            }
        }
        for (Map.Entry<BlockPos, MoveBlock> entry : blocks.entrySet()) {
            MoveBlock block = entry.getValue();
            if (block.blockEntityTag == null) {
                continue;
            }
            try {
                BlockEntity entity = BlockEntity.loadStatic(
                        entry.getKey(), block.state, block.blockEntityTag, level.registryAccess());
                if (entity == null) {
                    throw new IllegalStateException("Cannot restore block entity");
                }
                level.setBlockEntity(entity);
            } catch (RuntimeException exception) {
                throw moveBlockFailure(level, entry.getKey(), block.state, "restore_block_entity", exception);
            }
        }
    }

    private static void verifySnapshot(ServerLevel level, Map<BlockPos, MoveBlock> blocks) {
        for (Map.Entry<BlockPos, MoveBlock> entry : blocks.entrySet()) {
            if (!level.getBlockState(entry.getKey()).equals(entry.getValue().state)) {
                throw moveBlockFailure(level, entry.getKey(), entry.getValue().state, "verify_target", null);
            }
        }
    }

'''+s[b:]
assert 'MOVE_LOGGER.error("Move failed:' in s and 'moveStage = "verify_target";' in s
p.write_text(s,encoding='utf-8')
for name in ['README.md','VERSION_INFO.md']:
 p=v/name;s=p.read_text(encoding='utf-8').replace('1.1.4-test5','1.1.4-test6');p.write_text(s+'\n测试版6为移动诊断版：保留操作、数量规则、放置和恢复顺序；失败日志增加步骤、维度、玩家编号、第一点、目标点、旋转、方块数量、失败方块坐标、预期与实际状态及原异常堆栈。单独记录恢复完成或恢复失败。当前未确认移动失败根因，不宣称已修复。\n',encoding='utf-8')
print('诊断版源码准备完成')