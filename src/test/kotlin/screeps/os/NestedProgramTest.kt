package screeps.os

import kotlin.test.Test
import kotlin.test.assertEquals

class NestedProgramTest : KernelTest() {

    @Test
    fun requestingProgram() {
        val requesterProgram = RequesterProgram()
        kernel.spawnProcess(requesterProgram, 10)

        assertEquals(0, requesterProgram.getIds().size)

        repeat(10) {
            kernel.loop()
        }

        assertEquals(listOf(5,6,7,8,9), requesterProgram.getIds())
    }

    @Test
    fun callingWaitInSomethingNotAProgram() {
        val moduloProgram = ModuloProgram()
        kernel.spawnProcess(moduloProgram, 10)

        assertEquals(0, moduloProgram.firstTick)
        assertEquals(0, moduloProgram.secondTick)

        repeat(10) { // Counting from 0
            kernel.loop()
        }

        assertEquals(6, moduloProgram.firstTick)
        assertEquals(9, moduloProgram.secondTick)
    }

}