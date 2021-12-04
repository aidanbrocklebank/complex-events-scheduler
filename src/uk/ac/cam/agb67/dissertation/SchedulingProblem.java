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
    public List<Session_Predetermined> PDS_Details;

    // Key Individuals and their Preferences
    public List<KeyIndividual> KeyInd_Details;

    // Additional Event-wide Preferences
    public boolean Reduce_Overlap_Pref;
    public int Minimum_Gap_Pref;

}
