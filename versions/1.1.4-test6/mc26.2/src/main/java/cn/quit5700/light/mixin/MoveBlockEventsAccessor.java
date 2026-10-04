package cn.quit5700.light.mixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockEventData;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import java.util.List;
@Mixin(ServerLevel.class)
public interface MoveBlockEventsAccessor {
 @Accessor("blockEvents") ObjectLinkedOpenHashSet<BlockEventData> q5$blockEvents();
 @Accessor("blockEventsToReschedule") List<BlockEventData> q5$deferredEvents();
}
