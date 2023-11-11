// Run() is called from Scheduling.main() and is where
// the scheduling algorithm written by the user resides.
// User modification should occur within the Run() function.

package OS_lab2;

import java.util.Vector;
import java.io.*;

public class SchedulingAlgorithm {

  public static Results Run(int runtime, Vector processVector, Results result) {
    int i = 0;
    int comptime = 0;
    int currentProcess = 0;
    int previousProcess = 0;
    int size = processVector.size();
    int completed = 0;
    String resultsFile = "Summary-Processes";
    //=================================
    int time_quantum = 50;
    if (time_quantum==0) {
	result.schedulingType = "Batch (Nonpreemptive)";
	result.schedulingName = "Shortest Job First";
    } else {
	result.schedulingType = "Batch (preemptive)";
	result.schedulingName = "Shortest Remaining Time Next";
    }
    //=================================
    try {
      //BufferedWriter out = new BufferedWriter(new FileWriter(resultsFile));
      //OutputStream out = new FileOutputStream(resultsFile);
      PrintStream out = new PrintStream(new FileOutputStream(resultsFile));
      sProcess process = (sProcess) processVector.elementAt(currentProcess);
      currentProcess = findShortestRemainingTimeNext(processVector, NO_PROCESS);
      out.println("Process: " + currentProcess + " registered... (" + process.cputime + " " + process.ioblocking + " " + process.cpudone + " " + process.cpudone + ")");
      while (comptime < runtime) {
        if (process.cpudone == process.cputime) {
          completed++;
          out.println("Process: " + currentProcess + " completed... (" + process.cputime + " " + process.ioblocking + " " + process.cpudone + " " + process.cpudone + ")");
          if (completed == size) {
            result.compuTime = comptime;
            out.close();
            return result;
          }
	  currentProcess = findShortestRemainingTimeNext(processVector, NO_PROCESS);
          process = (sProcess) processVector.elementAt(currentProcess);
          out.println("Process: " + currentProcess + " registered... (" + process.cputime + " " + process.ioblocking + " " + process.cpudone + " " + process.cpudone + ")");
	//=================================
	// using else if as completed process already caused us to re-schedule
        } else if (time_quantum!=0 // disable pre-emption if time_quantum==0
		   && process.cpudone!=0 // we already did scheduling at start
		   // here % (div mod) imitates timer: it comes once in a time quantum
		   && (process.cpudone % time_quantum) == 0
	) {
	    previousProcess = currentProcess;
	    currentProcess = findShortestRemainingTimeNext(processVector, NO_PROCESS);
	    // if currentProcess is stll the shortest, do not switch
	    if (currentProcess != previousProcess) {
		out.println("Process: " + previousProcess + " pre-empted... (" + process.cputime + " " + process.ioblocking + " " + process.cpudone + " " + process.cpudone + ")");
		process = (sProcess) processVector.elementAt(currentProcess);
		out.println("Process: " + currentProcess + " registered... (" + process.cputime + " " + process.ioblocking + " " + process.cpudone + " " + process.cpudone + ")");
	    }
        }
	//=================================
        if (process.ioblocking == process.ionext) {
          out.println("Process: " + currentProcess + " I/O blocked... (" + process.cputime + " " + process.ioblocking + " " + process.cpudone + " " + process.cpudone + ")");
          process.numblocked++;
          process.ionext = 0;
          previousProcess = currentProcess;
	  currentProcess = findShortestRemainingTimeNext(processVector, previousProcess);
	  //=================================
	  if (currentProcess == NO_PROCESS) {
	      // no process left except for I/O blocked one
	      // we "wait" and resume it
	      currentProcess = previousProcess;
	  }
	  //=================================
          process = (sProcess) processVector.elementAt(currentProcess);
          out.println("Process: " + currentProcess + " registered... (" + process.cputime + " " + process.ioblocking + " " + process.cpudone + " " + process.cpudone + ")");
	}
        process.cpudone++;
        if (process.ioblocking > 0) {
          process.ionext++;
        }
        comptime++;
      }
      out.close();
    } catch (IOException e) { /* Handle exceptions */ }
    result.compuTime = comptime;
    return result;
  }

  //=================================
  public static final int NO_PROCESS = -1;
  public static int findShortestRemainingTimeNext(Vector processVector, int previousProcess) {
    int i = 0;
    int size = processVector.size();
    int remainingTime = Integer.MAX_VALUE;
    int nextProcess = NO_PROCESS;
    for (i = size - 1; i >= 0; i--) {
      sProcess process = (sProcess) processVector.elementAt(i);
      if (i != previousProcess &&
	process.cpudone < process.cputime &&
	process.cputime - process.cpudone < remainingTime
      ) {
	nextProcess = i;
	remainingTime = process.cputime - process.cpudone;
      }
    }
    //System.err.println("Debug: " + nextProcess);
    return nextProcess;
  }
  //=================================
}
