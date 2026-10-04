from pathlib import Path

r = Path(__file__).resolve().parents[1]
root = r / 'versions/1.1.6'
old = '本次法杖交互与资源修复正在执行八版本完整回归；最终结果见发布核验.json及安装核验.json，实际画面待玩家验收。'
new = '八版本完整构建通过，1564项自动测试、248项独立世界回归及24次灯保存/迁移/重载启动检查通过；详细结果见发布核验.json及安装核验.json。实际客户端画面仍待玩家验收。'
known = '\n\n已知独立问题：1.21.1有23个灯具配方使用不兼容的原料格式，游戏日志报告配方加载失败。本次五项法杖/模型修复未改动这些配方，建议下一批单独修复；详见项目记录 BUG-20261005-1211-LEGACY-RECIPE-FORMAT.md。\n'
for d in root.glob('mc*'):
    if not d.is_dir():
        continue
    for name in ['README.md', 'VERSION_INFO.md']:
        p = d/name
        t = p.read_text(encoding='utf-8').replace(old, new)
        if d.name == 'mc1.21.1' and '已知独立问题' not in t:
            t += known
        p.write_text(t, encoding='utf-8')
p = r/'docs/bugs/BUG-20261005-WAND-CLIENT-INTERACTIONS.md'
t = p.read_text(encoding='utf-8').replace('版本：1.1.6，正在验证。', '版本：1.1.6，八版本自动验证通过，等待玩家客户端验收。').replace('其余七版完整回归及上一轮灯存档重载测试正在执行。', '八版本完整构建通过，1564项自动测试和248项独立世界回归通过；上一轮六灯保存、迁移重载及再次重载检查共24次启动全部通过。')
p.write_text(t, encoding='utf-8')
p = r/'README.md'
t = p.read_text(encoding='utf-8')
if not t.startswith('当前本地正式版：1.1.6'):
    p.write_text('当前本地正式版：1.1.6（2026-10-05）。八版本运行包、源码包、依赖说明与验证记录见 versions/1.1.6；实际游戏模组目录已同步，GitHub等待玩家验收后再更新。修复重排器模型、范围框相机错位、首次持杖交互、双材料同步和箱子提醒。\n\n'+t, encoding='utf-8')
p = r/'scripts/archive_final_116.py'
t = p.read_text(encoding='utf-8').replace("r/'docs/bugs/BUG-20261005-WAND-CLIENT-INTERACTIONS.md',", "r/'docs/bugs/BUG-20261005-WAND-CLIENT-INTERACTIONS.md', r/'docs/bugs/BUG-20261005-1211-LEGACY-RECIPE-FORMAT.md',")
t = t.replace('GitHub未上传、未推送；', '另有已记录但未修复的独立问题：1.21.1有23个灯具配方原料格式不兼容，不包含在本次五项修复内，建议下一批修正。详见项目修复记录。\n\nGitHub未上传、未推送；')
p.write_text(t, encoding='utf-8')
print('最终文档已更新；独立配方问题明确保留为未修复。')
