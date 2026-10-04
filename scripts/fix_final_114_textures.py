from pathlib import Path
import shutil
r=Path.cwd();root=r/'versions/1.1.4';base=root/'mc26.2/src/main/resources/assets'
for d in root.glob('mc*'):
 if d.name=='mc26.2':continue
 for f in base.rglob('*.png'):
  dest=d/'src/main/resources/assets'/f.relative_to(base);dest.parent.mkdir(parents=True,exist_ok=True);shutil.copy2(f,dest)
print('已同步所有原生纹理，补齐建筑、移动法杖两项跨版本纹理遗漏。')
f=r/'docs/bugs/BUG-20261004-FORMAL-114-PORTING.md'
with f.open('a',encoding='utf-8') as stream:stream.write('\n外观根因：26.2正式1.1.3已含新版建筑/移动法杖PNG，但其他版本仍旧纹理。按26.2相对1.1.3差量无法识别这种历史遗漏；正式交付统一同步全部PNG，并逐文件校验。16色远程开关图片和模型与26.2基线一致，不重新设计。\n')
