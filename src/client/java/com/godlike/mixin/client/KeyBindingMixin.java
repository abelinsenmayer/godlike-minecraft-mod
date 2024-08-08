package com.godlike.mixin.client;

import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(KeyBinding.class)
public interface KeyBindingMixin {
    @Accessor("boundKey")
    public abstract InputUtil.Key getBoundKey();

    @Accessor("KEY_TO_BINDINGS")
    public static Map<InputUtil.Key, KeyBinding> getKeyToBindings() {
        throw new AssertionError("This mixin should have been transformed at runtime.");
    }
}
