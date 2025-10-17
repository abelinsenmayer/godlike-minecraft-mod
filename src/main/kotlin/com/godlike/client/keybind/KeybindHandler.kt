package com.godlike.client.keybind

import com.godlike.client.keybind.ModKeybinds.CHANGE_DFS_DISTANCE_TYPE
import com.godlike.client.keybind.ModKeybinds.LAUNCH_TK
import com.godlike.client.keybind.ModKeybinds.PICK_TO_TK
import com.godlike.client.keybind.ModKeybinds.PLACE_TK
import com.godlike.client.keybind.ModKeybinds.POINTER_PULL
import com.godlike.client.keybind.ModKeybinds.POINTER_PUSH
import com.godlike.client.keybind.ModKeybinds.ROTATE_TK_DOWN
import com.godlike.client.keybind.ModKeybinds.ROTATE_TK_LEFT
import com.godlike.client.keybind.ModKeybinds.ROTATE_TK_RIGHT
import com.godlike.client.keybind.ModKeybinds.ROTATE_TK_UP
import com.godlike.client.keybind.ModKeybinds.SET_TK_HOVERING
import com.godlike.client.keybind.ModKeybinds.TOGGLE_PLACEMENT_MODE
import com.godlike.client.keybind.ModKeybinds.TOGGLE_TK_MODE
import com.godlike.client.util.*
import com.godlike.common.Godlike
import com.godlike.common.components.Mode
import com.godlike.common.components.getMode
import com.godlike.common.components.selection
import com.godlike.common.components.telekinesis
import com.godlike.common.items.TkFocusTier
import com.godlike.common.networking.*
import com.godlike.common.networking.ModNetworking.CHANNEL
import com.godlike.common.telekinesis.EntityTkTarget
import com.godlike.common.telekinesis.LAUNCH_POINTER_DISTANCE
import com.godlike.common.telekinesis.ShipTkTarget
import com.godlike.common.telekinesis.getPointerAtDistance
import com.godlike.common.telekinesis.placement.Direction2D
import com.godlike.common.telekinesis.placement.rotateRelativeToFacing
import net.minecraft.client.Minecraft
import net.minecraft.core.Direction
import net.minecraft.world.phys.Vec3

const val POINTER_DELTA_INCREMENT = 0.5

/**
 * Called every client tick to handle keybinds related to telekinesis.
 * Constructs a [TelekinesisControlsPacket] based on key inputs and send it to the server.
 */
fun sendTelekinesisTick() {
    val playerLookDirection = Minecraft.getInstance().cameraEntity!!.lookAngle

    var pointerDistanceDelta = 0.0
    val mode = Minecraft.getInstance().player!!.getMode()
    if (mode == Mode.TELEKINESIS || mode == Mode.PLACEMENT) {
        // Only allow the player to change the pointer distance if they are in telekinesis mode;
        // we use these keybinds for other things in other modes.
        if (POINTER_PULL.isDown) {
            while (POINTER_PULL.consumeClick()) {
                // NOOP -- just make sure we aren't accumulating any clicks from holding down the key
            }
            pointerDistanceDelta -= POINTER_DELTA_INCREMENT
        }
        if (POINTER_PUSH.isDown) {
            while (POINTER_PUSH.consumeClick()) {
                // NOOP -- just make sure we aren't accumulating any clicks from holding down the key
            }
            pointerDistanceDelta += POINTER_DELTA_INCREMENT
        }
    }

    // Handle rotation controls
    var rotatingLeft = false
    var rotatingRight = false
    var rotatingUp = false
    var rotatingDown = false

    if (mode == Mode.TELEKINESIS) {
        if (ROTATE_TK_LEFT.isDown) {
            while (ROTATE_TK_LEFT.consumeClick()) {
                // NOOP -- just make sure we aren't accumulating any clicks from holding down the key
            }
            rotatingLeft = true
        }
        if (ROTATE_TK_RIGHT.isDown) {
            while (ROTATE_TK_RIGHT.consumeClick()) {
                // NOOP -- just make sure we aren't accumulating any clicks from holding down the key
            }
            rotatingRight = true
        }
        if (ROTATE_TK_UP.isDown) {
            while (ROTATE_TK_UP.consumeClick()) {
                // NOOP -- just make sure we aren't accumulating any clicks from holding down the key
            }
            rotatingUp = true
        }
        if (ROTATE_TK_DOWN.isDown) {
            while (ROTATE_TK_DOWN.consumeClick()) {
                // NOOP -- just make sure we aren't accumulating any clicks from holding down the key
            }
            rotatingDown = true
        }
    }

    CHANNEL.clientHandle().send(
        TelekinesisControlsPacket(playerLookDirection, pointerDistanceDelta, rotatingLeft, rotatingRight, rotatingUp, rotatingDown)
    )
}

/**
 * Called at the top of the client tick to handle input events. This means that it is called before any other
 * keybinds are processed.
 */
fun handleModInputEvents() {
    val client = Minecraft.getInstance()
    val player = client.player!!

    while (TOGGLE_TK_MODE.consumeClick() && player.telekinesis().tier != TkFocusTier.NONE && !player.selection().clientChargingLaunch) {
        val currentMode = player.getMode()
        if (currentMode == Mode.TELEKINESIS) {
            CHANNEL.clientHandle().send(
                SetModePacket(Mode.NONE.name)
            )
        } else if (currentMode == Mode.NONE) {
            CHANNEL.clientHandle().send(
                SetModePacket(Mode.TELEKINESIS.name)
            )
        }
    }

    while (TOGGLE_PLACEMENT_MODE.consumeClick() && (player.getMode() == Mode.TELEKINESIS || player.getMode() == Mode.PLACEMENT) && !player.selection().clientChargingLaunch) {
        val currentMode = player.getMode()
        if (currentMode == Mode.TELEKINESIS && player.telekinesis().activeTkTarget is ShipTkTarget) {
            CHANNEL.clientHandle().send(PlacementPacket(true))
            CHANNEL.clientHandle().send(HoverTkPacket(Minecraft.getInstance().cameraEntity!!.lookAngle))
            // Reset placement angles
            CHANNEL.clientHandle().send(PlacementDirectionPacket(Direction.UP, Direction.SOUTH))
        } else if (currentMode == Mode.PLACEMENT) {
            CHANNEL.clientHandle().send(PlacementPacket(false))
        }
    }

    while (PICK_TO_TK.consumeClick()) {
        if (player.getMode() == Mode.TELEKINESIS && !player.selection().clientChargingLaunch) {
            // If we are carrying something, drop it. Otherwise, pick up the block/entity/ship
            if (player.telekinesis().getTkTargets().isEmpty() || player.telekinesis().activeTkTarget == null) {
                val selection = player.selection()
                var didPick = false
                selection.cursorTargetBlock?.let {
                    val toTk = mutableListOf(it)
                    player.updatePreviewsFromPosition(it)
                    toTk.addAll(selection.previewPositions)
                    toTk.removeIf { pos -> !pos.isValidTkTargetFor(player) }
                    if (toTk.isNotEmpty()) {
                        CHANNEL.clientHandle().send(TkPositionsPacket(toTk))
                        didPick = true
                        player.selection().dfsDepth = 1
                    }
                }
                selection.cursorTargetEntity?.let {
                    if (player.canTkEntity(it)) {
                        CHANNEL.clientHandle().send(PickEntityToTkPacket(it.id))
                        didPick = true
                    }
                }
                selection.cursorTargetShip?.let {
                    if (player.canTkShip(it)) {
                        CHANNEL.clientHandle().send(PickShipToTkPacket(it.id))
                        didPick = true
                    }
                }
                if (didPick) {
                    player.selection().clear()
                }
            } else {
                CHANNEL.clientHandle().send(DropTkPacket())
            }
        }
    }

    while (CHANGE_DFS_DISTANCE_TYPE.consumeClick() && !player.selection().clientChargingLaunch) {
        if (player.getMode() == Mode.TELEKINESIS) {
            val newType = when(player.selection().dfsDistanceType) {
                DfsDistanceType.CUBE -> DfsDistanceType.SPHERE
                DfsDistanceType.SPHERE -> DfsDistanceType.CUBE
            }
            player.selection().dfsDistanceType = newType
        }
    }

    while (SET_TK_HOVERING.consumeClick() && !player.selection().clientChargingLaunch) {
        if (player.getMode() == Mode.TELEKINESIS) {
            CHANNEL.clientHandle().send(HoverTkPacket(Minecraft.getInstance().cameraEntity!!.lookAngle))
        }
    }

    while (PLACE_TK.consumeClick() && !player.selection().clientChargingLaunch) {
        if (player.getMode() == Mode.TELEKINESIS || player.getMode() == Mode.PLACEMENT) {
            if (player.getMode() == Mode.PLACEMENT) {
                CHANNEL.clientHandle().send(PrecisePlacementPacket(Minecraft.getInstance().cameraEntity!!.lookAngle))

            } else {
                CHANNEL.clientHandle().send(PlaceTkPacket())
            }
        }
    }

    if (LAUNCH_TK.isDown && !player.selection().clientChargingLaunch) {
        CHANNEL.clientHandle().send(SetChargingLaunchPacket(true))
        player.selection().clientChargingLaunch = true
    } else if (!LAUNCH_TK.isDown && player.selection().clientChargingLaunch) {
        val aimingAtPosition = player.selection().getSelectionPosition()
        val aimingAtTarget = player.telekinesis().getTkTargets().find { target ->
            when(target) {
                is ShipTkTarget -> player.selection().cursorTargetShip?.id == target.shipId
                is EntityTkTarget -> player.selection().cursorTargetEntity?.id == target.entity.id
                else -> false
            }
        }
        val targetChargingLaunch = aimingAtTarget?.chargingLaunch ?: false

        // If the player is aiming at nothing or at a target charging launch, use pointer to aim
        val launchTargetPos: Vec3 = if (aimingAtPosition == null || targetChargingLaunch) {
            getPointerAtDistance(player, Minecraft.getInstance().cameraEntity!!.lookAngle, LAUNCH_POINTER_DISTANCE)
        } else {
            aimingAtPosition
        }

        CHANNEL.clientHandle().send(LaunchTkPacket(launchTargetPos))
        player.selection().clientChargingLaunch = false
    }

    while (player.telekinesis().activeTkTarget == null && player.getMode() != Mode.PLACEMENT && POINTER_PUSH.consumeClick()) {
        player.selection().dfsDepth += 1
    }
    while (player.telekinesis().activeTkTarget == null && player.getMode() != Mode.PLACEMENT && POINTER_PULL.consumeClick()) {
        player.selection().dfsDepth -= 1
    }

    if (player.getMode() == Mode.PLACEMENT) {
        val lookingDirection = player.direction
        val topDirection = player.telekinesis().placementDirectionTop
        val frontDirection = player.telekinesis().placementDirectionFront

        while (ROTATE_TK_UP.consumeClick()) {
            val (newTopDirection, newFrontDirection) = rotateRelativeToFacing(topDirection, frontDirection, lookingDirection, Direction2D.UP)
            CHANNEL.clientHandle().send(PlacementDirectionPacket(newTopDirection, newFrontDirection))
        }

        while (ROTATE_TK_DOWN.consumeClick()) {
            val (newTopDirection, newFrontDirection) = rotateRelativeToFacing(topDirection, frontDirection, lookingDirection, Direction2D.DOWN)
            CHANNEL.clientHandle().send(PlacementDirectionPacket(newTopDirection, newFrontDirection))
        }

        while (ROTATE_TK_LEFT.consumeClick()) {
            val (newTopDirection, newFrontDirection) = rotateRelativeToFacing(topDirection, frontDirection, lookingDirection, Direction2D.LEFT)
            CHANNEL.clientHandle().send(PlacementDirectionPacket(newTopDirection, newFrontDirection))
        }

        while (ROTATE_TK_RIGHT.consumeClick()) {
            val (newTopDirection, newFrontDirection) = rotateRelativeToFacing(topDirection, frontDirection, lookingDirection, Direction2D.RIGHT)
            CHANNEL.clientHandle().send(PlacementDirectionPacket(newTopDirection, newFrontDirection))
        }
    }
}