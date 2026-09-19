from pathlib import Path
import re
R=Path(__file__).resolve().parents[1]/'versions/1.1.3'
P=R/'mc1.21.4'
paths=[p.relative_to(R/'mc1.21.8') for root in ['src/main/java/cn/quit5700/light','src/client/java/cn/quit5700/light'] for p in (R/'mc1.21.8'/root).rglob('*.java')]
for relative in paths:
 if any(x in relative.parts for x in ['registry','redstone','compat']):continue
 if relative.name in ['VariableConstantLightBlock.java','ConstantLightBlock.java','RedstoneEnergyBlock.java']:continue
 # Only carry classes changed by the new light implementation; retain other old-version hooks.
 source=R/'mc1.21.8'/relative
 if source.read_text(encoding='utf-8')==(R.parents[1]/'mc1.21.8'/relative).read_text(encoding='utf-8') if (R.parents[1]/'mc1.21.8'/relative).exists() else False:continue
 p=P/relative;s=source.read_text(encoding='utf-8')
 if p.name=='VariableConstantLightScreen.java':
  s=s.replace('.pushMatrix()', '.pushPose()').replace('.popMatrix()', '.popPose()').replace('.translate(centerX, y + 12)', '.translate(centerX, y + 12, 0)').replace('.scale(1.5F, 1.5F)', '.scale(1.5F, 1.5F, 1F)')
 if p.name=='AreaSelectionClient.java':s=s.replace(')).position()', ')).getPosition()')
 if p.name=='VariableConstantLightBlockEntity.java':
  s=s.replace('import net.minecraft.world.level.storage.ValueInput;','import net.minecraft.nbt.CompoundTag;').replace('import net.minecraft.world.level.storage.ValueOutput;','import net.minecraft.core.HolderLookup;')
  s=s.replace('ValueOutput data','CompoundTag data, HolderLookup.Provider registries').replace('ValueInput data','CompoundTag data, HolderLookup.Provider registries').replace('super.saveAdditional(data)', 'super.saveAdditional(data, registries)').replace('super.loadAdditional(data)', 'super.loadAdditional(data, registries)')
  s=re.sub(r'data.getIntOr\("([^"\n]+)",\s*([^\)]+)\)',r'(data.contains("\1") ? data.getInt("\1") : \2)',s)
  s=s[:s.index('    @Override\n    public void preRemoveSideEffects')]+'}\n'
 if p.name in ['AreaLightState.java','RealAreaLightState.java','VariableLightTaskState.java']:
  c=p.stem
  key={'AreaLightState':'area_light_ranges','RealAreaLightState':'real_area_lights','VariableLightTaskState':'variable_light_tasks'}[c]
  constructor={'AreaLightState':'new AreaLightState(List.of())','RealAreaLightState':'new RealAreaLightState(List.of(),List.of(),List.of())','VariableLightTaskState':'new VariableLightTaskState()'}[c]
  s=s.replace('import net.minecraft.world.level.saveddata.SavedDataType;', 'import net.minecraft.core.HolderLookup;\nimport net.minecraft.nbt.CompoundTag;\nimport net.minecraft.nbt.NbtOps;')
  start=s.index('    public static final SavedDataType<');end=s.index(';',start)+1
  s=s[:start]+f'''    private static final String SAVE_ID="light_{key}";
    public static final SavedData.Factory<{c}> TYPE=new SavedData.Factory<>(
            ()->{constructor},(tag,registries)->CODEC.parse(NbtOps.INSTANCE,tag).result().orElseGet(()->{constructor}),DataFixTypes.LEVEL);
    @Override public CompoundTag save(CompoundTag tag,HolderLookup.Provider registries) {{
        CODEC.encodeStart(NbtOps.INSTANCE,this).result().filter(CompoundTag.class::isInstance).map(CompoundTag.class::cast).ifPresent(tag::merge);
        return tag;
    }}'''+s[end:]
  s=s.replace('.computeIfAbsent(TYPE)', '.computeIfAbsent(TYPE,SAVE_ID)')
 if p.name=='DaylightChunkMixin.java':
  s=s.replace('int flags,','boolean moved,').replace('previous.isSolidRender()', 'previous.isSolidRender(world,pos)').replace('updated.isSolidRender()', 'updated.isSolidRender(world,pos)').replace('previous.getOcclusionShape()', 'previous.getOcclusionShape(world,pos)').replace('updated.getOcclusionShape()', 'updated.getOcclusionShape(world,pos)')
 if p.name=='DaylightOcclusion.java':s=s.replace('b.isSolidRender()', 'b.isSolidRender(level,to)').replace('a.getOcclusionShape()', 'a.getOcclusionShape(level,from)').replace('b.getOcclusionShape()', 'b.getOcclusionShape(level,to)')
 p.parent.mkdir(parents=True,exist_ok=True);p.write_text(s,encoding='utf-8')
p=P/'src/client/java/cn/quit5700/light/compat/jei/HiddenLegacyLightPlugin.java';s=p.read_text(encoding='utf-8').replace('Identifier','ResourceLocation');p.write_text(s,encoding='utf-8')
p=P/'build.gradle';s=p.read_text(encoding='utf-8');s=s.replace('    mavenCentral()', "    maven { url = 'https://maven.blamejared.com/' }\n    mavenCentral()")
s=s.replace('    testImplementation platform', '    modCompileOnly "mezz.jei:jei-1.21.4-common-api:20.0.0.4"\n\n    testImplementation platform');p.write_text(s,encoding='utf-8')
