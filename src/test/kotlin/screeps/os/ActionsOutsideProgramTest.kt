package screeps.os

import kotlin.test.Test
import kotlin.test.assertEquals


class ActionsOutsideProgramTest : KernelTestSetup() {
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

class ModuloNotAProgram() {
    suspend fun waitForTickModuloX(modulo: Int) : Int {

        wait({Kernel.kernel.getTick() % modulo == 0})

        return Kernel.kernel.getTick()
    }
}

class ModuloProgram : Program() {
    var firstTick = 0
    var secondTick = 0

    override suspend fun execute() {
        val moduloNotAProgram = ModuloNotAProgram()

        println("${getProgramName()} Started provider program, going to sleep")
        sleep(5)

        val modulo = 3
        println("${getProgramName()} Getting the first tick that is modulo $modulo")

        firstTick = moduloNotAProgram.waitForTickModuloX(modulo)
        println("${getProgramName()} The first tick that is modulo $modulo is $firstTick")


        sleep(1)
        println("${getProgramName()} Getting the second tick that is modulo $modulo")

        secondTick = moduloNotAProgram.waitForTickModuloX(modulo)
        println("${getProgramName()} The second tick that is modulo $modulo is $secondTick")
        exit()
    }

}