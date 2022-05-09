package uk.ac.cam.agb67.dissertation.algorithm.two;

import uk.ac.cam.agb67.dissertation.*;
import static uk.ac.cam.agb67.dissertation.Analyser.convert_time;

import org.chocosolver.solver.*;
import org.chocosolver.solver.variables.*;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.selectors.variables.*;
import org.chocosolver.solver.search.strategy.selectors.values.*;
import org.chocosolver.solver.exception.ContradictionException;

import java.util.ArrayList;
import java.util.List;

public class CoordinatorTwo implements SchedulingAlgorithm {
// In the following comments, timeslot refers to an assigned day/time/room combination

    private boolean optimise_for_prefs = false;
    String time_limt = "60s";
    String opt_time_limit = "60s";

    CoordinatorTwo() {}
    public CoordinatorTwo(boolean opt) {
        optimise_for_prefs = opt;
    }

    // Generates a schedule for the given event details using the CSP approach, or CSPRO variant
    @Override
    public Timetable generate(SchedulingProblem details) {

        String s = "";
        if (optimise_for_prefs) s="(Maximising preference values).";
        if (Main.DEBUG) System.out.println("\nAttempting to generate a schedule with algorithm two. "+s+"\n");

        // Input validation step
        if (!details.potentially_schedulable()) {
            System.err.println("The given scheduling problem details were invalid.");
            return null;
        }

        // Create the variable arrays which Choco-Solver will use in it's model
        IntVar[] day_assignments = new IntVar[details.Session_Details.size()];
        IntVar[] start_time_assignments = new IntVar[details.Session_Details.size()];
        IntVar[] room_assignments = new IntVar[details.Session_Details.size()];

        // Record first segment time
        Analyser.SEGMENT_TIMES[0] = System.nanoTime();
        if (Main.DEBUG) System.out.println("Pre-MODEL Time (ms): " + (convert_time(System.nanoTime())));

        // Use Choco-solver to model this scheduling problem,
        Model event_model = represent(details, day_assignments, start_time_assignments, room_assignments);
        Timetable schedule;

        // Record second segment time
        Analyser.SEGMENT_TIMES[1] = System.nanoTime();
        if (Main.DEBUG) System.out.println("Post-MODEL Time (ms): " + (convert_time(System.nanoTime())));

        if (optimise_for_prefs) {
            // Use Choco-solver to solve the model, finding the solution which maximises a metric for preference satisfaction
            Solution sol = optimise_and_solve(event_model, details, day_assignments, start_time_assignments, room_assignments);

            // Record third segment time
            Analyser.SEGMENT_TIMES[2] = System.nanoTime();
            if (Main.DEBUG) System.out.println("Post-OPT-SOLVE Time (ms): " + (convert_time(System.nanoTime())));

            if (sol == null) {
                System.err.println("The optimising variant failed to solve the model. Returning null.");
                Analyser.SEGMENT_TIMES[3] = System.nanoTime();
                return null;
            }

            // Finally decode the solution into a schedule
            schedule = decode_solution(sol, details, day_assignments, start_time_assignments, room_assignments);

            // Record fourth segment time
            Analyser.SEGMENT_TIMES[3] = System.nanoTime();
            if (Main.DEBUG) System.out.println("Post-DECODE Time (ms): " + (convert_time(System.nanoTime())));

        } else {

            // Use Choco-solver to solve the model, taking the first acceptable solution
            boolean solved = solve(event_model);

            // Record third segment time
            Analyser.SEGMENT_TIMES[2] = System.nanoTime();
            if (Main.DEBUG) System.out.println("Post-SOLVE Time (ms): " + (convert_time(System.nanoTime())));

            if (!solved) {
                System.err.println("The model was not solved."); Analyser.SEGMENT_TIMES[3] = System.nanoTime(); return null;
            }

            // Finally decode the solved model into a schedule
            schedule = decode_model_vars(details, day_assignments, start_time_assignments, room_assignments);

            // Record fourth segment time
            Analyser.SEGMENT_TIMES[3] = System.nanoTime();
            if (Main.DEBUG) System.out.println("Post-DECODE Time (ms): " + (convert_time(System.nanoTime())));
        }

        // Return schedule
        if (Main.DEBUG) System.out.println("Algorithm two has generated a schedule:");
        if (Main.DEBUG) System.out.println(schedule.toString());
        return schedule;
    }

    // Models the event details given as a CSP problem for Choco-solver, storing the assignment variables in the given arrays
    private Model represent(SchedulingProblem details, IntVar[] day_assignments, IntVar[] start_time_assignments, IntVar[] room_assignments) {

        Model event = new Model();
        int num_sessions = details.Session_Details.size();

        // Give the variables their appropriate domains, or for predetermined sessions set them to their given values
        for (int s = 0; s < num_sessions; s++) {
            if (details.Session_Details.get(s).getClass() != PredeterminedSession.class) {
                //if (Main.DEBUG) System.out.println("The number of days is "+details.Maximum_Days+" .");
                day_assignments[s] = event.intVar("Day Assignment for session #" + s, 0, details.Maximum_Days - 1, false);
                start_time_assignments[s] = event.intVar("Start Time Assignment for session #" + s, 0, details.Hours_Per_Day - 1, false);
                room_assignments[s] = event.intVar("Room Assignment for session #" + s, 0, details.Maximum_Rooms - 1, false);
            } else {
                // The session is predetermined, so lock the IntVars to the correct values
                //if (Main.DEBUG) System.out.println("Adding predetermined session #"+s+" .");
                PredeterminedSession session = (PredeterminedSession) details.Session_Details.get(s);
                day_assignments[s] = event.intVar("Day Assignment for session #" + s, session.PDS_Day);
                start_time_assignments[s] = event.intVar("Start Time Assignment for session #" + s, session.PDS_Start_Time);
                room_assignments[s] = event.intVar("Room Assignment for session #" + s, session.PDS_Room);
            }
        }


        // The following code creates the constraint which enforces each session having a unique combination of day, room, and set of hours.
        List<IntVar> timeslot_hash = new ArrayList<>();
        for (int s = 0; s < num_sessions; s++) {

            // We define a unique hash for every timeslot which is included in the length of the session
            // room + (time+offset)(MaxRooms) + day(MaxHours)(MaxRooms)
            for (int offset = 0; offset < details.Session_Details.get(s).Session_Length; offset++) {
                IntVar temp = room_assignments[s].add((start_time_assignments[s].add(offset)).mul(details.Maximum_Rooms),
                        day_assignments[s].mul(details.Hours_Per_Day).mul(details.Maximum_Rooms)).intVar();
                timeslot_hash.add(temp);
            }

            // The timeslot_hash list is not perfect however as offsets could spill over into other hashcodes if time+offset > MaxHours
            // So we include an additional constraint to ensure no session starts too close to the end of the day
            // Condition: [start + length <= MaxHours] for session s
            event.arithm(event.intOffsetView(start_time_assignments[s], details.Session_Details.get(s).Session_Length), "<=", details.Hours_Per_Day).post();
        }

        // We then require that time-slot hashcodes are all unique in the model
        IntVar[] timeslot_hash_array = intvar_list_to_array(timeslot_hash);
        event.allDifferent(timeslot_hash_array).post();


        // We will use an element integer constraint to impose the requirement that assigned rooms have enough capacity
        int[] room_occupancy_limits = int_list_to_array(details.Room_Occupancy_Limits);
        int greatest_limit = 0;
        for (int lim : details.Room_Occupancy_Limits) {
            greatest_limit = Math.max(greatest_limit, lim);
        }

        for (int s = 0; s < num_sessions; s++) {
            // For each session create an IntVar which tracks how large the room assigned to the session is
            IntVar room_limit = event.intVar(("Room Capacity for session #" + s), 0, greatest_limit, false);
            // This calls the element factory, creating a constraint which ensures that room_limit = room_occupancy_limits[room_assignments[s]]
            event.element(room_limit, room_occupancy_limits, room_assignments[s]).post();

            // Ensure that the room size available to this session is greater than the number of people involved
            event.arithm(room_limit, ">=", details.Session_Details.get(s).Session_KeyInds.size()).post();
        }


        // Ensure that key individuals are only assigned to one session at a time
        for (int keyID = 0; keyID < details.KeyInd_Details.size(); keyID++) {

            // Prepare a list of all timeslot-hour hashcodes for this individual
            List<IntVar> relevant_timeslot_hash = new ArrayList<>();

            // Iterate through all sessions and Check that is individual is included in this session
            for (Session sesh : details.Session_Details) {
                if (sesh.Session_KeyInds.contains(keyID)) {

                    // We take a hash of the day and time (but NOT the room) of each session which includes this individual
                    // So if they are in two parallel sessions with the same room, they will have the same hash
                    for (int offset = 0; offset < sesh.Session_Length; offset++) {

                        // Computes the hash value (start_time + offset) + (day)(Hours_Per_Day)
                        IntVar temp = start_time_assignments[sesh.Session_ID].add(offset).add(day_assignments[sesh.Session_ID].mul(details.Hours_Per_Day)).intVar();
                        relevant_timeslot_hash.add(temp);

                        // The above implementation is the correct one, but here is a version which introduces a redundant constant
                        // This modified version was necessary to make the algorithm work in Choco-solver 4.10.7
                        //IntVar extra_constant = event.intVar("constant for session #" + sesh.Session_ID, 1);
                        //IntVar temp = extra_constant.add((start_time_assignments[sesh.Session_ID].add(offset)),day_assignments[sesh.Session_ID].mul(details.Hours_Per_Day)).intVar();
                        //relevant_timeslot_hash.add(temp);
                    }
                }
            }

            // Then force all the timeslot hours that this individual is present in to be unique
            IntVar[] relevant_timeslot_hash_array = intvar_list_to_array(relevant_timeslot_hash);
            event.allDifferent(relevant_timeslot_hash_array).post();
        }

        return event;
    }

    // Uses the solver to find the first acceptable solution to the problem, and returns true if successful
    private boolean solve(Model event) {

        Solver solver = event.getSolver();

        if (Main.DEBUG) System.out.println("Printing full Event Model:\n");
        if (Main.DEBUG) System.out.println(event.toString());

        // Search Strategy
        // Choose an un-instantiated variable with the smallest domain, and select values from the beginning of its domain
        solver.setSearch(Search.intVarSearch(
                new FirstFail(event),
                new IntDomainMin(),
                event.retrieveIntVars(true)
        ));

        // Set a time limit to aid in bulk testing
        solver.limitTime(time_limt);

        if(solver.solve()) {
            // The values are now instantiated so return true to indicate that we can move to decoding
            return true;

        } else {
            // Output the relevant error message.

            if(solver.isStopCriterionMet()) {
                System.err.println("Choco-Solver could not determine whether or not a solution existed, because it was stopped by the time limit.");
            } else {
                System.err.println("No solution exists which satisfies the constraints.");
            }

            if (Main.DEBUG) {
                System.err.println("Search status: " + solver.getSearchState());
                solver.printStatistics();
            }
            return false;
        }
    }

    // Uses the solver with a randomised search strategy to produce a series of distinct solutions, and returns the highest-scoring
    private Solution optimise_and_solve(Model event, SchedulingProblem details, IntVar[] day_assignments, IntVar[] start_time_assignments, IntVar[] room_assignments) {

        // Use the solver to find an acceptable solution to the problem, and iterate to improve the satisfaction score
        Solver solver = event.getSolver();
        Solution best_solution = null;
        int max_score = 0;

        TimetableVerifier ttv = new TimetableVerifier();
        TimetableSatisfactionMeasurer ttsm = new TimetableSatisfactionMeasurer();

        // Visually track progress
        if (Main.DEBUG) System.out.println("- random optimising -");
        System.out.println("..........");

        for (int i=0; i<10; i++) {
            if (Main.DEBUG)System.out.print(".");

            // Search Strategy:
            // Choose an uninstantiated variable with a random domain, and select random values from within that domain to try
            long seedA = (long) (Math.random() * Long.MAX_VALUE);
            long seedB = (long) (Math.random() * Long.MAX_VALUE);
            if (Main.DEBUG) System.out.println("Iteration of optimising search. Seed A: " + seedA + ", Seed B: "+ seedB);

            // Reset the solver object and give it the newly randomised strategy
            solver.reset();
            solver.setSearch(Search.intVarSearch(
                    new Random<>(seedA),
                    new IntDomainRandom(seedB),
                    event.retrieveIntVars(true)
            ));
            solver.limitTime(opt_time_limit);

            // Find and decode a solution based on this search strategy
            Solution sol = solver.findSolution();
            if (sol == null) break;
            Timetable prospect = decode_solution(sol, details, day_assignments, start_time_assignments, room_assignments);
            boolean prospect_valid; int prospect_score;

            // Check that is it is valid and record it's score
            try {
                prospect_valid = ttv.timetable_is_valid(prospect, details);
                prospect_score = ttsm.timetable_preference_satisfaction(prospect, details);
                if (Main.DEBUG) System.out.println("Found a valid timetable with score "+prospect_score+".");
            } catch (Exception e) {
                if (Main.DEBUG) System.out.println("The decoded schedule was not valid, and testing it threw an exception.");
                break;
            }

            // If it creates a valid timetable and gives a better score, then replace the solution we will return
            if (prospect_valid && prospect_score > max_score) {
                if (Main.DEBUG) System.out.println("Updating best solution due to new score of "+prospect_score+".");
                max_score = prospect_score;
                best_solution = sol;
            }

        }

        if (best_solution != null) {
            return best_solution;

        } else {
            // Various relevant error messages and debug information:
            System.err.println("Optimising version of algorithm 2 failed to find a result. Reason: ");

            if(solver.isStopCriterionMet()) {
                System.err.println("Choco-Solver could not determine whether or not a solution existed, because it was stopped by the time limit.");
            } else {
                System.err.println("No solution exists which satisfies the constraints.");
            }
            if (Main.DEBUG) System.err.println("Search status: " + solver.getSearchState());
            return null;
        }
    }

    // Returns a timetable which is formed by the instantiated arrays of assignments
    private Timetable decode_model_vars(SchedulingProblem details, IntVar[] day_assignments, IntVar[] start_time_assignments, IntVar[] room_assignments) {
        // Create a new schedule, then iterate through the sessions in the event
        // For every session retrieve the instantiated value of the IntVars for the day, time and room
        Timetable schedule = new Timetable(details.Maximum_Days, details.Hours_Per_Day, details.Maximum_Rooms);
        for (int s=0; s<details.Session_Details.size(); s++) {
            schedule.set(day_assignments[s].getValue(), start_time_assignments[s].getValue(), room_assignments[s].getValue(), details.Session_Details.get(s));
        }
        return schedule;
    }

    // Returns a timetable which is formed by the instantiated assignments in the given solution
    private Timetable decode_solution(Solution sol, SchedulingProblem details, IntVar[] day_assignments, IntVar[] start_time_assignments, IntVar[] room_assignments) {
        // Create a new schedule, then iterate through the sessions in the event
        // For every session retrieve the instantiated value of the IntVars for the day, time and room
        Timetable schedule = new Timetable(details.Maximum_Days, details.Hours_Per_Day, details.Maximum_Rooms);
        for (int s=0; s<details.Session_Details.size(); s++) {
            schedule.set(sol.getIntVal(day_assignments[s]), sol.getIntVal(start_time_assignments[s]), sol.getIntVal(room_assignments[s]), details.Session_Details.get(s));
        }

        return schedule;
    }

    // Takes a list of integers and returns an array of those same numbers
    private int[] int_list_to_array(List<Integer> list) {
        int[] array = new int[list.size()];

        for (int i=0; i<list.size(); i++) {
            array[i] = list.get(i);
        }

        return array;
    }

    // Takes a list of IntVar objects and returns an array of those same objects
    private IntVar[] intvar_list_to_array(List<IntVar> list) {
        IntVar[] array = new IntVar[list.size()];

        for (int i=0; i<list.size(); i++) {
            array[i] = list.get(i);
        }

        return array;
    }

}