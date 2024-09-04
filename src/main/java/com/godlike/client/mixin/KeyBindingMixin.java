package com.godlike.client.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(KeyMapping.class)
public interface KeyBindingMixin {
    @Accessor("key")
    public abstract InputConstants.Key getBoundKey();

    @Accessor("MAP")
    public static Map<InputConstants.Key, KeyMapping> getKeyToBindings() {
        throw new AssertionError("This mixin should have been transformed at runtime.");
    }
}
