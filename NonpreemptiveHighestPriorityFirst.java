import java.util.Comparator;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;

public class NonpreemptiveHighestPriorityFirst extends Scheduler 
{
    @Override
    public Queue<Process> schedule(PriorityQueue<Process> q) 
    {
        int finishTime = 0;
        int startTime;
        Process p;
        Process scheduled;
        Scheduler.Stats stats = this.getStats();
        Queue<Process> scheduledQueue = new LinkedList<>();
        
        // Queue processes that are waiting to run, and order by shortest run time
        PriorityQueue<Process> readyQueue = new PriorityQueue<>(10, 
            new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                Process p1 = (Process) o1;
                Process p2 = (Process) o2;
                if (p1.getBurstTime() == p2.getBurstTime())
                    return p1.getArrivalTime() < p2.getArrivalTime() ? -1 : 1;
                else
                    return p1.getPriority() < p2.getPriority() ? -1 : 1;
            }            
        });
        
        while (!q.isEmpty())
        {
            if (readyQueue.isEmpty())
                p = q.poll();
            else
                p = readyQueue.poll();
                       
            startTime = Math.max((int) Math.ceil(p.getArrivalTime()), finishTime);
            finishTime = startTime + p.getBurstTime();
            
            while (q.peek() != null && Math.ceil(q.peek().getArrivalTime()) < finishTime)
                readyQueue.add(q.poll());
            
            // Record the statistics for this process
            stats.addWaitTime(startTime - p.getArrivalTime());
            stats.addTurnaroundTime(startTime - p.getArrivalTime() + p.getBurstTime());
            stats.addResponseTime(startTime - p.getArrivalTime() + p.getBurstTime());
            stats.addProcess();            
            
            // Don't start any processes after 100 time slices
            if (startTime > 100) 
                break;

            // Create a new process with the calculated start time and add to a new queue
            scheduled = new Process();
            scheduled.setBurstTime(p.getBurstTime());
            scheduled.setStartTime(startTime);
            scheduled.setName(p.getName());
            scheduledQueue.add(scheduled);            
        }
        // Get any remaining values out of the ready queue
        while (!readyQueue.isEmpty())
        {
            p = readyQueue.poll();           
            startTime = Math.max((int) Math.ceil(p.getArrivalTime()), finishTime);
            finishTime = startTime + p.getBurstTime();            
            stats.addWaitTime(startTime - p.getArrivalTime());
            stats.addTurnaroundTime(startTime - p.getArrivalTime() + p.getBurstTime());
            stats.addResponseTime(startTime - p.getArrivalTime() + p.getBurstTime());
            stats.addProcess();            
            if (startTime > 100) 
                break;
            scheduled = new Process();
            scheduled.setBurstTime(p.getBurstTime());
            scheduled.setStartTime(startTime);
            scheduled.setName(p.getName());
            scheduledQueue.add(scheduled);             
        }
        
        stats.addQuanta(finishTime); // Add the total quanta to finish all jobs
        printTimeChart(scheduledQueue);
        printRoundAvgStats();
        stats.nextRound();
        
        return scheduledQueue;
    }
}
