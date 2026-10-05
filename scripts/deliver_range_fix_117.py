from pathlib import Path
import hashlib, json, shutil, zipfile, xml.etree.ElementTree as ET

r = Path(__file__).resolve().parents[1]
root = r/'versions/1.1.7'
d = root/'mc26.1.2'
h = Path('H:/Codex交接文档库/快捷建筑模组_可调恒亮灯改版_01a0afdc/26.1.2范围显示修复1.1.7_20261005')
h.mkdir(parents=True, exist_ok=True)
sha = lambda p: hashlib.sha256(p.read_bytes()).hexdigest()
suites = [ET.parse(p).getroot() for p in (d/'build/test-results/test').glob('TEST-*.xml')]
counts = {k:sum(int(s.attrib.get(k,0)) for s in suites) for k in ['tests','failures','errors','skipped']}
assert counts['tests']==195 and counts['failures']==counts['errors']==counts['skipped']==0, counts
assert 'BUILD SUCCESSFUL' in (root/'正式构建_mc26.1.2.log').read_text(encoding='utf-8')
runtime = d/'dist/quick-build-1.1.7-mc26.1.2-fabric.jar'
sources = d/'dist/quick-build-1.1.7-mc26.1.2-fabric-sources.jar'
with zipfile.ZipFile(runtime) as z:
    assert json.loads(z.read('fabric.mod.json'))['version']=='1.1.7'
    assert not any('SmokeChecks' in n for n in z.namelist())
    for name in ['BuildBoxRenderer','DeleteBoxRenderer']:
        data=z.read('cn/quit5700/buildingwand/client/'+name+'.class')
        assert b'COLLECT_SUBMITS' in data and b'BEFORE_TRANSLUCENT_TERRAIN' not in data
with zipfile.ZipFile(r/'versions/1.1.6/mc26.1.2/dist/quick-build-1.1.6-mc26.1.2-fabric.jar') as old, zipfile.ZipFile(runtime) as new:
    changed=[n for n in old.namelist() if n in new.namelist() and old.read(n)!=new.read(n)]
    assert set(changed)=={'fabric.mod.json','cn/quit5700/buildingwand/client/BuildBoxRenderer.class','cn/quit5700/buildingwand/client/DeleteBoxRenderer.class'}, changed
summary='26.1.2范围显示修复版1.1.7：仅建筑与删除范围框从透明地形绘制前移到收集绘制任务阶段。游戏逻辑及模型不改，其他七版仍为1.1.6。195项自动测试全部通过，打包字节码验证两处事件正确；旧/新运行包仅模组版本信息及两个范围框类有差异。真实客户端画面待玩家验收，未重新执行服务端世界回归，不把已有服务端测试作为画面证据。GitHub未同步，不关机。\n'
for name in ['README.md','VERSION_INFO.md']:
    p=d/name
    text=p.read_text(encoding='utf-8')
    text=text.replace('八版本完整构建通过，1564项自动测试、248项独立世界回归及24次灯保存/迁移/重载启动检查通过；详细结果见发布核验.json及安装核验.json。实际客户端画面仍待玩家验收。','本次26.1.2完整构建及195项自动测试通过。其他版本与上一批世界检查属于1.1.6历史结果，不能作为本次客户端范围框画面验收。')
    text=text.replace('验证结果待更新。','195项自动测试通过，实际客户端画面待玩家验收。')
    p.write_text(text+'\n'+summary, encoding='utf-8')
bug=r/'docs/bugs/BUG-20261005-2612-RANGE-SUBMIT-PHASE.md'
text=bug.read_text(encoding='utf-8').replace('1.1.7修复验证中','1.1.7完整构建及195项自动测试通过').replace('构建及完整回归结果待追加。','完整clean test build通过，195项测试无失败、错误或跳过。运行包字节码确认两个范围框绑定COLLECT_SUBMITS；新旧包逐条目比较仅版本信息和两个范围框类改变。没有运行真实玩家GPU画面，不宣称已实机验收。')
bug.write_text(text, encoding='utf-8')
index=r/'docs/bugs/BUG_INDEX.md'
index.write_text(index.read_text(encoding='utf-8').replace('1.1.7仅修正该版两个渲染事件，验证中。','1.1.7仅修正该版两个渲染事件，195项测试通过，画面待玩家验收。'),encoding='utf-8')
for folder in [r/'dist/1.1.7/mc26.1.2',h]:
    folder.mkdir(parents=True,exist_ok=True)
    for p in [runtime,sources,d/'README.md',d/'VERSION_INFO.md',bug,root/'正式构建_mc26.1.2.log',root/'修复前范围提交阶段回归.log']:
        shutil.copy2(p,folder/p.name)
for name in ['26.1.2范围渲染阶段_Fabric.txt','26.1.2范围渲染阶段_MC.txt']:
    shutil.copy2(r/'outputs'/name,h/name)
mods=Path('G:/game/mcfabric/.minecraft/versions/26.1.2-Fabric/mods')
existing=list(mods.glob('quick-build-*.jar'))
assert len(existing)==1, existing
backup=h/'安装前旧运行包'
backup.mkdir(exist_ok=True)
old=existing[0]
shutil.copy2(old,backup/old.name)
assert sha(backup/old.name)==sha(old)
target=mods/runtime.name
shutil.copy2(runtime,target)
assert sha(target)==sha(runtime)
if old!=target: old.unlink()
assert len(list(mods.glob('quick-build-*.jar')))==1
record={'minecraft':'26.1.2','version':'1.1.7','自动测试':counts,'实际安装':str(target),'运行包SHA256':sha(target),'旧新差异':changed,'实际画面':'待玩家验收','GitHub':'未同步','其他版本':'保持1.1.6'}
(root/'安装及验证.json').write_text(json.dumps(record,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
shutil.copy2(root/'安装及验证.json',h/'安装及验证.json')
(h/'交付说明.md').write_text(summary,encoding='utf-8')
with zipfile.ZipFile(h/'26.1.2修复版1.1.7完整项目源码.zip','w',zipfile.ZIP_DEFLATED) as z:
    for p in d.rglob('*'):
        if p.is_file() and not any(x in {'.gradle','build','run','dist'} for x in p.relative_to(d).parts): z.write(p,p.relative_to(root))
    z.write(bug,'修复记录/'+bug.name)
    z.write(Path(__file__),'scripts/'+Path(__file__).name)
with zipfile.ZipFile(h/'自动测试原始结果.zip','w',zipfile.ZIP_DEFLATED) as z:
    for p in (d/'build/test-results/test').glob('TEST-*.xml'): z.write(p,p.name)
for name in ['最新状态_建筑移动法杖26.2.md','先读我_本对话交接.md']:
    with (h.parent/name).open('a',encoding='utf-8') as f: f.write('\n\n2026-10-05补充交付：\n'+summary+'交付目录：'+str(h)+'\n')
hashes={str(p.relative_to(h)):sha(p) for p in h.rglob('*') if p.is_file() and p.name!='交付文件SHA256.json'}
(h/'交付文件SHA256.json').write_text(json.dumps(hashes,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print(json.dumps(record,ensure_ascii=False))
