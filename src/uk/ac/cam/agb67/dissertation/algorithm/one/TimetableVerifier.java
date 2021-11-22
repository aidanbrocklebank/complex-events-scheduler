package uk.ac.cam.agb67.dissertation.algorithm.one;

import uk.ac.cam.agb67.dissertation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TimetableVerifier {

    boolean timetable_is_coherent(Timetable tt, List<Session> sessions) {
        // Session-hours each have one unique slot in the schedule:
        if (!timetable_excludes_duplicates(tt, sessions)) {
            return false;
        }

        // Every session is included:
        if (!timetable_is_comprehensive(tt, sessions)) {
            return false;
        }

        // Hours of the same session run consecutively:
        if (!timetabled_sessions_are_contiguous(tt, sessions)) {
            return false;
        }

        // Sessions at the same time in different rooms don’t share individuals
        if (!timetable_is_comprehensive(tt, sessions)) {
            return false;
        }

        return true;
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
            // Skip the dummy session
            if(ses.Session_ID == 0) continue;

            if(!appears.get(ses.Session_ID)) {
                ret=false;
                //break;
            }
        }
        return ret;
    }

    // Returns true if the session-hours of one session are consecutive
    boolean timetabled_sessions_are_contiguous(Timetable tt, List<Session> sessions) {

        // Iterate through all timetable slots
        for (int r = 0; r < tt.Total_Rooms; r++) {
            for (int d = 0; d < tt.Total_Days; d++) {
                 for (int h = 0; h < tt.Hours_Per_Day; h++) {

                    // When we find a session ID
                    int sid = tt.get_id(d,h,r);
                    if (sid != 0) {

                        // Find the session details for this sid
                        Session sesh = sessions.get(sid);
                        if (sesh.Session_ID != sid) {System.err.println("Session_ID of the session did not match the index of the Session List. Results invalid."); }
                        if (sesh.Session_Length + h > tt.Hours_Per_Day) {System.err.println("Session too long, exceeds daily hours. Results invalid."); }

                        // Iterate through the following time slots
                        for (int inner = 0; inner < sesh.Session_Length; inner++) {
                            // If the following timeslots don't have the same sid and the consecutive hours, return false
                            if (tt.get_id(d,h+inner, r) != sid || tt.get_hour(d, h+inner, r) != inner ) {
                                return false;
                            }
                        }
                        // Forcibly iterate h
                        h = h + sesh.Session_Length;

                    }
                }
            }
        }

        // If we never found any violations of contiguity then return true
        return true;
    }

}
