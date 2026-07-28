package dev.vercim.handycam.mixin;

import dev.vercim.handycam.camera.CameraShakeSystem;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {

    @Inject(method = "handleExplosion", at = @At("HEAD"))
    private void handycam$onExplosion(ClientboundExplodePacket packet, CallbackInfo ci) {
        CameraShakeSystem.onExplosion(packet.getX(), packet.getY(), packet.getZ());
    }
}
