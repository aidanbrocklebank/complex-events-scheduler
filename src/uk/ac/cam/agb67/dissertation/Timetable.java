package uk.ac.cam.agb67.dissertation;

public class Timetable {

    // The parameters of the timetable, given by event-wide constraints
    public int Total_Days;
    public int Hours_Per_Day;
    public int Total_Rooms;

    // The inner SessionHour class which acts as a pair of SessionID and hour of the session
    public static class SessionHour {
        public int sessionID;
        public int hour;

        public SessionHour(Session s, int h) {
            sessionID = s.ID; hour = h;
        }
        public SessionHour(int sid, int h) {
            sessionID = sid; hour = h;
        }
    }

    // The 3-d array which tells us which hours of which sessions happen at which day/time/room combinations.
    public SessionHour[][][] Map;

    // Create a new empty timetable from parameters
    public Timetable(int td, int hpd, int tr) {
        Total_Days = td;
        Hours_Per_Day = hpd;
        Total_Rooms = tr;
        Map = new SessionHour[Total_Days][Hours_Per_Day][Total_Rooms];
    }


}
