package cn.quit5700.buildingwand.client;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.*;
import static org.junit.jupiter.api.Assertions.*;

class SettingsBackgroundOrderTest {
    private ClassNode read(String name) throws Exception {
        try (var stream = getClass().getResourceAsStream("/" + name + ".class")) {
            assertNotNull(stream);
            var node = new ClassNode();
            new ClassReader(stream).accept(node, 0);
            return node;
        }
    }

    @Test void backgroundMustFinishBeforeSettingsContent() throws Exception {
        var screen = read("net/minecraft/client/gui/screens/Screen");
        var settings = read("cn/quit5700/buildingwand/client/WandSettingsScreen");
        var nativeRender = screen.methods.stream().filter(method -> method.name.equals("render")).findFirst().orElseThrow();
        boolean invokesBackground = false;
        for (var instruction : nativeRender.instructions)
            if (instruction instanceof MethodInsnNode call && call.name.equals("renderBackground"))
                invokesBackground = true;
        assertTrue(invokesBackground, "This regression applies to native Screen.render calling background.");
        var background = settings.methods.stream().filter(method -> method.name.equals("renderBackground")).findFirst().orElseThrow();
        for (var instruction : background.instructions)
            assertFalse(instruction instanceof MethodInsnNode, "The late native callback must not redraw background.");
        var render = settings.methods.stream().filter(method -> method.name.equals("render")).findFirst().orElseThrow();
        boolean backgroundComplete = false, contentFound = false;
        for (var instruction : render.instructions) {
            if (!(instruction instanceof MethodInsnNode call)) continue;
            if (call.owner.equals(screen.name) && call.name.equals("renderBackground"))
                backgroundComplete = true;
            if (call.owner.equals("net/minecraft/client/gui/GuiGraphics")) {
                assertTrue(backgroundComplete, "Settings graphics must follow the explicit native background.");
                contentFound = true;
                break;
            }
        }
        assertTrue(contentFound);
    }
}