from pathlib import Path
import json,re,shutil,zipfile,hashlib,xml.etree.ElementTree as ET
r=Path.cwd();v=r/'versions/1.1.4-test5/mc26.2';base=r/'versions/1.1.4-test4/mc26.2';out=r/'outputs/20261004_移动提示翻译26.2测试版5'
rel='src/client/java/cn/quit5700/buildingwand/client/ClientWandController.java'
old='client.player.sendSystemMessage(Component.literal(\n                        "text.quick_build.164"));';new='client.player.sendSystemMessage(Component.translatable("text.quick_build.164"));'
for p in (base/'src').rglob('*'):
 if p.is_file() and 'test' not in p.relative_to(base).parts:
  dest=v/p.relative_to(base)
  if p.relative_to(base).as_posix()==rel:assert dest.read_text(encoding='utf-8')==p.read_text(encoding='utf-8').replace(old,new)
  else:assert dest.read_bytes()==p.read_bytes(),str(p)
count=0
for p in (v/'build/test-results/test').glob('TEST-*.xml'):
 a=ET.parse(p).getroot().attrib;count+=int(a['tests']);assert int(a['failures'])==int(a['errors'])==0
assert count==170
for ns in ['building_wand','light','pathfinding_beacon']:
 data={p.stem:json.loads(p.read_text(encoding='utf-8')) for p in (v/f'src/main/resources/assets/{ns}/lang').glob('*.json')};assert len(data)==8
 for lang,strings in data.items():
  assert strings.keys()==data['en_us'].keys()
  for k,s in data['en_us'].items():assert re.findall(r'%(?:\d+\$)?[sd]',s)==re.findall(r'%(?:\d+\$)?[sd]',strings[k])
files=list((v/'dist').glob('*.jar'));assert len(files)==2
runtime=next(p for p in files if 'sources' not in p.name)
with zipfile.ZipFile(runtime) as z:assert json.loads(z.read('fabric.mod.json'))['version']=='1.1.4-test5'
result={'版本':'1.1.4-test5','自动测试':count,'失败':0,'生产源码对照':'仅移动提醒翻译调用改变，其他生产源码及资源与测试版4一致','八语言键与参数':'通过','实机验证':'待重启游戏验收'}
(out/'完整核验.json').write_text(json.dumps(result,ensure_ascii=False,indent=2),encoding='utf-8')
bug=r/'docs/bugs/BUG-20261004-MOVE-TRANSLATION-LITERAL.md'
text='''# 移动法杖提示直接显示翻译键

类型：玩家文字本地化 / 回归。

现象：选取第一点时聊天出现 text.quick_build.164。
根本原因：恢复旧移动代码时带回 Component.literal（普通文字），语言文件已存在该键，必须使用 Component.translatable（翻译查找）。
修改文件：测试版5 ClientWandController.java、LocalizationResourcesTest.java；版本属性及说明、当前项目问题索引与回归清单。
修复方式：只修改这一处翻译调用，移动、建筑、删除操作及生产资源与测试版4一致。回归检查扫描生产代码，禁止把语言资源已有的键传给普通文字调用。
验证：修复前新增检查失败，失败报告已保留；修复后完整构建及170项测试通过，八语言键和占位符一致；逐文件核对生产代码仅这一处调用改变。游戏内显示待用户重启验收。
已尝试失败：使用仅含LF的PowerShell字符串匹配CRLF源文件，未找到调用且未写入。改用UTF-8读取并标准化换行，避免重复该匹配方案。
下次避免：恢复旧代码须同时检查本地化调用；不能只确认语言键存在。文本修改保持UTF-8并兼容中文路径和换行差异。
'''
bug.write_text(text,encoding='utf-8')
for name,addition in [('BUG_INDEX.md','\n- [BUG-20261004-MOVE-TRANSLATION-LITERAL](BUG-20261004-MOVE-TRANSLATION-LITERAL.md)：测试版5修复移动提示直接显示翻译键；修复前回归失败、修复后170项测试通过。\n'),('REGRESSION_CHECKLIST.md','\n- 移动第一点提醒须显示当前语言文字；生产代码不得将语言资源已有的翻译键传入 Component.literal（普通文字）。测试版5自动检查通过，实机待验收。\n')]:
 p=r/'docs/bugs'/name;s=p.read_text(encoding='utf-8');assert addition not in s;p.write_text(s+addition,encoding='utf-8')
for name in ['README.md','VERSION_INFO.md']:
 p=v/name;s=p.read_text(encoding='utf-8').replace('1.1.4-test4','1.1.4-test5').replace('移动客户端方法一致','移动客户端操作一致，仅修正提醒的翻译调用');p.write_text(s+'\n测试版5：修复移动第一点提醒直接显示 text.quick_build.164 的问题；其余生产源码与资源保持测试版4。新增翻译键普通文字误用回归检查，修复前失败、修复后170项测试及完整构建通过。\n',encoding='utf-8')
archive=Path(r'H:\Codex交接文档库\快捷建筑模组_可调恒亮灯改版_01a0afdc\移动提示翻译_26.2测试版5_20261004');archive.mkdir(parents=True,exist_ok=True)
dist=r/'dist/1.1.4-test5/mc26.2';dist.mkdir(parents=True,exist_ok=True)
for p in files:shutil.copy2(p,dist/p.name);shutil.copy2(p,archive/p.name)
mods=Path(r'G:\game\mcfabric\.minecraft\versions\26.2\mods');active=list(mods.glob('quick-build*.jar'));assert len(active)==1 and active[0].name=='quick-build-1.1.4-test4-mc26.2-fabric.jar'
staged=mods/(runtime.name+'.staged');shutil.copy2(runtime,staged);assert staged.read_bytes()==runtime.read_bytes()
backup=archive/'quick-build-1.1.4-test4-mc26.2-fabric.安装前.jar';shutil.copy2(active[0],backup);assert backup.read_bytes()==active[0].read_bytes();active[0].unlink();staged.rename(mods/runtime.name)
assert list(mods.glob('quick-build*.jar'))==[mods/runtime.name]
for name in ['README.md','VERSION_INFO.md']:shutil.copy2(v/name,archive/name)
shutil.copy2(bug,archive/'问题与修复记录.md');shutil.copy2(out/'完整核验.json',archive/'完整核验.json');shutil.copy2(out/'修复前_翻译回归失败.xml',archive/'修复前_翻译回归失败.xml')
with zipfile.ZipFile(archive/'170项自动测试结果.zip','w',zipfile.ZIP_DEFLATED) as z:
 for p in (v/'build/test-results/test').glob('TEST-*.xml'):z.write(p,p.name)
with zipfile.ZipFile(archive/'1.1.4-test5-mc26.2-完整源码.zip','w',zipfile.ZIP_DEFLATED) as z:
 for p in v.rglob('*'):
  if p.is_file() and not any(x in {'build','.gradle','run','dist','__pycache__'} for x in p.relative_to(v).parts):z.write(p,'mc26.2/'+p.relative_to(v).as_posix())
 for p in [r/'AGENTS.md',bug,r/'scripts/deliver_translation_test5.py']:z.write(p,p.relative_to(r).as_posix())
status='''# 26.2测试版5当前交接

本次仅修复移动第一点提醒显示翻译键的问题：Component.literal（普通文字）改为Component.translatable（翻译查找）。建筑、移动和删除的操作保持测试版4，生产源码逐文件对照仅此调用不同，资源未变。

完整构建和170项自动测试通过，修复前失败报告、修复后报告、八语言校验及问题记录已保存。实际游戏显示待重启验收。

已安装 G:/game/mcfabric/.minecraft/versions/26.2/mods/quick-build-1.1.4-test5-mc26.2-fabric.jar，目录只启用此版本。旧测试版4保存在本目录。源码位于 versions/1.1.4-test5/mc26.2，运行及源码归档位于 dist/1.1.4-test5/mc26.2，本交接目录保存完整源码压缩包及两个JAR（Java归档）。

Minecraft（我的世界）26.2；Java（爪哇）25；Fabric Loader（Fabric模组加载器）0.19.3；Fabric API（Fabric应用程序接口）0.154.0+26.2；Fabric Loom（Fabric构建插件）1.15.5；Yarn Mappings（Yarn命名映射）未使用，采用Mojang Official Mappings（Mojang官方命名映射）。

未修改存档、未关机。其他版本及GitHub正式发布仍待26.2验收。本次没有重新改变移动逻辑，测试版3新增红石流体保护不能当作当前版本保证。
'''
(archive/'先读我_交接.md').write_text(status,encoding='utf-8');(archive.parent/'最新状态_建筑移动法杖26.2.md').write_text(status,encoding='utf-8')
with (archive.parent/'先读我_本对话交接.md').open('a',encoding='utf-8') as f:f.write('\n当前安装版为26.2测试版5，修复移动提醒翻译键显示。见移动提示翻译_26.2测试版5_20261004/先读我_交接.md。\n')
checks={p.name:hashlib.sha256(p.read_bytes()).hexdigest() for p in archive.iterdir() if p.suffix in {'.jar','.zip'}};(archive/'文件校验.json').write_text(json.dumps(checks,ensure_ascii=False,indent=2),encoding='utf-8');assert hashlib.sha256((mods/runtime.name).read_bytes()).hexdigest()==checks[runtime.name]
print(json.dumps({'安装文件':str(mods/runtime.name),'交接目录':str(archive),'核验':result},ensure_ascii=False))