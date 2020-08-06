package screeps.os

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame

class EndStateTest : KernelTestSetup() {
    @Test
    fun exitingProgram() {
        val exitingProgram = ExitingProgram(5)
        kernel.spawnProcess(exitingProgram, 10)

        repeat(10) {
            kernel.loop()
        }
        assertEquals(Process.State.EXIT, exitingProgram.getState())
        assertEquals(6, exitingProgram.executions)

        val exception = exitingProgram.getException()
        assertNull(exception)
    }

    @Test
    fun finishingNoExitProgram() {
        val finishingNoExitProgram = FinishingNoExitProgram()
        kernel.spawnProcess(finishingNoExitProgram, 10)

        repeat(10) {
            kernel.loop()
        }
        assertEquals(Process.State.KILLED, finishingNoExitProgram.getState())
        assertEquals(3, finishingNoExitProgram.executions)

        val exception = finishingNoExitProgram.getException()
        assertNull(exception)
    }

    @Test
    fun catchingException() {
        val exceptionProgram = ExceptionProgram()
        kernel.spawnProcess(exceptionProgram, 10)

        repeat(10) {
            kernel.loop()
        }

        assertEquals(Process.State.EXCEPTION, exceptionProgram.getState())

        val exception = exceptionProgram.getException()
        requireNotNull(exception)
        assertSame(ExceptionProgramException::class, exception::class)
    }
}

class ExitingProgram(private val runFor: Int) : TestProgram() {
    override suspend fun execute() {
        while (true) {
            if (executions == runFor) {
                print("Exiting")
                exit()
            }
            print("Sleeping")
            sleep(1)
        }
    }
}

class FinishingNoExitProgram : TestProgram() {
    override suspend fun execute() {
        print("Sleeping 1 tick")
        sleep(1)
        print("Sleeping 1 tick")
        sleep(1)
        print("Finishing")
    }
}

class ExceptionProgram : TestProgram() {
    override suspend fun execute() {
        print("Sleeping 1 tick")
        sleep(1)
        print("Throwing exception")
        throw ExceptionProgramException()
    }
}

class ExceptionProgramException : Exception("Test exception")