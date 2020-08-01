package screeps.os

import kotlin.coroutines.Continuation

abstract class Scheduler(protected val kernel: Kernel) {
    abstract fun addProcess(process: Process)
    abstract fun removeProcess(pid: Int)
    abstract fun putProcessToSleep(process: Process)
    abstract fun preLoopSetup()

    abstract fun getNextPid() : Int?
    abstract fun runOrSuspend(continuation: Continuation<Any?>): Any?
}