package uk.ac.cam.agb67.dissertation.algorithm.two;

import uk.ac.cam.agb67.dissertation.*;
import org.chocosolver.solver.*;
import org.chocosolver.solver.variables.*;
import org.chocosolver.solver.constraints.extension.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public class CoordinatorTwo implements SchedulingAlgorithm {

    public CoordinatorTwo() {

    }

    @Override
    public Timetable generate(SchedulingProblem details) {

        Model event = new Model();
        int num_sessions = details.Session_Details.size();

        // Variables declaration
        IntVar[] day_assignments = IntStream
                .range(0, num_sessions-1)
                .mapToObj(i -> event.intVar("day assignment for session #" + i, 0, details.Maximum_Days-1, false))
                .toArray(IntVar[]::new);
        IntVar[] start_time_assignments = IntStream
                .range(0, details.Session_Details.size()-1)
                .mapToObj(i -> event.intVar("start time for session #" + i, 0, details.Hours_Per_Day-1, false))
                .toArray(IntVar[]::new);
        IntVar[] room_assignments = IntStream
                .range(0, details.Session_Details.size()-1)
                .mapToObj(i -> event.intVar("room assignment for session #" + i, 0, details.Maximum_Rooms-1, false))
                .toArray(IntVar[]::new);

        System.out.println(day_assignments[2].toString());

        /*
        // Create a 3-part array for every session which holds their day, start-time and room
        List<IntVar[]> sessions = new ArrayList<>();
        for (int i=0; i<num_sessions; i++) {
            sessions.set(i, new IntVar[3]);
            sessions.get(i)[0] = day_assignments[i];
            sessions.get(i)[1] = start_time_assignments[i];
            sessions.get(i)[2] = room_assignments[i];
        }

        // Create a tuple for every valid assignment across the event
        Tuples timeslot = new Tuples(true);
        //allEqual.add(0, 0, 0);
        for (int i=0; i<details.Maximum_Days; i++) {
            for (int j=0; j<details.Hours_Per_Day; j++) {
                for (int k=0; k<details.Maximum_Rooms; k++) {
                    timeslot.add(i, j, k);
                }
            }
        }
        // Restrict every session array such that it must match a valid assignment
        event.table(sessions.get(0), timeslot, "CT+").post();
         */

        // TODO determine how to enforce a different tuple for every session

        /*
        int s = 0;
        IntVar unique = event.intOffsetView(room_assignments[s], event.intScaleView(details.Maximum_Rooms, event.intOffsetView(start_time_assignments[s],
                event.intScaleView(day_assignments[s], details.Hours_Per_Day))));
        //event_Model.allDifferent();
         */

        return null;
    }
}
