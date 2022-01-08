package uk.ac.cam.agb67.dissertation.algorithm.one;

import uk.ac.cam.agb67.dissertation.*;

import java.security.Key;
import java.util.ArrayList;
import java.util.List;

public class BruteForce {

    private int MaxDays, HoursPerDay, MaxRooms;
    private List<KeyIndividual> KeyIndividuals;
    private List<Integer> RoomOccupancyLimits;
    private List<Session> Sessions;

    BruteForce(int md, int hpd, int mr, List<KeyIndividual> keys, List<Integer> rol, List<Session> ses) {
        MaxDays = md;
        HoursPerDay = hpd;
        MaxRooms = mr;
        KeyIndividuals = keys;
        RoomOccupancyLimits = rol;
        Sessions = ses;
    }

    // Inserts a single session into the timetable in a place which looks acceptable, then recursively calls itself to add the rest
    // If inserting the session did not result in recursively generating a full schedule, it tries other slots until it runs out
    Timetable insert_sessions(Timetable CurrentMapping, List<Session> CurrentSessions) {

        // We have already succeeded if the list of sessions to add is empty
        if (CurrentSessions.isEmpty()) {
            return CurrentMapping;
        }

        // Retrieve the session from the top of the list, then copy the list (but remove that session)
        Session sesh = CurrentSessions.get(0);
        List<Session> RemainingSessions = copy_session_list(CurrentSessions);
        RemainingSessions.remove(0);

        // Check if this is a pre-determined session
        if (sesh.getClass() == PredeterminedSession.class) {
            // Recursively add the rest of the sessions and if it succeeds, propagate it back up the stack
            Timetable FinalMapping = insert_sessions(CurrentMapping, RemainingSessions);
            if (FinalMapping != null) return FinalMapping;
        }

        // Retrieve the details for clean use
        int sid = sesh.Session_ID;
        int len = sesh.Session_Length;
        List<Integer> KeyIDs = sesh.Session_KeyInds;

        // Determine the full set of preferred rooms for this session
        List<Integer> PreferredRooms = new ArrayList<>();
        for (int k : KeyIDs) {
            //if (Main.DEBUG) System.out.println("Unioning room preferences for particpant "+k+", with existing preferred rooms: "+PreferredRooms.toString());
            union_room_preferences(k, PreferredRooms);
        }

        // Fill a list with all the room IDs, then remove those in the preferred set
        List<Integer> RemainingRoomIDs = new ArrayList<>();
        for (int i=0; i<MaxRooms; i++) {
            RemainingRoomIDs.add(i);
        }
        RemainingRoomIDs.removeAll(PreferredRooms);

        //if (Main.DEBUG) System.out.println("PreferredRooms: "+ PreferredRooms.toString());
        //if (Main.DEBUG) System.out.println("RemainingRoomIDs: "+ RemainingRoomIDs.toString());

        // Iterate through the hours available for the whole event
        for (int day = 0; day < MaxDays; day++) {
            for (int hour = 0; hour < HoursPerDay; hour++) {
                if (hour + len > HoursPerDay) continue;

                // Iterate through the rooms available this hour, starting with preferred rooms
                for (int room : PreferredRooms) {

                    // Check for participants having bookings in other rooms at that time, and that the room is empty at that time
                    boolean SessionDoesntClash = Coordinator.check_session_doesnt_clash(CurrentMapping, day, hour, room, sesh, Sessions);
                    if (SessionDoesntClash && (RoomOccupancyLimits.get(room) >= sesh.Session_KeyInds.size())) {

                        if (Main.DEBUG) System.out.println("Adding session "+ sid +", at day:"+day+" time:"+hour+" room:"+room+".");

                        // Insert this session at this point in the current schedule, as a new schedule
                        Timetable NewMapping = CurrentMapping.deep_copy();
                        NewMapping.set(day, hour, room, sesh);

                        // Recursively add the rest of the sessions
                        Timetable FinalMapping = insert_sessions(NewMapping, RemainingSessions);

                        // If that didn't fail, propagate it back up the stack
                        if (FinalMapping != null) return FinalMapping;
                    }
                }

                // Iterate through the rest of the rooms available this hour
                for (int room : RemainingRoomIDs) {

                    // Check for participants having bookings in other rooms at that time, and that the room is empty at that time
                    boolean SessionDoesntClash = Coordinator.check_session_doesnt_clash(CurrentMapping, day, hour, room, sesh, Sessions);
                    if (SessionDoesntClash && (RoomOccupancyLimits.get(room) >= sesh.Session_KeyInds.size())) {

                        if (Main.DEBUG) System.out.println("Adding session "+ sid +", at day:"+day+" time:"+hour+" room:"+room+".\n");

                        // Insert this session at this point in the current schedule, as a new schedule
                        Timetable NewMapping = CurrentMapping.deep_copy();
                        NewMapping.set(day, hour, room, sesh);

                        // Recursively add the rest of the sessions
                        Timetable FinalMapping = insert_sessions(NewMapping, RemainingSessions);

                        // If that didn't fail, propagate it back up the stack
                        if (FinalMapping != null) return FinalMapping;
                    }
                }

            }
        }

        // If there is no way for us to insert the current session, this recursive call has failed
        System.err.println("No way to schedule session.");
        return null;
    }

    // Returns a deep copy of a list of sessions
    List<Session> copy_session_list(List<Session> Base) {
        List<Session> Copy = new ArrayList<>();
        for (Session s : Base) {
            Copy.add(s);
        }
        return Copy;
    }

    // Takes a list of room ids and a key individual id, and returns that list with all of the key individual's preferred rooms included
    void union_room_preferences(int KeyID, List<Integer> ExistingPrefs) {
        for (int rid : KeyIndividuals.get(KeyID).KeyInd_Room_Prefs) {
            if (!ExistingPrefs.contains(rid)) ExistingPrefs.add(rid);
        }
    }

}
