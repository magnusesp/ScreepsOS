package screeps.os

import kotlin.coroutines.Continuation
import kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn


abstract class Program {
    private var _process: Process? = null
    private val process: Process
        get() = _process ?: throw ProcessNotSetException("Program ${getProgramName()} has no process")

    open fun getProgramName() = this::class.simpleName

    fun setProcess(proc: Process) {
        if(_process != null)
            throw ProcessAlreadySetException("Program ${getProgramName()} already has a process with pid ${process.pid} ")

        _process = proc
    }

    suspend fun yield(): Unit = suspendCoroutineUninterceptedOrReturn { continuation ->
        val process = continuation.context[Process.Key]
                ?: throw NoProcessContextException("Continuation $continuation is missing a Process context")
                        // todo unssafeCast

        process.runOrSuspend(continuation.unsafeCast<Continuation<Any?>>())
    }

    suspend fun sleep(ticks: Int) {
        process.sleepUntil(Kernel.kernel.getTick() + ticks)
        yield()
    }

    suspend fun wait(condition: () -> Boolean, checkInterval: Int = 1) {
        while (!condition.invoke()) {
            sleep(checkInterval)
        }
    }

    abstract suspend fun execute()
}

class ProcessAlreadySetException(message: String) : Exception(message)
class ProcessNotSetException(message: String) : Exception(message)