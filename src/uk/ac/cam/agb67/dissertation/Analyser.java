package uk.ac.cam.agb67.dissertation;

import uk.ac.cam.agb67.dissertation.algorithm.one.Coordinator;
import uk.ac.cam.agb67.dissertation.algorithm.two.CoordinatorTwo;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Analyser {

    static boolean DEBUG = true;

    public static void main(String[] args) {

        // Get the number of times to test, and the name for the output file
        int repetitions = Integer.parseInt(args[0]);
        String location = args[1];

        // Establish the five algorithms we will be testing
        SchedulingAlgorithm[] algorithms = new SchedulingAlgorithm[5];
        algorithms[0] = new Coordinator(true, false);
        algorithms[1] = new Coordinator(false, false);
        algorithms[2] = new Coordinator(false,true);
        algorithms[3] = new CoordinatorTwo(false);
        algorithms[4] = new CoordinatorTwo(true);

        // Create the arrays we will store the results into
        boolean[][] VALID = new boolean[5][repetitions];
        int[][] SCORE = new int[5][repetitions];
        long[][] RAM = new long[5][repetitions];
        long[][] TIME = new long[5][repetitions];

        DEBUG = false;
        Main.DEBUG = false;

        // The main loop which runs as many times as the input specified
        // It generates a set of details and then tests each of the five algorithms on those details, storing the results
        for (int i=0; i<repetitions; i++) {
            // Generate the random event data, making sure they are legitimate details
            // TODO adjust these parameters
            // Make sure these are actually generating preferences, maybe read out some of the generated details

            SchedulingProblem details = null;
            boolean legitimate_details = false;
            while (!legitimate_details) {
                details = randomized_test_details(4, 5, 17, 25);
                legitimate_details = details.check_validity();
            }

            System.out.print("["+i+"]");

            // Loop through the algorithms and tell them to generate a schedule with these details
            for (int alg=0; alg<5; alg++) {
                test_algorithm_with_details(algorithms[alg], details, i, alg, VALID, SCORE, RAM, TIME);
            }
        }

        // Generate the path for the file, including a version number. If this file already exists then increment the version number
        boolean stored = false;
        int version = 1;
        String path = "";

        while (!stored) {
            path = "results\\" + location + "_" + version + ".csv";
            try {
                stored = save_to_spreadsheet(path, repetitions, VALID, SCORE, RAM, TIME);
            } catch (IOException e) {
                System.err.println("Was not able to save the results to a file. Will try again with a different name.");
                version++;
                if (version > 100) break;
            }
        }

        if (stored) System.out.println("Saved the results to a file: " + path);


        // Comparisons?
        // TODO possibly add some automatic comparisons?

    }


    // Test a provided algorithm on provided details, recording memory and latency, and checking score and validity
    public static void test_algorithm_with_details(SchedulingAlgorithm algorithm, SchedulingProblem details, int i, int alg, boolean[][] VALID, int[][] SCORE,
                                                   long[][] RAM, long[][] TIME) {
        // TODO Fix this to record RAM usage for the algos which were too fast.
        System.out.println("");

        // Prepare a thread which will run alongside the algorithm and record max RAM usage as it goes
        final long[] max_RAM_used = {0};
        final boolean[] in_execution = {true};
        Thread display = new Thread() {
            @Override
            public void run() {
                while (in_execution[0]) {
                    // Determine the currently free memory
                    long current_RAM_used = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
                    if (current_RAM_used > max_RAM_used[0]) max_RAM_used[0] = current_RAM_used;
                    /*try {
                        wait(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }*/
                }
            }
        };

        // Begin the thread to record RAM and check the system time
        Timetable tt;
        display.start();
        long start_time = System.nanoTime();

        // Run the algorithm on the given details
        try {
            tt = algorithm.generate(details);
        } catch (Exception e) {
            // This method generated an exception.
            System.err.println("The selected algorithms with the given details threw an exception.");
             tt = null;
        }

        // Record the system time again and end the memory-watching thread
        long end_time = System.nanoTime();
        in_execution[0] = false; System.gc();

        // Calculate the memory which was added to the program, and the execution time of the algorithm
        long execution_time = end_time - start_time;
        long memory_added = max_RAM_used[0];

        // Check the accuracy and score of the generated schedule
        boolean valid = false;
        int score = -1;
        if (tt != null) {
            TimetableVerifier ttv = new TimetableVerifier();
            TimetableSatisfactionMeasurer ttsm = new TimetableSatisfactionMeasurer();

            valid = ttv.timetable_is_valid(tt, details);
            score = ttsm.timetable_preference_satisfaction(tt, details);
        }

        // Store the results
        VALID[alg][i] = valid;
        SCORE[alg][i] = score;
        RAM[alg][i] = memory_added;
        TIME[alg][i] = execution_time;
    }

    static boolean save_to_spreadsheet(String path, int rows, boolean[][] VALID, int[][] SCORE, long[][] RAM, long[][] TIME) throws IOException {
        // Create a new file
        File csv = new File(path);
        FileWriter csvWriter = new FileWriter(csv);

        // Add header row here
        String top = "Greedy Brute Force, , , , ,Brute Force, , , , ,Brute Force (Greedy Optimised), , , , ,CSP Algorithm, , , , ,CSP (Randomised Optimisation), ,\n";
        String headers = "Valid?, Score, RAM (MB), Time (ms), ,Valid?, Score, RAM (MB), Time (ms), ,Valid?, Score, RAM (MB), Time (ms), ,Valid?, Score, RAM (MB), Time (ms)" +
                ", ," +
                "Valid?, Score, RAM, Time (ms), ,\n";
        csvWriter.write(top);
        csvWriter.write(headers);

        for (int r=0; r<rows; r++) {
            StringBuilder row = new StringBuilder();

            for (int alg=0; alg<5; alg++) {
                String time = String.valueOf((((double) TIME[alg][r]) / 1000000));
                String ram = String.valueOf(((double) RAM[alg][r]) / (1000 * 1000));
                row.append(VALID[alg][r]).append(",").append(SCORE[alg][r]).append(",").append(ram).append(",").append(time).append(", ,");
            }

            // Place the row into the file
            row.append("\n");
            csvWriter.write(row.toString());
        }

        csvWriter.close();
        return true;
    }

    // Randomly generates a scheduling problem to use as test data, given certain parameters
    static SchedulingProblem randomized_test_details(int days, int rooms, int num_sessions, int num_individuals, boolean overlap_pref, int gap_pref) {
        SchedulingProblem details = new SchedulingProblem();
        if (DEBUG) System.out.println("Generating a test data set, with "+days+" days and "+rooms+" rooms.");

        // Lock in the defined parameters
        details.Maximum_Days = days;
        details.Hours_Per_Day = 8;
        details.Maximum_Rooms = rooms;

        details.Reduce_Overlap_Pref = overlap_pref;
        details.Minimum_Gap_Pref = gap_pref;

        // Generate a room occupancy for every room, from 10% of the total individuals to double the total individuals
        details.Room_Occupancy_Limits = new ArrayList<>();
        for (int r=0; r<rooms; r++) {
            int limit = generate_number(num_individuals * 2, (int)(num_individuals * 0.1));
            details.Room_Occupancy_Limits.add(limit);
        }
        if (DEBUG) System.out.println("Room occupancy limits: "+details.Room_Occupancy_Limits.toString()+".");

        // Generate a list of key individuals, of length num_individuals
        details.KeyInd_Details = new ArrayList<>();
        for (int k=0; k<num_individuals; k++) {
            // Generate a preference for max sessions in one day
            int daily_limit_pref = generate_number(8, 1);

            // Then generate a set of room preferences
            int num_room_prefs = generate_number((int)((rooms * 0.1) + 1), 0);
            List<Integer> room_prefs = generate_numbers((rooms-1), 0, num_room_prefs);

            // Then create and add the key individual
            KeyIndividual KeyInd = new KeyIndividual("Person #"+k, daily_limit_pref, room_prefs);
            details.KeyInd_Details.add(KeyInd);
            if (DEBUG) System.out.println("Adding an individual with daily limit: "+daily_limit_pref+", and ("+num_room_prefs+") room prefs: "+room_prefs.toString()+".");
        }

        // Generate a list of sessions, of length num_sessions
        details.Session_Details = new ArrayList<>();
        for (int s=0; s<num_sessions; s++) {
            // Generate the number of key individuals to be in the session, and their IDs
            int num_participating_individuals = generate_number((int)(num_individuals * 0.15), 1);
            List<Integer> participating_individual_IDs = generate_numbers((num_individuals-1), 0, num_participating_individuals);

            // Then create and add the session
            Session sesh = new Session(s, "Session #"+s, generate_number(6,1), participating_individual_IDs);
            details.Session_Details.add(sesh);
            if (DEBUG) System.out.println("Adding session #"+s+" of length: "+sesh.Session_Length+", with individuals: "+participating_individual_IDs.toString()+".");
        }

        // Generate a number of predetermined sessions, replacing the last chunk of the session list
        details.PDS_Details = new ArrayList<>();
        int num_predetermined = generate_number((int)(num_sessions * 0.1) + 1, 1);
        if (DEBUG) System.out.println("Generating "+num_predetermined+" PDS sessions.");
        for (int s=(num_sessions-num_predetermined); s<num_sessions; s++) {
            Session remove = details.Session_Details.get(s);

            // Take the details of the existing session and create a new PredeterminedSession
            // Generate it a predetermined day, time and room
            int rand_day = generate_number(days-1, 0);
            int rand_start = generate_number(8-remove.Session_Length, 0);
            int rand_room = generate_number(rooms-1, 0);
            PredeterminedSession replace = new PredeterminedSession(remove.Session_ID, remove.Session_Name+"(PDS)", remove.Session_Length, remove.Session_KeyInds,
                    rand_day, rand_start, rand_room);
            if (DEBUG) System.out.println("Generated a PDS sessions with day:"+rand_day+" time:"+rand_start+" room:"+rand_room);

            // Then replace the session
            details.Session_Details.remove(s);
            details.Session_Details.add(s, replace);
            details.PDS_Details.add(replace);
            if (DEBUG) System.out.println("Converting a session to predetermined. ID: #"+s+", length: "+remove.Session_Length+", day: "+replace.PDS_Day+", time: "+replace.PDS_Start_Time+", room:"+replace.PDS_Room);
        }

        return details;
    }

    // Randomly generates test data with randomized gap preference and overlap preference
    static SchedulingProblem randomized_test_details(int days, int rooms, int num_sessions, int num_individuals) {
        int gap_pref = generate_number(3, 0);
        double overlap_rand = Math.random();

        if (DEBUG) System.out.println("desired gap: " +gap_pref+ ",    desire overlap?: "+ (overlap_rand >= 0.5));

        return randomized_test_details(days, rooms, num_sessions, num_individuals, (overlap_rand >= 0.5), gap_pref);
    }

    // Randomly generates a number between minimum and maximum
    static int generate_number(int maximum, int minimum) {
        return (int) (minimum + (Math.random() * (maximum - minimum + 1)));
    }

    // Randomly generates a (duplicate-free) list, length count, of numbers between minimum and maximum
    static List<Integer> generate_numbers(int maximum, int minimum, int count) {
        if (count > (maximum - minimum)) {
            System.err.println("Tried to generate a duplicate free list which was too long for the given range.");
            return null;
        }

        List<Integer> list = new ArrayList<>();
        for (int i=0; i<count; i++) {
            int gen = generate_number(maximum, minimum);
            if (!list.contains(gen)) {
                list.add(gen);
            } else {
                // Decrement because we did not find a new number
                i--;
            }
        }
        return list;
    }


}
