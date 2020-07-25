package screeps.os

class ParentProcess(parent: Process?, pri: Int) : Process(pri) {
    private val children = mutableSetOf<Process>()
    private val runQueue = mutableListOf<Process>()
    private val sleepingChildren = mutableListOf<Process>()

    override fun ready() = runQueue.isNotEmpty()

    override fun run(): Signal {

        val process = runQueue.firstOrNull()
            ?: throw Exception("run() was called on ${this::class.simpleName} (${getPid()}) but the runQueue was empty")

        var signal: Signal

        try {
            signal = process.run()
        } catch (e: Exception) {
            signal = ExceptionSignal(e)
        }

        when (signal) {
            is ExceptionSignal -> {
                console.log("Process got exception (${signal.exception}")
                killProcess(process)
            }

            is ExitSignal -> killProcess(process)

            is ReadySignal -> { }

            is SleepSignal -> {
                runQueue.removeAt(0)
                sleepingChildren.add(process)
                reorderSleepingChildren()
            }

            is WaitSignal -> runQueue.removeAt(0)
        }

        return if (runQueue.isNotEmpty()) {
            ReadySignal()
        } else {
            val nextTick = sleepingChildren
                .firstOrNull()
                ?.readyAt()
                ?: Int.MAX_VALUE

            sleepUntil(nextTick)

            SleepSignal()
        }
    }

    fun addChild(process: Process) {
        process.setParent(this)
        children.add(process)
        runQueue.add(process)

        reorderRunQueue()
    }

    fun addChildren(processes: Set<Process>) {
        processes.forEach { it.setParent(this) }

        children.addAll(processes)
        runQueue.addAll(processes)

        reorderRunQueue()
    }

    private fun killProcess(process: Process) {
        process.destructor()

        children.remove(process)
        if(runQueue.remove(process) || sleepingChildren.remove(process))
            updateWakeUp()
    }

    private fun reorderRunQueue()
            = runQueue.sortBy { it.getPriority() }

    private fun reorderSleepingChildren() {
        sleepingChildren.sortBy { it.readyAt() }
        updateWakeUp()
    }

    private fun updateWakeUp() = when {
        runQueue.isNotEmpty() -> sleepUntil(-1)
        sleepingChildren.isNotEmpty() -> sleepUntil(sleepingChildren.first().readyAt())
        else -> sleepUntil(Int.MAX_VALUE)
    }
}
