import java.util.ArrayList;

public class Simulator_Interrupts {
	public static void main(String[] args) {
		ArrayList<Process> readyQueue = new ArrayList<Process>();
		ArrayList<Process> finishedProcesses = new ArrayList<Process>();
		
		int numProcesses = 15;
		double priorityRatio = .30; //Describes the chance that a new process created will be "important"
		int avgArrivalRate = 4; //On average, a new process will arrive every 4 ms
		double timeslice = 5;
		int contextSwitchOverhead = 1;
		
		double currentTime = 0; //This tracks the time the pretend CPU is operating at
	
		Process dummyProcess = new Process(Integer.MAX_VALUE, 0); //A fake process so currentProcess always has something to point to. Priority set to lowest so it will never be selected.
		dummyProcess.burstTime = 0;
		Process currentProcess = dummyProcess; //The process that we pretend the CPU is currently operating on
		
		double arrivalTime = 0; //Tracks the times when processes are arriving
		
		//A bunch of variables to report statistics at the end
		double total_response_time_1 = 0;
		double total_response_time_9 = 0;
		int total_burst_time_1 = 0;
		int total_burst_time_9 = 0;
		int num_1 = 0;
		int num_9 = 0;
		int num_context_switches = 0;
		
		System.out.println("This will simulate the random arrival of 15 processes, of high and low priorities. ");
		System.out.println("In this version, a low priority process will be kicked off the processor as soon as a higher priority one arrives. ");
		System.out.println();
		
		do {					
			double firstArrivalTime = currentTime;
			
			while(arrivalTime <= currentTime + timeslice && numProcesses > 0) { //We look ahead to see what processes will be arriving within one timeslice of current time. This ensures any high priority processes coming in will be switched to as soon as possible
				
				Process newProcess; //Randomly selects the arrival time and priority of the new process (the burst time is also randomly selected, within the process class itself) 
				arrivalTime += (2 * avgArrivalRate) * Math.random();
				if(Math.random() < priorityRatio) {
					newProcess = new Process(1, arrivalTime);
					num_1++;
				} else {
					newProcess = new Process(9, arrivalTime);
					num_9++;
				}
				readyQueue.add(newProcess);
				numProcesses += -1;
				if(firstArrivalTime == currentTime) {
					firstArrivalTime = arrivalTime;
				}
			}

			int selectedIndex = selectFromQueue(readyQueue, currentProcess, currentTime); 
			if(selectedIndex != -1) { //It found a process other than the current one, so we are context switching
				if(currentProcess.priority < Integer.MAX_VALUE) { //We don't want to add the dummy process into the queue
					readyQueue.add(currentProcess);
				} 
				currentProcess = readyQueue.get(selectedIndex);
				readyQueue.remove(selectedIndex);
				currentTime += contextSwitchOverhead;
				num_context_switches++;
			} else {
				if(currentProcess.priority == Integer.MAX_VALUE) { //Want to make sure we don't operate on the dummy process
					currentTime = firstArrivalTime; //This generally occurs when the queue is empty but a process is arriving, makes current time that arrival time so it will start operating as soon as that first process arrives
					continue;
				}
			}
			
			double endingTime = timeslice;
			int interruptIndex = findInterrupt(readyQueue, currentProcess, currentTime);
			
			if(interruptIndex != -1) { //This means that a process with a higher priority than the current one is arriving soon
				Process interruptProcess = readyQueue.get(interruptIndex);
				if(interruptProcess.arrivalTime - currentTime < timeslice) { //If it arrives before the timeslice would naturally expire, the current process needs to be stopped sooner than that
					endingTime = interruptProcess.arrivalTime - currentTime;
				}
			}
			
			if(currentProcess.responseTime == 0) { //Will be true the first time a given process is selected
				currentProcess.responseTime = currentTime - currentProcess.arrivalTime;
				if(currentProcess.priority == 1) {
					total_response_time_1 += currentProcess.responseTime;
					total_burst_time_1 += currentProcess.burstTime;
				} else {
					total_response_time_9 += currentProcess.responseTime;
					total_burst_time_9 += currentProcess.burstTime;
				}
			}
			
			currentTime += operateOnProcess(currentProcess, endingTime, currentTime);
			
			if(currentProcess.burstTime == 0) { //The process has completed
				currentProcess.completionTime = currentTime;
				addFinishedProcess(finishedProcesses, currentProcess);
				currentProcess = dummyProcess; //The process being worked on has finished, assigning the dummy process makes it choose a new one next iteration.
			}
			
		} while(readyQueue.size() > 0 || numProcesses > 0);
		
		if(currentProcess.burstTime > 0) { //The queue is empty, but there might still be one last unfinished process on the CPU
			while(currentProcess.burstTime > 0) {
				currentTime += operateOnProcess(currentProcess, timeslice, currentTime);
			}
			currentProcess.completionTime = currentTime;
			addFinishedProcess(finishedProcesses, currentProcess);
		}
		
		
		//Everything below here is simply for displaying data
		System.out.println("PID  Priority  Burst Time  Arrival Time  Completion Time  Response Time  Turnaround Time");
		for(int i = 0; i < finishedProcesses.size(); i++) {
			Process process = finishedProcesses.get(i);
			process.calculateTimes();
			String spaces1 = "       ";
			String spaces2 = "        ";
			String spaces3 = "        ";
			if(process.arrivalTime < 10) {
				spaces1 += " ";
			}
			if(process.completionTime < 100) {
				spaces2 += " ";
			}
			if(process.responseTime < 10) {
				spaces3 += " ";
			}
			if(process.responseTime < 100) {
				spaces3 += " ";
			}
			if(i < 10) {
				System.out.println(i + "        " + process.priority + "        " + process.init_burstTime + "        " + String.format("    %.2f %s %.2f %s %.2f %s %.2f", process.arrivalTime, spaces1, process.completionTime, spaces2, process.responseTime, spaces3, process.turnaroundTime));
			} else {
				System.out.println(i + "       " + process.priority + "        " + process.init_burstTime + "        " + String.format("    %.2f %s %.2f %s %.2f %s %.2f", process.arrivalTime, spaces1, process.completionTime, spaces2, process.responseTime, spaces3, process.turnaroundTime));
			}
		}
		
		double percentage_time_context_switching = (num_context_switches * contextSwitchOverhead * 100) / currentTime;
		
		System.out.println();
		System.out.println("                            High Priority         Low Priority");
		System.out.println("Number of processes:             " + num_1 + "                     " + num_9);
		System.out.println("Average burst time:       " + String.format("       %.2f                 %.2f", (double)(total_burst_time_1) / (num_1), (double)(total_burst_time_9) / (num_9)));
		System.out.println("Average response time:    " + String.format("       %.2f                  %.2f", (total_response_time_1) / (num_1), (total_response_time_9) / (num_9)));
		
		
		System.out.println();
		System.out.println("Other stats: ");
		System.out.println("Time to complete all " + (num_9 + num_1) + " processes: " + String.format("%.2f", currentTime));
		System.out.println("Number of context switches: " + num_context_switches);
		System.out.println("Overhead time of context switch: " + contextSwitchOverhead);
		System.out.println("% of time context switching: " + String.format("%.2f", percentage_time_context_switching));
	}
	
	//Inserts a finished process into the array list based on its arrival time
	static void addFinishedProcess(ArrayList<Process> finishedProcesses, Process newProcess) {
		for(int i = 0; i < finishedProcesses.size(); i++) {
			if(newProcess.arrivalTime < finishedProcesses.get(i).arrivalTime) {
				finishedProcesses.add(i, newProcess);
				return;
			}
		}
		finishedProcesses.add(newProcess);
	}
	
	//Picks the index of the highest priority process that has already arrived
	static int selectFromQueue(ArrayList<Process> readyQueue, Process currentProcess, double currentTime) {
		int index = -1;
		int minPriority = currentProcess.priority;
		for(int i = 0; i < readyQueue.size(); i++) {
			if(readyQueue.get(i).arrivalTime <= currentTime && readyQueue.get(i).priority < minPriority) { 
				index = i;
				minPriority = readyQueue.get(i).priority;
			}
		}
		return index;
	}
	
	//Just like selectFromQueue, but will pick a process that is arriving very soon
	static int findInterrupt(ArrayList<Process> readyQueue, Process currentProcess, double currentTime) {
		int index = -1;
		int minPriority = currentProcess.priority;
		for(int i = 0; i < readyQueue.size(); i++) {
			if(readyQueue.get(i).priority < minPriority) { 
				index = i;
				minPriority = readyQueue.get(i).priority;
			}
		}
		return index;
	}
	
	//This method pretends to be the CPU when it is operating on a process. Updates the completion status of currentProcess, and returns the amount to increment the time by
	static double operateOnProcess(Process currentProcess, double timeslice, double currentTime) {
		if(currentProcess.burstTime <= timeslice) {
			double returnVal = currentProcess.burstTime;
			currentProcess.burstTime = 0;
			return returnVal;
		}
		currentProcess.burstTime -= timeslice;
		return timeslice;
	}
}
