package screeps.os

abstract class Scheduler(protected val kernel: Kernel) {
    abstract fun addProcess(process: Process)
    abstract fun removeProcess(pid: Int)
    abstract fun putProcessToSleep(process: Process)
    abstract fun processChangedPriority()

    abstract fun preLoopSetup()

    abstract fun getNextPid() : Int?
}