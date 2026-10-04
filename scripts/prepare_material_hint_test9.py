from pathlib import Path
import shutil,json
r=Path.cwd();v=r/'versions/1.1.4-test9/mc26.2';assert not v.exists();shutil.copytree(r/'versions/1.1.4-test8/mc26.2',v,ignore=shutil.ignore_patterns('build','.gradle','run','dist','__pycache__'))
p=v/'gradle.properties';p.write_text(p.read_text(encoding='utf-8').replace('1.1.4-test8','1.1.4-test9'),encoding='utf-8')
texts={'zh_cn':'尚未选择建造材料，请按潜行键＋右键点击方块选择材料，再左键记录第一点。','zh_tw':'尚未選擇建造材料，請按潛行鍵＋右鍵點擊方塊選擇材料，再左鍵記錄第一點。','en_us':'No building material selected. Sneak + right-click a block to select material, then left-click to record the first corner.','ja_jp':'建築材料が未選択です。スニーク＋右クリックでブロックを選び、左クリックで始点を記録してください。','ko_kr':'건축 재료를 선택하지 않았습니다. 웅크리기 + 우클릭으로 블록 재료를 선택한 뒤 좌클릭으로 첫 점을 기록하세요.','de_de':'Kein Baumaterial gewählt. Schleichen + Rechtsklick auf einen Block wählt das Material; danach mit Linksklick die erste Ecke markieren.','fr_fr':'Aucun matériau choisi. Accroupissez-vous et faites un clic droit sur un bloc pour choisir le matériau, puis un clic gauche pour définir le premier point.','es_es':'No se ha elegido material. Agáchate y haz clic derecho en un bloque para elegirlo; después, clic izquierdo para marcar el primer punto.'}
for lang,text in texts.items():
 p=v/f'src/main/resources/assets/building_wand/lang/{lang}.json';d=json.loads(p.read_text(encoding='utf-8'));d['text.quick_build.109']=text;p.write_text(json.dumps(d,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
for name in ['README.md','VERSION_INFO.md']:
 p=v/name;s=p.read_text(encoding='utf-8').replace('1.1.4-test8','1.1.4-test9');p.write_text(s+'\n测试版9仅修改未选建造材料时的旧操作提示，八语言改为潜行＋右键选择材料、再左键记录第一点。全部Java（爪哇）源码保持测试版8，区域替换保护规则不变。\n',encoding='utf-8')
print('仅八语言提示及版本信息已更新')