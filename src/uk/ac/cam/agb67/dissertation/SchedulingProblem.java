package uk.ac.cam.agb67.dissertation;

import java.util.List;

public class SchedulingProblem {

    // Event-wide Constraints
    public int Maximum_Days;
    public int Hours_Per_Day;
    public int Maximum_Rooms;

    public List<Integer> Room_Occupancy_Limits;

    // Individual Session Details
    // The list of sessions starts with a dummy session, as they are indexed from 1
    public List<Session> Session_Details;

    // Predetermined Session (PDS) Details
    // Note that all PredeterminedSessions in PDS_Details must also be present in Session_Details
    public List<PredeterminedSession> PDS_Details;

    // Key Individuals and their Preferences
    public List<KeyIndividual> KeyInd_Details;

    // Additional Event-wide Preferences
    public boolean Reduce_Overlap_Pref;
    public int Minimum_Gap_Pref;

    // Confirms the validity of the given data as a potentially schedule-able set of details
    public boolean check_validity() {
        boolean valid = true;

        // Check that there are no sessions in the list with id != index
        for (int i=0; i < Session_Details.size(); i++) {
            if (Session_Details.get(i).Session_ID != i) {
                valid = false;
                System.err.println("A Session was added to a List<Session> with an ID which did not match its list index.");
                break;
            }
        }

        // Check that all PreterminedSessions in PDS_Details are also present in Session_Details
        for (PredeterminedSession pds : PDS_Details) {
            boolean found = false;
            for (Session sesh : Session_Details) {
                if (pds.hashCode() == sesh.hashCode()) {found = true; break;}
            }
            if (!found) {
                valid = false;
                System.err.println("A PredeterminedSession was included in PDS_Details but not in Session_Details.");
                break;
            }
        }

        // Check that all mentioned Key Individuals exist
        for (Session sesh : Session_Details) {
            for (int keyID : sesh.Session_KeyInds) {
                if (keyID >= KeyInd_Details.size()) {
                    valid = false;
                    System.err.println("A Session was included with Key Individual IDs which were not indices in KeyInd_Details.");
                    break;
                }
            }
        }

        // Check that all preferred rooms exist
        for (KeyIndividual keyInd : KeyInd_Details) {
            for (int roomID : keyInd.KeyInd_Room_Prefs) {
                if (roomID >= Maximum_Rooms) {
                    valid = false;
                    System.err.println("A Key Individual was included with Preferred Room IDs which were outwith Max_Rooms.");
                    break;
                }
            }
        }

        // Check that all session durations fit into a day
        for (Session sesh : Session_Details) {
            if (sesh.Session_Length > Hours_Per_Day) {
                valid = false;
                System.err.println("A Session was included which is longer than the number of hours in each day.");
                break;
            }
        }

        // Check that for every session there is a room which has the capacity for its participants
        int Max_Capacity = 0;
        for (int cap : Room_Occupancy_Limits) {
            Max_Capacity = Math.max(Max_Capacity, cap);
        }
        for (Session sesh : Session_Details) {
            if (sesh.Session_KeyInds.size() > Max_Capacity) {
                valid = false;
                System.err.println("A Session was included which has more participating individuals then the greatest room capacity.");
                break;
            }
        }

        return valid;
    }

}
