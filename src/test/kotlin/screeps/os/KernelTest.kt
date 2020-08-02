package screeps.os

import screeps.os.examples.ExampleScheduler
import screeps.os.examples.MockGameObject
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

        val scheduler = ExampleScheduler(MockGameObj)

        kernel = Kernel.create(scheduler) { MockGameObj.tick++ }
    }
}