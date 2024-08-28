package com.godlike.common.networking;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

/**
 * Represents a packet sent from the client to the server to control the telekinesis ability.
 *
 * @param playerLookDirection The direction the player is looking in.
 */
public record TelekinesisControlsPacket(Vec3 playerLookDirection) {
}
