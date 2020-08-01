package screeps.os

import kotlin.coroutines.suspendCoroutine


abstract class Program {
    open fun getProgramName() = "${this::class.simpleName} (${_process?.pid})"

    private var _process: Process? = null
    private val process: Process
        get() = _process ?: throw ProcessNotSetException("Program ${getProgramName()} has no process")

    fun setProcess(proc: Process) {
        if(_process != null)
            throw ProcessAlreadySetException("Program ${getProgramName()} already has a process with pid ${process.pid} ")

        _process = proc
    }

    fun changePriority(priority: Int) = process.changePriority(priority)

    suspend fun yield(): Any? = suspendCoroutine { continuation ->
        Kernel.kernel.storeContinuation(continuation)
    }

    suspend fun sleep(ticks: Int) {
        process.sleepUntil(Kernel.kernel.getTick() + ticks)
        yield()
    }

    suspend fun wait(condition: () -> Boolean, checkInterval: Int = 1) {
        while (!condition.invoke())
            sleep(checkInterval)
    }

    abstract suspend fun execute()
}

class ProcessAlreadySetException(message: String) : Exception(message)
class ProcessNotSetException(message: String) : Exception(message)