package screeps.os

import kotlinx.coroutines.*
import kotlin.coroutines.*

object ScreepsCoroutineContext : CoroutineContext by Job() + ScreepsCoroutineDispatcher + ScreepsCoroutineContextElement

object ScreepsScope: CoroutineScope {
    override val coroutineContext: CoroutineContext = ScreepsCoroutineContext
}

object ScreepsCoroutineContextKey : CoroutineContext.Key<CoroutineContext.Element>

val ScreepsCoroutineContextElement: CoroutineContext.Element = object : CoroutineContext.Element {
    override val key: CoroutineContext.Key<*> = ScreepsCoroutineContextKey
}

object ScreepsCoroutineDispatcher: CoroutineDispatcher() {

    val stored = HashMap<CoroutineContext, Runnable>()

    fun startGameloop(){
        TestGame.cpu = 0
    }

    fun finishGameloop(){
    }

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        println("dispatch called")
        val contextElement = context[ScreepsCoroutineContextKey]
        val suspendedLastTick = stored[contextElement!!] // not nice
        println("suspendedLastTick=${suspendedLastTick} context=${context} contextElement=${contextElement}")
        if (suspendedLastTick != null){
            println("resuming stored coroutine")
            stored.remove(contextElement)
            suspendedLastTick.run()
        } else if (TestGame.cpu <= 3) {
            block.run()
        } else {
            println("storing")
            stored[contextElement] = block
        }
    }
}

fun gameloop(body: suspend () -> Unit){
    println("starting game loop")
    ScreepsCoroutineDispatcher.startGameloop()
    ScreepsScope.launch {
        body()
    }
    ScreepsCoroutineDispatcher.finishGameloop()
}

object TestGame {
    var cpu = 0
}

suspend fun doSomething() : Int{
    println("before yield")
    yield()
    println("after yield")
    println("doSomething() called at TestGame.cpu=${++TestGame.cpu}")
    return 14
}

suspend fun doSomethingElse() : Int {
    println("before yield")
    yield()
    println("after yield")
    println("doSomethingElse() called at TestGame.cpu=${++TestGame.cpu}")
    return 14
}