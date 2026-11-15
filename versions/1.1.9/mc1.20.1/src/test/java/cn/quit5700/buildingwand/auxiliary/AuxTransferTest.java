package cn.quit5700.buildingwand.aux;
import net.minecraft.nbt.*;
import net.minecraft.SharedConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
class AuxTransferTest {
 @BeforeAll static void version(){SharedConstants.tryDetectVersion();}
 @Test void compressedMultiPacketPayloadRoundTripsWithoutMixingOwners()throws Exception{var n=new CompoundTag();byte[] bytes=new byte[100000];new Random(123).nextBytes(bytes);n.putByteArray("Payload",bytes);var packets=AuxNet.packets("import",n);assertTrue(packets.size()>1);var a=new AuxNet.Inbox();var b=new AuxNet.Inbox();CompoundTag received=null;for(var part:packets){received=a.accept(part);b.accept(part);}assertNotNull(received);assertEquals(n,cn.quit5700.buildingwand.aux.LegacyNbt.compoundOrEmpty(received,"Data"));assertEquals("import",cn.quit5700.buildingwand.aux.LegacyNbt.stringOr(received,"Action",""));}
 @Test void outOfOrderAndOversizedTransfersAreRejected(){var inbox=new AuxNet.Inbox();assertThrows(java.io.IOException.class,()->inbox.accept(new AuxNet.Part(UUID.randomUUID(),1,5,new byte[]{1})));assertThrows(java.io.IOException.class,()->inbox.accept(new AuxNet.Part(UUID.randomUUID(),0,AuxNet.MAX+1,new byte[]{1})));}
}
