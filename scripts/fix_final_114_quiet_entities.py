from pathlib import Path
import shutil
r=Path.cwd();root=r/'versions/1.1.4'
for v in ['1.21.4','1.21.1','1.20.4','1.20.1']:
 f=root/('mc'+v)/'src/main/java/cn/quit5700/light/mixin/QuietPlacementMixin.java';s=f.read_text(encoding='utf-8').replace('if(!cn.quit5700.buildingwand.server.QuietPlacement.active())state.onRemove(level,pos,next,moved);','if(!cn.quit5700.buildingwand.server.QuietPlacement.active())state.onRemove(level,pos,next,moved);else if(state.hasBlockEntity()&&!state.is(next.getBlock()))level.removeBlockEntity(pos);');f.write_text(s,encoding='utf-8')
for v in ['1.20.4','1.20.1']:
 d=root/('mc'+v)
 for folder in ['src/test','src/smoke']:
  for f in (d/folder).rglob('*.java'):
   s=f.read_text(encoding='utf-8').replace('.getOrThrow()', '.getOrThrow(false,e->{throw new IllegalStateException(e);})').replace('.getFirst()', '.get(0)');f.write_text(s,encoding='utf-8')
for d in root.glob('mc*'):
 f=d/'src/smoke/java/cn/quit5700/buildingwand/server/WandSmokeChecks.java';s=f.read_text(encoding='utf-8').replace('"spare contents lost or duplicated"','"spare contents lost or duplicated: diamonds="+items(first,Items.DIAMOND)+", gold="+items(first,Items.GOLD_INGOT)');f.write_text(s,encoding='utf-8')
f=root/'mc26.2/run/mods';f.mkdir(exist_ok=True);shutil.copy2(r/'versions/1.1.4-test11/mc26.2/run/mods/fabric-api-0.154.0+26.2.jar',f/'fabric-api-0.154.0+26.2.jar')
f=root/'mc26.1.2/run/mods';f.mkdir(exist_ok=True);shutil.copy2(Path('G:/game/mcfabric/.minecraft/versions/26.1.2-Fabric/mods/fabric-api-0.155.2+26.1.2.jar'),f/'fabric-api-0.155.2+26.1.2.jar')
f=r/'docs/bugs/BUG-20261004-FORMAL-114-PORTING.md'
with f.open('a',encoding='utf-8') as stream:stream.write('\n独立1.21.4世界回归发现：静默跳过旧版onRemove时，方块实体清理原本依赖该回调，导致替换后残留旧实体。仅静默写入、方块种类变化且原方块有实体时显式removeBlockEntity；不触发内容掉落，移动内容仍由快照恢复。严格回归普通石头不受保护、多格警告不拦截和余位内容精确掉落未修改预期。26.2测试环境缺完整FabricAPI元数据，复制既有测试环境的同版本完整API，仅用于独立测试。Java17测试代码按旧DataResult.getOrThrow(false,error)与List.get(0)等价适配，不跳过或削弱断言。\n')
print('修复旧版静默删除遗留方块实体；保留失败断言并加实际掉落计数诊断。')
