    package screeps.os

import kotlin.coroutines.Continuation
import kotlin.coroutines.createCoroutine
import kotlin.coroutines.resume

open class Kernel(private val scheduler: Scheduler, private val tickFunction: () -> Int) {
    private var tick = 0

    private var nextPid = 0
    private val continuations = mutableMapOf<Int, Continuation<Unit>>()
    private val processTable = mutableMapOf<Int, Process>()

    fun spawnProcess(program: Program, priority: Int, restorePersistenceId: Int? = null): Int {
        val process = Process(nextPid++, priority, scheduler, restorePersistenceId)
        processTable[process.pid] = process

        program.setProcess(process)

        val body: suspend () -> Unit = {
            try {
                program.execute()
            } catch (e: Exception) {
                // TODO Handle exceptions somehow
                println("Got Exception $e")
                program.setException(e)
            }
        }
        continuations[process.pid] = body.createCoroutine(Continuation(process) {})

        scheduler.addProcess(process)

        return process.pid
    }

    fun loop() {
        tick = tickFunction.invoke()

        scheduler.preLoopSetup()

        var pid: Int? = scheduler.getNextPid()

        while(pid != null) {
            val cont = continuations.remove(pid)
                    ?: throw NoSuchProcessException("Kernel doesn't have a continuation for pid $pid")

            processTable[pid]?.setState(Process.State.RUNNING)
            cont.resume(Unit)

            // If the process doesn't store a new continuation it's done
            if(!continuations.containsKey(pid))
                killProcess(pid)

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

        val process = processTable[pid]
                ?: throw NoSuchProcessException("Process with pid $pid is not in the process table at kill time")

        process.setState(Process.State.KILLED)
        scheduler.removeProcess(process)
    }

    fun getTick() = tick

    companion object {
        private var _kernel: Kernel? = null

        val kernel: Kernel
            get() = _kernel ?: throw NoKernelSetException()

        fun create(scheduler: Scheduler, tickFunction: () -> Int) : Kernel {
            _kernel = Kernel(scheduler, tickFunction)
            return kernel
        }
    }
}

class NoKernelSetException : Exception("Kernel hasn't been set")
class NoSuchProcessException(message: String) : Exception(message)
class NoProcessContextException(message: String) : Exception(message)