package screeps.os

import kotlin.coroutines.Continuation
import kotlin.coroutines.createCoroutine
import kotlin.coroutines.resume

open class Kernel(private val tickFunction: () -> Int) {
    private var tick = 0

    private var nextPid = 0
    private val continuations = mutableMapOf<Int, Continuation<Any?>>()

    private var _scheduler: Scheduler? = null
    private val scheduler: Scheduler
        get() = _scheduler ?: throw NoSchedulerSetException()

    fun setScheduler(scheduler: Scheduler) {
        _scheduler = scheduler
    }

    fun spawnProcess(program: Program, priority: Int): Int {
        val process = Process(nextPid++, priority, scheduler)

        program.setProcess(process)

        val  body: suspend () -> Unit = { program.execute() }
        continuations[process.pid] = body.createCoroutine(Continuation(process) {}).unsafeCast<Continuation<Any?>>()

        scheduler.addProcess(process)

        return process.pid
    }

    fun loop() {
        tick = tickFunction.invoke()

        scheduler.preLoopSetup()

        var pid: Int? = scheduler.getNextPid()

        while(pid != null) {
            continuations[pid]?.resume(Unit)
                    ?: throw NoSuchProcessException("Kernel doesn't have a continuation for pid $pid")

            pid = scheduler.getNextPid()
        }
    }

    fun storeContinuation(continuation: Continuation<Any?>) {
        val process = continuation.context[Process.Key]
                ?: throw NoProcessContextException("Continuation $continuation is missing a Process context")

        continuations[process.pid] = continuation
    }

    fun killProcess(pid: Int) {
        continuations.remove(pid)
        scheduler.removeProcess(pid)
    }

    fun getTick() = tick

    companion object {
        private var _kernel: Kernel? = null

        val kernel: Kernel
            get() = _kernel ?: throw NoKernelSetException()

        fun create(tickFunction: () -> Int) : Kernel {
            _kernel = Kernel(tickFunction)
            return kernel
        }
    }
}

class NoKernelSetException : Exception("Kernel hasn't been set")
class NoSchedulerSetException : Exception("Kernel has no scheduler")
class NoSuchProcessException(message: String) : Exception(message)
class NoProcessContextException(message: String) : Exception(message)