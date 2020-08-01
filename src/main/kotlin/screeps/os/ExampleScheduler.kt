package screeps.os

import kotlin.coroutines.Continuation
import kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED

class ExampleScheduler(kernel: Kernel) : Scheduler(kernel) {
    private val processes = mutableMapOf<Int, Process>()
    private val runQueue = mutableListOf<Process>() // TODO Should be a heap
    private val sleepingProcesses = mutableSetOf<Process>()

    private lateinit var mockGameObject: MockGameObject
    fun setGameObject(mock: MockGameObject) {
        mockGameObject = mock

    }

    override fun addProcess(process: Process) {
        processes[process.pid]= process
        runQueue.add(process)
        reorderRunQueue()
    }

    override fun removeProcess(pid: Int) {
        val process = processes.remove(pid)

        runQueue.remove(process)
        sleepingProcesses.remove(process)
    }

    override fun putProcessToSleep(process: Process) {
        runQueue.remove(process)
        sleepingProcesses.add(process)

    }

    override fun preLoopSetup() {
        mockGameObject.cpu = 0
        wakeSleepingProcesses()
    }

    private fun shouldContinue() = mockGameObject.cpu++ < mockGameObject.cpuLimit

    override fun getNextPid() = if(shouldContinue()) runQueue.firstOrNull()?.pid else null

    override fun runOrSuspend(continuation: Continuation<Any?>): Any? {
        println("inside runOrSuspend")

        // get data about the specific coroutine from the continuation
        // this could be used to treat some coroutines differently
        val process = continuation.context[Process.Key]
                ?: throw NoProcessContextException("Continuation $continuation is missing a Process context")

        if(!shouldContinue() || runQueue.firstOrNull()?.pid != process.pid) {
            println("suspending pid ${process.pid}")
            kernel.storeContinuation(continuation)
            return COROUTINE_SUSPENDED
        }

        println("continuing to run pid ${process.pid}")

        return Unit
    }

    private fun reorderRunQueue() = runQueue.sortBy { it.getPriority() }

    private fun wakeSleepingProcesses() {
        val wakeUpThese = sleepingProcesses.filter { it.readyAt() <= kernel.getTick() }

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
