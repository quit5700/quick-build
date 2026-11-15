package cn.quit5700.buildingwand.client;

import cn.quit5700.buildingwand.blueprint.BlueprintFiles;
import cn.quit5700.buildingwand.network.BlueprintNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.io.ByteArrayOutputStream;
import java.nio.file.*;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class BlueprintSaveScreen extends cn.quit5700.buildingwand.aux.AuxScreens.Base {
    private static BlueprintSaveScreen active;
    private UUID token;
    private final Screen parent;
    private final String boxId;
    private final ByteArrayOutputStream incoming = new ByteArrayOutputStream();
    private EditBox name;
    private Button save;
    private boolean waiting, overwrite;
    private String pendingName;
    private int expectedTotal = -1;

    public BlueprintSaveScreen() {
        this(null, null);
    }

    public BlueprintSaveScreen(Screen parent, String boxId) {
        super(Component.translatable(boxId == null ? "blueprint.q5.save_title" : "aux.q5.save_box"));
        this.parent = parent;
        this.boxId = boxId;
    }

    @Override public void onClose() {
        minecraft.setScreen(parent);
    }

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(BlueprintNetworking.Chunk.ID, (payload, context) ->
            Minecraft.getInstance().execute(() -> {
                if (active != null) active.receive(payload);
            }));
    }

    @Override protected void init() {
        super.init();w=Math.min(width-16,300);h=Math.min(height-16,140);x=(width-w)/2;y=(height-h)/2;
        active = this;
        String previous = name == null ? "q5_structure" : name.getValue();
        name = addRenderableWidget(new EditBox(font, x + 12, y + 33, w-24, 20, Component.translatable("blueprint.q5.filename")));name.setTextShadow(false);
        name.setMaxLength(120); name.setValue(previous); name.setEditable(!waiting);
        name.setResponder(value -> { overwrite = false; if (!waiting && save != null) save.setMessage(Component.translatable("blueprint.q5.save")); });
        save = button(Component.translatable("blueprint.q5.save"),x+12,y+h-28,(w-30)/2,this::request);save.active=!waiting;
        button(Component.translatable("gui.cancel"),x+18+(w-30)/2,y+h-28,(w-30)/2,this::onClose);
        setInitialFocus(name);
    }

    private void request() {
        try {
            Path target = BlueprintFiles.target(minecraft.gameDirectory.toPath(), name.getValue());
            if (Files.exists(target, LinkOption.NOFOLLOW_LINKS) && !overwrite) {
                overwrite = true; status = Component.translatable("blueprint.q5.exists");
                save.setMessage(Component.translatable("blueprint.q5.overwrite")); return;
            }
            pendingName = name.getValue(); waiting = true; name.setEditable(false); save.active = false;
            status = Component.translatable("blueprint.q5.receiving");
            token = UUID.randomUUID();
            if (boxId == null) {
                ClientPlayNetworking.send(new BlueprintNetworking.Request(token));
            } else {
                var request = cn.quit5700.buildingwand.aux.AuxClient.id(boxId);
                request.putString("Token", token.toString());
                cn.quit5700.buildingwand.aux.AuxClient.send("save_box", request);
            }
        } catch (IllegalArgumentException ex) { status = Component.translatable("blueprint.q5.invalid_name"); }
        catch (Exception ex) { status = Component.translatable("blueprint.q5.failed"); }
    }

    private void receive(BlueprintNetworking.Chunk chunk) {
        if (!waiting || !token.equals(chunk.token())) return;
        if (chunk.total() < 0) { fail(); return; }
        if (expectedTotal == -1) expectedTotal = chunk.total();
        if (expectedTotal < 1 || expectedTotal > BlueprintNetworking.MAX_BYTES || chunk.total() != expectedTotal
                || chunk.offset() != incoming.size() || chunk.bytes().length == 0
                || (long) incoming.size() + chunk.bytes().length > expectedTotal) { fail(); return; }
        incoming.writeBytes(chunk.bytes());
        status = Component.translatable("blueprint.q5.progress", incoming.size(), expectedTotal);
        if (incoming.size() == expectedTotal) {
            byte[] data = incoming.toByteArray(); incoming.reset();
            var directory = minecraft.gameDirectory.toPath();
            CompletableFuture.supplyAsync(() -> {
                try { return BlueprintFiles.save(directory, pendingName, data, overwrite); }
                catch (Exception ex) { throw new java.util.concurrent.CompletionException(ex); }
            }).whenComplete((path, error) -> minecraft.execute(() -> {
                waiting = false;
                if (error != null) { fail(); return; }
                status = Component.translatable("blueprint.q5.saved", path.getFileName().toString());
                if (minecraft.player != null) minecraft.player.displayClientMessage(Component.translatable("blueprint.q5.saved_path", path.toString()),false);
            }));
        }
    }

    private void fail() {
        waiting = false; incoming.reset(); expectedTotal = -1;
        status = Component.translatable("blueprint.q5.failed"); name.setEditable(true); save.active = true;
    }

    @Override public void removed() { if (active == this) active = null; super.removed(); }

    @Override public boolean isPauseScreen() { return false; }
    @Override protected void drawContent(GuiGraphics graphics,int mx,int my,float tick) {
        text(graphics,Component.translatable("blueprint.q5.folder"),x+12,y+61,0xFF214E69);
    }
}
