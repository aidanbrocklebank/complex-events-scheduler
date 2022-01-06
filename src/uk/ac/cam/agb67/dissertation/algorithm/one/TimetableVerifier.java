package uk.ac.cam.agb67.dissertation.algorithm.one;

import uk.ac.cam.agb67.dissertation.*;

import java.util.*;

public class TimetableVerifier {

    // TODO consider moving TimetableVerifier (which is used across the program) into the main package
    //  and moving the checker-method in BruteForce through to Coordinator (as both methods use it??)

    // Returns true if a timetable is valid, that is if the timetable is coherent, comprehensive and usable
    public boolean timetable_is_valid(Timetable tt, SchedulingProblem details) {
        boolean valid = true;
        List<Session> sessions = details.Session_Details;
        List<Integer> capacities = details.Room_Occupancy_Limits;

        // Timetable exists
        if (tt == null) {
            if (Main.DEBUG) System.err.println("Timetable variable was null.\n");
            return false;
        }

        // (Coherency) Session-hours each have one unique slot in the schedule:
        if (!timetable_excludes_duplicates(tt, sessions)) {
            if (Main.DEBUG) System.err.println("Timetable included duplicates.\n");
            valid = false;
        }

        // (Comprehensiveness) Every session is included:
        if (!timetable_is_comprehensive(tt, sessions)) {
            if (Main.DEBUG) System.err.println("Timetable was not comprehensive.\n");
            valid = false;
        }

        // (Coherency) Hours of the same session run consecutively:
        if (!timetabled_sessions_are_contiguous(tt, sessions)) {
            if (Main.DEBUG) System.err.println("Timetable included sessions which were not contiguous.\n");
            valid = false;
        }

        // (Coherency) Sessions at the same time in different rooms don’t share individuals:
        if (!timetabled_individuals_dont_clash(tt, sessions)) {
            if (Main.DEBUG) System.err.println("Timetable had parallel sessions sharing individuals.\n");
            valid = false;
        }

        // (Usability) Each session occurs in a room with a large enough capacity limit:
        if (!sessions_are_scheduled_in_large_enough_rooms(tt, sessions, capacities)) {
            if (Main.DEBUG) System.err.println("Timetable had sessions booked in rooms without a great enough capacity limit.\n");
            valid = false;
        }

        // (Usability) Every predetermined session is scheduled for the day, time and room which it requires:
        if (!predetermined_sessions_are_scheduled_correctly(tt, details.PDS_Details)) {
            if (Main.DEBUG) System.err.println("Timetable did not include the predtermined sessions in their assigned slots.\n");
            valid = false;
        }

        return valid;
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
        Set<Integer> All_KeyIDs = new HashSet<>();
        for (Session s : sessions) {
            if (s.Session_ID == -1) continue;
            All_KeyIDs.addAll(s.Session_KeyInds);
        }
        int maxKeyInd = 0;
        for (Integer i : All_KeyIDs) {
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
                        List<Integer> Session_KeyIDs = sesh.Session_KeyInds;

                        // Increment occurrences (for this day/time) for every key individual included
                        for (Integer ki : Session_KeyIDs) {
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
    boolean sessions_are_scheduled_in_large_enough_rooms(Timetable tt, List<Session> sessions, List<Integer> capacities) {

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

    // Returns true if every predetermined session is scheduled (to start) in the correct slot
    boolean predetermined_sessions_are_scheduled_correctly(Timetable tt, List<PredeterminedSession> pds_list) {

        // Iterate through all predetermined sessions
        for (PredeterminedSession pds : pds_list) {

            // Find the session which has been scheduled into the slot where this PDS was meant to be
            int scheduled_id = tt.get_id(pds.PDS_Day, pds.PDS_Start_Time, pds.PDS_Room);
            if (scheduled_id != pds.Session_ID) {
                // Return false if we don't find this PDS
                System.err.println("Timetable did not include a predetermined session in the correct assigned slot.");
                return false;
            }
        }

        // If they were all found then return true
        return true;
    }

}
