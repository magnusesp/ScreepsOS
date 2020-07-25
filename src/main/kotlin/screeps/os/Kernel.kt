package screeps.os

import kotlinx.coroutines.launch

class Kernel(
    private val tickUpdateFunction: () -> Int,
    private val loopFunction: suspend () -> Unit,
    private val dispatcherObject: ScreepsOSScheduler
) {

    fun loop() {
        currentTick = tickUpdateFunction.invoke()

        ScreepsOSScope.launch {
            loopFunction.invoke()
        }
    }


    // Ticks
    private var currentTick: Int = 0
    fun getTick() = currentTick

    // Dispatcher
    fun getDispatcher() = dispatcherObject

    companion object {
        private var kernel: Kernel? = null

        fun create(
            tickUpdateFunction: () -> Int,
            loopFunction: suspend () -> Unit,
            dispatcherObject: ScreepsOSScheduler) {
            kernel = Kernel(tickUpdateFunction, loopFunction, dispatcherObject)
        }

        fun loop() = kernel?.loop()
            ?: throw KernelNotCreatedException()

        fun getTick() = kernel?.getTick()
            ?: throw KernelNotCreatedException()

        fun getScheduler() = kernel?.getDispatcher()
            ?: throw KernelNotCreatedException()

        fun reset() {
            kernel = null
        }
    }

    class KernelNotCreatedException : Exception("No kernel created by create(), or it has been reset()")
}