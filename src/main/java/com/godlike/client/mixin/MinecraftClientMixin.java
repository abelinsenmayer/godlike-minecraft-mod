package com.godlike.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.godlike.client.keybind.KeybindHandlerKt.handleModInputEvents;

@Mixin(net.minecraft.client.Minecraft.class)
public class MinecraftClientMixin {
    @Inject(method = "handleKeybinds", at = @At("HEAD"))
    private void handleInputEvents(CallbackInfo info) {
        handleModInputEvents();
    }
}
