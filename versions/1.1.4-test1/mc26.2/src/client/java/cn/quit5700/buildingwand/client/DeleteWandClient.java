package cn.quit5700.buildingwand.client;
import cn.quit5700.buildingwand.network.DeleteNetworking;
import cn.quit5700.buildingwand.logic.DeleteBox;
import cn.quit5700.buildingwand.registry.ModItems;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import java.util.List;

public final class DeleteWandClient {
    private static int stage;
    private static long revision,sequence;
    private static BlockPos first=BlockPos.ZERO,second=BlockPos.ZERO;
    private static boolean pending,holding;
    private static DeleteBox box;
    private static net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dimension;
    public static DeleteBox box(){return box;}
    public static void register(){
        ClientPlayNetworking.registerGlobalReceiver(DeleteNetworking.State.ID,(p,c)->c.client().execute(()->{
            if(p.sequence()!=sequence)return;
            stage=p.stage();first=p.first();second=p.second();revision=p.revision();pending=false;
        }));
        ClientPlayConnectionEvents.DISCONNECT.register((handler,client)->clear());
    }
    public static void inactive(Minecraft client) {
        if(holding && client.player!=null && client.getConnection()!=null)send(DeleteNetworking.RESET,BlockPos.ZERO);
        if(holding){stage=0;first=second=BlockPos.ZERO;pending=false;holding=false;dimension=null;}
        box=null;
    }
    private static void clear(){stage=0;revision=0;sequence=0;pending=false;holding=false;first=second=BlockPos.ZERO;box=null;dimension=null;}
    public static void tick(Minecraft client,BlockPos target,boolean left,boolean right) {
        if(!holding || !client.level.dimension().equals(dimension)){
            holding=true;stage=0;first=second=BlockPos.ZERO;dimension=client.level.dimension();
            send(DeleteNetworking.RESET,BlockPos.ZERO);
        }
        if(!client.mouseHandler.isMouseGrabbed()){box=null;WandPreviewState.clear();return;}
        if(right){send(DeleteNetworking.CANCEL,BlockPos.ZERO);stage=0;first=second=BlockPos.ZERO;box=null;}
        else if(left && !pending)send(stage==2?DeleteNetworking.CONFIRM:DeleteNetworking.PICK,target);
        if(stage==2) {
            box=DeleteBox.between(first.getX(),first.getY(),first.getZ(),second.getX(),second.getY(),second.getZ());
            WandPreviewState.clear();
        } else {
            box=null;
            WandPreviewState.set(stage==1?List.of(first,target):List.of(target),WandPreviewState.PreviewColor.RED);
        }
    }
    private static void send(int action,BlockPos pos){pending=true;ClientPlayNetworking.send(new DeleteNetworking.Request(action,pos,revision,++sequence));}
}
