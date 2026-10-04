from pathlib import Path
import json
r=Path.cwd();v=r/'versions/1.1.4-test11/mc26.2'
texts={'zh_cn':['建造受阻，已暂停：%s/%s；清除障碍后继续，本法杖右键取消。','无法完整放置，请检查范围、权限和原有方块。'],'zh_tw':['建造受阻，已暫停：%s/%s；清除障礙後繼續，本法杖右鍵取消。','無法完整放置，請檢查範圍、權限和原有方塊。'],'en_us':['Build blocked, paused: %s/%s. Clear the obstruction to continue; right-click this wand to cancel.','Cannot place the complete structure. Check bounds, permissions and existing blocks.'],'ja_jp':['建築が妨げられ停止中：%s/%s。障害を除くと再開。この杖の右クリックで取消。','完全な構造を設置できません。範囲、権限、既存ブロックを確認してください。'],'ko_kr':['건축이 막혀 일시 정지: %s/%s. 장애물을 치우면 계속합니다. 이 지팡이 우클릭으로 취소합니다.','완전한 구조를 놓을 수 없습니다. 범위, 권한과 기존 블록을 확인하세요.'],'de_de':['Bau blockiert, pausiert: %s/%s. Hindernis entfernen; Rechtsklick mit diesem Stab bricht ab.','Die vollständige Struktur kann nicht platziert werden. Grenzen, Rechte und vorhandene Blöcke prüfen.'],'fr_fr':['Construction bloquée, en pause : %s/%s. Retirez l’obstacle pour reprendre ; clic droit avec cette baguette pour annuler.','Impossible de placer la structure complète. Vérifiez limites, autorisations et blocs existants.'],'es_es':['Construcción bloqueada, pausada: %s/%s. Quita el obstáculo para seguir; clic derecho con esta varita para cancelar.','No se puede colocar la estructura completa. Comprueba límites, permisos y bloques existentes.']}
for lang,values in texts.items():
 p=v/f'src/main/resources/assets/building_wand/lang/{lang}.json';d=json.loads(p.read_text(encoding='utf-8'));d['message.q5.build_blocked'],d['message.q5.multiblock_dimensions']=values;p.write_text(json.dumps(d,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
text='''# 快捷建筑1.1.4-test11：26.2任务取消与自然掉落测试版

基于测试版10，保留四种建筑模式、默认单块、每杖记忆、切物品保留选区和闪烁、无限制分批任务、原移动及删除已验收功能、单箱和连接修正。

Q5模组方块说明台底部新增“取消全部建造”按钮。点击后服务端按已认证当前玩家身份，取消所有维度、所有建筑法杖的建造和区域替换任务，包括遗失/销毁法杖留下的任务和退出后保存恢复的任务；释放区块、清理建筑选区及闪烁，返回实际取消数量。保留已完成部分，不操作其他玩家，不取消删除法杖任务。移动为即时执行，没有分批在建任务。按钮不要求持有原法杖，也不要求设置管理员权限；等待回复时暂时禁用，避免重复请求。

多格规则沿用测试版10：从第一点方向成组，实心无法配对的末端为空气；空心向内占结构厚度，奇数尺寸也建造，末端留空。实心、空心的空气余位现在允许直接覆盖原有箱子等容器：内容按游戏容器掉落流程释放一次并清空，再将余位设为空气，重复运行不重复掉落。箱子本体不额外生成掉落物。区域替换保持箱子、容器和多格结构保护，余位原有箱子不会清除。

支撑与环境条件不再导致建筑任务暂停。玩家放置采用自然模式：完整结构先装配，随后调用原版邻居形状、生存条件和破坏/掉落流程。无支撑的门、土壤不合的植物、无附着位置的火把按游戏规则破坏或掉落；沙子/砂砾仍走正常重力更新。不因掉落重试建造。每成功放出完整床、门或双格植物消耗一个物品，即使随后按游戏规则掉落也正常消费；普通方块每格一个材料。缺料仍暂停，补料继续；原有不可覆盖方块、权限与边界限制仍可暂停，右键取消。

兼容识别从原版具体类扩展到标准状态属性：具有上下两格状态或床式头尾状态的模组方块，使用同一两格规划、材料、余位、自然更新和区域替换保护。已用不继承门/植物类的测试模组双格方块验证。无法从标准状态识别的私有结构，尤其三格以上或自定义控制器结构，仍需对应模组提供布局，不能擅自假定为门式两格。

说明台八语言同步修正单块含义、材料比例、空心厚度、取消范围、自然掉落、余位容器内容和模组识别范围；既有布局与翻页保持，新增按钮在底部取消/关闭区域，窄界面截断按钮文字并提供完整悬浮提示。

Minecraft（我的世界）26.2；Java（爪哇）25；Fabric Loader（Fabric模组加载器）0.19.3；Fabric API（Fabric应用程序接口）0.154.0+26.2；Fabric Loom（Fabric构建插件）1.15.5；Yarn Mappings（Yarn命名映射）未使用，采用Mojang Official Mappings（Mojang官方命名映射）。

独立世界27场景通过，包含原21场景和新增6场景。完整测试结果以交付核验为准。测试世界不读取玩家存档，测试注册方块及测试类不进入运行包。真实客户端按钮显示、组合模组表现及性能待用户重启验收。

正式版、其他游戏版本与GitHub更新继续暂停等待用户叫更新。用户确认大区域加载现状可接受，本次不改加载策略、不加数量上限、不关机、不修改玩家存档。
'''
for name in ['README.md','VERSION_INFO.md']:(v/name).write_text(text,encoding='utf-8')
p=r/'docs/bugs/BUG-20261004-TASK-CANCEL-NATURAL-DROPS.md';p.write_text(p.read_text(encoding='utf-8')+'''\n修改文件：BuildTaskState、BuildWandServer、BuildNetworking、BuildingWandClient初始化、BuildWandClient、WandSettingsScreen、SettingsScreenLayout、StructurePlacement、WandActions单块分支、ReplacementRules、八语言、SettingsCancellationLayoutTest、独立世界WandSmokeChecks、版本属性及说明。
具体实现：取消以连接身份为准，不接受客户端指定其他玩家；所有维度按任务owner（所属玩家）清理并释放租用区块，响应数量清理客户端选区。余位容器掉落后立即清空再设空气；自然放置调用Block.updateOrDestroy（原版形状更新和破坏），不恢复已经自然掉落的材料，避免重复产物。模组双格按标准属性识别并受保护。
验证：新增2项按钮布局测试，原181项测试预期保留；27项独立世界回归通过，测试取消跨维度/多杖/本人隔离/重复取消/保存恢复/丢失法杖、无支撑门和植物及火把真实掉落且扣材料、实心空心余位内容13钻石7金锭精确掉落并重复执行不复制、区域替换余位容器内容保留、非门类模组两格完整状态/材料/保护。玩家真实界面待验收。
防回归：不得接受客户端传入取消他人身份；取消必须释放区块并让保存数据更新；内容掉落后必须清空，不能再让旧容器重复掉落；自然掉落不能触发退款、回滚或重新放置；标准属性识别不等于所有私有模组结构自动支持；旧严格回滚方法只作内部独立验证，实际玩家调用自然模式。
''',encoding='utf-8')
for name,line in [('BUG_INDEX.md','- [BUG-20261004-TASK-CANCEL-NATURAL-DROPS](BUG-20261004-TASK-CANCEL-NATURAL-DROPS.md)：测试版11跨维度取消本人建造、余位容器精确掉落与自然支撑规则；27场景通过，待完整构建核验。'),('REGRESSION_CHECKLIST.md','- 测试版11：本人跨维度/多杖/遗失/保存恢复任务取消与他人隔离、按钮/关闭/翻页不重叠、无支撑门/植物/火把自然掉落且消耗材料、实心空心余位箱子内容一次掉落、区域替换仍保留箱子、标准属性模组双格识别及保护。')]:
 p=r/'docs/bugs'/name;p.write_text(p.read_text(encoding='utf-8')+'\n'+line+'\n',encoding='utf-8')
p=r/'docs/PORTING_NOTES.md';addition='''\n## 26.2测试版11：服务端获取服务器

ServerPlayer.getServer（服务端玩家获取服务器）已尝试失败：26.2无此方法。成功方式：先取得当前ServerLevel（服务端世界），再调用世界getServer（获取服务器），如((ServerLevel)player.level()).getServer()。跨维度迭代使用此服务器的getAllLevels（所有世界）。保持Java（爪哇）25与Mojang官方映射，完整版本见versions/1.1.4-test11/mc26.2/VERSION_INFO.md；本项目之前适配记录保存在各版本docs/PORTING_NOTES.md。
''';p.write_text((p.read_text(encoding='utf-8') if p.exists() else '# 当前项目版本适配参考\n')+addition,encoding='utf-8');vp=v/'docs/PORTING_NOTES.md';vp.write_text(vp.read_text(encoding='utf-8')+addition,encoding='utf-8')
print('说明台文本、版本信息与问题/适配记录已同步')