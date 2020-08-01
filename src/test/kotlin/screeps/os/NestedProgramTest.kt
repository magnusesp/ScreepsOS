package screeps.os

import kotlin.test.Test
import kotlin.test.assertEquals

class NestedProgramTest : KernelTest() {

    @Test
    fun nestedProgram() {
        val requesterProgram = RequesterProgram()
        kernel.spawnProcess(requesterProgram, 10)

        assertEquals(0, requesterProgram.getIds().size)

        repeat(10) {
            kernel.loop()
        }

        assertEquals(listOf(5,6,7,8,9), requesterProgram.getIds())
    }
}