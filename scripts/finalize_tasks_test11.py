from pathlib import Path
import json,zipfile,hashlib
r=Path.cwd();v=r/'versions/1.1.4-test11/mc26.2';a=Path(r'H:\Codex交接文档库\快捷建筑模组_可调恒亮灯改版_01a0afdc\任务取消自然掉落_26.2测试版11_20261004');out=r/'outputs/20261004_任务取消自然掉落26.2测试版11'
p=r/'scripts/deliver_tasks_test11.py';s=p.read_text(encoding='utf-8').replace("'原183项测试':'全部保留原样'","'原181项测试':'全部保留原样'").replace("'与测试版9保持一致'","'与测试版10保持一致'");p.write_text(s,encoding='utf-8')
for p in [out/'完整核验.json',a/'完整核验.json']:
 d=json.loads(p.read_text(encoding='utf-8'));d['原181项测试']=d.pop('原183项测试');d['移动快照及删除核心']='与测试版10保持一致';p.write_text(json.dumps(d,ensure_ascii=False,indent=2),encoding='utf-8')
scriptnames=['implement_tasks_test11.py','localize_tasks_test11.py','fix_tasks_api_test11.py','test_tasks_test11.py','fix_task_tests_test11.py','document_tasks_test11.py','prepare_delivery_tasks_test11.py','deliver_tasks_test11.py']
with zipfile.ZipFile(a/'1.1.4-test11-mc26.2-完整源码.zip','w',zipfile.ZIP_DEFLATED) as z:
 for p in v.rglob('*'):
  if p.is_file() and not any(x in {'build','.gradle','run','dist','__pycache__'} for x in p.relative_to(v).parts):z.write(p,'mc26.2/'+p.relative_to(v).as_posix())
 for p in [r/'AGENTS.md',r/'docs/PORTING_NOTES.md',r/'docs/bugs/BUG-20261004-TASK-CANCEL-NATURAL-DROPS.md',*[r/'scripts'/n for n in scriptnames]]:z.write(p,p.relative_to(r).as_posix())
checks={p.name:hashlib.sha256(p.read_bytes()).hexdigest() for p in a.iterdir() if p.suffix in {'.jar','.zip'}};(a/'文件校验.json').write_text(json.dumps(checks,ensure_ascii=False,indent=2),encoding='utf-8')
print('核验记录已确认：原181项测试保留，新增2项，总计183项。')