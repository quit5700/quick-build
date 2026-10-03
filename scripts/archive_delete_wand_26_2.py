from pathlib import Path
import hashlib, json, shutil, subprocess, sys, zipfile

sys.stdout.reconfigure(encoding='utf-8')
root=Path(__file__).resolve().parents[1]
project=root/'versions/1.1.4-test1/mc26.2'
archive=Path('H:/Codex交接文档库/快捷建筑模组_可调恒亮灯改版_01a0afdc/删除法杖_26.2测试版_20261003')
archive.mkdir(parents=True,exist_ok=True)
sourcezip=archive/'快捷建筑_1.1.4-test1_26.2_完整源码.zip'
skip={'.gradle','build','run','dist'}
with zipfile.ZipFile(sourcezip,'w',zipfile.ZIP_DEFLATED,compresslevel=6) as output:
    for path in sorted(project.rglob('*')):
        relative=path.relative_to(project)
        if path.is_file() and not skip.intersection(relative.parts):
            output.write(path,Path('mc26.2')/relative)
    for name in ['docs/DELIVERY-20261003-DELETE-WAND-26.2.md',
                 'docs/bugs/BUG-20261003-DELETE-WAND-26-API.md',
                 'docs/bugs/BUG_INDEX.md','docs/bugs/REGRESSION_CHECKLIST.md',
                 'scripts/verify_delete_wand_26_2.py','scripts/archive_delete_wand_26_2.py']:
        output.write(root/name,name)
with zipfile.ZipFile(sourcezip) as check:
    assert check.testzip() is None
    assert 'mc26.2/gradle/wrapper/gradle-wrapper.jar' in check.namelist()
    assert 'mc26.2/src/main/java/cn/quit5700/buildingwand/server/DeleteTaskState.java' in check.namelist()
for source in (project/'dist').glob('*.jar'):
    target=archive/source.name
    shutil.copy2(source,target)
    assert hashlib.sha256(source.read_bytes()).digest()==hashlib.sha256(target.read_bytes()).digest()
shutil.copy2(root/'outputs/20261003_删除法杖26.2测试版/交付校验.json',archive/'交付校验.json')
shutil.copy2(root/'docs/DELIVERY-20261003-DELETE-WAND-26.2.md',archive/'交接说明.md')
shutil.copy2(root/'docs/bugs/BUG-20261003-DELETE-WAND-26-API.md',archive/'问题与修复记录.md')
reports=archive/'自动测试结果'
reports.mkdir(exist_ok=True)
for source in (project/'build/test-results/test').glob('TEST-*.xml'):
    shutil.copy2(source,reports/source.name)
logs=project/'run/logs/latest.log'
if logs.exists():shutil.copy2(logs,archive/'开发服务端加载检查.log')
images=archive/'外观示意'
images.mkdir(exist_ok=True)
for source in (root/'outputs/20261003_删除法杖预览').glob('*.png'):
    shutil.copy2(source,images/source.name)
git=subprocess.run(['git','rev-parse','HEAD'],cwd=root,capture_output=True,text=True,encoding='utf-8',check=True).stdout.strip()
branch=subprocess.run(['git','branch','--show-current'],cwd=root,capture_output=True,text=True,encoding='utf-8',check=True).stdout.strip()
record=json.loads((archive/'安装记录.json').read_text(encoding='utf-8-sig'))
installed=Path(record['安装路径'])
assert hashlib.sha256(installed.read_bytes()).hexdigest().upper()==record['SHA256']
summary=f'''# 当前交接状态：删除法杖 26.2 测试版

最终授权：只处理 Minecraft（我的世界）26.2，用户自行实机测试，交付保存完成后一次性关机。
已完成删除法杖的三次左键确认、两阶段右键取消、红色单格与范围框、普通左右键行为拦截、设置台介绍、物品页和原版/JEI（物品与配方查询）合成配方。
范围无人工上限，按区块分批执行，任务保存到对应维度，离线或离开维度暂停并在返回后恢复。
版本 1.1.4-test1；完整 141 项测试通过；游戏内显示与执行由用户验收。

安装：{installed}
安装包 SHA256（安全散列校验）：{record['SHA256']}
当前分支：{branch}
本地提交：{git}
源码：{project}
交付归档：{archive}
归档包含完整可构建源码 ZIP（压缩归档）、运行 JAR（Java归档）、源码 JAR、自动测试结果、外观示意、安装记录、旧版备份、问题修复记录和校验清单。
其他游戏版本保持 1.1.3，未作为正式版发布到 GitHub Releases（GitHub发行版）。

现有未跟踪的原 mc26.2 延迟实验测试不属于本轮工作；此前用户取消延迟问题，禁止继续处理或纳入本次测试版。
关机需在最终归档完成并复核之后执行一次；以关机执行记录为准，禁止自动再次执行。
'''
(archive/'当前状态.md').write_text(summary,encoding='utf-8')
(archive.parent/'最新状态_删除法杖26.2.md').write_text(summary,encoding='utf-8')
hashes=[]
for path in sorted(archive.rglob('*')):
    if path.is_file() and path.name!='归档文件校验.json':
        hashes.append({'文件':str(path.relative_to(archive)),'字节':path.stat().st_size,'SHA256':hashlib.sha256(path.read_bytes()).hexdigest()})
manifest={'版本':'1.1.4-test1','游戏版本':'26.2','本地分支':branch,'本地提交':git,
          '安装记录':record,'归档文件':hashes,'游戏内验收':'待用户测试','GitHub发行版':'未将测试版作为正式版发布'}
(archive/'归档文件校验.json').write_text(json.dumps(manifest,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print(json.dumps({'归档目录':str(archive),'完整源码压缩包':str(sourcezip),'归档文件数':len(hashes),'本地提交':git,'安装校验':'通过'},ensure_ascii=False,indent=2))
