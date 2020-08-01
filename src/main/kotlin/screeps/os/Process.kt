package screeps.os

import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext

class Process(val pid: Int, private var pri: Int, private val scheduler: Scheduler) : CoroutineContext.Element {
    object Key : CoroutineContext.Key<Process>

    override val key = Key

    private var wakeUpAt = 0

    fun getPriority() = pri
    fun readyAt() = wakeUpAt
    fun sleepUntil(until: Int) {
        wakeUpAt = until
        scheduler.putProcessToSleep(this)
    }

    fun runOrSuspend(continuation: Continuation<Any?>) = scheduler.runOrSuspend(continuation)
}