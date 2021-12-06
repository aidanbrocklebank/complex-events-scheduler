package uk.ac.cam.agb67.dissertation;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import uk.ac.cam.agb67.dissertation.*;

import java.security.Key;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RunWith(JUnit4.class)
public class MainTest {

    public static SchedulingProblem test_details_A() {
        SchedulingProblem details = new SchedulingProblem();

        // Create fake details for testing

        details.Maximum_Days = 14;
        details.Hours_Per_Day = 8;
        details.Maximum_Rooms = 10;

        details.Room_Occupancy_Limits = Arrays.asList(5, 5, 30, 30, 100, 100, 100, 50, 20, 20);

        details.Reduce_Overlap_Pref = false;
        details.Minimum_Gap_Pref = 0;

        List<Session> ls = new ArrayList<>();
        ls.add(new Session(0, "Session A", 1, Arrays.asList(3)));
        ls.add(new Session(1, "Session B", 1, Arrays.asList(1, 3)));
        ls.add(new Session(2, "Session C", 1, Arrays.asList(2)));
        ls.add(new Session(3, "Session D", 1, Arrays.asList(2, 4)));
        ls.add(new Session(4, "Session E", 1, Arrays.asList(1, 4)));
        ls.add(new Session(5, "Session F", 1, Arrays.asList(1, 4)));
        details.Session_Details = ls;

        List<KeyIndividual> keyls = new ArrayList<>();
        keyls.add(new KeyIndividual("Person A", 2, Arrays.asList(0, 1, 9)));
        keyls.add(new KeyIndividual("Person B", 1, Arrays.asList(2, 3)));
        keyls.add(new KeyIndividual("Person C", 2, Arrays.asList(4, 5, 6)));
        keyls.add(new KeyIndividual("Person D", 3, Arrays.asList(2, 3, 7)));
        keyls.add(new KeyIndividual("Person E", 1, Arrays.asList(0, 1)));
        keyls.add(new KeyIndividual("Person F", 1, Arrays.asList(1)));
        details.KeyInd_Details = keyls;

        List<PredeterminedSession> pdsls = new ArrayList<>();
        pdsls.add(new PredeterminedSession(0, "Session A*", 2, Arrays.asList(3), 0, 0, 0));
        details.PDS_Details = pdsls;

        return details;
    }

}
