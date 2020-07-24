package screeps.os

enum class SignalType {
    EXCEPTION,
    EXIT,
    READY,
    SLEEP,
    WAIT,
}

abstract class Signal(val type: SignalType) {
    override fun toString(): String {
        return "Signal $type"
    }
}
class ExceptionSignal(val exception: Exception) : Signal(SignalType.EXCEPTION) {
    override fun toString() = super.toString() + " ($exception)"
}
class ExitSignal(val exitCode: Int) : Signal(SignalType.EXIT) {
    override fun toString() = super.toString() + " ($exitCode)"
}

class ReadySignal : Signal(SignalType.READY)

class SleepSignal : Signal(SignalType.SLEEP)

class WaitSignal(val conditionId: Long) : Signal(SignalType.WAIT) {
    override fun toString() = super.toString() + " ($conditionId)"
}
