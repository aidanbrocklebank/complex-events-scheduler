package uk.ac.cam.agb67.dissertation.algorithm.one;

import uk.ac.cam.agb67.dissertation.KeyIndividual;
import uk.ac.cam.agb67.dissertation.Session;
import uk.ac.cam.agb67.dissertation.Timetable;

import java.util.List;

public class GreedyMethod {

    private int MaxDays, HoursPerDay, MaxRooms;
    private List<KeyIndividual> KeyIndividuals;
    private List<Integer> RoomOccupancyLimits;
    private List<Session> Sessions;

    GreedyMethod(int md, int hpd, int mr, List<KeyIndividual> keys, List<Integer> rol, List<Session> ses) {
        MaxDays = md;
        HoursPerDay = hpd;
        MaxRooms = mr;
        KeyIndividuals = keys;
        RoomOccupancyLimits = rol;
        Sessions = ses;
    }

    // Inserts a single session into the timetable in an acceptable slot, then recursively calls itself
    // ALTERNATIVELY: Loops through the list of sessions adding them each in a slot which looks acceptable
    Timetable insert_sessions(Timetable CurrentMapping, List<Session> CurrentSessions) {

        // TODO implement this

        return null;
    }

}
