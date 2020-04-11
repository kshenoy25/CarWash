import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Random;
import java.util.Scanner;

public class PQTester {
    public static Random rg = new Random();
    public static int qsize = rg.nextInt(9) + 3;
    public static void main(String [] args) throws FileNotFoundException {
        System.out.println("Testing Priority Queue");
        System.out.println("Priorities range from 0 to " + (qsize-1));
        PQ<String> items = new PQ<String>(qsize);
        populateQueue(items);
        removeNshow(items);
    }

    public static void populateQueue(PQ<String> q) throws FileNotFoundException {
        Scanner input = new Scanner(new FileInputStream("PQTester.java"));
        while (input.hasNext()) {
            int priority = rg.nextInt(qsize);
            String s = input.next();
            System.out.println("Enqueuing " + s + " with priority " + priority);
            q.PQenqueue(s, priority);
        }
        input.close();
    }

    public static void removeNshow(PQ<String> q) {
        while (!q.is_empty()) {
            System.out.println(q.PQdequeue());
        }
    }
}
