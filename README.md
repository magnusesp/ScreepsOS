# ScreepsOS
An Kotlin coroutine based operatingsystem framework for Screeps

All you need to do is to implement your own `Scheduler`, give the function to know which tick it is (`{ Game.time }` will do) and spawn your first process:

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


