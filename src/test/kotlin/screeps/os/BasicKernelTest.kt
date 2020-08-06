package screeps.os

import kotlin.test.*

class BasicKernelTest : KernelTestSetup() {

    @Test
    fun ticksAreUpdating() {
        assertEquals(0, kernel.getTick())

        kernel.loop()
        assertEquals(0, kernel.getTick())

        kernel.loop()
        assertEquals(1, kernel.getTick())

        kernel.loop()
        assertEquals(2, kernel.getTick())
    }

    @Test
    fun sleepingProgram() {
        val testProgram = SleepingProgram()
        kernel.spawnProcess(testProgram, 1)

        assertEquals(0, testProgram.executions)
        assertEquals(Process.State.NEW, testProgram.getState())

        kernel.loop()
        assertEquals(1, testProgram.executions)
        assertEquals(Process.State.SLEEPING, testProgram.getState())

        kernel.loop()
        assertEquals(2, testProgram.executions)
        assertEquals(Process.State.SLEEPING, testProgram.getState())

        kernel.loop()
        assertEquals(3, testProgram.executions)
        assertEquals(Process.State.SLEEPING, testProgram.getState())

        val exception = testProgram.getException()
        assertNull(exception)
    }

    @Test
    fun twoPrograms() {
        val testProgram1 = SleepingProgram()
        kernel.spawnProcess(testProgram1, 1)

        val testProgram2 = SleepingProgram()
        kernel.spawnProcess(testProgram2, 1)

        assertEquals(0, testProgram1.executions)
        assertEquals(0, testProgram2.executions)
        assertEquals(Process.State.NEW, testProgram1.getState())
        assertEquals(Process.State.NEW, testProgram2.getState())

        kernel.loop()
        assertEquals(1, testProgram1.executions)
        assertEquals(1, testProgram2.executions)
        assertEquals(Process.State.SLEEPING, testProgram1.getState())
        assertEquals(Process.State.SLEEPING, testProgram2.getState())

        kernel.loop()
        assertEquals(2, testProgram1.executions)
        assertEquals(2, testProgram2.executions)
        assertEquals(Process.State.SLEEPING, testProgram1.getState())
        assertEquals(Process.State.SLEEPING, testProgram2.getState())

        kernel.loop()
        assertEquals(3, testProgram1.executions)
        assertEquals(3, testProgram2.executions)
        assertEquals(Process.State.SLEEPING, testProgram1.getState())
        assertEquals(Process.State.SLEEPING, testProgram2.getState())
    }

    @Test
    fun highAndLowPriPrograms() {
        val lowPriProgram = RunningProgram()
        kernel.spawnProcess(lowPriProgram, 20)

        val highPriProgram = RunningProgram()
        kernel.spawnProcess(highPriProgram, 10)

        assertEquals(0, lowPriProgram.executions)
        assertEquals(0, highPriProgram.executions)

        kernel.loop()
        assertEquals(0, lowPriProgram.executions)
        assertEquals(5, highPriProgram.executions)
        assertEquals(Process.State.NEW, lowPriProgram.getState())
        assertEquals(Process.State.READY, highPriProgram.getState())

        kernel.loop()
        assertEquals(0, lowPriProgram.executions)
        assertEquals(10, highPriProgram.executions)
        assertEquals(Process.State.NEW, lowPriProgram.getState())
        assertEquals(Process.State.READY, highPriProgram.getState())

        kernel.loop()
        assertEquals(0, lowPriProgram.executions)
        assertEquals(15, highPriProgram.executions)
        assertEquals(Process.State.NEW, lowPriProgram.getState())
        assertEquals(Process.State.READY, highPriProgram.getState())
    }

    @Test
    fun changingPriorities() {
        val programA = RunningProgram()
        kernel.spawnProcess(programA, 10)

        val programB = RunningProgram()
        kernel.spawnProcess(programB, 20)

        assertEquals(0, programA.executions)
        assertEquals(0, programB.executions)
        assertEquals(Process.State.NEW, programA.getState())
        assertEquals(Process.State.NEW, programB.getState())


        kernel.loop()
        assertEquals(5, programA.executions)
        assertEquals(0, programB.executions)
        assertEquals(Process.State.READY, programA.getState())
        assertEquals(Process.State.NEW, programB.getState())

        kernel.loop()
        assertEquals(10, programA.executions)
        assertEquals(0, programB.executions)
        assertEquals(Process.State.READY, programA.getState())
        assertEquals(Process.State.NEW, programB.getState())

        programA.changePriority(30)

        kernel.loop()
        assertEquals(10, programA.executions)
        assertEquals(5, programB.executions)
        assertEquals(Process.State.READY, programA.getState())
        assertEquals(Process.State.READY, programB.getState())

        programB.changePriority(40)

        kernel.loop()
        assertEquals(15, programA.executions)
        assertEquals(5, programB.executions)
        assertEquals(Process.State.READY, programA.getState())
        assertEquals(Process.State.READY, programB.getState())

        programB.changePriority(5)

        kernel.loop()
        assertEquals(15, programA.executions)
        assertEquals(10, programB.executions)
        assertEquals(Process.State.READY, programA.getState())
        assertEquals(Process.State.READY, programB.getState())
    }

    @Test
    fun spawnAndKill() {
        val shortLivingProgramA = SleepingProgram()
        val shortLivingPidA = kernel.spawnProcess(shortLivingProgramA, 10)

        assertEquals(0, shortLivingProgramA.executions)

        kernel.loop()
        assertEquals(1, shortLivingProgramA.executions)

        val longLivingProgram = SleepingProgram()
        kernel.spawnProcess(longLivingProgram, 10)

        assertEquals(0, longLivingProgram.executions)

        kernel.loop()
        assertEquals(2, shortLivingProgramA.executions)
        assertEquals(1, longLivingProgram.executions)

        kernel.killProcess(shortLivingPidA)
        assertEquals(Process.State.KILLED, shortLivingProgramA.getState())
        assertEquals(Process.State.SLEEPING, longLivingProgram.getState())

        kernel.loop()
        assertEquals(2, shortLivingProgramA.executions)
        assertEquals(2, longLivingProgram.executions)

        kernel.loop()
        assertEquals(2, shortLivingProgramA.executions)
        assertEquals(3, longLivingProgram.executions)

        val shortLivingProgramB = SleepingProgram()
        val shortLivingPidB = kernel.spawnProcess(shortLivingProgramB, 10)

        assertEquals(0, shortLivingProgramB.executions)

        kernel.loop()
        assertEquals(2, shortLivingProgramA.executions)
        assertEquals(4, longLivingProgram.executions)
        assertEquals(1, shortLivingProgramB.executions)

        kernel.loop()
        assertEquals(2, shortLivingProgramA.executions)
        assertEquals(5, longLivingProgram.executions)
        assertEquals(2, shortLivingProgramB.executions)

        kernel.killProcess(shortLivingPidB)
        assertEquals(Process.State.KILLED, shortLivingProgramA.getState())
        assertEquals(Process.State.SLEEPING, longLivingProgram.getState())
        assertEquals(Process.State.KILLED, shortLivingProgramB.getState())

        kernel.loop()
        assertEquals(2, shortLivingProgramA.executions)
        assertEquals(6, longLivingProgram.executions)
        assertEquals(2, shortLivingProgramB.executions)
    }

    @Test
    fun waitingProgram() {
        val waitingProgram = WaitingProgram {kernel.getTick() % 3 == 0}
        kernel.spawnProcess(waitingProgram, 10)

        val sleepingProgram =  SleepingProgram()
        kernel.spawnProcess(sleepingProgram, 10)


        // Tick = 0, condition true
        kernel.loop()
        assertEquals(1, waitingProgram.executions)
        assertEquals(Process.State.SLEEPING, waitingProgram.getState())
        assertEquals(1, sleepingProgram.executions)

        // Tick = 1, condition false
        kernel.loop()
        assertEquals(2, waitingProgram.executions)
        assertEquals(Process.State.WAITING, waitingProgram.getState())
        assertEquals(2, sleepingProgram.executions)

        // Tick = 2, condition false
        kernel.loop()
        assertEquals(2, waitingProgram.executions)
        assertEquals(Process.State.WAITING, waitingProgram.getState())
        assertEquals(3, sleepingProgram.executions)

        // Tick = 3, condition true
        kernel.loop()
        assertEquals(2, waitingProgram.executions)
        assertEquals(Process.State.SLEEPING, waitingProgram.getState())
        assertEquals(4, sleepingProgram.executions)

        // Tick = 4, condition false
        kernel.loop()
        assertEquals(3, waitingProgram.executions)
        assertEquals(Process.State.WAITING, waitingProgram.getState())
        assertEquals(5, sleepingProgram.executions)

        // Tick = 5, condition false
        kernel.loop()
        assertEquals(3, waitingProgram.executions)
        assertEquals(Process.State.WAITING, waitingProgram.getState())
        assertEquals(6, sleepingProgram.executions)

        // Tick = 6, condition true
        kernel.loop()
        assertEquals(3, waitingProgram.executions)
        assertEquals(Process.State.SLEEPING, waitingProgram.getState())
        assertEquals(7, sleepingProgram.executions)
    }
}

open class SleepingProgram : TestProgram() {
    override suspend fun execute() {
        while(true) {
            print("Sleeping 1 tick")
            sleep(1)
        }
    }
}

class RunningProgram : TestProgram() {
    override suspend fun execute() {
        while(true) {
            print("Yielding")
            yield()
        }
    }
}

class WaitingProgram(private val condition: () -> Boolean) : TestProgram() {
    override suspend fun execute() {
        while(true) {
            print("Waiting")
            wait(condition)
            sleep(1)
        }
    }
}