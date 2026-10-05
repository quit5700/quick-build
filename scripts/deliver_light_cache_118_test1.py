from pathlib import Path
import hashlib, json, shutil, zipfile, subprocess, xml.etree.ElementTree as ET

r=Path(__file__).resolve().parents[1]
root=r/'versions/1.1.8-test1'
d=root/'mc1.21.8'
h=Path('H:/Codex交接文档库/快捷建筑模组_可调恒亮灯改版_01a0afdc/1.21.8灯缓存修复1.1.8-test1_20261005')
h.mkdir(parents=True,exist_ok=True)
sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
checks={
 '测试版构建_mc1.21.8.log':'BUILD SUCCESSFUL',
 '测试版真实服务端重入回归.log':'Q5_REENTRANT_RESULT: 4/4',
 '世界回归_mc1.21.8.log':'Q5_MOVE_REGRESSION_PASS: 31 cases',
 '灯加载回归_mc1.21.8_prepare.log':'Q5_LIGHT_LOAD_PREPARED:',
 '灯加载回归_mc1.21.8_verify.log':'Q5_LIGHT_LOAD_PASS: verify',
 '灯加载回归_mc1.21.8_persist.log':'Q5_LIGHT_LOAD_PASS: persist'
}
for name,signal in checks.items():
    text=(root/name).read_text(encoding='utf-8',errors='replace')
    assert signal in text and not any(s in text for s in ['Q5_REENTRANT_CASE_FAIL:','Q5_CASE_FAIL:','Q5_LIGHT_LOAD_FAIL:']),name
oldlog=(root/'修复前真实服务端重入回归.log').read_text(encoding='utf-8')
assert 'Q5_REENTRANT_RESULT: 3/4' in oldlog and 'NullPointerException' in oldlog
suites=[ET.parse(p).getroot() for p in (d/'build/test-results/test').glob('TEST-*.xml')]
counts={k:sum(int(s.attrib.get(k,0)) for s in suites) for k in ['tests','failures','errors','skipped']}
assert counts['tests']==197 and counts['failures']==counts['errors']==counts['skipped']==0,counts
runtime=d/'dist/quick-build-1.1.8-test1-mc1.21.8-fabric.jar'
sources=d/'dist/quick-build-1.1.8-test1-mc1.21.8-fabric-sources.jar'
with zipfile.ZipFile(runtime) as new,zipfile.ZipFile(r/'versions/1.1.6/mc1.21.8/dist/quick-build-1.1.6-mc1.21.8-fabric.jar') as old:
    assert json.loads(new.read('fabric.mod.json'))['version']=='1.1.8-test1'
    assert not any('SmokeChecks' in n for n in new.namelist())
    changes=[n for n in old.namelist() if n in new.namelist() and old.read(n)!=new.read(n)]
    assert set(changes)=={'fabric.mod.json','cn/quit5700/light/light/DaylightOcclusion.class','cn/quit5700/light/light/DaylightOcclusion$SourceCache.class','cn/quit5700/light/light/DaylightOcclusion$WorldCache.class'},changes
    for nested in ['SourceCache','WorldCache']:
        outputs=[subprocess.check_output(['C:/Program Files/Java/jdk-21/bin/javap.exe','-c','-p','-classpath',str(jar),'cn.quit5700.light.light.DaylightOcclusion$'+nested]) for jar in [r/'versions/1.1.6/mc1.21.8/dist/quick-build-1.1.6-mc1.21.8-fabric.jar',runtime]]
        assert outputs[0]==outputs[1],nested
with zipfile.ZipFile(sources) as z:assert not any('SmokeChecks' in n for n in z.namelist())
summary='''快捷建筑1.21.8测试版1.1.8-test1（尚未确认正式版）

修复灯光计算读取世界期间更新回调清空缓存导致的空指针崩溃。持有本轮计算对象，只有缓存身份未变化时才发布完成；失效后下一轮重算。原灯模式、预算、存档格式、法杖操作、范围框和模型不改。运行包与1.1.6逐条目比较，仅版本信息和DaylightOcclusion缓存类变化；两个内部类仅调试元数据不同，字段与方法字节码完全一致。

真实独立服务器旧代码成功复现同样空指针，修复后4项缓存回归通过。197项完整自动测试、31项既有世界回归、六灯保存/迁移重载/再次重载均通过。后三组是在命名规则变更前的相同修复代码上执行，最终test1元数据重新构建并再次执行4项缓存回归，算法字节码一致。用户原存档未修改；实际玩家存档继续待验收，不将自动通过认定为正式版。

Minecraft（我的世界）：1.21.8
Java（爪哇运行环境）：21
Fabric Loader（Fabric模组加载器）：构建0.17.3，实际游戏0.19.3
Fabric API（Fabric应用程序接口）：0.136.1+1.21.8
Fabric Loom（Fabric构建插件）：1.11.8
Yarn Mappings（Yarn命名映射）：未使用；采用对应1.21.8的Mojang Official Mappings（Mojang官方命名映射）。

只向1.21.8实际mods（模组）文件夹安装运行包，源码包用于维护，不放入游戏。26.1.2保持已验收1.1.7，1.21.11保持已验收1.1.6；其他版本不改。GitHub未同步，不关机。用户明确确认正式版后，再按约定生成1.1.8正式包。
'''
for name in ['README.md','VERSION_INFO.md']:(d/name).write_text(summary,encoding='utf-8')
(root/'README.md').write_text(summary,encoding='utf-8')
bug=r/'docs/bugs/BUG-20261005-1218-LIGHT-CACHE-REENTRANT.md'
t=bug.read_text(encoding='utf-8').replace('版本：1.1.8，仅1.21.8本次授权修复，验证中。','版本：1.1.8-test1，仅1.21.8本次授权修复，自动及独立服务器验证通过，玩家存档待验收。').replace('最终验证结果待追加。','实际结果：旧代码3/4，缓存清空案例同样空指针；修复后test1为4/4。完整197项测试、31项世界回归和六灯三阶段磁盘重载全部通过；运行包仅DaylightOcclusion类及版本信息变化，两个内部类仅源文件行号调试元数据变化，字段及方法字节码一致。实际玩家存档仍需验收。').replace('versions/1.1.8/','versions/1.1.8-test1/')
bug.write_text(t,encoding='utf-8')
p=r/'docs/bugs/BUG_INDEX.md';t=p.read_text(encoding='utf-8').replace('1.1.8验证中。','1.1.8-test1四项新回归、197项测试及31项世界回归通过，玩家待验收。');p.write_text(t,encoding='utf-8')
for folder in [r/'dist/1.1.8-test1/mc1.21.8',h]:
    folder.mkdir(parents=True,exist_ok=True)
    for p in [runtime,sources,d/'README.md',d/'VERSION_INFO.md',bug]:shutil.copy2(p,folder/p.name)
for p in root.glob('*.log'):shutil.copy2(p,h/p.name)
shutil.copy2(Path('G:/game/mcfabric/minecraft-exported-crash-info-2026-10-05T10-19-13.zip'),h/'用户原始错误报告.zip')
mods=Path('G:/game/mcfabric/.minecraft/versions/1.21.8-Fabric/mods')
existing=list(mods.glob('quick-build-*.jar'));assert len(existing)==1,existing
previous=existing[0];backup=h/'安装前旧包';backup.mkdir(exist_ok=True)
shutil.copy2(previous,backup/previous.name);assert sha(previous)==sha(backup/previous.name)
target=mods/runtime.name;shutil.copy2(runtime,target);assert sha(target)==sha(runtime)
if previous!=target:previous.unlink()
assert len(list(mods.glob('quick-build-*.jar')))==1
record={'minecraft':'1.21.8','version':'1.1.8-test1','正式版':False,'自动测试':counts,'缓存回调回归':4,'世界回归':31,'灯磁盘重载阶段':3,'安装路径':str(target),'运行包SHA256':sha(target),'旧新条目差异':changes,'GitHub':'未同步','用户存档':'未修改，待实测'}
(root/'安装及验证.json').write_text(json.dumps(record,ensure_ascii=False,indent=2)+'\n',encoding='utf-8');shutil.copy2(root/'安装及验证.json',h/'安装及验证.json')
(h/'交付说明.md').write_text(summary,encoding='utf-8')
with zipfile.ZipFile(h/'1.1.8-test1完整项目源码.zip','w',zipfile.ZIP_DEFLATED) as z:
    for p in d.rglob('*'):
        if p.is_file() and not any(x in {'.gradle','build','run','dist','内部验证产物_命名规则调整前未交付'} for x in p.relative_to(d).parts):z.write(p,p.relative_to(root))
    z.write(bug,'修复记录/'+bug.name)
    for p in (r/'scripts').glob('*118*.py'):z.write(p,'scripts/'+p.name)
    z.write(r/'AGENTS.md','AGENTS.md')
with zipfile.ZipFile(h/'自动测试原始结果.zip','w',zipfile.ZIP_DEFLATED) as z:
    for p in (d/'build/test-results/test').glob('TEST-*.xml'):z.write(p,p.name)
for p in h.glob('*.zip'):
    with zipfile.ZipFile(p) as z:assert z.testzip() is None
for name in ['最新状态_建筑移动法杖26.2.md','先读我_本对话交接.md']:
    with (h.parent/name).open('a',encoding='utf-8') as f:f.write('\n\n2026-10-05补充：用户确认1.21.11功能正常、26.1.2的1.1.7范围框及功能正常。\n'+summary+'\n交付目录：'+str(h)+'\n')
hashes={str(p.relative_to(h)):sha(p) for p in h.rglob('*') if p.is_file() and p.name!='交付文件SHA256.json'}
(h/'交付文件SHA256.json').write_text(json.dumps(hashes,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print(json.dumps(record,ensure_ascii=False))
