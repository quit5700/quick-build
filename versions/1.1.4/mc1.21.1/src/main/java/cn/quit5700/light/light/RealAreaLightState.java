package cn.quit5700.light.light;

import cn.quit5700.light.LightMod;
import cn.quit5700.light.block.VariableConstantLightBlock;
import cn.quit5700.light.block.VariableConstantLightBlockEntity;
import cn.quit5700.light.logic.LightBox;
import cn.quit5700.light.logic.RealAreaPolicy;
import cn.quit5700.light.logic.VariableLightMode;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import java.util.*;

/** Persistent desired regions and reconciliation cursors; never loads chunks. */
public final class RealAreaLightState extends SavedData {
    private record Source(long pos,LightBox box) { }
    private record Scan(LightBox box,int next,int order) {
        private Scan(LightBox box,int next){this(box,next,RealAreaPolicy.CENTER_OUT_ORDER);}
    }
    private static final Codec<Source> SOURCE=RecordCodecBuilder.create(i->i.group(
            Codec.LONG.fieldOf("pos").forGetter(Source::pos),AreaLightState.BOX_CODEC.fieldOf("box").forGetter(Source::box)
    ).apply(i,Source::new));
    private static final Codec<Scan> SCAN=RecordCodecBuilder.create(i->i.group(
            AreaLightState.BOX_CODEC.fieldOf("box").forGetter(Scan::box),Codec.INT.optionalFieldOf("next",0).forGetter(Scan::next),
            Codec.INT.optionalFieldOf("order",0).forGetter(Scan::order)
    ).apply(i,Scan::new));
    private static final Codec<RealAreaLightState> CODEC=RecordCodecBuilder.create(i->i.group(
            SOURCE.listOf().optionalFieldOf("sources",List.of()).forGetter(s->s.sources),
            SCAN.listOf().optionalFieldOf("scans",List.of()).forGetter(s->s.scans),
            Codec.LONG.listOf().optionalFieldOf("owned",List.of()).forGetter(s->new ArrayList<>(s.owned))
    ).apply(i,RealAreaLightState::new));
    private static final String SAVE_ID="light_real_area_lights";
    public static final SavedData.Factory<RealAreaLightState> TYPE=new SavedData.Factory<>(
            ()->new RealAreaLightState(List.of(),List.of(),List.of()),(tag,registries)->CODEC.parse(NbtOps.INSTANCE,tag).result().orElseGet(()->new RealAreaLightState(List.of(),List.of(),List.of())),DataFixTypes.LEVEL);
    @Override public CompoundTag save(CompoundTag tag,HolderLookup.Provider registries) {
        CODEC.encodeStart(NbtOps.INSTANCE,this).result().filter(CompoundTag.class::isInstance).map(CompoundTag.class::cast).ifPresent(tag::merge);
        return tag;
    }
    private final List<Source> sources;
    private final List<Scan> scans;
    private final Set<Long> owned;
    private RealAreaLightState(List<Source> sources,List<Scan> scans,List<Long> owned) {
        this.sources=new ArrayList<>(sources);this.scans=new ArrayList<>(scans);this.owned=new HashSet<>(owned);
    }
    public static RealAreaLightState get(ServerLevel world){return world.getDataStorage().computeIfAbsent(TYPE,SAVE_ID);}
    private void enqueue(LightBox box) {
        if(box!=null && RealAreaPolicy.accepts(box) && scans.stream().noneMatch(s->s.box().equals(box)))
            scans.add(new Scan(box,0));
    }
    public void update(VariableConstantLightBlockEntity lamp) {
        long pos=lamp.getBlockPos().asLong();
        LightBox desired=!lamp.isLegacy() && lamp.mode()==VariableLightMode.AREA_REAL_LIGHT
                && lamp.getBlockState().getValue(VariableConstantLightBlock.POWERED) && RealAreaPolicy.accepts(lamp.area()) ? lamp.area():null;
        Source previous=sources.stream().filter(s->s.pos()==pos).findFirst().orElse(null);
        if(Objects.equals(previous==null?null:previous.box(),desired))return;
        if(previous!=null)enqueue(previous.box());
        sources.removeIf(s->s.pos()==pos);
        if(desired!=null){sources.add(new Source(pos,desired));enqueue(desired);}
        // Restart affected scans: a rapid off/on must revisit positions already cleaned.
        for(int i=0;i<scans.size();i++) {
            Scan scan=scans.get(i);
            if(Objects.equals(scan.box(),desired) || (previous!=null && scan.box().equals(previous.box())))
                scans.set(i,new Scan(scan.box(),0));
        }
        setDirty();
    }
    public void remove(BlockPos source) {
        for(Source s:List.copyOf(sources))if(s.pos()==source.asLong()){
            enqueue(s.box());sources.remove(s);
            for(int i=0;i<scans.size();i++)if(scans.get(i).box().equals(s.box()))scans.set(i,new Scan(s.box(),0));
            setDirty();
        }
    }
    public boolean contains(BlockPos pos) {
        return sources.stream().anyMatch(s->s.box().contains(pos.getX(),pos.getY(),pos.getZ()));
    }
    public void claimSharedLight(ServerLevel world,BlockPos pos) {
        if(contains(pos) && world.getBlockState(pos).is(Blocks.LIGHT) && owned.add(pos.asLong()))setDirty();
    }
    public void tick(ServerLevel world) {
        if(world.getGameTime()%200==0){
            for(Source s:List.copyOf(sources)) {
                BlockPos source=BlockPos.of(s.pos());
                if(world.hasChunk(source.getX()>>4,source.getZ()>>4)) {
                    if(world.getBlockEntity(source) instanceof VariableConstantLightBlockEntity lamp)update(lamp);
                    else remove(source);
                }
            }
            for(Source s:sources)enqueue(s.box());if(!scans.isEmpty())setDirty();
        }
        int budget=512, deferred=0;
        while(budget>0 && !scans.isEmpty() && deferred<scans.size()) {
            Scan scan=scans.removeFirst();
            if(!RealAreaPolicy.accepts(scan.box()) || scan.next()<0 || scan.next()>=RealAreaPolicy.operationCount(scan.box(),scan.order())){setDirty();continue;}
            var point=RealAreaPolicy.scanPosition(scan.box(),scan.next(),scan.order());
            BlockPos pos=new BlockPos(point.x(),point.y(),point.z());
            if(!world.hasChunk(pos.getX()>>4,pos.getZ()>>4)) {scans.add(scan);deferred++;continue;}
            deferred=0;budget--;
            if(!world.isOutsideBuildHeight(pos)) {
                var current=world.getBlockState(pos);
                boolean desired=contains(pos);
                if(desired) {
                    if(current.isAir()) {
                        ConstantLightTaskQueue.installVariableLight(world,pos);
                        if(world.getBlockState(pos).is(Blocks.LIGHT))owned.add(pos.asLong());
                    } else if(current.is(Blocks.LIGHT) && protectedByOtherSource(world,pos)) {
                        owned.add(pos.asLong());
                    }
                } else if(owned.contains(pos.asLong())) {
                    boolean other=protectedByOtherSource(world,pos);
                    if(RealAreaPolicy.canRemove(true,false,other,current.is(Blocks.LIGHT)))
                        ConstantLightTaskQueue.cleanupVariableLight(world,pos,false);
                    if(!other)owned.remove(pos.asLong());
                }
            }
            if(scan.next()+1<RealAreaPolicy.operationCount(scan.box(),scan.order()))scans.add(new Scan(scan.box(),scan.next()+1,scan.order()));
            setDirty();
        }
    }
    private boolean protectedByOtherSource(ServerLevel world,BlockPos pos) {
        return ConstantLightTaskQueue.isConstantLightTracked(world,pos)
                || VariableLightTaskState.get(world).isCoveredByRadiusSource(world,pos);
    }
}
