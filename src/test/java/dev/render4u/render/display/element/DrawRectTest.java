package dev.render4u.render.display.element;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DrawRectTest {

    @Test
    public void testUserSpecificationPanelCreation() {
        DrawRect panel = new DrawRect();
        panel.size(256, 128);
        panel.bg = 0x66A1A1A1;
        panel.blur = 3;

        DrawOutline outline = new DrawOutline();
        outline.Right = 0;
        outline.Left = 0;
        outline.Up = 0;
        outline.Down = 5;
        outline.color = 0xFFFFFF00;

        panel.outline = outline;

        DrawRounding round = new DrawRounding();
        round.RightUp = 5;
        round.LeftUp = 5;
        round.LeftDown = 25;
        round.RightDown = 25;

        panel.rounding = round;
        assertEquals(256.0F, panel.width);
        assertEquals(128.0F, panel.height);
        assertEquals(0x66A1A1A1, panel.bg);
        assertEquals(3, panel.blur);

        assertNotNull(panel.outline);
        assertEquals(0.0F, panel.outline.Right);
        assertEquals(0.0F, panel.outline.Left);
        assertEquals(0.0F, panel.outline.Up);
        assertEquals(5.0F, panel.outline.Down);
        assertEquals(0xFFFFFF00, panel.outline.color);
        assertTrue(panel.outline.isEnabled());
        assertFalse(panel.outline.isUniform());

        assertNotNull(panel.rounding);
        assertEquals(5.0F, panel.rounding.LeftUp);
        assertEquals(5.0F, panel.rounding.RightUp);
        assertEquals(25.0F, panel.rounding.LeftDown);
        assertEquals(25.0F, panel.rounding.RightDown);
    }

    @Test
    public void testShortNamesAndFluentBuilder() {
        DrawRect panel = DrawRect.create();
        panel.size(200, 100)
                .bg(0xFF112233)
                .blur(2, 0x33000000)
                .outline(new DrawOutline().all(1.5F).color(0xFF445566))
                .rounding(new DrawRounding(8.0F))
                .shadow(new DrawShadow(10.0F, 0.0F, 4.0F, 0x66000000));

        assertEquals(200.0F, panel.width);
        assertEquals(100.0F, panel.height);
        assertEquals(0xFF112233, panel.bg);
        assertEquals(2, panel.blur);
        assertEquals(0x33000000, panel.blurTint);

        assertTrue(panel.outline.isUniform());
        assertEquals(1.5F, panel.outline.Up);
        assertEquals(1.5F, panel.outline.Down);
        assertEquals(1.5F, panel.outline.Left);
        assertEquals(1.5F, panel.outline.Right);

        assertTrue(panel.rounding.isUniform());
        assertEquals(8.0F, panel.rounding.LeftUp);
        assertEquals(8.0F, panel.rounding.RightDown);

        assertNotNull(panel.shadow);
        assertEquals(10.0F, panel.shadow.blur);
        assertEquals(4.0F, panel.shadow.offsetY);
    }

    @Test
    public void testDrawRectFluentBounds() {
        DrawRect builder = DrawRect.create();
        builder.bounds(10, 20, 150, 80)
                .bgGradientH(0xFF000000, 0xFFFFFFFF)
                .rounding(4, 6, 8, 10);

        assertEquals(10.0F, builder.x);
        assertEquals(20.0F, builder.y);
        assertEquals(150.0F, builder.width);
        assertEquals(80.0F, builder.height);
        assertTrue(builder.hasBg);
        assertEquals(4.0F, builder.rounding.LeftUp);
        assertEquals(6.0F, builder.rounding.RightUp);
        assertEquals(8.0F, builder.rounding.RightDown);
        assertEquals(10.0F, builder.rounding.LeftDown);
    }
}
