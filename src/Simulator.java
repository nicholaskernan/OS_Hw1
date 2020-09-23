import java.util.ArrayList;

public class Simulator {
	public static void main(String[] args) {
		ArrayList<Process> readyQueue = new ArrayList<Process>();
		ArrayList<Process> finishedProcesses = new ArrayList<Process>();
		
		int numProcesses = 15;
		double priorityRatio = .30; //Describes the chance that a new process created will be "important"
		int avgArrivalRate = 4; //On average, a new process will arrive every 4 ms
		int timeslice = 5;
		int contextSwitchOverhead = 1;
		
		double currentTime = 0;
		
		boolean firstTime = true;
		Process dummyProcess = new Process(Integer.MAX_VALUE, 0); //A fake process so currentProcess always has something to point to. Priority set to lowest so it will never be selected.
		dummyProcess.burstTime = 0;
		Process currentProcess = dummyProcess; //The process that we pretend the CPU is currently operating on
		
		double arrivalTime = 0;
		
		double total_response_time_1 = 0;
		double total_response_time_9 = 0;
		
		int num_1 = 0;
		int num_9 = 0;
		int num_context_switches = 0;
		
		do {		
			//System.out.println("Current time: " + currentTime);
		
			if(numProcesses > 0) {
				arrivalTime += (2 * avgArrivalRate) * Math.random();
				
				Process newProcess;
				if(Math.random() < priorityRatio) {
					newProcess = new Process(1, arrivalTime);
					num_1++;
				} else {
					newProcess = new Process(9, arrivalTime);
					num_9++;
				}
				//System.out.println("New process arriving at " + String.format("%.2f", arrivalTime) + " priority level: " + newProcess.priority);	
				readyQueue.add(newProcess);
				numProcesses += -1;
			}
			
			//update current time to arrival time?
			
			//System.out.println("Queue size: " + readyQueue.size());
			int selectedIndex = selectFromQueue(readyQueue, currentProcess, currentTime);
			if(selectedIndex != -1) {
				if(currentProcess.priority < Integer.MAX_VALUE) { //We don't want to add the dummy process into the queue
					readyQueue.add(currentProcess);
					//System.out.println("Switching to a higher priority process at time " + currentTime);
				} 
				currentProcess = readyQueue.get(selectedIndex);
				readyQueue.remove(selectedIndex);
				currentTime += contextSwitchOverhead;
				//System.out.println("Context switching");
				num_context_switches++;
			} else {
				if(currentProcess.priority == Integer.MAX_VALUE) {
					currentTime = arrivalTime;
					continue;
				}
			}
			//System.out.println("Operating at time: " + String.format("%.2f", currentTime) + " Arrival time of process: " + String.format("%.2f", currentProcess.arrivalTime));
			//System.out.println("Burst time: " + currentProcess.burstTime);
			currentTime += operateOnProcess(currentProcess, timeslice, currentTime);
			if(currentProcess.responseTime == 0) {
				currentProcess.responseTime = currentTime - currentProcess.arrivalTime;
				if(currentProcess.priority == 1) {
					total_response_time_1 += currentProcess.responseTime;
				} else {
					total_response_time_9 += currentProcess.responseTime;
				}
			}
			
			if(currentProcess.burstTime == 0) {
				currentProcess.completionTime = currentTime;
				addFinishedProcess(finishedProcesses, currentProcess);
				currentProcess = dummyProcess; //The process being worked on has finished, assigning the dummy process makes it choose a new one next iteration.
				
				//System.out.println("Process has finished at time: " + String.format("%.2f", currentTime)); 
			}
			
		} while(readyQueue.size() > 0);
		
		if(currentProcess.burstTime > 0) {
			while(currentProcess.burstTime > 0) {
				//System.out.println("Operating at time: " + String.format("%.2f", currentTime) + " Arrival time of process: " + String.format("%.2f", currentProcess.arrivalTime));
				//System.out.println("Burst time: " + currentProcess.burstTime);
				currentTime += operateOnProcess(currentProcess, timeslice, currentTime);
			}
			currentProcess.completionTime = currentTime;
			addFinishedProcess(finishedProcesses, currentProcess);
			//System.out.println("Process has finished at time: " + String.format("%.2f", currentTime)); 
		}
		
		System.out.println("PID  Priority  Arrival Time  Completion Time  Response Time  Turnaround Time ");
		for(int i = 0; i < finishedProcesses.size(); i++) {
			Process process = finishedProcesses.get(i);
			process.calculateTimes();
			String spaces1 = "       ";
			String spaces2 = "        ";
			if(process.arrivalTime < 10) {
				spaces1 += " ";
			}
			if(process.completionTime < 100) {
				spaces2 += " ";
			}
			if(i < 10) {
				System.out.println(i + "        " + process.priority + "        " + String.format("%.2f %s %.2f %s %.2f            %.2f", process.arrivalTime, spaces1, process.completionTime, spaces2, process.responseTime, process.turnaroundTime));
			} else {
				System.out.println(i + "       " + process.priority + "        " + String.format("%.2f %s %.2f %s %.2f            %.2f", process.arrivalTime, spaces1, process.completionTime, spaces2, process.responseTime, process.turnaroundTime));
			}
		}
		
		double percentage_time_context_switching = (num_context_switches * contextSwitchOverhead * 100) / currentTime;
		System.out.println("(" + num_1 + ") Processes with high priority: average response time: " + String.format("%.2f", (total_response_time_1) / (num_1)));
		System.out.println("Processes with low priority: average response time: " + String.format("%.2f", (total_response_time_9) / (num_9)));
		System.out.println("Number of context switches: " + num_context_switches + ", % of time context switching: " + String.format("%.2f", percentage_time_context_switching));
	}
	
	static void addFinishedProcess(ArrayList<Process> finishedProcesses, Process newProcess) {
		for(int i = 0; i < finishedProcesses.size(); i++) {
			if(newProcess.arrivalTime < finishedProcesses.get(i).arrivalTime) {
				finishedProcesses.add(i, newProcess);
				return;
			}
		}
		finishedProcesses.add(newProcess);
	}
	
	static int selectFromQueue(ArrayList<Process> readyQueue, Process currentProcess, double currentTime) {
		int index = -1;
		int minPriority = currentProcess.priority;
		for(int i = 0; i < readyQueue.size(); i++) {
			//Of processes that have already arrived, picks the highest priority one (which is actually the lowest number)
			if(readyQueue.get(i).arrivalTime <= currentTime && readyQueue.get(i).priority < minPriority) { 
				index = i;
				minPriority = readyQueue.get(i).priority;
			}
		}
		return index;
	}
	
	static double operateOnProcess(Process currentProcess, int timeslice, double currentTime) {
		if(currentProcess.burstTime <= timeslice) {
			double returnVal = currentProcess.burstTime;
			currentProcess.burstTime = 0;
			return returnVal;
		}
		currentProcess.burstTime -= timeslice;
		return timeslice;
	}
}
