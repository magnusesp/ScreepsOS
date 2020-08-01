package screeps.os

class AccumulatorProgram : SleepingProgram() {

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
        val accumulatorProgram = AccumulatorProgram()
        val accPid = Kernel.kernel.spawnProcess(accumulatorProgram, 10)

        println("${getProgramName()} Started accumulator program, going to sleep")
        sleep(5)

        val numberOfExecutionIdsToRequest = 5
        println("${getProgramName()} Going to request $numberOfExecutionIdsToRequest execution ids")

        ids.addAll(accumulatorProgram.getXExecutionIds(numberOfExecutionIdsToRequest))

        println("${getProgramName()} Currently have the following ids: $ids")

        Kernel.kernel.killProcess(accPid)
        exit()
    }

    fun getIds() = ids
}