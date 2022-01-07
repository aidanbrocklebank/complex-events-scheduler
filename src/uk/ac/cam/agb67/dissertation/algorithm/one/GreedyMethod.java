package uk.ac.cam.agb67.dissertation.algorithm.one;

import uk.ac.cam.agb67.dissertation.*;

import java.util.ArrayList;
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
    Timetable insert_sessions(Timetable CurrentMapping, List<Session> AllSessions) {
        if (Main.DEBUG) System.out.println("Using the greedy variant of the brute force algorithm.");

        sessionLoop:
        for (Session sesh : AllSessions) {

            // Check if this is a pre-determined session
            if (sesh.getClass() == PredeterminedSession.class) {
                // This will have been added to the timetable by the coordinator, so this session is done
                continue;
            }

            // Retrieve the details for clean use
            int sid = sesh.Session_ID;
            int len = sesh.Session_Length;
            List<Integer> KeyIDs = sesh.Session_KeyInds;

            /*
            // Determine the full set of preferred rooms for this session
            List<Integer> PreferredRooms = new ArrayList<>();
            for (int k : KeyIDs) {
                if (Main.DEBUG) System.out.println("Unioning room preferences for particpant "+k+", with existing preferred rooms: "+PreferredRooms.toString());
                union_room_preferences(k, PreferredRooms);
            }

            // Fill a list with all the room IDs, then remove those in the preferred set
            List<Integer> RemainingRoomIDs = new ArrayList<>();
            for (int i=0; i<MaxRooms; i++) {
                RemainingRoomIDs.add(i);
            }
            RemainingRoomIDs.removeAll(PreferredRooms);

             */

            // Iterate through the hours available for the whole event
            for (int day = 0; day < MaxDays; day++) {
                for (int hour = 0; hour < HoursPerDay; hour++) {
                    if (hour + len > HoursPerDay) continue;

                    // Iterate through the rooms available
                    for (int room = 0; room < MaxRooms; room++) {

                        // Check for participants having bookings in other rooms at that time, and that the room is empty at that time
                        boolean SessionDoesntClash = Coordinator.check_session_doesnt_clash(CurrentMapping, day, hour, room, sesh, Sessions);
                        if (SessionDoesntClash && (RoomOccupancyLimits.get(room) >= sesh.Session_KeyInds.size())) {

                            // The session doesn't cause a clash, and the room has the capacity, so we will add it to the timetable
                            CurrentMapping.set(day, hour, room, sesh);

                            // And move on to the next session in the list
                            continue sessionLoop;
                        }

                    }
                }
            }

            // If the program reaches here then we didn't find any slot into which we could schedule the session
            // In this case the greedy variant of the algorithm fails
            return null;

        }

        // The loop has concluded and all sessions have been added to the timetable, without any session failing to find a place
        return CurrentMapping;
    }

}
