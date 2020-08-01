package screeps.os

import kotlin.test.BeforeTest

open class KernelTest {
    protected lateinit var kernel: Kernel

    object MockGameObj : MockGameObject {
        override var tick = 0
        override var cpu = 0
        override var cpuLimit = 5
    }

    @BeforeTest
    fun reset() {
        MockGameObj.tick = 0
        MockGameObj.cpu = 0

        kernel = Kernel.create {MockGameObj.tick++}

        val scheduler = ExampleScheduler(kernel)

        kernel.setScheduler(scheduler)

        scheduler.setGameObject(MockGameObj)
    }
}