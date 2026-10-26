package dev.render4u.render.display.shape;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RectBuilderTest {

    private static final class TestBuilder extends RectBuilder<TestBuilder> {
    }

    @Test
    void fluentSettersReturnSameInstance() {
        TestBuilder builder = new TestBuilder();
        assertEquals(builder, builder.position(1.0F, 2.0F));
        assertEquals(builder, builder.size(3.0F, 4.0F));
        assertEquals(builder, builder.radius(5.0F));
        assertEquals(builder, builder.color(0xFF000000));
        assertEquals(builder, builder.alpha(0.5F));
    }

    @Test
    void boundsSetsPositionAndSize() {
        TestBuilder builder = new TestBuilder();
        builder.bounds(1.0F, 2.0F, 3.0F, 4.0F);
        assertEquals(3.0F, builder.width);
        assertEquals(4.0F, builder.height);
    }

    @Test
    void alphaClampsToOne() {
        TestBuilder builder = new TestBuilder();
        builder.alpha(5.0F);
        assertEquals(1.0F, builder.alpha);
        builder.alpha(-1.0F);
        assertEquals(0.0F, builder.alpha);
    }

    @Test
    void applyAlphaScalesColorAlpha() {
        TestBuilder builder = new TestBuilder();
        assertEquals(0xFF000000, builder.applyAlpha(0xFF000000));
        builder.alpha(0.5F);
        assertEquals(0x80000000, builder.applyAlpha(0xFF000000));
    }

    @Test
    void packRadiusClampsToBitDepth() {
        assertEquals(20, RectBuilder.packRadius(100.0F, 10.0F, 2.0F, 6));
        assertEquals(15, RectBuilder.packRadius(10.0F, 10.0F, 2.0F, 4));
        assertEquals(255, RectBuilder.packRadius(500.0F, 1000.0F, 2.0F, 8));
        assertEquals(0, RectBuilder.packRadius(-5.0F, 10.0F, 2.0F, 8));
    }
}
