package screeps.os

import kotlin.test.Test
import kotlin.test.assertEquals

class ProgramPersistenceTest : KernelTestSetup() {

    @Test
    fun simplePersistenceTest() {
        val persistentProgramA = ProgramWithSmallState()
        assertEquals(0, persistentProgramA.intVariable)

        kernel.spawnProcess(persistentProgramA, priority = 10)

        kernel.loop()
        assertEquals(0, persistentProgramA.intVariable)

        kernel.loop()
        assertEquals(Process.State.EXIT, persistentProgramA.getState())

        val persistenceIdA = persistentProgramA.getPersistenceId()

        resetKernel()

        val persistentProgramB = ProgramWithSmallState()
        assertEquals(0, persistentProgramB.intVariable)

        kernel.spawnProcess(persistentProgramB, priority = 10, restorePersistenceId = persistenceIdA)

        kernel.loop()
        assertEquals(1, persistentProgramB.intVariable)

        kernel.loop()
        assertEquals(Process.State.EXIT, persistentProgramB.getState())

        val persistenceIdB = persistentProgramB.getPersistenceId()

        resetKernel()

        val persistentProgramC = ProgramWithSmallState()
        assertEquals(0, persistentProgramC.intVariable)

        kernel.spawnProcess(persistentProgramC, priority = 10, restorePersistenceId = persistenceIdB)

        kernel.loop()
        assertEquals(2, persistentProgramC.intVariable)

        kernel.loop()
        assertEquals(Process.State.EXIT, persistentProgramC.getState())
    }


}

class ProgramWithSmallState : TestProgram() {
    var intVariable = 0
    var stringVariable = "Initialized"

    override suspend fun execute() {
        println("${getProgramName()} starting up")
        restoreIfNecessary()
        print("Maybe restored? intVariable = $intVariable stringVariable = $stringVariable")
        sleep(1)

        intVariable++
        stringVariable = "Persisted $intVariable times"
        persistProgram()
        exit()
    }
}