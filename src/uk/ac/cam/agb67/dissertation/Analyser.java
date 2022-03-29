package uk.ac.cam.agb67.dissertation;

import uk.ac.cam.agb67.dissertation.algorithm.one.Coordinator;
import uk.ac.cam.agb67.dissertation.algorithm.two.CoordinatorTwo;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Analyser {

    static boolean DEBUG = true;

    // Used by algorithms to communicate segment times
    static long BASETIME;
    public static long[] SEGMENT_TIMES = new long[4];

    // Default parameters for random tests
    static final int DEF_DAYS = 4;
    static final int DEF_ROOMS = 5;
    static final int DEF_SESSIONS = 12;
    static final int DEF_INDIVIDUALS = 25;

    // Usage: Analyser <repetitions> <filename>
    // Usage: Analyser <repetitions> <filename> <algorithm 1> <algorithm 2>
    public static void main(String[] args) {
        BASETIME = System.nanoTime();

        // Get the number of times to test, and the name for the output file
        int repetitions = Integer.parseInt(args[0]);
        String location = args[1];
        if (args.length > 4) {
            individual_test(args);
            return;
        }
        List<Integer> Selected = null;
        if (args.length > 2) {
            Selected = Arrays.asList(Integer.parseInt(args[2]),Integer.parseInt(args[3]));
        }

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
                details = randomized_test_details(DEF_DAYS, DEF_ROOMS, DEF_SESSIONS, DEF_INDIVIDUALS);
                legitimate_details = details.check_validity();
            }

            System.out.print("["+i+"]");

            // Loop through the algorithms and tell them to generate a schedule with these details
            for (int alg=0; alg<5; alg++) {
                if (Selected != null && !Selected.contains(alg)) continue;
                test_algorithm_with_details(algorithms[alg], details, i, alg, VALID, SCORE, RAM, TIME);
            }
        }

        // Generate the path for the file, including a version number. If this file already exists then increment the version number
        boolean stored = false;
        int version = 0;
        String path = "";

        while (!stored) {
            version++;
            if (version > 100) break;
            path = "results\\" + location + "_" + version + ".csv";

            try {
                stored = save_to_spreadsheet(path, repetitions, VALID, SCORE, RAM, TIME);
            } catch (IOException e) {
                System.err.println("Was not able to save the results to a file. Will try again with a different name.");
            }
        }

        if (stored) System.out.println("Saved the results to a file: " + path);

    }

    // Usage: Analyser <repetitions> <filename> <algorithm> <parameter> <runs-per-rep>
    public static void individual_test(String[] args) {

        // Decode the user input
        int repetitions = Integer.parseInt(args[0]) * Integer.parseInt(args[4]);
        String location = args[1];
        int target_algorithm = Integer.parseInt(args[2]);
        String target_parameter = args[3];

        // Select the algorithm to use based on the input
        SchedulingAlgorithm algorithm;
        String name = "";
        switch (target_algorithm) {
            case 0:  algorithm = new Coordinator(true, false); name = "Greedy Brute Force"; break;
            case 1:  algorithm = new Coordinator(false, false); name = "Brute Force"; break;
            case 2:  algorithm = new Coordinator(false,true); name = "Brute Force (Greedy Optimised)"; break;
            case 3:  algorithm = new CoordinatorTwo(false); name = "CSP Algorithm"; break;
            case 4:  algorithm = new CoordinatorTwo(true); name = "CSP (Randomised Optimisation)"; break;
            default:
                throw new IllegalStateException("No valid algorithm of index " + target_algorithm);
        }
        
        // Create the arrays we will store the results into
        boolean[][] VALID = new boolean[1][repetitions];
        int[][] SCORE = new int[1][repetitions];
        long[][] RAM = new long[1][repetitions];
        long[][] TIME = new long[1][repetitions];
        int[] PARAM = new int[repetitions];

        long[][] SEGMENTS = new long[4][repetitions + 1];
        if (target_algorithm != 3 && target_algorithm != 4) {
            SEGMENTS = null;
        }

        DEBUG = false;
        Main.DEBUG = false;

        // One parameter will be altered throughout to record how the results of the algorithm change
        // Choose and set that parameter to 1, and the others to their default value
        int num_sessions; int num_individuals; String param_name;
        switch (target_parameter) {
            case "s": param_name = "#Sessions"; num_sessions = 1; num_individuals = DEF_INDIVIDUALS; break;
            case "i": param_name = "#Individuals"; num_sessions = DEF_SESSIONS; num_individuals = 4; break;
            default: param_name = ""; num_sessions = DEF_SESSIONS; num_individuals = DEF_INDIVIDUALS;
        }

        // The main loop which runs as many times as the input specified
        // It generates a set of details and then tests each of the five algorithms on those details, storing the results
        for (int i=0; i<repetitions; i++) {

            // Generate the random event data, making sure they are legitimate details
            SchedulingProblem details = null;
            boolean legitimate_details = false;
            while (!legitimate_details) {
                details = randomized_test_details(DEF_DAYS, DEF_ROOMS, num_sessions, num_individuals);
                legitimate_details = details.check_validity();
            }

            // After every block of steps we will increase the changing parameter by one
            if (target_parameter.equals("s")) PARAM[i] = num_sessions;
            if (target_parameter.equals("s") && ((i-1) % Integer.parseInt(args[4])) == 0) num_sessions++;
            if (target_parameter.equals("i")) PARAM[i] = num_individuals;
            if (target_parameter.equals("i") && ((i-1) % Integer.parseInt(args[4])) == 0) num_individuals++;

            System.out.print("["+i+"]");

            // Now run the algorithm on these details
            test_algorithm_with_details(algorithm, details, i, 0, VALID, SCORE, RAM, TIME);

            if (target_algorithm == 3 || target_algorithm == 4) {
                // Obtain the segment times for either CSP algorithm
                SEGMENTS[0][i] = SEGMENT_TIMES[0];
                SEGMENTS[1][i] = SEGMENT_TIMES[1];
                SEGMENTS[2][i] = SEGMENT_TIMES[2];
                SEGMENTS[3][i] = SEGMENT_TIMES[3];
                SEGMENT_TIMES = new long[4];
            }
        }

        // Further Processing:

        // Calculate average segment times when relevant, and place them in the final row of the array
        if (target_algorithm == 3 || target_algorithm == 4) {
            long phase_one = 0, phase_two = 0, phase_three = 0;
            for (int i = 0; i < repetitions; i++) {
                phase_one = phase_one + (SEGMENTS[1][i] - SEGMENTS[0][i]);
                phase_two = phase_two + (SEGMENTS[2][i] - SEGMENTS[1][i]);
                phase_three = phase_three + (SEGMENTS[3][i] - SEGMENTS[2][i]);
            }
            System.out.println("/// Totals: p1: "+phase_one+", p2: "+phase_two+", p3: "+phase_three);
            SEGMENTS[1][repetitions] = phase_one / (long) repetitions;
            SEGMENTS[2][repetitions] = phase_two / (long) repetitions;
            SEGMENTS[3][repetitions] = phase_three / (long) repetitions;
            System.out.println("/// Averages: p1: "+SEGMENTS[1][repetitions]+", p2: "+SEGMENTS[2][repetitions]+", p3: "+SEGMENTS[3][repetitions]);
        }

        // Generate the path for the file, including a version number. If this file already exists then increment the version number
        boolean stored = false;
        int version = 1;
        String path = "";

        while (!stored) {
            version++;
            if (version > 100) break;
            path = "results\\" + location + "_" + version + ".csv";

            try {
                stored = save_to_spreadsheet(path, repetitions, name, VALID[0], SCORE[0], RAM[0], TIME[0], SEGMENTS, PARAM, param_name);
            } catch (IOException e) {
                System.err.println("Was not able to save the results to a file. Will try again with a different name.");
            }
        }

        if (stored) System.out.println("Saved the results to a file: " + path);
        
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

                    // TODO break out of loops which run for too long?
                }
            }
        };

        // Begin the thread to record RAM and check the system time
        Timetable tt;
        display.setPriority(10);
        display.start();
        System.out.println("Start Time (ms): " + (((double) System.nanoTime()) / (1000 * 1000)));
        long start_time = System.nanoTime();
        System.out.println("Start Time (ms): " + (((double) start_time) / (1000 * 1000)));

        // Run the algorithm on the given details
        try {
            tt = algorithm.generate(details);
        } catch (Exception e) {
            // This method generated an exception.
            System.err.println("The selected algorithms with the given details threw an exception.");
             tt = null;
        }

        // TODO find a better way to look at the times in MS
        // Record the system time again and end the memory-watching thread
        long end_time = System.nanoTime();
        System.out.println("End Time (ms): " + (((double) System.nanoTime()) / (1000 * 1000)));
        System.out.println("End Time (ms): " + (((double) end_time) / (1000 * 1000)));
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

    // Saves the results of testing five algorithms together to a spreadsheet
    static boolean save_to_spreadsheet(String path, int rows, boolean[][] VALID, int[][] SCORE, long[][] RAM, long[][] TIME) throws IOException {
        // Create a new file
        File csv = new File(path);
        if (csv.exists()) return false;
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

        String footer =
                "\n\nThe parameters of the random test data: Days:"+DEF_DAYS+"  Hours:8  Rooms:"+DEF_ROOMS+"  Sessions:"+DEF_SESSIONS+"  Individuals:"+DEF_INDIVIDUALS;
        csvWriter.write(footer);

        csvWriter.close();
        return true;
    }

    // Saves the results of testing an individual algorithm to a spreadsheet
    static boolean save_to_spreadsheet(String path, int rows, String name, boolean[] VALID, int[] SCORE, long[] RAM, long[] TIME, long[][] SEGMENTS, int[] PARAM,
                                       String param_name) throws IOException {
        // Create a new file
        File csv = new File(path);
        if (csv.exists()) return false;
        FileWriter csvWriter = new FileWriter(csv);

        // Add header row here
        String top = name + ", , , , , ,\n";
        String header = "Valid?, Score, RAM (MB), Time (ms), "+param_name+", ,\n";
        if (SEGMENTS != null) {
            top = name + ", , , , , , Times (ms), , , ,\n";
            header = "Valid?, Score, RAM (MB), Time (ms), "+param_name+", , Start, Modelled, Solved, Decoded,\n";
        }
        csvWriter.write(top);
        csvWriter.write(header);

        for (int r=0; r<rows; r++) {
            StringBuilder row = new StringBuilder();

            // Write the values into the rows
            String time = String.valueOf((((double) TIME[r]) / (1000 * 1000)));
            String ram = String.valueOf(((double) RAM[r]) / (1000 * 1000));
            row.append(VALID[r]).append(",").append(SCORE[r]).append(",").append(ram).append(",").append(time).append(",").append(PARAM[r]).append(", ,");

            if (SEGMENTS != null) {
                row.append(convert_time(SEGMENTS[0][r])).append(",").append(convert_time(SEGMENTS[1][r])).append(",").append(convert_time(SEGMENTS[2][r])).append(",").append(convert_time(SEGMENTS[3][r])).append(",");
            }

            // Place the row into the file
            row.append("\n");
            csvWriter.write(row.toString());
        }

        if (SEGMENTS != null) {
            System.out.println("/// Writing: p1: "+((double) SEGMENTS[1][rows] / (1000*1000))+", p2: "+(SEGMENTS[2][rows] / (1000*1000))+", p3: "+(SEGMENTS[3][rows] / (1000*1000)));
            String summary =
                    "\n , , , , , , , "+((double) SEGMENTS[1][rows] / (1000*1000))+", "+((double) SEGMENTS[2][rows] / (1000*1000))+", "+((double) SEGMENTS[3][rows] / (1000*1000))+",\n";
            csvWriter.write(summary);
        }

        String footer =
                "\n\nThe DEFAULT parameters of the random test data: Days:"+DEF_DAYS+"  Hours:8  Rooms:"+DEF_ROOMS+"  Sessions:"+DEF_SESSIONS+"  Individuals:"+DEF_INDIVIDUALS+" (Ignore for the specified parameter).";
        csvWriter.write(footer);

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

    // Randomly generates a (duplicate-free) list, of length count, of numbers between minimum and maximum
    static List<Integer> generate_numbers(int maximum, int minimum, int count) {
        if (count > (maximum - minimum)) {
            System.err.println("Tried to generate a duplicate free list which was too long for the given range. Count: "+count+", Range: "+minimum+" to "+maximum);
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

    // Converts a system time in nanoseconds into a time in milliseconds, offset by the start-time of the analyser
    public static double convert_time(long NanoSeconds) {
        return ((double) (NanoSeconds - BASETIME)) / (1000 * 1000);
    }

}
