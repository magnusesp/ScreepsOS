package screeps.os

import kotlinx.coroutines.launch

class Kernel(
    private val tickUpdateFunction: () -> Int,
    private val loopFunction: suspend () -> Unit,
    private val dispatcherScheduler: ScreepsOSScheduler
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
    fun getSchduler() = dispatcherScheduler

    companion object {
        private var kernel: Kernel? = null

        fun create(
            tickUpdateFunction: () -> Int,
            loopFunction: suspend () -> Unit,
            schedulerObject: ScreepsOSScheduler) {
            kernel = Kernel(tickUpdateFunction, loopFunction, schedulerObject)
        }

        fun loop() = kernel?.loop()
            ?: throw KernelNotCreatedException()

        fun getTick() = kernel?.getTick()
            ?: throw KernelNotCreatedException()

        fun getScheduler() = kernel?.getSchduler()
            ?: throw KernelNotCreatedException()

        fun reset() {
            kernel = null
        }
    }

    class KernelNotCreatedException : Exception("No kernel created by create(), or it has been reset()")
}