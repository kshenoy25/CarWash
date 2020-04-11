import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;

public class PQ<E> {
    private Queue[] queues;
    public int highest;
    public int total;
    public int highCurrent;

    public PQ (int h) {
        highest = h;
        queues = new LinkedList[h+1];
        for (int x=0; x<h+1; x++) {
            queues[x] = new LinkedList();
        }
        total = 0;
        highCurrent = 0;
    }

    public int size () {
        return total;
    }

    public boolean is_empty() {
        return (total == 0);
    }

    public void PQenqueue(E entry, int priority){
        assert (priority <= highest);
        // if this is highest priority entry so far, so note:
        if (priority > highCurrent) {
            highCurrent = priority;
        }
        // place entry in queue:
        queues[priority].add(entry);
        // increment count of total entries:
        total++;
    }

    public E PQdequeue(){
        assert (size() > 0);
        int p = highCurrent;
        total--;
        for(; p>=0; p--) {
            if (!queues[p].isEmpty()){
                highCurrent = p;
                return (E)(queues[p].remove());
            }
        }
        return (E)(queues[p].remove());
    }
}


