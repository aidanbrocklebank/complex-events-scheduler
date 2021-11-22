package uk.ac.cam.agb67.dissertation.algorithm.one;

import uk.ac.cam.agb67.dissertation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TimetableVerifier {

    boolean timetable_is_coherent(Timetable tt) {
        // Session-hours each have one unique slot in the schedule:

        // Hours of the same session run consecutively

        // Sessions at the same time in different rooms don’t share individuals

        return false;
    }

    // Returns true if the timetable includes all sessions
    boolean timetable_is_comprehensive(Timetable tt, List<Session> sessions) {

        // Create a map from sessions to bools, indicating whether they have appeared yet (all starting at false)
        Map<Integer, Boolean> appears = new HashMap<>();
        for (Session ses : sessions) {
            appears.put(ses.ID, false);
        }

        // Iterate through all timetable slots
        for (int d = 0; d < tt.Total_Days; d++) {
            for (int h = 0; h < tt.Hours_Per_Day; h++) {
                for (int r = 0; r < tt.Total_Rooms; r++) {

                    // When we find a session ID, update the appears map to say we have seen that session
                    if (tt.Map[d][h][r].sessionID != 0) {
                        appears.put(tt.Map[d][h][r].sessionID, true);
                    }
                }
            }
        }

        // Return false if any session was not seen
        Boolean ret = true;
        for (Session ses : sessions) {
            ret = ret && appears.get(ses.ID);
        }

        return false;
    }

}
