package screeps.os

import screeps.os.yield
import screeps.os.sleep
import screeps.os.wait
import screeps.os.exit

abstract class TestProgram : Program() {
    var executions = 0

    protected fun print(nextState: String) {
        println("${getProgramName()} executing for the ${++executions}. time. $nextState")
    }
}

open class SleepingProgram : TestProgram() {
    override suspend fun execute() {
        while(true) {
            print("Sleeping 1 tick")
            sleep(1)
        }
    }
}

class RunningProgram : TestProgram() {
    override suspend fun execute() {
        while(true) {
            print("Yielding")
            yield()
        }
    }
}

class WaitingProgram(private val condition: () -> Boolean) : TestProgram() {
    override suspend fun execute() {
        while(true) {
            print("Waiting")
            wait(condition)
            sleep(1)
        }
    }
}

class ExitingProgram(private val runFor: Int) : TestProgram() {
    override suspend fun execute() {
        while (true) {
            if(executions == runFor) {
                print("Exiting")
                exit()
            }
            print("Sleeping")
            sleep(1)
        }
    }

}
