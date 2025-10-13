package com.godlike.common.networking;

/**
 * Packet sent to the server to indicate that the player should stop or start precise placement.
 * @param starting true if the player is starting precise placement, false if they are stopping it.
 */
public record PlacementPacket(boolean starting) {
}
