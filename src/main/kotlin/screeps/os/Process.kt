package screeps.os

import kotlin.coroutines.CoroutineContext

class Process(val pid: Int, private var priority: Int, private val scheduler: Scheduler) : CoroutineContext.Element {
    object Key : CoroutineContext.Key<Process>

    override val key = Key

    fun getPriority() = priority
    fun changePriority(pri: Int) {
        priority = pri
        scheduler.processChangedPriority()
    }

    private var wakeUpAt = 0
    fun readyAt() = wakeUpAt
    fun sleepUntil(until: Int) {
        wakeUpAt = until
        scheduler.putProcessToSleep(this)
    }

    fun exit() = Kernel.kernel.killProcess(pid)
}