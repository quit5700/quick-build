from pathlib import Path
import re
r=Path.cwd();root=r/'versions/1.1.4';reference=(root/'mc26.2/src/client/java/cn/quit5700/light/client/AreaSelectionClient.java').read_text(encoding='utf-8');method=reference[reference.index('    public static void cancelForDeletionWand()'):reference.index('    public static boolean rangeShown()')]
def convert_call(s,name,new,extra):
 pos=0
 while True:
  a=s.find('.'+name+'(',pos)
  if a<0:return s
  begin=a+len(name)+2;i=begin;depth=1;quote=False;escape=False
  while depth:
   c=s[i]
   if quote:
    if escape:escape=False
    elif c=='\\':escape=True
    elif c=='"':quote=False
   elif c=='"':quote=True
   elif c=='(':depth+=1
   elif c==')':depth-=1
   i+=1
  s=s[:a]+'.'+new+'('+s[begin:i-1]+extra+')'+s[i:];pos=a+len(new)+2
for d in root.glob('mc*'):
 if d.name=='mc26.2':continue
 f=d/'src/client/java/cn/quit5700/light/client/AreaSelectionClient.java';s=f.read_text(encoding='utf-8');anchor='    public static boolean rangeShown()';assert anchor in s;s=s.replace(anchor,method+anchor);f.write_text(s,encoding='utf-8')
 if d.name=='mc26.1.2':continue
 for f in list((d/'src/main/java/cn/quit5700/buildingwand').rglob('*.java'))+list((d/'src/client/java/cn/quit5700/buildingwand').rglob('*.java')):
  s=f.read_text(encoding='utf-8');s=convert_call(s,'sendOverlayMessage','displayClientMessage',',true')
  if '/src/client/' in f.as_posix():s=convert_call(s,'sendSystemMessage','displayClientMessage',',false')
  s=s.replace('graphics.item(', 'graphics.renderItem(').replace('client.rendering.v1.level.LevelRenderEvents','client.rendering.v1.world.WorldRenderEvents').replace('LevelRenderEvents.BEFORE_TRANSLUCENT_TERRAIN','WorldRenderEvents.BEFORE_TRANSLUCENT')
  if d.name=='mc1.21.11':s=s.replace('context.poseStack()', 'context.matrices()').replace('context.submitNodeCollector()', 'context.commandQueue()')
  if d.name in ['mc1.21.11','mc1.21.8']:s=s.replace('new SavedDataType<>(BuildingWandMod.id("delete_wand_tasks"),','new SavedDataType<>("building_wand_delete_wand_tasks",')
  f.write_text(s,encoding='utf-8')
f=r/'docs/bugs/BUG-20261004-FORMAL-114-PORTING.md';f.write_text('''# 正式版1.1.4跨版本交付与外观核查
当前根目录：快捷建筑模组 Git 仓库。
类型：版本接口适配、资源加载、交付。
基线：用户验收测试版11；仅调整取消按钮位置，不改变功能。
已尝试失败：直接覆盖新法杖调用但保留旧 AreaSelectionClient 会遗漏 cancelForDeletionWand；补入同等选择取消方法，保留版本专属渲染。
已尝试失败：1.21.11 照搬26.2 sendOverlayMessage、SavedDataType 标识符和 LevelRenderEvents；按该版本改为 displayClientMessage（动作栏）、字符串存档名称和 WorldRenderEvents，客户端普通消息用 displayClientMessage false。
修改文件：各版本 buildingwand 模块与 AreaSelectionClient、正式适配脚本。
验证：26.2完整构建和183自动测试通过；其他版本待完成，未安装未发布。
避免重复：按各目标实际接口适配；保留旧版光源恢复和渲染钩子，不能用新版本整个项目覆盖旧版本。
''',encoding='utf-8')
print('已补齐跨版本取消灯选点接口与旧版消息/事件/存档名称适配，失败原因已记录。')
