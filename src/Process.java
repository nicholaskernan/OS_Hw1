
public class Process {
	int priority;
	int burstTime = 10 + (int) Math.round((Math.random() * 4));
	int init_burstTime = burstTime;
	double arrivalTime;
	double responseTime = 0;
	double completionTime;
	double turnaroundTime;
	double waitingTime;
	
	public Process(int prio, double time) {
		arrivalTime = time;
		priority = prio;
	}
	
	void calculateTimes() {
		turnaroundTime = completionTime - arrivalTime;
		waitingTime = turnaroundTime - init_burstTime;
	}
}

