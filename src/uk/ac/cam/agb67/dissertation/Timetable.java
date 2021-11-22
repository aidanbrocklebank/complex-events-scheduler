package uk.ac.cam.agb67.dissertation;

public class Timetable {

    // The parameters of the timetable, given by event-wide constraints
    public int Total_Days;
    public int Hours_Per_Day;
    public int Total_Rooms;

    // The 3-d array which tells us which hours of which sessions happen at which day/time/room combinations.
    int[][][] session_id_map;
    int[][][] session_hour_map;

    // Create a new empty timetable from parameters
    public Timetable(int td, int hpd, int tr) {
        Total_Days = td;
        Hours_Per_Day = hpd;
        Total_Rooms = tr;
        session_id_map = new int[Total_Days][Hours_Per_Day][Total_Rooms];
        session_hour_map = new int[Total_Days][Hours_Per_Day][Total_Rooms];
    }

    // Place a full session into the timetable
    public void set(int day, int time, int room, Session session) {
        for (int t = 0; t < session.Session_Length; t++) {
            session_id_map[day][time][room] = session.Session_ID;
            session_hour_map[day][time][room] = t;
        }
    }
    // Place a single session-hour into the timetable
    public void set(int day, int time, int room, int sid, int hour) {
        session_id_map[day][time][room] = sid;
        session_hour_map[day][time][room] = hour;
    }

    // Getters
    public int get_id(int day, int time, int room) {
        return session_id_map[day][time][room];
    }
    public int get_hour(int day, int time, int room) {
        return session_hour_map[day][time][room];
    }

}
