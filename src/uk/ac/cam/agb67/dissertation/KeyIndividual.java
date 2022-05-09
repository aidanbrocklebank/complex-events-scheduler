package uk.ac.cam.agb67.dissertation;

import java.util.List;

public class KeyIndividual {

    // Key Individual Preferences
    public String KeyInd_Name;
    public int KeyInd_Daily_Limit_Pref;
    public List<Integer> KeyInd_Room_Prefs;

    public KeyIndividual(String n, int dlp, List<Integer> rp) {
        KeyInd_Name = n;
        KeyInd_Daily_Limit_Pref = dlp;
        KeyInd_Room_Prefs = rp;
    }

    @Override
    public String toString() {
        return "<Individual: Name:'"+KeyInd_Name+"', Daily Limit:"+KeyInd_Daily_Limit_Pref+", Preferred Rooms:"+KeyInd_Room_Prefs.toString()+">";
    }

}
