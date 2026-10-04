from pathlib import Path
r=Path.cwd()
for v in ['1.21.1','1.20.4','1.20.1']:
 d=r/'versions/1.1.4'/('mc'+v)
 f=d/'src/client/java/cn/quit5700/buildingwand/client/LegacyWandFlash.java';s=f.read_text(encoding='utf-8').replace('Minecraft.getInstance().getItemColors().register(', 'net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry.ITEM.register(');f.write_text(s,encoding='utf-8')
 f=d/'src/main/java/cn/quit5700/buildingwand/item/DeletionWandInteractions.java';s=f.read_text(encoding='utf-8').replace('UseItemCallback.EVENT.register((player,world,hand)->result(player));','UseItemCallback.EVENT.register((player,world,hand)->new net.minecraft.world.InteractionResultHolder<>(result(player),player.getItemInHand(hand)));');f.write_text(s,encoding='utf-8')
print('旧版右键返回值和物品颜色注册已按Fabric接口适配。')
