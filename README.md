# ScreepsOS
An Kotlin coroutine based operatingsystem framework for Screeps


### How to get started
You need to:
- Implement your own `Scheduler` (feel free to base it on the provided `ExampleScheduler`)
- Give the function to know which tick it is (`{ Game.time }` will do)
- Spawn your first process:

```
val scheduler = YourScheduler()

kernel = Kernel.create(scheduler) { Game.time } 

val mainProgram = MainProgram()

kernel.spawnProcess(mainProgram, priority = 1)

```

Then you can start looping: 
```
fun mainloop() {
  kernel.loop()
}
```

### Implemented functions
- `yield()` Returns control back to the kernel, letting your `Scheduler` decide what to (if anything) to run next. Used for splitting up heavy tasks over several ticks
- `sleep(10)` - Puts the process to sleep for `10` ticks, and returns control to the kernel
- `wait({Intel.hasVisibility(roomName)}, , checkInterval = 5)` - Waits until the condition is true before continuing. In this example we wait for `{Intel.hasVisibility(roomName)}` to become true, checking every `5` ticks.
- `exit()` - Exits the `Process`. The `Program` will still be accessable until all references is gone and it is removed by the GC.


To utilize `yield()`, `sleep()`, `wait()` and `exit()` you need the `suspend` keyword on your function call-chain outwards from `Program.execute()`.
