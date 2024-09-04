package com.godlike.common.networking;

import net.minecraft.world.phys.Vec3;

/**
 * Represents a packet sent from the client to the server to control the telekinesis ability.
 *
 * @param playerLookDirection The direction the player is looking in.
 * @param pointerDistanceDelta The distance the player's telekinesis pointer should be pushed/pulled relative to the player.
 */
public record TelekinesisControlsPacket(Vec3 playerLookDirection, Double pointerDistanceDelta) {
}
