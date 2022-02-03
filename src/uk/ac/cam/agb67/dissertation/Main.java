package uk.ac.cam.agb67.dissertation;

import uk.ac.cam.agb67.dissertation.algorithm.one.*;
import uk.ac.cam.agb67.dissertation.algorithm.two.*;

import java.util.ArrayList;
import java.util.List;

public class Main {

    public static boolean DEBUG = true;

    // Algorithm Selection
    String Algorithm_Selection;

    // The Full Details of the Event to Schedule
    SchedulingProblem Our_Event;


    public static void main(String[] args) {

        Coordinator algo_one = new Coordinator(false);
        CoordinatorTwo algo_two = new CoordinatorTwo(false);

        DEBUG = false;

        int tests= 100;
        boolean[] algo_one_results = new boolean[tests];
        boolean[] algo_two_results = new boolean[tests];
        boolean all_passed = true;

        for (int i = 0; i < tests; i++) {
            // Generate random data
            SchedulingProblem details = randomized_test_details(8, 5, 10, 25);

            // Test both algorithms with the generated data
            algo_one_results[i] = test_algorithm(algo_one, details);
            algo_two_results[i] = test_algorithm(algo_two, details);
            if (!algo_one_results[i] || !algo_two_results[i]) all_passed = false;
        }

        if (!all_passed) {
            int algo_one_passes = 0;
            int algo_two_passes = 0;
            int mutual_failures = 0;
            for (int i = 0; i < tests; i++) {
                if (algo_one_results[i]) algo_one_passes++;
                if (algo_two_results[i]) algo_two_passes++;
                if (!algo_one_results[i] && !algo_two_results[i]) mutual_failures++;
            }
            System.out.println("Algorithm One passed "+algo_one_passes+" tests. Algorithm two passed "+algo_two_passes+" tests. There were "+mutual_failures+" mutual " +
                    "failures.");
        }

        System.out.println("Done.");
        System.out.println("Did all tests pass? "+all_passed);
    }

    // Test out a given algorithm on some randomly generated data
    public static boolean test_algorithm(SchedulingAlgorithm algo, SchedulingProblem details) {

        // Use the given algorithm to generate a schedule
        Timetable tt = algo.generate(details);

        // Print the schedule
        if (DEBUG) System.out.println("The produced schedule: \n"+tt.toString());

        // Check it's accuracy
        TimetableVerifier ttv = new TimetableVerifier();
        boolean valid = ttv.timetable_is_valid(tt, details);
        System.out.println("Schedule was valid? "+ valid+"!\n\n");
        return valid;
    }

    // Randomly generates a scheduling problem to use as test data, given certain parameters
    public static SchedulingProblem randomized_test_details(int days, int rooms, int num_sessions, int num_individuals, boolean overlap_pref, int gap_pref) {
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
            PredeterminedSession replace = new PredeterminedSession(remove.Session_ID, remove.Session_Name+"(PDS)", remove.Session_Length, remove.Session_KeyInds,
                    generate_number(days-1, 0), generate_number(8-remove.Session_Length, 0), generate_number(rooms-1, 0));

            // Then replace the session
            details.Session_Details.remove(s);
            details.Session_Details.add(s, replace);
            details.PDS_Details.add(replace);
            if (DEBUG) System.out.println("Converting a session to predetermined. ID: #"+s+", length: "+remove.Session_Length+", day: "+replace.PDS_Day+", time: "+replace.PDS_Start_Time+", room:"+replace.PDS_Room);
        }

        return details;
    }

    // Randomly generates test data with randomized gap preference and overlap preference
    public static SchedulingProblem randomized_test_details(int days, int rooms, int num_sessions, int num_individuals) {
        int gap_pref = generate_number(3, 0);
        double overlap_rand = Math.random();

        if (DEBUG) System.out.println("desired gap: " +gap_pref+ ",    desire overlap?: "+ (overlap_rand >= 0.5));

        return randomized_test_details(days, rooms, num_sessions, num_individuals, (overlap_rand >= 0.5), gap_pref);
    }

    // Randomly generates a number between minimum and maximum
    public static int generate_number(int maximum, int minimum) {
        return (int) (minimum + (Math.random() * (maximum - minimum + 1)));
    }

    // Randomly generates a (duplicate-free) list, length count, of numbers between minimum and maximum
    public static List<Integer> generate_numbers(int maximum, int minimum, int count) {
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
