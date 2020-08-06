package screeps.os

import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.suspendCoroutine

class Process(val pid: Int, private var priority: Int, private val scheduler: Scheduler) : CoroutineContext.Element {
    object Key : CoroutineContext.Key<Process>

    override val key = Key

    enum class State {
        NEW,
        READY,
        RUNNING,
        SLEEPING,
        WAITING,
        EXIT,
        KILLED,
        EXCEPTION
    }

    private var state = State.NEW

    fun getState() = state
    fun setState(newState: State) {
        state = when {
            state == State.EXCEPTION -> state

            state == State.EXIT && newState != State.EXCEPTION -> state

            else -> newState
        }
    }

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

suspend fun yield(): Any? = suspendCoroutine { continuation ->
    val process = continuation.context[Process.Key]
            ?: throw NoProcessContextException("Continuation $continuation is missing a Process context")

    process.setState(Process.State.READY)
    Kernel.kernel.storeContinuation(continuation)
}

suspend fun sleep(ticks: Int, state: Process.State = Process.State.SLEEPING): Any? = suspendCoroutine { continuation ->
    val process = continuation.context[Process.Key]
            ?: throw NoProcessContextException("Continuation $continuation is missing a Process context")

    process.setState(state)
    process.sleepUntil(Kernel.kernel.getTick() + ticks)
    Kernel.kernel.storeContinuation(continuation)
}

suspend fun wait(condition: () -> Boolean, checkInterval: Int = 1) {
    while (!condition.invoke())
        sleep(checkInterval, state = Process.State.WAITING)
}

suspend fun exit(): Unit = suspendCoroutine { continuation ->
    val process = continuation.context[Process.Key]
            ?: throw NoProcessContextException("Continuation $continuation is missing a Process context")

    process.setState(Process.State.EXIT)
    process.exit()
}