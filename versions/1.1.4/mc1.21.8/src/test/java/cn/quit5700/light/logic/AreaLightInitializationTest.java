package cn.quit5700.light.logic;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.MethodInsnNode;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Compiled-call-site guard: setLevel runs before LevelChunk registers the entity. */
class AreaLightInitializationTest {
    private static final String LAMP = "cn/quit5700/light/block/VariableConstantLightBlockEntity";
    private static final String AREA = "cn/quit5700/light/light/AreaLightState";

    private ClassNode read(String name) throws Exception {
        try (var input = getClass().getClassLoader().getResourceAsStream(name + ".class")) {
            assertNotNull(input);
            ClassNode node=new ClassNode();
            new ClassReader(input).accept(node,0);
            return node;
        }
    }

    private List<MethodInsnNode> calls(MethodNode method) {
        return java.util.Arrays.stream(method.instructions.toArray())
                .filter(MethodInsnNode.class::isInstance).map(MethodInsnNode.class::cast).toList();
    }

    @Test void initializationPassesExistingEntityInsteadOfQueryingUnregisteredPosition() throws Exception {
        var method = read(LAMP).methods.stream().filter(m -> m.name.equals("setLevel")).findFirst().orElseThrow();
        var updates = calls(method).stream().filter(c -> c.owner.equals(AREA)
                && c.name.equals("update")).toList();
        assertEquals(1, updates.size());
        assertEquals("(L" + LAMP + ";)V", updates.get(0).desc,
                "Initialization must use the existing entity, not query its unregistered position");
    }

    @Test void entityUpdateHasNoWorldLookupOrLifecycleReentry() throws Exception {
        var method = read(AREA).methods.stream().filter(m -> m.name.equals("update")
                && m.desc.equals("(L" + LAMP + ";)V")).findFirst();
        assertTrue(method.isPresent(), "An entity-only update entry point is required");
        var allowed = java.util.Set.of("getBlockPos", "remove", "isLegacy", "mode", "area", "getBlockState",
                "getValue", "booleanValue", "asLong", "<init>", "add", "setDirty");
        for (var call : calls(method.orElseThrow())) {
            assertTrue(allowed.contains(call.name), "Review new call for lifecycle reentry: " + call);
        }
    }
    @Test void realAreaInitializationAlsoUsesTheCurrentEntityWithoutWorldLookup() throws Exception {
        String owner="cn/quit5700/light/light/RealAreaLightState";
        var initializer=read(LAMP).methods.stream().filter(m->m.name.equals("setLevel")).findFirst().orElseThrow();
        var update=calls(initializer).stream().filter(c->c.owner.equals(owner) && c.name.equals("update")).findFirst().orElseThrow();
        assertEquals("(L"+LAMP+";)V",update.desc);
        var method=read(owner).methods.stream().filter(m->m.name.equals("update")).findFirst().orElseThrow();
        assertTrue(calls(method).stream().noneMatch(c->c.name.equals("getBlockEntity") || c.name.equals("getChunkAt")));
    }
}
