from pathlib import Path
import re,json
r=Path.cwd();root=r/'versions/1.1.4'
for v in ['1.21.8','1.21.4','1.21.1','1.20.4','1.20.1']:
 d=root/('mc'+v)
 for f in (d/'src/client/java/cn/quit5700/buildingwand').rglob('*.java'):
  s=f.read_text(encoding='utf-8').replace('WorldRenderEvents.BEFORE_TRANSLUCENT','WorldRenderEvents.AFTER_TRANSLUCENT').replace('.setScreenAndShow(','.setScreen(');f.write_text(s,encoding='utf-8')
 f=d/'src/main/java/cn/quit5700/buildingwand/server/DeleteChunkLease.java';s=f.read_text(encoding='utf-8')
 if v=='1.21.8':s=s.replace('new TicketType(40L,0)', 'new TicketType(40L,false,TicketType.TicketUse.LOADING)')
 else:
  s=re.sub(r'private static final TicketType TYPE=[^;]+;', 'private static final TicketType<ChunkPos> TYPE=TicketType.create("q5_delete_wand_chunk",java.util.Comparator.comparingLong(ChunkPos::toLong),40);',s)
  s=s.replace('addTicketWithRadius(TYPE,pos,0)','addRegionTicket(TYPE,pos,2,pos)').replace('removeTicketWithRadius(TYPE,pos,0)','removeRegionTicket(TYPE,pos,2,pos)')
  if v.startswith('1.20.'):s=s.replace('net.minecraft.world.level.chunk.status.ChunkStatus','net.minecraft.world.level.chunk.ChunkStatus')
 f.write_text(s,encoding='utf-8')
 if v=='1.21.8':continue
 for name in ['StructurePlacement','WandActions']:
  f=d/f'src/main/java/cn/quit5700/buildingwand/server/{name}.java';s=f.read_text(encoding='utf-8').replace('Block.UPDATE_SKIP_ALL_SIDEEFFECTS','QuietPlacement.FLAG').replace('ServerLevel level = player.level();','ServerLevel level = (ServerLevel)player.level();').replace('BuildTaskState.get(player.level())','BuildTaskState.get((ServerLevel)player.level())')
  s=re.sub(r'\b(level|w)\.setBlock\(',r'QuietPlacement.setBlock(\1,',s);f.write_text(s,encoding='utf-8')
 f=d/'src/main/java/cn/quit5700/buildingwand/server/QuietPlacement.java';f.write_text('''package cn.quit5700.buildingwand.server;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
/** Preserve the accepted complete-structure assembly before placement callbacks. */
public final class QuietPlacement {
 public static final int FLAG=1024;
 private static final ThreadLocal<Boolean> ACTIVE=ThreadLocal.withInitial(()->false);
 public static boolean active(){return ACTIVE.get();}
 public static boolean setBlock(Level level,BlockPos pos,BlockState state,int flags){boolean previous=ACTIVE.get();ACTIVE.set(previous||(flags&FLAG)!=0);try{return level.setBlock(pos,state,flags&~FLAG);}finally{ACTIVE.set(previous);}}
}
''',encoding='utf-8')
 f=d/'src/main/java/cn/quit5700/light/mixin/QuietPlacementMixin.java';f.write_text('''package cn.quit5700.light.mixin;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
@Mixin(LevelChunk.class)
public abstract class QuietPlacementMixin {
 @Redirect(method="setBlockState",at=@At(value="INVOKE",target="Lnet/minecraft/world/level/block/state/BlockState;onRemove(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Z)V"))
 private void q5$remove(BlockState state,Level level,BlockPos pos,BlockState next,boolean moved){if(!cn.quit5700.buildingwand.server.QuietPlacement.active())state.onRemove(level,pos,next,moved);}
 @Redirect(method="setBlockState",at=@At(value="INVOKE",target="Lnet/minecraft/world/level/block/state/BlockState;onPlace(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Z)V"))
 private void q5$place(BlockState state,Level level,BlockPos pos,BlockState previous,boolean moved){if(!cn.quit5700.buildingwand.server.QuietPlacement.active())state.onPlace(level,pos,previous,moved);}
}
''',encoding='utf-8')
 f=d/'src/main/resources/quick_build.mixins.json';data=json.loads(f.read_text(encoding='utf-8'));data.setdefault('mixins',[]).append('QuietPlacementMixin');f.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
f=r/'docs/bugs/BUG-20261004-FORMAL-114-PORTING.md'
with f.open('a',encoding='utf-8') as stream:stream.write('\n旧版静默装配：1.21.4及以下没有UPDATE_SKIP_ALL_SIDEEFFECTS。已核对实际游戏LevelChunk字节码，普通setBlock仍调用onPlace/onRemove，不能简单删除标志或假定移动活塞标志等价。新增仅法杖静默写入期间的线程局部守卫及LevelChunk两处回调拦截，保留正常游戏回调；写齐后仍走原确认/邻居更新逻辑。需独立世界验证。旧版区块票据按目标TicketType.create/addRegionTicket适配；1.21.8为TicketUse.LOADING。\n')
print('票据、旧版静默结构装配和界面事件接口适配完成。')
