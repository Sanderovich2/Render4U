package dev.render4u.render.display.shape;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CircleTest {

    @Test
    void encodesAngleRange() {
        assertEquals(0, Circle.encodeAngle(0.0F));
        assertEquals(1023, Circle.encodeAngle(360.0F));
        assertEquals(512, Circle.encodeAngle(180.0F));
        assertEquals(256, Circle.encodeAngle(90.0F));
    }

    @Test
    void encodeAngleClampsOutOfRange() {
        assertEquals(0, Circle.encodeAngle(-45.0F));
        assertEquals(1023, Circle.encodeAngle(720.0F));
    }
}
