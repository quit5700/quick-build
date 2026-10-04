package cn.quit5700.light.logic;

import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassModel;
import java.lang.classfile.MethodModel;
import java.lang.classfile.instruction.InvokeInstruction;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Compiled-call-site guard: setLevel runs before LevelChunk registers the entity. */
class AreaLightInitializationTest {
    private static final String LAMP = "cn/quit5700/light/block/VariableConstantLightBlockEntity";
    private static final String AREA = "cn/quit5700/light/light/AreaLightState";

    private ClassModel read(String name) throws Exception {
        try (var input = getClass().getClassLoader().getResourceAsStream(name + ".class")) {
            assertNotNull(input);
            return ClassFile.of().parse(input.readAllBytes());
        }
    }

    private List<InvokeInstruction> calls(MethodModel method) {
        return method.code().orElseThrow().elementList().stream()
                .filter(InvokeInstruction.class::isInstance).map(InvokeInstruction.class::cast).toList();
    }

    @Test void initializationPassesExistingEntityInsteadOfQueryingUnregisteredPosition() throws Exception {
        var method = read(LAMP).methods().stream().filter(m -> m.methodName().equalsString("setLevel")).findFirst().orElseThrow();
        var updates = calls(method).stream().filter(c -> c.owner().asInternalName().equals(AREA)
                && c.name().equalsString("update")).toList();
        assertEquals(1, updates.size());
        assertEquals("(L" + LAMP + ";)V", updates.getFirst().type().stringValue(),
                "Initialization must use the existing entity, not query its unregistered position");
    }

    @Test void entityUpdateHasNoWorldLookupOrLifecycleReentry() throws Exception {
        var method = read(AREA).methods().stream().filter(m -> m.methodName().equalsString("update")
                && m.methodType().equalsString("(L" + LAMP + ";)V")).findFirst();
        assertTrue(method.isPresent(), "An entity-only update entry point is required");
        var allowed = java.util.Set.of("getBlockPos", "remove", "isLegacy", "mode", "area", "getBlockState",
                "getValue", "booleanValue", "asLong", "<init>", "add", "setDirty");
        for (var call : calls(method.orElseThrow())) {
            assertTrue(allowed.contains(call.name().stringValue()), "Review new call for lifecycle reentry: " + call);
        }
    }
    @Test void realAreaInitializationAlsoUsesTheCurrentEntityWithoutWorldLookup() throws Exception {
        String owner="cn/quit5700/light/light/RealAreaLightState";
        var initializer=read(LAMP).methods().stream().filter(m->m.methodName().equalsString("setLevel")).findFirst().orElseThrow();
        var update=calls(initializer).stream().filter(c->c.owner().asInternalName().equals(owner) && c.name().equalsString("update")).findFirst().orElseThrow();
        assertEquals("(L"+LAMP+";)V",update.type().stringValue());
        var method=read(owner).methods().stream().filter(m->m.methodName().equalsString("update")).findFirst().orElseThrow();
        assertTrue(calls(method).stream().noneMatch(c->c.name().equalsString("getBlockEntity") || c.name().equalsString("getChunkAt")));
    }

    @Test void migrationMustNotRequestItsOwnLoadingChunkThroughSetChanged() throws Exception {
        var method=read(LAMP).methods().stream().filter(m->m.methodName().equalsString("setLevel")).findFirst().orElseThrow();
        assertTrue(calls(method).stream().noneMatch(c->c.name().equalsString("setChanged")),
                "Migration must not ask the world for its unfinished chunk");
    }

    @Test void migrationSaveUsesOnlyTheProvidedChunk() throws Exception {
        var method=read(LAMP).methods().stream().filter(m->m.methodName().equalsString("finishMigrationSave")).findFirst().orElseThrow();
        var allowed=java.util.Set.of("getBlockEntities", "get", "markUnsaved");
        assertTrue(calls(method).stream().allMatch(c->allowed.contains(c.name().stringValue())));
        assertTrue(calls(method).stream().anyMatch(c->c.name().equalsString("markUnsaved")));
    }
}
