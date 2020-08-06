package screeps.os.examples

import screeps.os.Kernel
import screeps.os.Process
import screeps.os.Scheduler

class ExampleScheduler(private val mockGameObject: MockGameObject): Scheduler() {
    private val runQueue = mutableListOf<Process>() // TODO Should be a heap
    private val sleepingProcesses = mutableSetOf<Process>()

    override fun addProcess(process: Process) {
        runQueue.add(process)
        reorderRunQueue()
    }

    override fun removeProcess(process: Process) {
        runQueue.remove(process)
        sleepingProcesses.remove(process)
    }

    override fun putProcessToSleep(process: Process) {
        runQueue.remove(process)
        sleepingProcesses.add(process)

    }

    override fun processChangedPriority() = reorderRunQueue()

    override fun preLoopSetup() {
        mockGameObject.cpu = 0
        wakeSleepingProcesses()
    }

    private fun shouldContinue() = mockGameObject.cpu++ < mockGameObject.cpuLimit

    override fun getNextPid() = if(shouldContinue()) runQueue.firstOrNull()?.pid else null

    private fun reorderRunQueue() = runQueue.sortBy { it.getPriority() }

    private fun wakeSleepingProcesses() {
        val wakeUpThese = sleepingProcesses
                .filter { it.readyAt() <= Kernel.kernel.getTick() }
                .onEach { it.setState(Process.State.READY) }

        sleepingProcesses.removeAll(wakeUpThese)
        runQueue.addAll(wakeUpThese)
        reorderRunQueue()
    }
}

interface MockGameObject {
    var tick: Int
    var cpu: Int
    var cpuLimit: Int
}
