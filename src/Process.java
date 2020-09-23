
public class Process {
	int priority;
	int burstTime = 10;
	double arrivalTime;
	double responseTime = 0;
	double completionTime;
	double turnaroundTime;
	
	
	public Process(int prio, double time) {
		arrivalTime = time;
		priority = prio;
	}
	
	void calculateTimes() {
		turnaroundTime = completionTime - arrivalTime;
	}
}

