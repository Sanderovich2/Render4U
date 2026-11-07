package dev.render4u.render.drag;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.InputConstants;
import dev.render4u.config.ConfigManager;
import dev.render4u.render.display.DisplayUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import java.util.*;

public final class DragManager {

    public static final DragManager INSTANCE = new DragManager();

    private static final float THRESHOLD = 3.0f;

    public enum Context {
        GUI,
        HUD
    }

    public enum State {
        IDLE,
        ARMED,
        DRAGGING
    }

    public record Handle(float x, float y, float w, float h, float z) {
        public boolean contains(float px, float py) {
            return px >= x && px <= x + w && py >= y && py <= y + h;
        }
    }

    private static final class Registration {
        final List<Handle> handles = new ArrayList<>();
        boolean hasSelf;
        float baseX;
        float baseY;
        float selfW;
        float selfH;
    }

    private final Map<String, float[]> offsets = new HashMap<>();
    private final Map<Context, Map<String, Registration>> building = new EnumMap<>(Context.class);
    private final Map<Context, Map<String, Registration>> committed = new EnumMap<>(Context.class);
    private final Map<Context, Long> committedAt = new EnumMap<>(Context.class);

    private long frame;
    private Context currentContext = Context.GUI;

    private State state = State.IDLE;
    private boolean leftMouseDown;
    private String activeId;
    private Context activeContext;
    private float pressX;
    private float pressY;
    private float grabDx;
    private float grabDy;

    private DragManager() {
        for (Context c : Context.values()) {
            building.put(c, new HashMap<>());
            committed.put(c, new HashMap<>());
        }
    }

    public void beginFrame(Context context) {
        if (context == Context.GUI) {
            frame++;
        }
        currentContext = context;
        committed.put(context, new HashMap<>(building.getOrDefault(context, Collections.emptyMap())));
        committedAt.put(context, frame);
        building.put(context, new HashMap<>());
    }

    public void register(String id, float x, float y, float w, float h, float z, boolean isTagged) {
        register(id, x, y, w, h, w, h, z, isTagged);
    }

    public void register(String id, float x, float y, float w, float h, float boundW, float boundH, float z, boolean isTagged) {
        if (currentContext == null || id == null || w <= 0.0f || h <= 0.0f) {
            return;
        }
        Map<String, Registration> map = building.computeIfAbsent(currentContext, c -> new HashMap<>());
        Registration reg = map.computeIfAbsent(id, k -> new Registration());

        reg.handles.add(new Handle(x, y, w, h, z));
        if (isTagged) {
            reg.hasSelf = true;
            reg.baseX = x - offsetX(id);
            reg.baseY = y - offsetY(id);
            reg.selfW = boundW;
            reg.selfH = boundH;
        }
    }

    public void registerHandle(String id, float x, float y, float w, float h) {
        register(id, x, y, w, h, 0.0f, false);
    }

    public boolean onPress(int button, int action) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.mouseHandler == null || mc.mouseHandler.isMouseGrabbed()) {
            return false;
        }

        boolean isLeft = (button == InputConstants.MOUSE_BUTTON_LEFT || button == 0);
        boolean isMiddle = (button == InputConstants.MOUSE_BUTTON_MIDDLE || button == 2);
        boolean isPress = (action == InputConstants.PRESS || action == 1);
        boolean isRelease = (action == InputConstants.RELEASE || action == 0);

        if (isMiddle && isPress) {
            Context ctx = Context.GUI;
            String hit = hitTest(ctx, mouseX(ctx), mouseY(ctx));
            Screen screen = mc.gui != null ? mc.gui.screen() : null;
            if (hit == null && mc.level != null && screen != null) {
                ctx = Context.HUD;
                hit = hitTest(ctx, mouseX(ctx), mouseY(ctx));
            }
            if (hit != null) {
                reset(hit);
                ConfigManager.save();
                return true;
            }
            return false;
        }

        if (!isLeft) {
            return false;
        }

        if (isRelease) {
            leftMouseDown = false;
            boolean wasActive = (state != State.IDLE);
            stop();
            if (wasActive) {
                ConfigManager.save();
            }
            return wasActive;
        }

        if (!isPress) {
            return false;
        }

        leftMouseDown = true;

        Context context = Context.GUI;
        float mx = mouseX(context);
        float my = mouseY(context);
        String hit = hitTest(context, mx, my);

        Screen screen = mc.gui != null ? mc.gui.screen() : null;
        if (hit == null && mc.level != null && screen != null) {
            context = Context.HUD;
            mx = mouseX(context);
            my = mouseY(context);
            hit = hitTest(context, mx, my);
        }

        if (hit == null) {
            return false;
        }

        state = State.ARMED;
        activeId = hit;
        activeContext = context;
        pressX = mx;
        pressY = my;
        grabDx = mx - offsetX(hit);
        grabDy = my - offsetY(hit);
        return true;
    }

    public void update() {
        if (!leftMouseDown || state == State.IDLE) {
            if (state != State.IDLE) {
                stop();
                ConfigManager.save();
            }
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.getWindow() == null) {
            stop();
            return;
        }

        Screen screen = mc.gui != null ? mc.gui.screen() : null;
        if (screen == null && (mc.mouseHandler != null && mc.mouseHandler.isMouseGrabbed())) {
            stop();
            ConfigManager.save();
            return;
        }

        Map<String, Registration> regs = committed.get(activeContext);
        Registration reg = regs != null ? regs.get(activeId) : null;
        if (reg == null) {
            Map<String, Registration> buildMap = building.get(activeContext);
            reg = buildMap != null ? buildMap.get(activeId) : null;
        }

        float mx = mouseX(activeContext);
        float my = mouseY(activeContext);

        if (state == State.ARMED) {
            if (Math.hypot(mx - pressX, my - pressY) <= THRESHOLD) {
                return;
            }
            state = State.DRAGGING;
        }

        float ox = mx - grabDx;
        float oy = my - grabDy;

        if (reg != null && reg.hasSelf) {
            float vw = viewportWidth(activeContext);
            float vh = viewportHeight(activeContext);
            ox = Math.max(-reg.baseX, Math.min(vw - reg.selfW - reg.baseX, ox));
            oy = Math.max(-reg.baseY, Math.min(vh - reg.selfH - reg.baseY, oy));
        }

        setOffset(activeId, ox, oy);
    }

    public boolean isHovered(String id) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.gui == null || mc.gui.screen() == null || mc.mouseHandler == null || mc.mouseHandler.isMouseGrabbed()) {
            return false;
        }
        if (id == null) {
            return false;
        }
        if (state != State.IDLE && id.equals(activeId)) {
            return true;
        }
        Context ctx = Context.HUD;
        float mx = mouseX(ctx);
        float my = mouseY(ctx);
        return id.equals(hitTest(ctx, mx, my));
    }

    public boolean isDragging(String id) {
        return state == State.DRAGGING && id != null && id.equals(activeId);
    }

    public float offsetX(String id) {
        float[] off = offsets.get(id);
        return off != null ? off[0] : 0.0f;
    }

    public float offsetY(String id) {
        float[] off = offsets.get(id);
        return off != null ? off[1] : 0.0f;
    }

    public void setOffset(String id, float x, float y) {
        offsets.put(id, new float[]{x, y});
    }

    public void reset(String id) {
        offsets.remove(id);
    }

    public void resetAll() {
        offsets.clear();
    }

    public boolean isDragging() {
        return state == State.DRAGGING;
    }

    public boolean isArmed() {
        return state == State.ARMED;
    }

    public String getActiveId() {
        return activeId;
    }

    public Context getActiveContext() {
        return activeContext;
    }

    public void stop() {
        state = State.IDLE;
        activeId = null;
        activeContext = null;
    }

    public String hitTest(Context context, float mx, float my) {
        Map<String, Registration> regs = committed.get(context);
        if (regs == null || regs.isEmpty()) {
            regs = building.get(context);
        }
        if (regs == null) return null;

        String best = null;
        float bestZ = Float.NEGATIVE_INFINITY;
        for (Map.Entry<String, Registration> entry : regs.entrySet()) {
            for (Handle handle : entry.getValue().handles) {
                if (handle.z() >= bestZ && handle.contains(mx, my)) {
                    best = entry.getKey();
                    bestZ = handle.z();
                }
            }
        }
        return best;
    }

    public float mouseX(Context context) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.getWindow() == null || mc.mouseHandler == null) return 0.0f;
        if (context == Context.GUI) {
            return (float) net.minecraft.client.MouseHandler.getScaledXPos(mc.getWindow(), mc.mouseHandler.xpos());
        }
        int screenW = mc.getWindow().getScreenWidth();
        if (screenW <= 0) return 0.0f;
        return (float) (mc.mouseHandler.xpos() / (double) screenW * (double) DisplayUtil.width());
    }

    public float mouseY(Context context) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.getWindow() == null || mc.mouseHandler == null) return 0.0f;
        if (context == Context.GUI) {
            return (float) net.minecraft.client.MouseHandler.getScaledYPos(mc.getWindow(), mc.mouseHandler.ypos());
        }
        int screenH = mc.getWindow().getScreenHeight();
        if (screenH <= 0) return 0.0f;
        return (float) (mc.mouseHandler.ypos() / (double) screenH * (double) DisplayUtil.height());
    }

    public float viewportWidth(Context context) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.getWindow() == null) return 1920.0f;
        return context == Context.GUI ? (float) mc.getWindow().getGuiScaledWidth() : DisplayUtil.width();
    }

    public float viewportHeight(Context context) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.getWindow() == null) return 1080.0f;
        return context == Context.GUI ? (float) mc.getWindow().getGuiScaledHeight() : DisplayUtil.height();
    }

    public JsonObject toJson() {
        JsonObject obj = new JsonObject();
        for (Map.Entry<String, float[]> entry : offsets.entrySet()) {
            JsonObject pos = new JsonObject();
            pos.addProperty("x", entry.getValue()[0]);
            pos.addProperty("y", entry.getValue()[1]);
            obj.add(entry.getKey(), pos);
        }
        return obj;
    }

    public void fromJson(JsonObject obj) {
        if (obj == null) return;
        for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
            if (entry.getValue().isJsonObject()) {
                JsonObject pos = entry.getValue().getAsJsonObject();
                if (pos.has("x") && pos.has("y")) {
                    float x = pos.get("x").getAsFloat();
                    float y = pos.get("y").getAsFloat();
                    offsets.put(entry.getKey(), new float[]{x, y});
                }
            }
        }
    }
}
