package uk.ac.cam.agb67.dissertation.algorithm.one;

import uk.ac.cam.agb67.dissertation.*;

public class Coordinator implements SchedulingAlgorithm {

    public Coordinator() {

    }

    @Override
    public Timetable generate(SchedulingProblem details) {

        // Input validation step
        if (!details.check_validity()) {
            System.err.println("The given scheduling problem details were invalid.");
            return null;
        }

        // Create an empty timetable
        Timetable schedule = new Timetable(details.Maximum_Days, details.Hours_Per_Day, details.Maximum_Rooms);

        // Place the pre-determined sessions into the timetable
        for (PredeterminedSession pds : details.PDS_Details) {
            schedule.set(pds.PDS_Day, pds.PDS_Start_Time, pds.PDS_Room, pds);
        }

        // Pass it into BruteForce solver
        BruteForce solver = new BruteForce(details.Maximum_Days, details.Hours_Per_Day, details.Maximum_Rooms, details.KeyInd_Details, details.Room_Occupancy_Limits,
                details.Session_Details);
        schedule = solver.insert_sessions(schedule, details.Session_Details);

        // Check it with the TimetableVerifier
        TimetableVerifier verifier = new TimetableVerifier();
        boolean coherent = verifier.timetable_is_valid(schedule, details);
        if (!coherent) {
            System.err.println("Timetable was not coherent after brute force approach.");
            if (Main.DEBUG) System.out.println(schedule.toString());
            return null;
        }

        // Inform the user if this algorithm has failed
        if (schedule == null) {
            System.err.println("Algorithm one failed to generate a schedule.");
            return null;
        }

        // Return schedule
        if (Main.DEBUG) System.out.println("Schedule generated by algorithm one successfully.");
        if (Main.DEBUG) System.out.println(schedule.toString());
        return schedule;
    }

}
