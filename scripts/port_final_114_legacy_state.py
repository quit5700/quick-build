from pathlib import Path
import re
r=Path.cwd();root=r/'versions/1.1.4'
for v in ['1.21.8','1.21.4','1.21.1','1.20.4','1.20.1']:
 d=root/('mc'+v)
 for name in ['BuildBoxRenderer','DeleteBoxRenderer']:
  f=d/f'src/client/java/cn/quit5700/buildingwand/client/{name}.java';s=f.read_text(encoding='utf-8').replace('client.rendering.v1.world.WorldRenderEvents','client.rendering.v1.WorldRenderEvents').replace('net.minecraft.client.renderer.rendertype.RenderTypes','net.minecraft.client.renderer.RenderType').replace('RenderTypes.','RenderType.').replace('context.poseStack()', 'context.matrixStack()')
  s=s.replace('context.submitNodeCollector().submitCustomGeometry(context.matrixStack(),RenderType.debugQuads(),(pose,buffer)->{','{\n            var buffer=context.consumers().getBuffer(RenderType.debugQuads());').replace('Matrix4f matrix=pose.pose();','Matrix4f matrix=context.matrixStack().last().pose();').replace('        });\n    });}', '        }\n    });}')
  if v not in ['1.21.8']:s=s.replace(')).position()', ')).getPosition()')
  if v in ['1.20.4','1.20.1']:
   s=re.sub(r'b.addVertex\((m,[^)]+)\).setColor\(([^)]+)\);',r'b.vertex(\1).color(\2).endVertex();',s)
  f.write_text(s,encoding='utf-8')
 if v=='1.21.8':continue
 for name,key in [('BuildTaskState','build_tasks'),('DeleteTaskState','delete_wand_tasks')]:
  f=d/f'src/main/java/cn/quit5700/buildingwand/server/{name}.java';s=f.read_text(encoding='utf-8').replace('import net.minecraft.world.level.saveddata.SavedDataType;','').replace('import net.minecraft.world.level.saveddata.*;', 'import net.minecraft.world.level.saveddata.SavedData;')
  s=s.replace('public final class '+name,'import net.minecraft.nbt.CompoundTag;\nimport net.minecraft.nbt.NbtOps;\nimport net.minecraft.core.HolderLookup;\nimport net.minecraft.util.datafix.DataFixTypes;\npublic final class '+name)
  decl=f'private static final SavedDataType<{name}> TYPE=';start=s.index(decl);end=s.index(';',start)+1
  supplier=f'()->new {name}()';decoder=f'CODEC.parse(NbtOps.INSTANCE,tag).result().orElseGet({supplier})'
  if v=='1.20.1':code=f'private static final String SAVE_ID="building_wand_{key}";';s=s.replace('computeIfAbsent(TYPE)',f'computeIfAbsent(tag->{decoder},{supplier},SAVE_ID)')
  else:
   lam='(tag,registries)->' if v in ['1.21.4','1.21.1'] else 'tag->'
   code=f'private static final String SAVE_ID="building_wand_{key}";\n private static final SavedData.Factory<{name}> TYPE=new SavedData.Factory<>({supplier},{lam}{decoder},DataFixTypes.LEVEL);'
   s=s.replace('computeIfAbsent(TYPE)','computeIfAbsent(TYPE,SAVE_ID)')
  s=s[:start]+code+s[end:]
  param=',HolderLookup.Provider registries' if v in ['1.21.4','1.21.1'] else ''
  s=s.replace(' private final List<Job>',f' @Override public CompoundTag save(CompoundTag tag{param}){{CODEC.encodeStart(NbtOps.INSTANCE,this).result().filter(CompoundTag.class::isInstance).map(CompoundTag.class::cast).ifPresent(tag::merge);return tag;}}\n private final List<Job>').replace('    private final List<Job>',f'    @Override public CompoundTag save(CompoundTag tag{param}){{CODEC.encodeStart(NbtOps.INSTANCE,this).result().filter(CompoundTag.class::isInstance).map(CompoundTag.class::cast).ifPresent(tag::merge);return tag;}}\n    private final List<Job>')
  f.write_text(s,encoding='utf-8')
print('旧版选区框与任务存档适配完成，待构建验证。')
