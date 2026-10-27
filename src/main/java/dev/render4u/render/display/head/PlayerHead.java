package dev.render4u.render.display.head;

import com.mojang.authlib.GameProfile;
import dev.render4u.render.display.batch.DisplayBatcher;
import dev.render4u.render.display.shape.RoundedRect;
import dev.render4u.util.color.Color;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerSkin;

public final class PlayerHead {
    private static final float MAX_RADIUS = 63.5F;

    private float x;
    private float y;
    private float width;
    private float height;
    private float radiusTopLeft;
    private float radiusTopRight;
    private float radiusBottomRight;
    private float radiusBottomLeft;
    private boolean circle;
    private int colorTopLeft = -1;
    private int colorTopRight = -1;
    private int colorBottomRight = -1;
    private int colorBottomLeft = -1;
    private float alpha = 1.0F;
    private boolean hat = true;
    private boolean smooth;
    private int overlay;
    private float overlayAmount;
    private Supplier<Identifier> source = () -> HeadSkins.lookup(Minecraft.getInstance().getGameProfile()).get().body().texturePath();

    private PlayerHead() {
    }

    public static PlayerHead create() {
        return new PlayerHead();
    }

    public static PlayerHead of(float x, float y, float size) {
        return new PlayerHead().position(x, y).size(size);
    }

    public static PlayerHead of(float x, float y, float width, float height) {
        return new PlayerHead().bounds(x, y, width, height);
    }

    public PlayerHead position(float x, float y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public PlayerHead size(float size) {
        return size(size, size);
    }

    public PlayerHead size(float width, float height) {
        this.width = width;
        this.height = height;
        return this;
    }

    public PlayerHead bounds(float x, float y, float width, float height) {
        return position(x, y).size(width, height);
    }

    public PlayerHead skin(PlayerSkin skin) {
        this.source = () -> skin.body().texturePath();
        return this;
    }

    public PlayerHead skin(Supplier<PlayerSkin> skin) {
        this.source = () -> skin.get().body().texturePath();
        return this;
    }

    public PlayerHead profile(GameProfile profile) {
        return skin(HeadSkins.lookup(profile));
    }

    public PlayerHead player(AbstractClientPlayer player) {
        return skin(player::getSkin);
    }

    public PlayerHead info(PlayerInfo info) {
        return skin(info::getSkin);
    }

    public PlayerHead self() {
        return profile(Minecraft.getInstance().getGameProfile());
    }

    public PlayerHead texture(Identifier texture) {
        this.source = () -> texture;
        return this;
    }

    public PlayerHead hat(boolean hat) {
        this.hat = hat;
        return this;
    }

    public PlayerHead smooth(boolean smooth) {
        this.smooth = smooth;
        return this;
    }

    public PlayerHead circle() {
        this.circle = true;
        return this;
    }

    public PlayerHead radius(float radius) {
        return radius(radius, radius, radius, radius);
    }

    public PlayerHead radius(float topLeft, float topRight, float bottomRight, float bottomLeft) {
        this.circle = false;
        this.radiusTopLeft = topLeft;
        this.radiusTopRight = topRight;
        this.radiusBottomRight = bottomRight;
        this.radiusBottomLeft = bottomLeft;
        return this;
    }

    public PlayerHead radiusTop(float radius) {
        this.circle = false;
        this.radiusTopLeft = radius;
        this.radiusTopRight = radius;
        return this;
    }

    public PlayerHead radiusBottom(float radius) {
        this.circle = false;
        this.radiusBottomLeft = radius;
        this.radiusBottomRight = radius;
        return this;
    }

    public PlayerHead radiusLeft(float radius) {
        this.circle = false;
        this.radiusTopLeft = radius;
        this.radiusBottomLeft = radius;
        return this;
    }

    public PlayerHead radiusRight(float radius) {
        this.circle = false;
        this.radiusTopRight = radius;
        this.radiusBottomRight = radius;
        return this;
    }

    public PlayerHead color(int argb) {
        return corners(argb, argb, argb, argb);
    }

    public PlayerHead verticalGradient(int top, int bottom) {
        return corners(top, top, bottom, bottom);
    }

    public PlayerHead horizontalGradient(int left, int right) {
        return corners(left, right, right, left);
    }

    public PlayerHead corners(int topLeft, int topRight, int bottomRight, int bottomLeft) {
        this.colorTopLeft = topLeft;
        this.colorTopRight = topRight;
        this.colorBottomRight = bottomRight;
        this.colorBottomLeft = bottomLeft;
        return this;
    }

    public PlayerHead color(Color color) {
        return color(color.argb());
    }

    public PlayerHead verticalGradient(Color top, Color bottom) {
        return verticalGradient(top.argb(), bottom.argb());
    }

    public PlayerHead horizontalGradient(Color left, Color right) {
        return horizontalGradient(left.argb(), right.argb());
    }

    public PlayerHead corners(Color topLeft, Color topRight, Color bottomRight, Color bottomLeft) {
        return corners(topLeft.argb(), topRight.argb(), bottomRight.argb(), bottomLeft.argb());
    }

    public PlayerHead mix(int argb, float amount) {
        this.overlay = argb;
        this.overlayAmount = Math.clamp(amount, 0.0F, 1.0F);
        return this;
    }

    public PlayerHead mix(Color color, float amount) {
        return mix(color.argb(), amount);
    }

    public PlayerHead alpha(float alpha) {
        this.alpha = Math.clamp(alpha, 0.0F, 1.0F);
        return this;
    }

    public PlayerHeadRenderState build(GuiGraphicsExtractor graphics) {
        float maxRadius = Math.min(Math.min(width, height) * 0.5F, MAX_RADIUS);
        int topLeft = packRadius(circle ? maxRadius : radiusTopLeft, maxRadius);
        int topRight = packRadius(circle ? maxRadius : radiusTopRight, maxRadius);
        int bottomRight = packRadius(circle ? maxRadius : radiusBottomRight, maxRadius);
        int bottomLeft = packRadius(circle ? maxRadius : radiusBottomLeft, maxRadius);
        return new PlayerHeadRenderState(
                DisplayBatcher.pose(graphics), x, y, width, height,
                topLeft | topRight << 7 | (hat ? 1 : 0) << 14, bottomLeft | bottomRight << 7,
                applyAlpha(colorTopLeft), applyAlpha(colorTopRight), applyAlpha(colorBottomRight), applyAlpha(colorBottomLeft),
                source.get(), smooth, graphics.scissorStack.peek()
        );
    }

    public void render(GuiGraphicsExtractor graphics) {
        if (width <= 0.0F || height <= 0.0F || alpha <= 0.0F) {
            return;
        }
        DisplayBatcher.submit(graphics, build(graphics));
        if (overlayAmount > 0.0F && (overlay >>> 24) > 0) {
            float maxRadius = Math.min(Math.min(width, height) * 0.5F, MAX_RADIUS);
            RoundedRect.of(x, y, width, height)
                    .radius(circle ? maxRadius : radiusTopLeft, circle ? maxRadius : radiusTopRight, circle ? maxRadius : radiusBottomRight, circle ? maxRadius : radiusBottomLeft)
                    .color(overlay)
                    .alpha(alpha * overlayAmount)
                    .render(graphics);
        }
    }

    private int applyAlpha(int color) {
        int a = Math.round((color >>> 24) * alpha);
        return a << 24 | color & 0xFFFFFF;
    }

    private static int packRadius(float radius, float max) {
        return Math.clamp(Math.round(Math.clamp(radius, 0.0F, max) * 2.0F), 0, 127);
    }
}
