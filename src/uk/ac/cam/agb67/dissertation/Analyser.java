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

    private static boolean DEBUG = true;

    // Used by algorithms to communicate segment times
    private static long BASETIME;
    public static long[] SEGMENT_TIMES = new long[4];

    // Default parameters for random tests
    private static final int DEF_DAYS = 60;
    private static final int DEF_ROOMS = 30;
    private static int DEF_SESSIONS = 10;
    private static int DEF_INDIVIDUALS = 10;

    // Saved to a file in the case of a forced exit
    private static SchedulingProblem latest_details;
    private static Timetable latest_details_tt;

    // A thread for saving test details if we have to suddenly shut down the analyser
    private static Thread shutdown = new Thread() {
        @Override
        public void run() {
            if (save_details_to_file(latest_details, "results\\latest_test_details.txt")) {
                System.err.println("Saved the latest test details to results\\latest_test_details.txt.");
            }
            if (save_details_to_file(latest_details_tt, "results\\latest_test_example_timetable.txt")) {
                System.err.println("Saved an example timetable for the latest test to results\\latest_test_example_timetable.txt.");
            }
        }
    };

    // Usage: Analyser <repetitions> <filename>
    // Usage: Analyser <repetitions> <filename> <algorithm 1> <algorithm 2>
    // Usage: Analyser <repetitions> <filename> #<sessions>#<individuals> <algorithm 1> <algorithm 2>
    public static void main(String[] args) {
        BASETIME = System.nanoTime();
        if (DEBUG) Runtime.getRuntime().addShutdownHook(shutdown);

        // Get the number of times to test, and the name for the output file
        int repetitions = Integer.parseInt(args[0]);
        String location = args[1];
        if (args.length == 5 && (args[3].equals("s") || args[3].equals("b") || args[3].equals("i"))) {
            individual_test(args);
            return;
        }
        // Decode the rest of the arguments for the test run
        int off = 0;
        if (args.length > 2 && args[2].substring(0,1).equals("#")) {
            off = 1;
            String[] params = args[2].substring(1).split("#");
            DEF_SESSIONS = Integer.parseInt(params[0]);
            DEF_INDIVIDUALS = Integer.parseInt(params[1]);
        }
        List<Integer> Selected = null;
        if (args.length > 2+off) {
            Selected = Arrays.asList(Integer.parseInt(args[2+off]),Integer.parseInt(args[3+off]));
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
            SchedulingProblem details = null;
            boolean legitimate_details = false;
            while (!legitimate_details) {
                details = guaranteed_randomized_test_details(DEF_DAYS, DEF_ROOMS, DEF_SESSIONS, DEF_INDIVIDUALS);
                if (details == null) continue;
                legitimate_details = details.potentially_schedulable();
            }

            System.out.println("["+i+"]");

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
            path = "results\\"+location+"_"+version+".csv";

            try {
                stored = save_to_spreadsheet(path, repetitions, VALID, SCORE, RAM, TIME);
            } catch (IOException e) {
                System.err.println("Was not able to save the results to a file. Will try again with a different name.");
            }
        }

        // Indicate the final name of the saved file
        if (stored) System.out.println("Saved the results to a file: " + path);
    }

    // Usage: Analyser <repetitions> <filename> <algorithm> <parameter> <runs-per-rep>
    private static void individual_test(String[] args) {

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
            case "b": param_name = "#Sessions = #Individuals"; num_sessions = 4; num_individuals = 4; break;
            default: param_name = ""; num_sessions = DEF_SESSIONS; num_individuals = DEF_INDIVIDUALS;
        }

        // The main loop which runs as many times as the input specified
        // It generates a set of details and then tests each of the five algorithms on those details, storing the results
        for (int i=0; i<repetitions; i++) {
            System.out.print("["+i+"]");
            System.out.println(" With "+num_sessions+" sessions and "+num_individuals+" individuals.");

            // Generate the random event data, making sure they are legitimate details
            SchedulingProblem details = null;
            boolean legitimate_details = false;
            while (!legitimate_details) {
                details = guaranteed_randomized_test_details(DEF_DAYS, DEF_ROOMS, num_sessions, num_individuals);
                if (details == null) continue;
                legitimate_details = details.potentially_schedulable();
            }

            // After every block of steps we will increase the changing parameter by one
            if (target_parameter.equals("s")) PARAM[i] = num_sessions;
            if (target_parameter.equals("s") && ((i+1) % Integer.parseInt(args[4])) == 0) num_sessions++;
            if (target_parameter.equals("i")) PARAM[i] = num_individuals;
            if (target_parameter.equals("i") && ((i+1) % Integer.parseInt(args[4])) == 0) num_individuals++;
            if (target_parameter.equals("b")) PARAM[i] = num_sessions;
            if (target_parameter.equals("b") && ((i+1) % Integer.parseInt(args[4])) == 0) {num_sessions++; num_individuals++;}

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

        // Calculate average segment times when relevant, and place them in the final row of the array
        if (target_algorithm == 3 || target_algorithm == 4) {
            long phase_one = 0, phase_two = 0, phase_three = 0;
            for (int i = 0; i < repetitions; i++) {
                phase_one = phase_one + (SEGMENTS[1][i] - SEGMENTS[0][i]);
                phase_two = phase_two + (SEGMENTS[2][i] - SEGMENTS[1][i]);
                phase_three = phase_three + (SEGMENTS[3][i] - SEGMENTS[2][i]);
            }
            SEGMENTS[1][repetitions] = phase_one / (long) repetitions;
            SEGMENTS[2][repetitions] = phase_two / (long) repetitions;
            SEGMENTS[3][repetitions] = phase_three / (long) repetitions;
        }

        // Generate the path for the file, including a version number. If this file already exists then increment the version number
        boolean stored = false;
        int version = 0;
        String path = "";
        while (!stored) {
            version++;
            if (version > 100) break;
            path = "results\\"+location+"_"+version+".csv";
            try {
                stored = save_to_spreadsheet(path, repetitions, name, VALID[0], SCORE[0], RAM[0], TIME[0], SEGMENTS, PARAM, param_name);
            } catch (IOException e) {
                System.err.println("Was not able to save the results to a file. Will try again with a different name.");
            }
        }

        // Indicate the final name of the saved file.
        if (stored) System.out.println("\nSaved the results to a file: " + path);
    }

    // Test a provided algorithm on provided details, recording memory and latency, and checking score and validity
    private static void test_algorithm_with_details(SchedulingAlgorithm algorithm, SchedulingProblem details, int i, int alg, boolean[][] VALID, int[][] SCORE,
                                                   long[][] RAM, long[][] TIME) {
        latest_details = details;

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
                }
            }
        };

        // Begin the thread to record RAM and check the system time
        Timetable tt;
        display.setPriority(10);
        display.start();
        System.out.println("Start Time (ms): " + (((double) System.nanoTime()) / (1000 * 1000)));
        long start_time = System.nanoTime();

        // Run the algorithm on the given details
        try {
            tt = algorithm.generate(details);
        } catch (Exception e) {
            // This method generated an exception.
            System.err.println("The selected algorithms with the given details threw an exception.");
            if (algorithm.getClass() == CoordinatorTwo.class) {
                SEGMENT_TIMES[2] = System.nanoTime();
                SEGMENT_TIMES[3] = System.nanoTime();
            }
            tt = null;
        }

        // Record the system time again and end the memory-watching thread
        long end_time = System.nanoTime();
        System.out.println("End Time (ms):   " + (((double) end_time) / (1000 * 1000)));
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
        String top = "Greedy Brute Force,,,,,Brute Force,,,,,Brute Force (Greedy Optimised),,,,,CSP Algorithm,,,,,CSP (Randomised Optimisation),,\n";
        String headers = "Valid?,Score,RAM (MB),Time (ms),,Valid?,Score,RAM (MB),Time (ms),,Valid?,Score,RAM (MB),Time (ms),,Valid?,Score,RAM (MB),Time (ms)" +
                ",,Valid?,Score,RAM,Time (ms),,\n";
        csvWriter.write(top);
        csvWriter.write(headers);

        // Create each row in the file based on the data in the correct entries in the arrays
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

        // Include a footer with details of the parameters
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
        String top = name + ",,,,,,\n";
        String header = "Valid?, Score, RAM (MB), Time (ms), "+param_name+", ,\n";
        if (SEGMENTS != null) {
            top = name + ",,,,,,Times (ms),,,,\n";
            header = "Valid?, Score, RAM (MB), Time (ms), "+param_name+", , Start, Modelled, Solved, Decoded,\n";
        }
        csvWriter.write(top);
        csvWriter.write(header);

        // Create each row in the file based on the data in the correct entries in the arrays
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

        // Include a summary line in datasets which include segment times, indicating the average segment durations
        if (SEGMENTS != null) {
            String summary =
                    "\n,,,,,,,"+((double) SEGMENTS[1][rows] / (1000*1000))+","+((double) SEGMENTS[2][rows] / (1000*1000))+","+((double) SEGMENTS[3][rows] / (1000*1000))+",\n";
            csvWriter.write(summary);
        }

        // Include a footer with details of the parameters
        String footer =
                "\n\nThe DEFAULT parameters of the random test data: Days:"+DEF_DAYS+"  Hours:8  Rooms:"+DEF_ROOMS+"  Sessions:"+DEF_SESSIONS+"  Individuals:"+DEF_INDIVIDUALS+" (Ignore for the specified parameter(s)).";
        csvWriter.write(footer);

        csvWriter.close();
        return true;
    }


    public static SchedulingProblem guaranteed_randomized_test_details(int days, int rooms, int num_sessions, int num_individuals) {
        int hours = 8;
        Timetable sample = new Timetable(days, hours, rooms);
        SchedulingProblem details = new SchedulingProblem();

        if (num_sessions > days * rooms * hours) {
            System.err.println("Cannot create a schedule with "+num_sessions+" sessions in only "+(days*rooms*hours)+" available timeslots.");
            return null;
        }

        // Lock in the defined parameters and randomise the preferences
        details.Maximum_Days = days;
        details.Hours_Per_Day = hours;
        details.Maximum_Rooms = rooms;

        details.Session_Details = new ArrayList<>();
        details.PDS_Details = new ArrayList<>();
        details.KeyInd_Details = new ArrayList<>();
        details.Room_Occupancy_Limits = new ArrayList<>();

        details.Reduce_Overlap_Pref = (Math.random() >= 0.5);
        details.Minimum_Gap_Pref = generate_number(4, 0);

        // Add a session to the timetable by randomising details
        for (int s=0; s<num_sessions;) {
            int rand_day = generate_number(days-1, 0);
            int rand_time = generate_number(hours-1, 0);
            int rand_room = generate_number(rooms-1, 0);
            //int len = generate_number(6, 0);

            // Check if those details point to a free slot and add the session
            if (sample.get_id(rand_day, rand_time, rand_room) == -1) {
                Session new_session = new PredeterminedSession(s, "Session #"+s, 1, new ArrayList<>(), rand_day, rand_time, rand_room);
                sample.set(rand_day, rand_time, rand_room, new_session);
                details.Session_Details.add(new_session);
                s++;
            }
        }

        // Give the sessions lengths
        for (int s=0; s<num_sessions; s++) {
            PredeterminedSession pds = (PredeterminedSession) details.Session_Details.get(s);
            int h = pds.PDS_Start_Time;

            // Find the number of free spaces after the sessions start
            while ((h+1)<hours && sample.get_id(pds.PDS_Day, h+1, pds.PDS_Room) == -1) {
                h++;
            }
            if (h == pds.PDS_Start_Time) continue;

            int len = generate_number(h-pds.PDS_Start_Time, 1);
            pds.Session_Length = len;
            for (int hour=pds.PDS_Start_Time; hour<(pds.PDS_Start_Time+len); hour++) {
                sample.set(pds.PDS_Day, hour, pds.PDS_Room, s,hour-pds.PDS_Start_Time);
            }
        }

        // Create individuals who are included in random sessions
        for (int p=0; p<num_individuals; p++) {
            List<Integer> new_room_prefs = generate_numbers(rooms-1, 0, generate_number((int)(rooms*0.1)+1,0));
            KeyIndividual new_individual = new KeyIndividual("Person #"+p, generate_number(hours-1,0), new_room_prefs);
            details.KeyInd_Details.add(new_individual);

            // Choose a number of sessions to be part of
            int included_sessions_num = generate_number((int)((num_sessions*0.2)+1), 1);
            if (included_sessions_num > (num_sessions-1)) included_sessions_num = 1;
            List<Integer> included_sessions = generate_numbers(num_sessions-1, 0, included_sessions_num);

            // Check that those sessions do not clash
            boolean[][] busy = new boolean[days][hours];
            for (Integer s : included_sessions) {
                PredeterminedSession pds = (PredeterminedSession) details.Session_Details.get(s);

                //  Find out if this individual is not busy at any of the times in this slot,
                boolean slot_open = true;
                for (int h = pds.PDS_Start_Time; h<(pds.PDS_Start_Time+pds.Session_Length); h++) {
                    if (busy[pds.PDS_Day][h]) {slot_open = false; break;}
                }

                // If they're not: add this individual to the session, and record them as busy
                if (slot_open) {
                    pds.Session_KeyInds.add(p);
                    for (int h = pds.PDS_Start_Time; h<(pds.PDS_Start_Time+pds.Session_Length); h++) {
                        busy[pds.PDS_Day][h] = true;
                    }
                }
            }
        }

        // Add individuals to sessions with none
        for (int s=0; s<num_sessions;) {
            PredeterminedSession pds = (PredeterminedSession) details.Session_Details.get(s);
            if (details.Session_Details.get(s).Session_KeyInds.size() > 0) {s++; continue;}

            // Determine which individuals are busy at this time
            List<Integer> busy_individuals = new ArrayList<>();
            for (int h=pds.PDS_Start_Time; h<(pds.PDS_Start_Time+pds.Session_Length); h++) {
                for (int r=0; r<rooms; r++) {
                    if (sample.get_id(pds.PDS_Day, h, r) == -1) continue;
                    List<Integer> busy = details.Session_Details.get(sample.get_id(pds.PDS_Day, h, r)).Session_KeyInds;
                    busy_individuals.addAll(busy);
                }
            }

            // If every single person is busy at this time just leave the session empty
            if (busy_individuals.size() == num_individuals) {s++; continue;};

            // Find the list of individuals who are not busy
            List<Integer> all_individuals = new ArrayList<>();
            List<Integer> free_individuals = new ArrayList<>();
            for (int p=0; p<num_individuals; p++) all_individuals.add(p);
            for (Integer a : all_individuals) {
                if (!busy_individuals.contains(a)) free_individuals.add(a);
            }

            if (free_individuals.size() == 1) {pds.Session_KeyInds.add(free_individuals.get(0)); s++; continue;}
            if (free_individuals.size() == 0) {s++; continue;}

            // Add a random selection of them to the session
            int num_to_add = generate_number((int)Math.min(free_individuals.size(), (num_individuals*0.15)+1), 1);
            List<Integer> indexes_to_add = generate_numbers(free_individuals.size()-1, 0, num_to_add);
            List<Integer> to_add = new ArrayList<>();
            for (Integer i : indexes_to_add) {
                to_add.add(free_individuals.get(i));
            }
            pds.Session_KeyInds.addAll(to_add);
        }

        // Give room occupancies (at minimum the number of participants in the session, in that room, with most people)
        for (int r=0; r<rooms; r++) {

            // Determined the maximum number of people in any one session in this room
            int max_people_in_room = 1;
            for (Session sesh : details.Session_Details) {
                PredeterminedSession pds = (PredeterminedSession) sesh;
                if (pds.Session_KeyInds.size() > max_people_in_room) max_people_in_room = pds.Session_KeyInds.size();
            }

            // And randomly generate a limit between that and the total individuals available
            int occupancy = generate_number(num_individuals+1, max_people_in_room);
            occupancy = (int) (Math.ceil((float)occupancy / 5) * 5);
            details.Room_Occupancy_Limits.add(occupancy);
        }

        // Chose a portion of PDS and change the rest into normal sessions (keep the PDS in the normal details!)
        int num_predetermined = generate_number((int)(num_sessions*0.1)+1, 1);
        for (int s=0; s<num_sessions; s++) {

            if (s < (num_sessions - num_predetermined)) {
                // Swap out the existing PDS for a session with the same details (but no set slot)
                PredeterminedSession existing = (PredeterminedSession) details.Session_Details.get(s);
                assert (s == existing.Session_ID);
                Session replacement = new Session(s, existing.Session_Name, existing.Session_Length, existing.Session_KeyInds);
                details.Session_Details.set(s, replacement);

            } else {
                // Keep the last num_predetermined of the list as PDSes, also add those to the PDS list
                details.PDS_Details.add((PredeterminedSession) details.Session_Details.get(s));
            }
        }

        if (Main.DEBUG && false) {
            System.out.println("The random schedule which the algorithms may recreate:\n" + sample.toString());
            System.out.println("The details which arise:\n" + details.toString());
            TimetableVerifier ttv = new TimetableVerifier();
            System.out.println("The schedule + details are valid together? " + (ttv.timetable_is_valid(sample, details)));
        }
        latest_details_tt = sample;
        return details;
    }

    // Randomly generates a number between minimum and maximum
    static int generate_number(int maximum, int minimum) {
        return (int) (minimum + (Math.random() * (maximum - minimum + 1)));
    }

    // Randomly generates a (duplicate-free) list, of length count, of numbers between minimum and maximum
    static List<Integer> generate_numbers(int maximum, int minimum, int count) {
        if (count > (maximum - minimum + 1)) {
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

    // Prints a set of session details to a text file
    private static boolean save_details_to_file(Object details, String path)  {
        if (details == null) return false;
        File text = new File(path);

        try {
            FileWriter fileWriter = new FileWriter(text);
            fileWriter.write(details.toString());
            fileWriter.close();

        } catch (IOException e) {
            return false;
        }
        return true;
    }

}
