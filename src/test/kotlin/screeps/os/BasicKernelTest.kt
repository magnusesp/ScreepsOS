package screeps.os

import kotlin.test.*


class BasicKernelTest : KernelTest() {

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

        kernel.loop()
        assertEquals(1, testProgram.executions)

        kernel.loop()
        assertEquals(2, testProgram.executions)

        kernel.loop()
        assertEquals(3, testProgram.executions)
    }

    @Test
    fun twoPrograms() {
        val testProgram1 = SleepingProgram()
        kernel.spawnProcess(testProgram1, 1)

        val testProgram2 = SleepingProgram()
        kernel.spawnProcess(testProgram2, 1)

        assertEquals(0, testProgram1.executions)
        assertEquals(0, testProgram2.executions)

        kernel.loop()
        assertEquals(1, testProgram1.executions)
        assertEquals(1, testProgram2.executions)

        kernel.loop()
        assertEquals(2, testProgram1.executions)
        assertEquals(2, testProgram2.executions)

        kernel.loop()
        assertEquals(3, testProgram1.executions)
        assertEquals(3, testProgram2.executions)
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

        kernel.loop()
        assertEquals(0, lowPriProgram.executions)
        assertEquals(10, highPriProgram.executions)

        kernel.loop()
        assertEquals(0, lowPriProgram.executions)
        assertEquals(15, highPriProgram.executions)
    }

    @Test
    fun changingPriorities() {
        val programA = RunningProgram()
        kernel.spawnProcess(programA, 10)

        val programB = RunningProgram()
        kernel.spawnProcess(programB, 20)

        assertEquals(0, programA.executions)
        assertEquals(0, programB.executions)

        kernel.loop()
        assertEquals(5, programA.executions)
        assertEquals(0, programB.executions)

        kernel.loop()
        assertEquals(10, programA.executions)
        assertEquals(0, programB.executions)

        programA.changePriority(30)

        kernel.loop()
        assertEquals(10, programA.executions)
        assertEquals(5, programB.executions)

        programB.changePriority(40)

        kernel.loop()
        assertEquals(15, programA.executions)
        assertEquals(5, programB.executions)

        programB.changePriority(5)

        kernel.loop()
        assertEquals(15, programA.executions)
        assertEquals(10, programB.executions)
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

        kernel.loop()
        assertEquals(1, waitingProgram.executions)
        assertEquals(1, sleepingProgram.executions)

        kernel.loop()
        assertEquals(2, waitingProgram.executions)
        assertEquals(2, sleepingProgram.executions)

        kernel.loop()
        assertEquals(2, waitingProgram.executions)
        assertEquals(3, sleepingProgram.executions)

        kernel.loop()
        assertEquals(2, waitingProgram.executions)
        assertEquals(4, sleepingProgram.executions)

        kernel.loop()
        assertEquals(3, waitingProgram.executions)
        assertEquals(5, sleepingProgram.executions)

        kernel.loop()
        assertEquals(3, waitingProgram.executions)
        assertEquals(6, sleepingProgram.executions)

        kernel.loop()
        assertEquals(3, waitingProgram.executions)
        assertEquals(7, sleepingProgram.executions)
    }

    @Test
    fun exitingProgram() {
        val exitingProgram = ExitingProgram(5)
        kernel.spawnProcess(exitingProgram, 10)

        repeat(10) {
            kernel.loop()
        }

        assertEquals(6, exitingProgram.executions)
    }

    @Test
    fun finishingProgram() {
        val finishingProgram = FinishingProgram()
        kernel.spawnProcess(finishingProgram, 10)

        repeat(10) {
            kernel.loop()
        }

        assertEquals(3, finishingProgram.executions)
    }

    @Test
    fun catchingException() {
        val exceptionProgram = ExceptionProgram()
        kernel.spawnProcess(exceptionProgram, 10)

        repeat(10) {
            kernel.loop()
        }

    }
}