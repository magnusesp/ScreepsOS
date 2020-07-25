package screeps.os

import kotlinx.coroutines.*
import kotlin.coroutines.*

object ScreepsOSCoroutineContext : CoroutineContext by Job() + ScreepsOSCoroutineDispatcher + ScreepsOSCoroutineContextElement

object ScreepsOSScope: CoroutineScope {
    override val coroutineContext: CoroutineContext = ScreepsOSCoroutineContext
}

object ScreepsOSCoroutineContextKey : CoroutineContext.Key<CoroutineContext.Element>

val ScreepsOSCoroutineContextElement: CoroutineContext.Element = object : CoroutineContext.Element {
    override val key: CoroutineContext.Key<*> = ScreepsOSCoroutineContextKey
}

object ScreepsOSCoroutineDispatcher: CoroutineDispatcher() {

    val stored = HashMap<CoroutineContext, Runnable>()

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        val runnable = Kernel.getScheduler().getNextJob(context, block)

        runnable?.run()

//        println("dispatch called")

//        val contextElement = context[ScreepsOSCoroutineContextKey]

//        val suspendedLastTick = stored[contextElement!!] // not nice

//        println("suspendedLastTick=${suspendedLastTick} context=${context} contextElement=${contextElement}")

//        if (suspendedLastTick != null){
//            println("resuming stored coroutine")
//            stored.remove(contextElement)
//            suspendedLastTick.run()
//        } else if (true) {

//            block.run()
//        } else {

//            println("storing")
//            stored[contextElement] = block
//        }
    }
}

interface ScreepsOSScheduler {
    fun getNextJob(context: CoroutineContext, block: Runnable): Runnable?
}