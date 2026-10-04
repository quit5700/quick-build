package cn.quit5700.buildingwand.client;
import cn.quit5700.buildingwand.network.DeleteNetworking;
import cn.quit5700.buildingwand.logic.DeleteBox;
import cn.quit5700.buildingwand.logic.DeleteSelection;
import cn.quit5700.buildingwand.logic.DeleteSelectionMemory;
import cn.quit5700.buildingwand.registry.ModItems;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;

import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.ArrayList;

public final class DeleteWandClient {
    private static final DeleteSelectionMemory MEMORY=new DeleteSelectionMemory();
    private static DeleteBox box;private static long tick;
    public static DeleteBox box(){return box;}
    public static void register(){
        cn.quit5700.buildingwand.client.LegacyWandClientPackets.receiver(DeleteNetworking.State.ID,DeleteNetworking.State::read,(p,c)->c.client().execute(()->{
            var world=c.client().level;if(world==null||!world.dimension().location().toString().equals(p.dimension()))return;
            MEMORY.apply(p.stage(),point(p.first()),point(p.second()),p.revision(),p.sequence(),p.checking(),p.insufficient());
        }));
        ClientPlayConnectionEvents.DISCONNECT.register((handler,client)->{MEMORY.disconnect();box=null;});
        ClientTickEvents.END_CLIENT_TICK.register(client->{
            tick++;
            if(client.player==null||client.level==null){MEMORY.disconnect();box=null;return;}
            MEMORY.dimension(client.level.dimension().location().toString());
            boolean dim=MEMORY.flashing(tick);
            for(int slot=0;slot<client.player.getInventory().getContainerSize();slot++)flash(client.player.getInventory().getItem(slot),dim);
            flash(client.player.getOffhandItem(),dim);
        });
    }
    private static void flash(ItemStack stack,boolean dim){
        if(!stack.is(ModItems.DELETE_WAND))return;
        LegacyWandFlash.set(stack,dim);
    }
    public static void inactive(Minecraft client){MEMORY.pause();box=null;if(client.level==null)MEMORY.disconnect();}
    public static void tick(Minecraft client,BlockPos target,boolean left,boolean right){
        if(MEMORY.activate(client.level.dimension().location().toString()))send(DeleteNetworking.QUERY,BlockPos.ZERO);
        if(!client.mouseHandler.isMouseGrabbed()){box=null;WandPreviewState.clear();return;}
        if(right){send(DeleteNetworking.CANCEL,BlockPos.ZERO);MEMORY.cancelLocally();box=null;}
        else if(left&&!MEMORY.pending&&!MEMORY.checking)send(MEMORY.stage==2?DeleteNetworking.CONFIRM:DeleteNetworking.PICK,target);
        if(MEMORY.stage==2){
            var a=MEMORY.first;var b=MEMORY.second;
            box=DeleteBox.between(a.x(),a.y(),a.z(),b.x(),b.y(),b.z());WandPreviewState.clear();
        }else{
            box=null;BlockPos first=pos(MEMORY.first);
            WandPreviewState.set(MEMORY.stage==1?List.of(first,target):List.of(target),WandPreviewState.PreviewColor.RED);
        }
    }
    private static void send(int action,BlockPos pos){cn.quit5700.buildingwand.client.LegacyWandClientPackets.send(new DeleteNetworking.Request(action,pos,MEMORY.revision,MEMORY.request()));}
    private static BlockPos pos(DeleteSelection.Point p){return new BlockPos(p.x(),p.y(),p.z());}
    private static DeleteSelection.Point point(BlockPos p){return new DeleteSelection.Point(p.getX(),p.getY(),p.getZ());}
}
