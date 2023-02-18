package cc.zork.event

import net.minecraft.block.Block
import net.minecraft.client.multiplayer.WorldClient
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.network.Packet
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraft.util.MovingObjectPosition

/**
 * Called when player attacks other entity
 *
 * @param targetEntity Attacked entity
 */
class AttackEvent(val targetEntity: Entity?) : Event()

/**
 * Called when player clicks a block
 */
class ClickBlockEvent(val clickedBlock: BlockPos?, val WEnumFacing: EnumFacing?) : Event()

/**
 * Called when player jumps
 *
 * @param motion jump motion (y motion)
 */
class JumpEvent(var motion: Float) : CancellableEvent()

/**
 * Called when user press a key once
 *
 * @param key Pressed key
 */
class KeyEvent(val key: Int) : Event()

/**
 * Called in "onUpdateWalkingPlayer"
 *
 * @param eventState PRE or POST
 */
class MotionEvent(
    val eventState: EventType,
    var X: Double,
    val Y: Double,
    var Z: Double,
    val onGround: Boolean,
    var Yaw: Float,
    var Pitch: Float,
    var lastReportedYaw: Float,
    var lastReportedPitch: Float
) : Event()


/**
 * Called when receive or send a packet
 */
class PacketEvent(val type: EventType, var packet: Packet<*>) : CancellableEvent()

/**
 * Called when a block tries to push you
 */
class PushOutEvent : CancellableEvent()

/**
 * Called when screen is going to be rendered
 */
class Render2DEvent(val partialTicks: Float) : Event()

/**
 * Called when world is going to be rendered
 */
class Render3DEvent(val type: EventType, val partialTicks: Float) : Event()

/**
 * Called when player is going to step
 */
class StepEvent(var stepHeight: Float) : Event()

/**
 * Called when player step is confirmed
 */
class StepConfirmEvent : Event()

/**
 * Called when a text is going to be rendered
 */
class TextEvent(var text: String?) : CancellableEvent()

/**
 * tick... tack... tick... tack
 */
class TickEvent : Event()

/**
 * Called when minecraft player will be updated
 */
class UpdateEvent : Event()

/**
 * Called when the world changes
 */
class WorldEvent(val worldClient: WorldClient?) : Event()
class SafeWalkEvent(var safe: Boolean) : Event() {
    fun set(b: Boolean) {
        safe = b
    }
}

/**
 * Called when window clicked
 */
class ClickWindowEvent(val windowId: Int, val slotId: Int, val mouseButtonClicked: Int, val mode: Int) :
    CancellableEvent()

class RenderBlockEvent(val x: Int, val y: Int, val z: Int, val block: Block, val pos: BlockPos) : Event()

class BlockClickEvent(var obj: MovingObjectPosition) : CancellableEvent() {
    var enumFacing = obj.sideHit ?: null
}

class Shader2DEvent(val type: Int) : Event() {

}

class RenderEntityLivingEvent(
    val type: EventType,
    val entity: EntityLivingBase,
    val limbSwing: Float,
    val limbSwingAmount: Float,
    val ageInTicks: Float,
    val rotationYawHead: Float,
    val rotationPitch: Float,
    val chestRot: Float,
    val offset: Float
) : CancellableEvent() {
    constructor(post: EventType, entity: EntityLivingBase) : this(post, entity, 0f, 0f, 0f, 0f, 0f, 0f, 0f)
}

class MoveEvent(var x: Double, var y: Double, var z: Double) : CancellableEvent() {

}

