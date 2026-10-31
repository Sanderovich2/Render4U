package dev.render4u.core.event;

import dev.render4u.core.event.impl.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NewEventsTest {

    @Test
    void testEventPacket() {
        EventPacket sendPacket = new EventPacket(null, true);
        assertTrue(sendPacket.isSend());
        assertFalse(sendPacket.isReceive());
        assertFalse(sendPacket.isCancelled());

        sendPacket.cancel();
        assertTrue(sendPacket.isCancelled());

        EventPacket receivePacket = new EventPacket(null, false);
        assertFalse(receivePacket.isSend());
        assertTrue(receivePacket.isReceive());
    }

    @Test
    void testEventInput() {
        EventInput input = new EventInput(true, false, true, false, true, false, true, 0.5f, 1.0f);
        assertTrue(input.isForward());
        assertFalse(input.isBackward());
        assertTrue(input.isLeft());
        assertFalse(input.isRight());
        assertTrue(input.isJump());
        assertFalse(input.isSneak());
        assertTrue(input.isSprint());
        assertEquals(0.5f, input.getStrafeImpulse());
        assertEquals(1.0f, input.getForwardImpulse());

        input.setForward(false);
        input.setBackward(true);
        input.setStrafeImpulse(-0.5f);
        assertFalse(input.isForward());
        assertTrue(input.isBackward());
        assertEquals(-0.5f, input.getStrafeImpulse());
    }

    @Test
    void testEventMouseButton() {
        EventMouseButton buttonPress = new EventMouseButton(0, 1, 100f, 200f);
        assertEquals(0, buttonPress.getButton());
        assertEquals(1, buttonPress.getAction());
        assertEquals(100f, buttonPress.getMouseX());
        assertEquals(200f, buttonPress.getMouseY());
        assertTrue(buttonPress.isPressed());
        assertFalse(buttonPress.isReleased());

        EventMouseButton buttonRelease = new EventMouseButton(1, 0, 50f, 60f);
        assertTrue(buttonRelease.isReleased());
        assertFalse(buttonRelease.isPressed());
    }

    @Test
    void testEventMouseScroll() {
        EventMouseScroll scroll = new EventMouseScroll(0.0, 1.0);
        assertEquals(0.0, scroll.getScrollX());
        assertEquals(1.0, scroll.getScrollY());
        assertFalse(scroll.isCancelled());
        scroll.cancel();
        assertTrue(scroll.isCancelled());
    }

    @Test
    void testEventChar() {
        EventChar charEvent = new EventChar("a");
        assertEquals("a", charEvent.getCharacter());
        assertFalse(charEvent.isCancelled());
        charEvent.cancel();
        assertTrue(charEvent.isCancelled());
    }
}
