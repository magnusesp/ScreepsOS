package screeps.os

abstract class Process(private var pri: Int) {

    private val pid = 0
    private var wakeUpAt = 0

    private var parent: Process? = null
    fun getParent() = parent
    fun setParent(process: Process) {
        parent = process
    }

    abstract fun ready(): Boolean
    abstract fun run() : Signal

    fun getPid() = pid
    fun getPriority() = pri
    fun readyAt() = wakeUpAt
    fun sleepUntil(until: Int) {
        wakeUpAt = until
    }

    open fun destructor() {}
}