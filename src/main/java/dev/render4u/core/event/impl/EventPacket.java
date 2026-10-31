package dev.render4u.core.event.impl;

import dev.render4u.core.event.Event;
import net.minecraft.network.protocol.Packet;

public class EventPacket extends Event {
    private final Packet<?> packet;
    private final boolean isSend;

    public EventPacket(Packet<?> packet, boolean isSend) {
        this.packet = packet;
        this.isSend = isSend;
    }

    @SuppressWarnings("unchecked")
    public <T extends Packet<?>> T getPacket() {
        return (T) packet;
    }

    public boolean isSend() {
        return isSend;
    }

    public boolean isReceive() {
        return !isSend;
    }
}
