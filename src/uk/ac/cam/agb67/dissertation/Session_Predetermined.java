package uk.ac.cam.agb67.dissertation;

import java.util.List;

public class Session_Predetermined extends Session {

    // Additional details for a Predetermined Session (PDS)
    public int PDS_Day;
    public int PDS_Start_Time;
    public int PDS_Room;

    // Constructors
    public Session_Predetermined(int id) {
        super(id);
    }
    public Session_Predetermined(int id, String sn, int sl, List<Integer> skid) {
        super(id, sn, sl, skid);
    }

}
