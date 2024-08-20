package com.godlike.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.godlike.keybind.KeybindHandlerKt.handleModInputEvents;

@Mixin(net.minecraft.client.Minecraft.class)
public class MinecraftClientMixin {
    @Inject(method = "handleKeybinds", at = @At("HEAD"))
    private void handleInputEvents(CallbackInfo info) {
        handleModInputEvents();
    }
}
