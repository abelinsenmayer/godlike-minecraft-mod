package com.godlike.networking

import com.godlike.Godlike.logger
import com.godlike.MOD_ID
import io.wispforest.owo.network.OwoNetChannel
import net.minecraft.util.Identifier
import org.apache.logging.log4j.core.jmx.Server

object ModNetworking {
    @JvmField
    val CHANNEL = OwoNetChannel.create(Identifier.of(MOD_ID, "main"))

    fun register() {
        logger.info("Registering network channel")
        CHANNEL.registerServerbound(ServerBoundPacket::class.java) { packet, ctx ->
            logger.info("Received message on server: ${packet.message}")
        }
    }
}