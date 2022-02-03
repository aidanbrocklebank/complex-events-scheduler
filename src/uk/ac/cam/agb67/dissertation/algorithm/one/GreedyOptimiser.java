package uk.ac.cam.agb67.dissertation.algorithm.one;

import uk.ac.cam.agb67.dissertation.*;

public class GreedyOptimiser {

    TimetableSatisfactionMeasurer TSM = new TimetableSatisfactionMeasurer();

    public GreedyOptimiser() {}

    public Timetable optimisation_pass(Timetable current, SchedulingProblem details) {

        // look through the current timetable, select a session to move
        for (Session sesh : details.Session_Details) {
            // Don't move predetermined sessions.
            if (sesh.getClass() == PredeterminedSession.class) continue;

            // Take the current satisfaction value of the timetable
            int current_score = TSM.timetable_preference_satisfaction(current, details);

            // make a copy of the timetable which excludes this session
            Timetable improvable = current.deep_copy_excluding(sesh.Session_ID);
            boolean improvable_contains_sesh = false;

            // find a new place for the session which works AND gives a better value
            // Iterate through all timetable slots
            outer_loop:
            for (int r = 0; r < current.Total_Rooms; r++) {
                for (int d = 0; d < current.Total_Days; d++) {
                    for (int h = 0; h < current.Hours_Per_Day; h++) {
                        if (h + sesh.Session_Length > current.Hours_Per_Day) continue;

                        // Find empty spaces
                        int sid = improvable.get_id(d, h, r);
                        if (sid == -1) {

                            // Check that we could place this session here
                            boolean doesnt_clash = Coordinator.check_session_doesnt_clash(improvable, d, h, r, sesh, details.Session_Details);
                            if (doesnt_clash && (details.Room_Occupancy_Limits.get(r) >= sesh.Session_KeyInds.size())) {

                                Timetable potential = improvable.deep_copy();
                                potential.set(d, h, r, sesh);
                                int potential_score = TSM.timetable_preference_satisfaction(potential, details);
                                if (potential_score > current_score) {
                                    improvable = potential;
                                    improvable_contains_sesh = true;
                                    break outer_loop;
                                }
                            }

                        }

                    }
                }
            }

            // replace current (if we found a better score)
            if (improvable_contains_sesh) {
                current = improvable;
            }

        }

        return current;
    }

}
