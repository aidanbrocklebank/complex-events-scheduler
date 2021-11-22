package uk.ac.cam.agb67.dissertation;

import java.util.List;

public class SchedulingProblem {

    // Event-wide Constraints
    int Maximum_Days;
    int Hours_Per_Day;
    int Maximum_Rooms;

    // Individual Session Details
    // The list of sessions starts with a dummy session, as they are indexed from 1
    List<Session> Session_Details;

    // Predetermined Session (PDS) Details
    List<Session_Predetermined> PDS_Details;

    // Key Individuals and their Preferences
    List<KeyIndividuals> KeyInd_Details;


}
