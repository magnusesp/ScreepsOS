package screeps.os

import screeps.os.examples.ExampleScheduler
import screeps.os.examples.MockGameObject
import kotlin.test.BeforeTest

open class KernelTestSetup {
    protected lateinit var kernel: Kernel

    object MockGameObj : MockGameObject {
        override var tick = 0
        override var cpu = 0
        override var cpuLimit = 5
    }

    @BeforeTest
    fun resetKernel() {
        MockGameObj.tick = 0
        MockGameObj.cpu = 0

        val scheduler = ExampleScheduler(MockGameObj)

        kernel = Kernel.create(scheduler) { MockGameObj.tick++ }
    }
}

abstract class TestProgram : Program() {
    var executions = 0

    protected fun print(nextState: String) {
        println("${getProgramName()} executing for the ${++executions}. time. $nextState")
    }
}