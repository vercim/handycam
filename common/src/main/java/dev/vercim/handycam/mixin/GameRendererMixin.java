package dev.vercim.handycam.mixin;

import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Reserved for future FOV modifications via CameraOffset.fovDelta.
 * Roll is applied directly to Camera.rotation in CameraMixin.
 */
@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
}
