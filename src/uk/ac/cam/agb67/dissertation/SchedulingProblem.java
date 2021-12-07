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
                System.err.println("A Session was added to a List<Session> with an ID which did not match it's list index.");
            }
        }

        // TODO Check that all mentioned Key Individuals exist

        // TODO Check that all preferred rooms exist


        return valid;
    }

}
