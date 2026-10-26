package dev.render4u.util.color;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GradientTest {

    private static final int BLACK = 0xFF000000;
    private static final int WHITE = 0xFFFFFFFF;

    @Test
    void samplesEndpoints() {
        Gradient gradient = Gradient.of(BLACK, WHITE);
        assertEquals(BLACK, gradient.sample(0.0F));
        assertEquals(WHITE, gradient.sample(1.0F));
    }

    @Test
    void samplesMidpoint() {
        Gradient gradient = Gradient.of(BLACK, WHITE);
        assertEquals(0xFF808080, gradient.sample(0.5F));
    }

    @Test
    void clampsOutsideRange() {
        Gradient gradient = Gradient.of(BLACK, WHITE);
        assertEquals(BLACK, gradient.sample(-1.0F));
        assertEquals(WHITE, gradient.sample(2.0F));
    }

    @Test
    void singleColorGradientIsConstant() {
        Gradient gradient = Gradient.of(BLACK);
        assertEquals(BLACK, gradient.sample(0.0F));
        assertEquals(BLACK, gradient.sample(1.0F));
    }

    @Test
    void repeatModeWrapsProgress() {
        Gradient gradient = Gradient.of(BLACK, WHITE).mode(Gradient.Mode.REPEAT);
        assertEquals(BLACK, gradient.sample(0.0F));
        assertEquals(BLACK, gradient.sample(1.0F));
        assertEquals(0xFF808080, gradient.sample(1.5F));
        assertEquals(BLACK, gradient.sample(2.0F));
    }

    @Test
    void mirrorModeReflectsProgress() {
        Gradient gradient = Gradient.of(BLACK, WHITE).mode(Gradient.Mode.MIRROR);
        assertEquals(0xFF808080, gradient.sample(1.5F));
        assertEquals(BLACK, gradient.sample(2.0F));
    }

    @Test
    void customStopsShiftColors() {
        Gradient gradient = Gradient.stops(new float[]{0.0F, 0.8F, 1.0F}, new int[]{BLACK, WHITE, BLACK});
        assertEquals(0xFF808080, gradient.sample(0.4F));
        assertEquals(BLACK, gradient.sample(1.0F));
    }

    @Test
    void horizontalAngleSamplesLeftToRight() {
        Gradient gradient = Gradient.of(BLACK, WHITE).angle(0.0F);
        assertEquals(BLACK, gradient.sample(0.0F, 0.5F));
        assertEquals(WHITE, gradient.sample(1.0F, 0.5F));
    }

    @Test
    void verticalAngleSamplesTopToBottom() {
        Gradient gradient = Gradient.of(BLACK, WHITE).angle(90.0F);
        assertEquals(BLACK, gradient.sample(0.5F, 0.0F));
        assertEquals(WHITE, gradient.sample(0.5F, 1.0F));
    }

    @Test
    void rejectsEmptyColors() {
        assertThrows(IllegalArgumentException.class, () -> Gradient.stops(new float[]{}, new int[]{}));
    }

    @Test
    void rejectsMismatchedStops() {
        assertThrows(IllegalArgumentException.class, () -> Gradient.stops(new float[]{0.0F, 1.0F}, new int[]{BLACK}));
    }

    @Test
    void rejectsDescendingStops() {
        assertThrows(IllegalArgumentException.class, () -> Gradient.stops(new float[]{1.0F, 0.0F}, new int[]{BLACK, WHITE}));
    }
}
