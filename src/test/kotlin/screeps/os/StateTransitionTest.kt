package screeps.os

import kotlin.test.Test
import kotlin.test.assertEquals

class StateTransitionTest : KernelTestSetup() {

    @Test
    fun stateTransitions() {
        val program = StateTransitionProgram()
        kernel.spawnProcess(program, 10)

        assertEquals(Process.State.NEW, program.getState())

        kernel.loop()
        assertEquals(Process.State.RUNNING, program.states[1])
        assertEquals(Process.State.WAITING, program.getState())

        kernel.loop()
        assertEquals(Process.State.RUNNING, program.states[2])
        assertEquals(Process.State.SLEEPING, program.getState())

        kernel.loop()
        assertEquals(Process.State.RUNNING, program.states[3])
        assertEquals(Process.State.READY, program.getState())

        kernel.loop()
        assertEquals(Process.State.RUNNING, program.states[4])
        assertEquals(Process.State.EXIT, program.getState())
    }
}

class StateTransitionProgram : TestProgram() {
    val states = mutableMapOf<Int, Process.State>()

    override suspend fun execute() {
        print("Waiting for {tick == 1}")
        states[executions] = getState()
        wait({Kernel.kernel.getTick() == 1})

        print("Sleeping 1 ticks")
        states[executions] = getState()
        sleep(1)

        print("Yielding for 5 iterations")
        states[executions] = getState()
        repeat(5) { yield() }

        print("Exiting")
        states[executions] = getState()
        exit()
    }
}