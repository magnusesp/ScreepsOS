package screeps.os

import kotlin.coroutines.Continuation
import kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn

external interface Record<in K, out V>

@Suppress("NOTHING_TO_INLINE")
inline fun Any.asRecord() = this.unsafeCast<Record<String, Any?>>()

inline val <K, V>Record<K, V>.keys: Array<K> get () = js("Object").keys(this).unsafeCast<Array<K>>()
inline operator fun <K, V> Record<K, V>.get(key: K): V? = asDynamic()[key].unsafeCast<V?>()

external fun delete(p: dynamic): Boolean = definedExternally

class ProgramPersistence {
    companion object {
        private var persistenceCounter = 0
        private val persistedPrograms = mutableMapOf<Int, String>()
        private val persistedProgramVariables = mutableMapOf<Int, String>()

        fun persistProgram(continuation: Continuation<Unit>) : Int {
            val persistenceId = persistenceCounter++

            val plain = js("{}")
            plain.state_0 = continuation.asDynamic().state_0
            plain.original_name_1 = continuation.asDynamic().constructor.name
            plain.exceptionState_0 = continuation.asDynamic().exceptionState_0
            //  TODO could use something like this for logging
            //  plain.createdSavePointAt = Game.time

            continuation.asRecord().keys.toList()
                    .onEach { println("Looking at key $it") }
                    .filter { it.startsWith("local") && !it.contains("this") }
                    .forEach {
                        val value = continuation.asRecord()[it].asDynamic()
                        plain[it] = value
                        if (js("typeof value") == "object" && value.constructor.name != "Object") {
                            plain["$it\$prototype\$name"] = value.constructor.name
                    }
            }

            val localVariables = js("{}")

            val tmp = continuation.asRecord()["\$this"]
            tmp
                    ?.asRecord()
                    ?.keys
                    ?.toList()
                    ?.forEach {
                        val value = tmp.asRecord()[it]
                        println("\$this : $it -> $value")

                        if(!value.toString().contains("object"))
                        localVariables[it] = value
                    }

            val serialized = JSON.stringify(plain as? Any)
            persistedPrograms[persistenceId] = serialized.asDynamic()

            persistedProgramVariables[persistenceId] = JSON.stringify(localVariables as? Any).asDynamic()

            println("Persisted program with id $persistenceId : $serialized and ${persistedProgramVariables[persistenceId]}")

            return persistenceId
        }

        fun restoreProgram(continuation: Continuation<Unit>) {
            val process = continuation.context[Process.Key]
                    ?: throw NoProcessContextException("Continuation $continuation is missing a Process context")

            val persistenceId = process.getPersistenceId()

            println("Process ${process.pid} requests restore from $persistenceId")

            val oldState = persistedPrograms.remove(persistenceId)
            val persistedVariables = JSON.parse<Any>(persistedProgramVariables.remove(persistenceId) ?: "{}")

            if (oldState == null) {
                println("No such state $persistenceId")
                return
            }

            val deserializedState = JSON.parse<Any>(oldState)

            println("Deserialized state: $deserializedState")

            val resumeFromState = deserializedState.asDynamic().state_0
            val currentState = continuation.asDynamic().state_0

            if(currentState >= resumeFromState)
                return // we would go backwards in time

            restoreState(continuation, deserializedState, persistedVariables)
        }

        private fun restoreState(continuation: Continuation<Unit>, persistedState: Any, persistedVariables: Any) {
            js("Object").assign(continuation, persistedState)
            js("Object").assign(continuation.asDynamic()["\$this"], persistedVariables)

            val evalfn: (String) -> dynamic = ::eval

            persistedState.asRecord().keys.forEach {
                if (it.endsWith("\$prototype\$name")) {
                    delete(continuation.asDynamic()[it])
                    val value = persistedState.asRecord()[it]
                    val constructor = evalfn("$value")
                    val propertyName = it.removeSuffix("\$prototype\$name")
                    if (constructor?.prototype != null) {
                        val realValue = continuation.asDynamic()[propertyName]
                        js("Object").setPrototypeOf(realValue, constructor.prototype)
                    } else {
                        println("unable to restore prototype $value to $propertyName")
                    }
                }
            }

        }
    }
}

suspend fun persistProgram(): Unit = suspendCoroutineUninterceptedOrReturn<Unit> { continuation ->
    val process = continuation.context[Process.Key]
            ?: throw NoProcessContextException("Continuation $continuation is missing a Process context")

    println("Process ${process.pid} requests persisting")

    val persistenceId = ProgramPersistence.persistProgram(continuation)
    process.setPersistenceId(persistenceId)
}

suspend fun restoreIfNecessary() : Unit = suspendCoroutineUninterceptedOrReturn<Unit> { ProgramPersistence.restoreProgram(it) }
