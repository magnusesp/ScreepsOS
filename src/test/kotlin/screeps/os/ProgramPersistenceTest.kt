package screeps.os

import kotlin.test.Test
import kotlin.test.assertEquals

class ProgramPersistenceTest : KernelTestSetup() {

    @Test
    fun simplePersistenceTest() {
        val firstIntVariable = 1
        val persistentProgramA = ProgramWithSmallState(firstIntVariable)
        assertEquals(firstIntVariable, persistentProgramA.intVariable)

        kernel.spawnProcess(persistentProgramA, priority = 10)

        kernel.loop()
        assertEquals(firstIntVariable, persistentProgramA.intVariable)

        kernel.loop()
        assertEquals(Process.State.EXIT, persistentProgramA.getState())

        val persistenceIdA = persistentProgramA.getPersistenceId()

        resetKernel()

        val secondIntVariable = 2
        val persistentProgramB = ProgramWithSmallState(secondIntVariable)
        assertEquals(secondIntVariable, persistentProgramB.intVariable)

        kernel.spawnProcess(persistentProgramB, priority = 10, restorePersistenceId = persistenceIdA)

        kernel.loop()
        assertEquals(firstIntVariable, persistentProgramB.intVariable)

        kernel.loop()
        assertEquals(Process.State.EXIT, persistentProgramB.getState())
    }


}

class ProgramWithSmallState(val intVariable: Int) : TestProgram() {
    override suspend fun execute() {
        println("${getProgramName()} starting up")
        restoreIfNecessary()
        print("Not restored intVariable = $intVariable")
        sleep(1)
        persistProgram()
        print("Restored intVariable = $intVariable")
        exit()
    }
}