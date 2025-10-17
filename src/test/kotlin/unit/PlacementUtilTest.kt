package unit

import com.godlike.common.telekinesis.placement.transformVectorUsingPlacementDirection
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import net.minecraft.core.Direction
import net.minecraft.world.phys.Vec3

class PlacementUtilTest : FunSpec({

    test("transformVectorUsingPlacementDirection") {
        data class TestCase(val topDir: Direction, val fronDir: Direction, val vec: Vec3, val outVec: Vec3)

        val inVec = Vec3(1.0, 3.0, 2.0)
        val testCases = listOf(
            TestCase(Direction.UP, Direction.SOUTH, inVec, inVec),
            TestCase(Direction.DOWN, Direction.SOUTH, inVec, Vec3(-1.0, -3.0, 2.0)),
            TestCase(Direction.EAST, Direction.SOUTH, inVec, Vec3(3.0, -1.0, 2.0)),
            TestCase(Direction.WEST, Direction.SOUTH, inVec, Vec3(-3.0, 1.0, 2.0)),

        )

        testCases.forEach { (topDir, frontDir, vec, expected) ->
            withClue("top: $topDir, front: $frontDir, in: $vec, expected: $expected") {
                transformVectorUsingPlacementDirection(topDir, frontDir, vec) shouldBe expected
            }
        }
    }
})