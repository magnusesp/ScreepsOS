package screeps.os

import kotlinx.coroutines.Runnable
import kotlin.coroutines.CoroutineContext
import kotlin.test.*


class KernelTest {
    private var tick = 0

    private val increaseTick = {tick++}
    private val emptyLoop = suspend {}
    private val runOncePerTickDispatcher = object : ScreepsOSScheduler {
        var lastTick = -1

        override fun getNextJob(context: CoroutineContext, block: Runnable): Runnable? {
            if(lastTick != Kernel.getTick()) {
                lastTick = Kernel.getTick()
                return block
            }

            return null
        }
    }

    @BeforeTest
    fun resetKernel() = Kernel.reset()

    @Test
    fun ticksAreUpdating() {
        Kernel.create(increaseTick, emptyLoop, runOncePerTickDispatcher)
        tick = 0

        Kernel.loop()
        assertEquals(0, Kernel.getTick())

        Kernel.loop()
        assertEquals(1, Kernel.getTick())
    }

    @Test
    fun simpleLoopFunction() {
        var counter = 0
        fun countUpwards() { counter++ }

        val loop = suspend {
            countUpwards()
        }

        Kernel.create(increaseTick, loop, runOncePerTickDispatcher)

        assertEquals(0, counter)
        Kernel.loop()
        assertEquals(1, counter)
        Kernel.loop()
        assertEquals(2, counter)
    }

    @Test
    // This must not be the first test
    fun resetWorks() {
        assertFailsWith<Kernel.KernelNotCreatedException> { Kernel.loop() }
    }
}