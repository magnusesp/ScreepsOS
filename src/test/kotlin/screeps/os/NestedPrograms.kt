package screeps.os

import screeps.os.yield
import screeps.os.sleep
import screeps.os.wait
import screeps.os.exit

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

    suspend fun waitForTickModuloX(modulo: Int) : Int {

        wait({Kernel.kernel.getTick() % modulo == 0})

        return Kernel.kernel.getTick()
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

class ModuloProgram : Program() {
    var firstTick = 0
    var secondTick = 0

    override suspend fun execute() {
        val providerProgram = ProviderProgram()
        val provPid = Kernel.kernel.spawnProcess(providerProgram, 10)

        println("${getProgramName()} Started provider program, going to sleep")
        sleep(5)

        val modulo = 3
        println("${getProgramName()} Getting the first tick that is modulo $modulo")

        firstTick = providerProgram.waitForTickModuloX(modulo)
        println("${getProgramName()} The first tick that is modulo $modulo is $firstTick")


        sleep(1)
        println("${getProgramName()} Getting the second tick that is modulo $modulo")

        secondTick = providerProgram.waitForTickModuloX(modulo)
        println("${getProgramName()} The second tick that is modulo $modulo is $secondTick")

        Kernel.kernel.killProcess(provPid)
        exit()
    }

}