from pathlib import Path
import json, shutil, zipfile, hashlib

r = Path(__file__).resolve().parents[1]
root = r / 'versions/1.1.6'
h = Path('H:/Codex交接文档库/快捷建筑模组_可调恒亮灯改版_01a0afdc')
dest = h / '正式版1.1.6_20261005'
results = json.loads((root / '发布核验.json').read_text(encoding='utf-8'))
installed = json.loads((root / '安装核验.json').read_text(encoding='utf-8'))
assert len(results) == len(installed) == 8
text = '''正式版1.1.6本地交付已完成（2026-10-05）。

26.2、26.1.2、1.21.11、1.21.8、1.21.4、1.21.1、1.20.4、1.20.1均已生成运行包及源码包，并更新实际游戏mods（模组）文件夹。每个游戏目录仅保留一个本模组运行包；源码包不放入游戏。游戏重启后加载新版。

取消全部建造按钮只显示在建筑魔杖说明第一页，位于内容左右翻页箭头右侧，随文字高度排列。已补齐七个旧版本遗漏的建筑、移动法杖纹理；1.1.6修复七版重排器立体模型遗漏、范围框相机错位、首次持杖吞点击、区域替换双材料同步及箱子保护红字提醒。点击串行处理，超时只查询、不重复执行确认；手持槽位与法杖ID防串杖。上一轮旧灯迁移修复保持。

八版本完整构建通过，1564项自动测试全部通过，248项独立世界回归全部通过；每版本六盏灯的磁盘保存、迁移重载、再次重载持久化检查通过，共24次测试世界启动。八语言键与占位符一致，所有PNG资源与验收基线一致，运行包未混入测试类。实际玩家客户端中的画面及其他模组联合使用仍由玩家重启验收。局域网客户端与服务端同时更新1.1.6。

每版本目录含运行包、源码包、依赖版本说明、构建日志和独立世界回归日志。完整项目源码及自动测试证据另存ZIP（压缩包）。安装前旧包已单独保留。

另有已记录但未修复的独立问题：1.21.1有23个灯具配方原料格式不兼容，不包含在本次五项修复内，建议下一批修正。详见项目修复记录。

GitHub未上传、未推送；等待玩家测试后明确通知。不会关机，不修改玩家存档。
'''
(dest / '交付说明.md').write_text(text, encoding='utf-8')
for name in ['README.md', '依赖版本.json', '发布核验.json', '安装核验.json', '外观资源核查.json']:
    shutil.copy2(root / name, dest / name)
docs = dest / '项目修复记录'
docs.mkdir(exist_ok=True)
for p in [r/'docs/PORTING_NOTES.md', r/'docs/bugs/BUG_INDEX.md', r/'docs/bugs/BUG-20261005-WAND-CLIENT-INTERACTIONS.md', r/'docs/bugs/BUG-20261005-1211-LEGACY-RECIPE-FORMAT.md', r/'docs/bugs/REGRESSION_CHECKLIST.md']:
    shutil.copy2(p, docs / p.name)
archive = dest / '正式版1.1.6_八版本完整项目源码.zip'
with zipfile.ZipFile(archive, 'w', zipfile.ZIP_DEFLATED) as z:
    for p in sorted(root.rglob('*')):
        if p.is_file() and not any(part in {'.gradle', 'build', 'run', 'dist', '__pycache__'} for part in p.relative_to(root).parts):
            z.write(p, p.relative_to(r).as_posix())
    for p in sorted((r/'scripts').glob('*116*.py')):
        z.write(p, p.relative_to(r).as_posix())
    for p in docs.iterdir():
        z.write(p, '交付修复记录/' + p.name)
    for name in ['AGENTS.md', '.gitignore']:
        if (r/name).is_file(): z.write(r/name, name)
with zipfile.ZipFile(dest / '自动测试原始结果.zip', 'w', zipfile.ZIP_DEFLATED) as z:
    for p in sorted(root.glob('mc*/build/test-results/test/TEST-*.xml')):
        z.write(p, p.relative_to(root).as_posix())
with zipfile.ZipFile(archive) as z:
    assert z.testzip() is None
    for item in installed:
        assert 'versions/1.1.6/mc'+item['minecraft']+'/build.gradle' in z.namelist()
for name in ['最新状态_建筑移动法杖26.2.md', '先读我_本对话交接.md']:
    p=h/name
    with p.open('a', encoding='utf-8') as f:
        f.write('\n\n## 2026-10-05 正式版1.1.6最终交付\n\n'+text+'\n交付目录：'+str(dest)+'\n')
hashes={str(p.relative_to(dest)):hashlib.sha256(p.read_bytes()).hexdigest() for p in dest.rglob('*') if p.is_file() and p.name!='交付文件SHA256.json'}
(dest/'交付文件SHA256.json').write_text(json.dumps(hashes, ensure_ascii=False, indent=2)+'\n', encoding='utf-8')
print(json.dumps({'完整源码':str(archive), '校验文件数':len(hashes), '交付完成':True}, ensure_ascii=False))
