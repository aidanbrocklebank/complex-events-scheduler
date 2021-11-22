package uk.ac.cam.agb67.dissertation;

import java.util.List;

public class Session_Predetermined extends Session {

    // Additional details for a Predetermined Session (PDS)
    int PDS_Start_Time;
    int PDS_Day;
    int PDS_Room;


    public Session_Predetermined(int id) {
        super(id);
    }
    public Session_Predetermined(int id, String sn, int sl, List<Integer> skid) {
        super(id, sn, sl, skid);
    }

}
