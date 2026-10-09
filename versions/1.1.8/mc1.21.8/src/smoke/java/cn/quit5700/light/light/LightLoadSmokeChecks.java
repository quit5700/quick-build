package cn.quit5700.light.light;

import cn.quit5700.light.block.VariableConstantLightBlock;
import cn.quit5700.light.block.VariableConstantLightBlockEntity;
import cn.quit5700.light.registry.LightBlocks;
import cn.quit5700.light.logic.LightBox;
import cn.quit5700.light.logic.VariableLightMode;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.minecraft.core.BlockPos;

/** Separate disk-save/restart fixture. Never included in production archives. */
public final class LightLoadSmokeChecks {
    private static final BlockPos ORIGIN = new BlockPos(0,80,0);
    private static boolean loadedChunkMarked;
    public static void register() {
        if ("reentrant".equals(System.getProperty("q5.lightLoadPhase"))) {
            DaylightReentrantSmokeChecks.register();
            return;
        }
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            ServerChunkEvents.CHUNK_LOAD.register((world, chunk) -> {
                if (chunk.getBlockEntities().containsKey(ORIGIN)) {
                    loadedChunkMarked = chunk.isUnsaved();
                }
            });
        });
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            String phase=System.getProperty("q5.lightLoadPhase", "verify");
            try {
                var world=server.overworld();
                if (phase.equals("prepare")) {
                    for (int i=0;i<6;i++) {
                        var pos=ORIGIN.offset(i,0,0);
                        var block=i==3?LightBlocks.COPY_VARIABLE_CONSTANT_LIGHT:LightBlocks.VARIABLE_CONSTANT_LIGHT;
                        world.setBlock(pos,block.defaultBlockState().setValue(VariableConstantLightBlock.POWERED,i!=2),2|16);
                        var lamp=(VariableConstantLightBlockEntity)world.getBlockEntity(pos);
                        put(lamp,"radius",2);put(lamp,"spacing",3);
                        put(lamp,"mode",VariableLightMode.values()[new int[]{1,2,0,2,3,4}[i]]);
                        put(lamp,"area",LightBox.of(0,81,0,1,82,1));
                        lamp.setChanged();
                    }
                    server.saveEverything(false,true,true);
                    System.out.println("Q5_LIGHT_LOAD_PREPARED: 6 lamps saved with historical and current modes");
                } else {
                    System.out.println("Q5_LIGHT_LOAD_VERIFY_BEGIN");
                    for(int i=0;i<6;i++) {
                        var pos=ORIGIN.offset(i,0,0);
                        var lamp=(VariableConstantLightBlockEntity)world.getBlockEntity(pos);
                        require(lamp!=null,"saved lamp missing");
                        require(lamp.radius()==2,"radius lost");
                        require(lamp.spacing()==(i==3?3:1),"spacing policy changed");
                        require(lamp.mode()==VariableLightMode.values()[new int[]{3,3,3,2,3,4}[i]],"mode migration wrong");
                        require(lamp.isLegacy()==(i==3),"legacy isolation lost");
                        require(!((Boolean)get(lamp,"migrationSavePending")),"migration save not completed");
                        if(i!=3)require(LightBox.of(0,81,0,1,82,1).equals(lamp.area()),"area lost");
                        if(phase.equals("persist"))require(get(lamp,"retiredMode")==null,"migration not persisted to disk");
                    }
                    if(phase.equals("verify"))require(loadedChunkMarked,"migrated chunk was not marked for save");
                    server.saveEverything(false,true,true);
                    System.out.println("Q5_LIGHT_LOAD_PASS: "+phase+" 6 lamps; disk reload, power, legacy, area and save verified");
                }
            } catch(Throwable failure) {
                System.out.println("Q5_LIGHT_LOAD_FAIL: "+phase); failure.printStackTrace();
            }
            server.halt(false);
        });
    }
    private static void require(boolean condition,String message){if(!condition)throw new AssertionError(message);}
    private static void put(Object object,String name,Object value)throws Exception{var f=object.getClass().getDeclaredField(name);f.setAccessible(true);f.set(object,value);}
    private static Object get(Object object,String name)throws Exception{var f=object.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(object);}
}
