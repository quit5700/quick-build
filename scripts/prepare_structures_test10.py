from pathlib import Path
import shutil
r=Path.cwd();v=r/'versions/1.1.4-test10/mc26.2';assert not v.exists();shutil.copytree(r/'versions/1.1.4-test9/mc26.2',v,ignore=shutil.ignore_patterns('build','.gradle','run','dist','__pycache__'))
p=v/'gradle.properties';p.write_text(p.read_text(encoding='utf-8').replace('1.1.4-test9','1.1.4-test10'),encoding='utf-8')