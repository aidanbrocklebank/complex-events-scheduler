package uk.ac.cam.agb67.dissertation;

import java.util.List;

public class Session {

    public int Session_ID;

    // Individual Session Details
    public String Session_Name;
    public int Session_Length;
    public List<Integer> Session_KeyInds;

    public Session(int id) {
        Session_ID = id;
    }
    public Session(int id, int len) {
        Session_ID = id;
        Session_Length = len;
    }
    public Session(int id, String name, int len, List<Integer> kids) {
        Session_ID = id;
        Session_Name = name;
        Session_Length = len;
        Session_KeyInds = kids;
    }

}
