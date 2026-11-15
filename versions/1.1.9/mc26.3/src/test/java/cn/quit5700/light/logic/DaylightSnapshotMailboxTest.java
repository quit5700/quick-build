package cn.quit5700.light.logic;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class DaylightSnapshotMailboxTest {
    @Test
    void networkSideOnlyPublishesAndClientSideDrainsTheLatestSnapshot() {
        DaylightSnapshotMailbox<Integer> mailbox = new DaylightSnapshotMailbox<>();

        mailbox.offer(List.of(1));
        mailbox.offer(List.of(2, 3));

        assertEquals(List.of(2, 3), mailbox.drain());
        assertNull(mailbox.drain());
    }
}
