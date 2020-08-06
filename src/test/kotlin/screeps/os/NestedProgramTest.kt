package screeps.os

import kotlin.test.Test
import kotlin.test.assertEquals

class NestedProgramTest : KernelTestSetup() {

    @Test
    fun requestingProgram() {
        val requesterProgram = RequesterProgram()
        kernel.spawnProcess(requesterProgram, 10)

        assertEquals(0, requesterProgram.getIds().size)

        repeat(10) {
            kernel.loop()
        }

        assertEquals(listOf(5,6,7,8,9), requesterProgram.getIds())
    }
}

class ProviderProgram : SleepingProgram() {

    suspend fun getXExecutionIds(amount: Int) : List<Int> {
        val executionList = mutableListOf<Int>()

        executionList.add(executions)

        while(executionList.size < amount) {
            sleep(1)
            executionList.add(executions)
        }

        return executionList
    }
}

class RequesterProgram : Program() {
    val ids = mutableListOf<Int>()

    override suspend fun execute() {
        val providerProgram = ProviderProgram()
        val provPid = Kernel.kernel.spawnProcess(providerProgram, 10)

        println("${getProgramName()} Started provider program, going to sleep")
        sleep(5)

        val numberOfExecutionIdsToRequest = 5
        println("${getProgramName()} Going to request $numberOfExecutionIdsToRequest execution ids")

        ids.addAll(providerProgram.getXExecutionIds(numberOfExecutionIdsToRequest))

        println("${getProgramName()} Currently have the following ids: $ids")

        Kernel.kernel.killProcess(provPid)
        exit()
    }

    fun getIds() = ids
}