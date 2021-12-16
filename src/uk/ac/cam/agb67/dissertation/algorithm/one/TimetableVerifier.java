package uk.ac.cam.agb67.dissertation.algorithm.one;

import uk.ac.cam.agb67.dissertation.*;

import java.util.*;

public class TimetableVerifier {

    public boolean timetable_is_coherent(Timetable tt, List<Session> sessions) {
        boolean coherent = true;

        // Timetable exists
        if (tt == null) {
            if (Main.DEBUG) System.err.println("Timetable variable was null.\n");
            coherent = false;
        }

        // Session-hours each have one unique slot in the schedule:
        if (!timetable_excludes_duplicates(tt, sessions)) {
            if (Main.DEBUG) System.err.println("Timetable included duplicates.\n");
            coherent = false;
        }

        // Every session is included:
        if (!timetable_is_comprehensive(tt, sessions)) {
            if (Main.DEBUG) System.err.println("Timetable was not comprehensive.\n");
            coherent = false;
        }

        // Hours of the same session run consecutively:
        if (!timetabled_sessions_are_contiguous(tt, sessions)) {
            if (Main.DEBUG) System.err.println("Timetable included sessions which were not contiguous.\n");
            coherent = false;
        }

        // Sessions at the same time in different rooms don’t share individuals
        if (!timetabled_individuals_dont_clash(tt, sessions)) {
            if (Main.DEBUG) System.err.println("Timetable had parallel sessions sharing individuals.\n");
            coherent = false;
        }

        return coherent;
    }

    // Returns true if every session-hour appears only once
    boolean timetable_excludes_duplicates(Timetable tt, List<Session> sessions) {

        // Record how many times each session-hour appears
        int fee = tt.hashCode();
        int[][] occurrences = new int[sessions.size()][tt.Hours_Per_Day];

        // Iterate through all timetable slots
        for (int d = 0; d < tt.Total_Days; d++) {
            for (int h = 0; h < tt.Hours_Per_Day; h++) {
                for (int r = 0; r < tt.Total_Rooms; r++) {

                    // When we find a session-hour, add one to it's count
                    if (tt.get_id(d,h,r) != -1) {
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
                    if (tt.get_id(d,h,r) != -1) {
                        appears.put(tt.get_id(d,h,r), true);
                    }
                }
            }
        }

        // Return false if any session was not seen
        boolean ret = true;
        for (Session ses : sessions) {
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

                    // When we find a session ID ...
                    int sid = tt.get_id(d,h,r);
                    if (sid != -1) {

                        // Find the session details for this sid
                        Session sesh = sessions.get(sid);
                        if (sesh.Session_ID != sid) {System.err.println("Session_ID of the session did not match the index of the Session List. Results invalid."); }
                        if (sesh.Session_Length + h > tt.Hours_Per_Day) {System.err.println("Session too long, exceeds daily hours. Results invalid."); }

                        // Iterate through the following time slots
                        for (int inner = 0; inner < sesh.Session_Length; inner++) {
                            // If the following timeslots don't have the same sid and the consecutive hours, return false
                            if (tt.get_id(d,h+inner, r) != sid || tt.get_hour(d, h+inner, r) != inner ) {
                                System.err.println("Day: "+d+", Time: "+(h+inner)+" (offset: "+inner+"), Room: "+r+", session: "+sid+" - Flagged as not contiguous.");
                                return false;
                            }
                        }
                        // Forcibly iterate h to skip over the rest of this session's hours
                        h = h + sesh.Session_Length - 1;

                    }
                }
            }
        }

        // If we never found any violations of contiguity then return true
        return true;
    }

    // Returns true if no individuals are booked into two sessions at once
    boolean timetabled_individuals_dont_clash(Timetable tt, List<Session> sessions) {

        // Make a set of all the keyIndividuals present in the session list
        Set<Integer> All_KeyInds = new HashSet<>();
        for (Session s : sessions) {
            if (s.Session_ID == -1) continue;
            All_KeyInds.addAll(s.Session_KeyInds);
        }
        int maxKeyInd = 0;
        for (Integer i : All_KeyInds) {
            maxKeyInd = Math.max(i, maxKeyInd);
        }

        // Record how many rooms each key individual is in at once
        int[][][] occurrences = new int[tt.Total_Days][tt.Hours_Per_Day][maxKeyInd+1];

        // Iterate through all timetable slots
        for (int d = 0; d < tt.Total_Days; d++) {
            for (int h = 0; h < tt.Hours_Per_Day; h++) {
                for (int r = 0; r < tt.Total_Rooms; r++) {

                    // When we find a session ID ...
                    int sid = tt.get_id(d,h,r);
                    if (sid != -1) {

                        // Find the session details for this sid
                        Session sesh = sessions.get(sid);
                        List<Integer> KeyInds = sesh.Session_KeyInds;

                        // Increment occurrences (for this day/time) for every key individual included
                        for (Integer ki : KeyInds) {
                            occurrences[d][h][ki] += 1;
                        }
                    }

                }
            }
        }

        // Return false if any key individual has more than one room, at the same day/time
        boolean ret = true;
        for (int[][] slice : occurrences) {
            for (int[] row : slice) {
                for (int x : row) {
                    if (x > 1) {ret = false; break; }
                }
            }
        }
        return ret;
    }

    // Returns true if every session is scheduled in a room with enough capacity for it's key individuals
    public boolean sessions_are_scheduled_in_large_enough_rooms(Timetable tt, List<Session> sessions, List<Integer> capacities) {

        // Iterate through all timetable slots
        for (int d = 0; d < tt.Total_Days; d++) {
            for (int r = 0; r < tt.Total_Rooms; r++) {
                for (int h = 0; h < tt.Hours_Per_Day; h++) {

                    // When we find a session, check it's room and it's capacity
                    if (tt.get_id(d,h,r) != -1) {
                        if (sessions.get(tt.get_id(d,h,r)).Session_KeyInds.size() > capacities.get(r)) {
                            System.err.println("Timetable included sessions scheduled in rooms which did not have capacity for the participants.");
                            return false;
                        }

                        // Forcibly iterate h to skip over the rest of this session's hours
                        h = h + sessions.get(tt.get_id(d,h,r)).Session_Length - 1;
                    }

                }
            }
        }

        return true;
    }
}
