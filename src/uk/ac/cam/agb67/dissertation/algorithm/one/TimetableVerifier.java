package uk.ac.cam.agb67.dissertation.algorithm.one;

import uk.ac.cam.agb67.dissertation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TimetableVerifier {

    boolean timetable_is_coherent(Timetable tt, List<Session> sessions) {
        // Session-hours each have one unique slot in the schedule:



        // Hours of the same session run consecutively

        // Sessions at the same time in different rooms don’t share individuals

        return false;
    }

    // Returns true if every session-hour appears only once
    boolean timetable_excludes_duplicates(Timetable tt, List<Session> sessions) {

        // Record how many times each session-hour appears
        int[][] occurrences = new int[sessions.size()][tt.Hours_Per_Day];

        // Iterate through all timetable slots
        for (int d = 0; d < tt.Total_Days; d++) {
            for (int h = 0; h < tt.Hours_Per_Day; h++) {
                for (int r = 0; r < tt.Total_Rooms; r++) {

                    // When we find a session-hour, add one to it's count
                    if (tt.get_id(d,h,r) != 0) {
                        occurrences[tt.get_id(d,h,r)][tt.get_hour(d,h,r)] += 1;
                    }

                }
            }
        }

        // Return false if any session-hour appears more than once
        boolean ret = true;
        for (int[] row : occurrences) {
            for (int x : row) {
                if(x>1) {ret=false; break;}
            }
        }
        return ret;
    }

    // Returns true if the timetable includes all sessions
    boolean timetable_is_comprehensive(Timetable tt, List<Session> sessions) {

        // Create a map from sessions to bools, indicating whether they have appeared yet (all starting at false)
        Map<Integer, Boolean> appears = new HashMap<>();
        for (Session ses : sessions) {
            appears.put(ses.Session_ID, false);
        }

        // Iterate through all timetable slots
        for (int d = 0; d < tt.Total_Days; d++) {
            for (int h = 0; h < tt.Hours_Per_Day; h++) {
                for (int r = 0; r < tt.Total_Rooms; r++) {

                    // When we find a session ID, update the appears map to say we have seen that session
                    if (tt.get_id(d,h,r) != 0) {
                        appears.put(tt.get_id(d,h,r), true);
                    }
                }
            }
        }

        // Return false if any session was not seen
        boolean ret = true;
        for (Session ses : sessions) {
            if(!appears.get(ses.Session_ID)) {ret=false; break;}
        }
        return ret;
    }

}
