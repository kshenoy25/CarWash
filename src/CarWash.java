import java.util.*;

public class CarWash {


    public static void main(String[ ] args) {
        Scanner kb = new Scanner (System.in);
        System.out.println("Enter wash time: ");
        int WASHTIME = kb.nextInt();
        System.out.println("Enter arrival probability: ");
        double ARRIVALPROB = kb.nextDouble();
        System.out.println("enter time for simulation: ");
        int TOTALTIME = kb.nextInt();
        carWashSimulate(WASHTIME, ARRIVALPROB, TOTALTIME);
    }
    public static int getRandomNumber(int max, int min){
        // If Max is zero or less return zero
        if (max <= 0){
            return 0;
        }
        if (max == min){
            max++;
        }
        if (min > max){
            throw new IllegalArgumentException("Max must be greater than min.");
        }
        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }

    public static void carWashSimulate (int washTime, double arrivalProb, int totalTime) {
        int minServer = 2; // Min number of machines to keep the car wash open
        int maxServer = 6; // Max number of servers the car wash can have
        int changerServerEvent = 50; // Initiate a change server event count
        int countWashes = 0; // Keep count of total number washes
        int customerBonus = 12; // Determines every xth customer free special service
        boolean isUp = false; // Variable used during making changes to adding or removing machines

        int NormalQueues = 3; // Sets number of normal queues
        int SpecialQueues =1; // Sets number of special queues
        //Creates 3 Queues
        PQ<Integer> arrivalTimesNormalSvc = new PQ<Integer>(NormalQueues-1 );
        //Creates 1 Queue for special service
        PQ<Integer> arrivalTimesSplSvc = new PQ<Integer>(SpecialQueues -1 );

        // Created 4 car wash machines
        ArrayList<Server> machines = new ArrayList<>();
        int next;
        ClientGenerator arrival = new ClientGenerator(arrivalProb);

        // Initialize servers with ID, variable WashTime & set Speical Services
        for (int i = 0; i < maxServer; i++){

            Server machine = new Server(getRandomNumber(washTime + 10, 1), i+1);
            if (i % 3 == 0 ) {
                // Setting every 3rd machine for Special Service
                machine.setSpecialService(true);
            }
            machines.add(machine);
        }

        Averager waitTimes = new Averager( );
        int currentSecond;

        // Write the parameters to System.out.
        System.out.println("Seconds to wash one car: " + washTime);
        System.out.print("Probability of customer arrival during a second: ");
        System.out.println(arrivalProb);
        System.out.println("Total simulation seconds: " + totalTime);

        // Check the precondition:
        if (washTime<=0 || arrivalProb<0 || arrivalProb>1 || totalTime<0)
            throw new IllegalArgumentException("Values out of range");
        for (currentSecond = 1; currentSecond < totalTime; currentSecond++) {

            // Simulate the passage of one second of time.
            // Check whether a new customer has arrived.
            if (arrival.query( )) {

                // Every 5th customer will be set up for special service
                if ((currentSecond % 5 == 0) || (currentSecond % customerBonus == 0)){
                    ////////////////////////////////////////////////////////////////////////////////////
                    // EVENT2 - EVERY - 12th customer set by variable - customerBonus will be given special service for FREE
                    // If customerSecond is divisible by 5 will require special service
                    // else into normal queue
                    if(currentSecond % customerBonus == 0){
                        System.out.println("EVENT 2: Free special wash offer - " + currentSecond);
                    }
                    arrivalTimesSplSvc.PQenqueue((Integer)currentSecond, getRandomNumber(SpecialQueues-1 , 0));
                    System.out.println("Customer arrived for special service at " + currentSecond);
                }else{
                    // Every customer is added with random priority based of number of queues
                    arrivalTimesNormalSvc.PQenqueue((Integer)currentSecond, getRandomNumber(NormalQueues-1 , 0));
                    System.out.println("Customer arrived for normal service at " + currentSecond);
                }

            }

            ////////////////////////////////////////////////////////////////////////////////////
            // EVENT1 - CHANGE SERVER ON DUTY
            // For every 'x' -> changerServerEvent washses, when a server is free
            // we will start reducing servers on duty till we get to minimum
            // At minimum servers, we will start adding servers till maximum
            for (int serverCount = 0; serverCount < machines.size(); serverCount++){
                Server tempMachine = machines.get(serverCount);
                if (!tempMachine.isBusy() && countWashes >= changerServerEvent){
                    if(machines.size() >= maxServer){
                        isUp = false;
                    }
                    if (machines.size() <= minServer){
                        isUp = true;
                    }
                    if(isUp){
                        Server machine = new Server(getRandomNumber(washTime + 10, 1), machines.size());
                        if (machines.size() % 3 == 0 ) {
                            // Setting every 3rd machine for Special Service
                            machine.setSpecialService(true);
                        }
                        countWashes = 0; // Resetting wash counter

                        machines.add(machine);
                        System.out.println("EVENT 1: CHANGE SERVER ON DUTY - " + machines.size());
                        break;
                    }
                    else{
                        machines.remove(serverCount);

                        countWashes = 0; // Resetting wash counter
                        System.out.println("EVENT 1: CHANGE SERVER ON DUTY - " + machines.size());
                        break;
                    }
                }
            }

            // Loop through every machine to check if its available and start washing
            for (int i = 0; i < machines.size(); i++){
                // Check if Normal Machine is free and Normal Queue has any cars
                Server tempMachine = machines.get(i);

                // Check if a normal machine is free and if normal service queue has any cars
                if (!tempMachine.isBusy() && !tempMachine.isSpecialService() && !arrivalTimesNormalSvc.is_empty()){
                    next = arrivalTimesNormalSvc.PQdequeue();
                    waitTimes.addNumber(currentSecond - next);
                    System.out.println("Server started at " + currentSecond);
                    tempMachine.start();
                    countWashes++;
                }

                // Check if a special machine is free and if special service queue has any cars
                if (!tempMachine.isBusy() && tempMachine.isSpecialService() && !arrivalTimesSplSvc.is_empty()) {
                    next = arrivalTimesSplSvc.PQdequeue();
                    waitTimes.addNumber(currentSecond - next);
                    System.out.println("Server started at " + currentSecond);
                    tempMachine.start();
                    countWashes++;

                }

            }

            // For every machine reduce its remaining time
            // Subtract one second from the remaining time
            // in the current wash cycle
            for (int i = 0; i < machines.size(); i++) {
                machines.get(i).reduceRemainingTime();
            }
        }

        // Write the summary information about the simulation.
        System.out.println("Customers served: " + waitTimes.howManyNumbers( ));
        if (waitTimes.howManyNumbers( ) > 0)
            System.out.println("Average wait: " + waitTimes.average( ) + " sec");
    }

}

class Averager {
    private int count;  // How many numbers have been given to this averager
    private double sum; // Sum of all numbers given to this averager
    public Averager( ) {
        count =0;
        sum = 0;
    }

    public void addNumber(double value) {
        if (count == Integer.MAX_VALUE)
            throw new IllegalStateException("Too many numbers");
        count++;
        sum += value;
    }

    public double average( ) {
        if (count == 0)
            return Double.NaN;
        else
            return sum/count;
    }

    public int howManyNumbers( ) {
        return count;
    }
}

class ClientGenerator {
    private double probability; // The approximate probability
    // of query( ) returning true.
    public ClientGenerator(double p) {
        if ((p < 0) || (1 < p))
            throw new IllegalArgumentException("Illegal p: " + p);
        probability = p;
    }

    public boolean query( ) {
        return (Math.random( ) < probability);
    }
}

class Server {
    private int secondsForService; // Seconds for a single wash
    private int timeLeft; // Seconds until this Server is no longer busy
    private boolean specialService; // Does this service provide special service
    private int id; // Number to identify this server

    public Server(int s, int _id) {
        id = _id;
        secondsForService = s;
        timeLeft =0;
        specialService = false;
        System.out.println("Server Id " + id + " Time for Wash " + secondsForService);
    }
    public Server(int s, int _id, boolean spl) {
        id = _id;
        secondsForService = s;
        timeLeft =0;
        specialService = spl;
        System.out.println("Server Id " + id + " Time for Wash " + secondsForService);
    }

    public boolean isBusy( ) {
        return (timeLeft > 0);
    }

    public void reduceRemainingTime( ) {
        if (timeLeft > 0){
            timeLeft--;
        }
    }

    public void start( ) {
        if (timeLeft > 0)
            throw new IllegalStateException("Server is already busy.");
        if (specialService){
            System.out.println("Starting special service by machine: " + id);
        }
        else{
            System.out.println("Starting normal service by machine: " + id);

        }
        timeLeft = secondsForService;
    }

    public boolean isSpecialService() {
        return specialService;
    }

    public void setSpecialService(boolean specialService) {
        System.out.println("Machine ID: " + id + " is Special Service" );
        this.specialService = specialService;
    }

}