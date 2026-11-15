# 未接入的照明回归草稿

清理前发现旧根目录有两个未提交测试草稿，未出现在当前已验收版本中。为避免丢失开发思路，将其原文保存在此，不作为正式版已通过的测试或已实现功能。后续若改照明刷新队列，可先核对本版接口和已有SectionRefreshQueueTest，再决定是否适配。

## DaylightRefreshOrderTest.java

```java
package cn.quit5700.light.client;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DaylightRefreshOrderTest {
    @Test void snapshotIsAppliedBeforeAnyRefreshSubmission() throws Exception {
        String source=Files.readString(Path.of("src/client/java/cn/quit5700/light/client/ClientDaylightState.java"),StandardCharsets.UTF_8);
        int start=source.indexOf("private static void applyPending(");
        int apply=source.indexOf("applySnapshot(minecraft);",start);
        int dirty=source.indexOf("setSectionRangeDirty(",start);
        assertTrue(apply>start && apply<dirty,"Apply latest snapshot before submitting geometry rebuilds");
    }
}

```

## AreaRefreshQueueTest.java

```java
package cn.quit5700.light.logic;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;

class AreaRefreshQueueTest {
    @Test void newestOverlappingChangesDoNotMultiplyPendingWork() {
        var queue = new AreaRefreshQueue();
        for (int i=0;i<20;i++) queue.add(LightBox.of(0,0,0,128,128,128), (x,z)->true);
        assertEquals(1000, queue.size());
        var seen = new java.util.HashSet<AreaRefreshQueue.Section>();
        while(queue.size()>0) queue.drain(128,4,4,4,seen::add);
        assertEquals(1000,seen.size());
    }
    @Test void nearCameraIsFirstAndBudgetIsBounded() {
        var queue=new AreaRefreshQueue();
        queue.add(LightBox.of(0,0,0,128,128,128),(x,z)->true);
        var submitted=new ArrayList<AreaRefreshQueue.Section>();
        queue.drain(128,7,6,5,submitted::add);
        assertEquals(new AreaRefreshQueue.Section(7,6,5),submitted.getFirst());
        assertEquals(128,submitted.size());
        assertEquals(872,queue.size());
    }
    @Test void skipsUnloadedColumnsAndKeepsOldRegionForSwitchOff() {
        var queue=new AreaRefreshQueue();
        queue.add(LightBox.of(-16,-16,-16,-1,-1,-1),(x,z)->x==-1 && z==-1);
        assertEquals(3,queue.size());
        queue.add(LightBox.of(160,0,160,175,15,175),(x,z)->false);
        assertEquals(3,queue.size());
        queue.clear(); assertEquals(0,queue.size());
    }
}

```
