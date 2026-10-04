from pathlib import Path
import shutil
r=Path.cwd();v=r/'versions/1.1.4-test7/mc26.2';out=r/'outputs/20261004_移动红石修复26.2测试版7';out.mkdir(parents=True,exist_ok=True);shutil.copy2(v/'run/logs/latest.log',out/'修复前_独立世界.log')
p=v/'src/main/java/cn/quit5700/buildingwand/server/WandActions.java';s=p.read_text(encoding='utf-8').replace('import net.minecraft.world.level.block.state.BlockState;','import net.minecraft.world.level.block.state.BlockState;\nimport net.minecraft.world.level.block.state.properties.BlockStateProperties;')
a=s.index('    private static void clearPositions(');b=s.index('    private static BlockPos rotateAroundCenter(',a);part=s[a:b].replace('Block.UPDATE_CLIENTS','movePlacementFlags()');s=s[:a]+part+s[b:]
s=s.replace('''    private static void verifySnapshot(ServerLevel level, Map<BlockPos, MoveBlock> blocks) {
        for (Map.Entry<BlockPos, MoveBlock> entry : blocks.entrySet()) {
            if (!level.getBlockState(entry.getKey()).equals(entry.getValue().state)) {
                throw moveBlockFailure(level, entry.getKey(), entry.getValue().state, "verify_target", null);
            }
        }
    }''','''    private static int movePlacementFlags() {
        return Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_SKIP_ALL_SIDEEFFECTS;
    }

    private static boolean moveStatesMatch(BlockState expected, BlockState actual) {
        if (expected.equals(actual)) {
            return true;
        }
        return expected.is(Blocks.REDSTONE_WIRE) && actual.is(Blocks.REDSTONE_WIRE)
                && expected.setValue(BlockStateProperties.POWER,
                        actual.getValue(BlockStateProperties.POWER)).equals(actual);
    }

    private static void verifySnapshot(ServerLevel level, Map<BlockPos, MoveBlock> blocks) {
        for (Map.Entry<BlockPos, MoveBlock> entry : blocks.entrySet()) {
            BlockState actual = level.getBlockState(entry.getKey());
            if (!moveStatesMatch(entry.getValue().state, actual)) {
                throw moveBlockFailure(level, entry.getKey(), entry.getValue().state, "verify_target", null);
            }
            if (actual.is(Blocks.REDSTONE_WIRE) && !actual.canSurvive(level, entry.getKey())) {
                throw moveBlockFailure(level, entry.getKey(), entry.getValue().state, "verify_wire_support", null);
            }
        }
    }''')
assert 'private static int movePlacementFlags()' in s and part.count('movePlacementFlags()')==3
p.write_text(s,encoding='utf-8')
print('仅调整搬移标志与红石校验，保留旧操作和诊断')