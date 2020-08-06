package screeps.os

abstract class Scheduler() {
    abstract fun addProcess(process: Process)
    abstract fun removeProcess(process: Process)
    abstract fun putProcessToSleep(process: Process)
    abstract fun processChangedPriority()

    abstract fun preLoopSetup()

    abstract fun getNextPid() : Int?
}