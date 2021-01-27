# Simulating-CPU-Scheduler

These files simulate the selection process of a CPU scheduler. 
To do so, 15 processes with random arrival times are created, about 30% of which will be of high priority.
Clearly, when the CPU has finished a process, it should pick any high priority processes next before attending to the low priority ones.
But there is an additional decision:
In "Simulator_Interrupts.java", if a new process is higher priority than the current one, the scheduler will immediately context-switch to it. 
In "Simulator.java", if a new process arriving is higher priority than the current one, the scheduler will wait until the time-slice for the current process has expired before switching.
Outputs will vary due to random chance, but one set of output images are attatched above. They will tend to fit the following patterns:
1. Response times will be much better for high priority processes than low priority ones, especially for Simulator_Interrupts.
2. The tradeoff is that more context swtiches will typically occur in Simulator_Interrupts. 
