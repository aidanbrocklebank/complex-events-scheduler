package uk.ac.cam.agb67.dissertation;

import java.util.List;

public class KeyIndividual {

    public int ID;

    // Key Individual Preferences
    public String KeyInd_Name;
    public int KeyInd_Daily_Limit_Pref;
    public List<Integer> KeyInd_Room_Prefs;

    public KeyIndividual(String n, int dlp, List<Integer> rp) {
        KeyInd_Name = n;
        KeyInd_Daily_Limit_Pref = dlp;
        KeyInd_Room_Prefs = rp;
    }

}
