package dev.render4u.mixin;

import dev.render4u.Render4U;
import dev.render4u.core.event.impl.EventPacket;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Connection.class)
public abstract class ConnectionMixin {
    @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;)V", at = @At("HEAD"), cancellable = true)
    private void render4u$onSendPacket(Packet<?> packet, CallbackInfo ci) {
        if (Render4U.getEventBus() != null) {
            EventPacket event = new EventPacket(packet, true);
            Render4U.getEventBus().post(event);
            if (event.isCancelled()) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/protocol/Packet;)V", at = @At("HEAD"), cancellable = true)
    private void render4u$onReceivePacket(ChannelHandlerContext context, Packet<?> packet, CallbackInfo ci) {
        if (Render4U.getEventBus() != null) {
            EventPacket event = new EventPacket(packet, false);
            Render4U.getEventBus().post(event);
            if (event.isCancelled()) {
                ci.cancel();
            }
        }
    }
}
