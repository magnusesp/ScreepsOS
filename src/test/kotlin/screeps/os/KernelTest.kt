package screeps.os

import kotlin.test.*


class KernelTest {

    private lateinit var kernel: Kernel

    object mockGameObject : MockGameObject {
        override var tick = 0
        override var cpu = 0
        override var cpuLimit = 5
    }

    @BeforeTest
    fun reset() {
        mockGameObject.tick = 0
        mockGameObject.cpu = 0

        kernel = Kernel.create {mockGameObject.tick++}

        val scheduler = ExampleScheduler(kernel)

        kernel.setScheduler(scheduler)

        scheduler.setGameObject(mockGameObject)
    }

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
    fun spawnAndKill() {
        val shortLivingProgram = SleepingProgram()
        val shortLivingPid1 = kernel.spawnProcess(shortLivingProgram, 10)

        kernel.loop()
        assertEquals(1, shortLivingProgram.executions)

        val longLivingProgram = SleepingProgram()
        kernel.spawnProcess(longLivingProgram, 10)

        assertEquals(0, longLivingProgram.executions)

        kernel.loop()
        assertEquals(2, shortLivingProgram.executions)
        assertEquals(1, longLivingProgram.executions)

        kernel.killProcess(shortLivingPid1)

        kernel.loop()
        assertEquals(2, shortLivingProgram.executions)
        assertEquals(2, longLivingProgram.executions)


        kernel.loop()
        assertEquals(2, shortLivingProgram.executions)
        assertEquals(3, longLivingProgram.executions)
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

}


abstract class TestProgram : Program() {
    var executions = 0

    protected fun print(nextState: String) {
        println("${getProgramName()} executing for the ${++executions}. time. $nextState")
    }
}

class SleepingProgram : TestProgram() {
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
