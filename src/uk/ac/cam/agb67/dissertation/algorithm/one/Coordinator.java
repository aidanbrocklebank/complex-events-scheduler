package uk.ac.cam.agb67.dissertation.algorithm.one;

import uk.ac.cam.agb67.dissertation.*;

public class Coordinator implements SchedulingAlgorithm {

    public Coordinator() {

    }

    public Timetable generate(SchedulingProblem details) {

        // Input validation step
        details.check_validity();

        // Create an empty timetable
        Timetable schedule = new Timetable(details.Maximum_Days, details.Hours_Per_Day, details.Maximum_Rooms);

        // Place the pre-determined sessions into the timetable
        for (PredeterminedSession pds : details.PDS_Details) {
            schedule.set(pds.PDS_Day, pds.PDS_Start_Time, pds.PDS_Room, pds);
        }
        //if (Main.DEBUG) System.out.println(schedule.toString());

        // Pass it into BruteForce solver
        BruteForce solver = new BruteForce(details.Maximum_Days, details.Hours_Per_Day, details.Maximum_Rooms, details.KeyInd_Details, details.Room_Occupancy_Limits,
                details.Session_Details);
        schedule = solver.insert_sessions(schedule, details.Session_Details);

        // Check it with Verifier
        TimetableVerifier verifier = new TimetableVerifier();
        boolean coherent = verifier.timetable_is_coherent(schedule, details.Session_Details);
        if (!coherent) System.err.println("Timetable was not coherent after brute force approach.");

        // Simple/greedy Optimisation stage?

        System.out.println(schedule.toString());
        return schedule;
    }

}
