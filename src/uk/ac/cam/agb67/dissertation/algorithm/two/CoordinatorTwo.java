package uk.ac.cam.agb67.dissertation.algorithm.two;

import uk.ac.cam.agb67.dissertation.*;
import org.chocosolver.solver.*;
import org.chocosolver.solver.variables.*;
import org.chocosolver.solver.constraints.extension.*;

import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

public class CoordinatorTwo implements SchedulingAlgorithm {
// In the following comments, timeslot refers to an assigned day/time/room combination

    public CoordinatorTwo() {

    }

    @Override
    public Timetable generate(SchedulingProblem details) {

        // Input validation step
        if (!details.check_validity()) {
            System.err.println("The given scheduling problem details were invalid.");
            return null;
        }

        // Use Choco-solver to model this scheduling problem, solve it, and then translate the solution back into a timetable
        Timetable schedule = represent_and_solve(details);

        if (Main.DEBUG && schedule != null) System.out.println(schedule.toString());

        return schedule;
    }

    private Timetable represent_and_solve(SchedulingProblem details) {

        Model event = new Model();
        int num_sessions = details.Session_Details.size();

        // Variables declaration
        IntVar[] day_assignments = new IntVar[num_sessions];
        IntVar[] start_time_assignments = new IntVar[num_sessions];
        IntVar[] room_assignments = new IntVar[num_sessions];

        // Give the variables there appropriate domains, or for predetermined sessions set them to their given values
        for (int s=0; s<num_sessions; s++) {
            if (details.Session_Details.get(s).getClass() != PredeterminedSession.class) {
                //if (Main.DEBUG) System.out.println("Adding normal session #"+s+" .");
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


        // Define a unique hash for each timeslot assignment with the formula:
        // room + time(MaxRooms) + day(MaxHours)(MaxRooms)
        IntVar[] start_timeslot_hash = new IntVar[num_sessions];
        List<IntVar> timeslot_hash = new ArrayList<>();
        for (int s=0; s<num_sessions; s++) {
            //start_timeslot_hash[s] = room_assignments[s].add(start_time_assignments[s].mul(details.Maximum_Rooms), day_assignments[s].mul(details.Hours_Per_Day).mul(details.Maximum_Rooms)).intVar();

            // We also define a unique hash for every timeslot which is included in the length of the session
            // room + (time+offset)(MaxRooms) + day(MaxHours)(MaxRooms)
            for (int offset=0; offset<details.Session_Details.get(s).Session_Length; offset++) {
                IntVar temp = room_assignments[s].add((start_time_assignments[s].add(offset)).mul(details.Maximum_Rooms),
                        day_assignments[s].mul(details.Hours_Per_Day).mul(details.Maximum_Rooms)).intVar();
                timeslot_hash.add(temp);
            }
            // The timeslot_hash list is not perfect however as offsets could spill over into other hashcodes if time+offset > MaxHours

            // So we include an additional constraint to ensure no session starts too close to the end of the day
            event.arithm(event.intScaleView(start_time_assignments[s], details.Session_Details.get(s).Session_Length), "<=", details.Hours_Per_Day).post();
            // Condition: [start + length <= MaxHours] for session s
        }

        // We then force these start-timeslot hashcodes to be unique among sessions
        // event.allDifferent(start_timeslot_hash).post();
        // TODO I believe the above line is redundant due to it's purpose being covered by the following

        // And the same for all timeslot hashcodes
        IntVar[] timeslot_hash_array = intvar_list_to_array(timeslot_hash);
        event.allDifferent(timeslot_hash_array).post();


        // We will use the integer constraint factory to impose the requirement that assigned rooms have enough capacity
        int[] room_occupancy_limits = int_list_to_array(details.Room_Occupancy_Limits);
        int greatest_limit = 0; for (int lim : details.Room_Occupancy_Limits) {greatest_limit = Math.max(greatest_limit, lim);}

        for (int s=0; s<num_sessions; s++) {
            // For each session create an IntVar which tracks how large the room assigned to the session is
            IntVar room_limit = event.intVar(("Room Capacity for session #" + s), 0, greatest_limit);
            event.element(room_limit, room_occupancy_limits, room_assignments[s]).post();

            // Ensure that the room size available to this session is greater than the number of people involved
            event.arithm(room_limit, ">=", details.Session_Details.get(s).Session_KeyInds.size()).post();
        }


        // Ensure that key individuals are only assigned to one session at a time
        for (int keyID=0; keyID<details.KeyInd_Details.size(); keyID++) {

            // Prepare a list of all timeslot-hour hashcodes for this individual
            List<IntVar> relevant_timeslot_hash = new ArrayList<>();

            // Iterate through all sessions and Check that is individual is included in this session
            for (Session sesh : details.Session_Details) {
                if (sesh.Session_KeyInds.contains(keyID)) {

                    // We take a hash of the day and time (but NOT the room) of each session which includes this individual
                    // So if they are in two parallel sessions with the same room, they will have the same hash
                    for (int offset = 0; offset < sesh.Session_Length; offset++) {
                        // TODO IntVar temp = start_time_assignments[sesh.Session_ID].add(offset).add(day_assignments[sesh.Session_ID].mul(details.Hours_Per_Day)).intVar();
                        // TODO relevant_timeslot_hash.add(temp);

                        // TODO figure out why this hack works/is necessary?
                        // The above implementation is the correct one, as afar as I can tell
                        // But Choco-Solver tells me that it cannot find any solutions when I add those variables to the model, before even applying the constraint
                        // Hack: So here is a modification which includes a pointless addition, and two pointless multiplications, and this version works

                        int s = sesh.Session_ID;
                        // SAMPLE FROM ABOVE:
                         IntVar temp = room_assignments[0].add((start_time_assignments[s].add(offset)).mul(details.Maximum_Rooms),
                                                day_assignments[s].mul(details.Hours_Per_Day).mul(details.Maximum_Rooms)).intVar();
                        relevant_timeslot_hash.add(temp);
                    }
                }
            }

            // Then force all the timeslot hours that this individual is present in to be unique
            IntVar[] relevant_timeslot_hash_array = intvar_list_to_array(relevant_timeslot_hash);
            event.allDifferent(relevant_timeslot_hash_array).post();
        }


        // Use the solver to find the first acceptable solution to the problem
        Solver solver = event.getSolver();

        if (Main.DEBUG) System.out.println("Printing full Event Model:\n");
        if (Main.DEBUG) System.out.println(event.toString());

        solver.getSearchState();

        if(solver.solve()){
            // Turn the instantiated values into a timetable to return
            Timetable schedule = decode_model(details, day_assignments, start_time_assignments, room_assignments);
            return schedule;

        }else if(solver.isStopCriterionMet()){
            System.err.println("The Choco-Solver could not determine whether or not a solution existed.");
            if (Main.DEBUG) System.err.println(solver.getSearchState());
            return null;
        }else {
            System.err.println("No solution exists which satisfies the constraints.");
            if (Main.DEBUG) System.err.println(solver.getSearchState());
            return null;
        }
    }

    private Timetable decode_model(SchedulingProblem details, IntVar[] day_assignments, IntVar[] start_time_assignments, IntVar[] room_assignments) {
        // Create a new schedule, then iterate through the sessions in the event
        // For every session retrieve the instantiated value of the IntVars for the day, time and room
        Timetable schedule = new Timetable(details.Maximum_Days, details.Hours_Per_Day, details.Maximum_Rooms);
        for (int s=0; s<details.Session_Details.size(); s++) {
            schedule.set(day_assignments[s].getValue(), start_time_assignments[s].getValue(), room_assignments[s].getValue(), details.Session_Details.get(s));
        }
        return schedule;
    }

    private int[] int_list_to_array(List<Integer> list) {
        int[] array = new int[list.size()];

        for (int i=0; i<list.size(); i++) {
            array[i] = list.get(i);
        }

        return array;
    }

    private IntVar[] intvar_list_to_array(List<IntVar> list) {
        IntVar[] array = new IntVar[list.size()];

        for (int i=0; i<list.size(); i++) {
            array[i] = list.get(i);
        }

        return array;
    }

}
