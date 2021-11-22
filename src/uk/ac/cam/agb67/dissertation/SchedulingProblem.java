package uk.ac.cam.agb67.dissertation;

import java.util.List;

public class SchedulingProblem {

    // Event-wide Constraints
    int Maximum_Rooms;
    int Maximum_Hours;
    int Hours_Per_Day;

    // Individual Session Details
    List<Session> Session_Details;

    // Predetermined Session (PDS) Details
    List<Session_Predetermined> PDS_Details;

    // Key Individuals and their Preferences
    List<KeyIndividuals> KeyInd_Details;


}
