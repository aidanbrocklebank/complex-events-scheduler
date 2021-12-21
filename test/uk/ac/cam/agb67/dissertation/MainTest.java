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

        details.Maximum_Days = 5;
        details.Hours_Per_Day = 8;
        details.Maximum_Rooms = 4;

        details.Room_Occupancy_Limits = Arrays.asList(5, 5, 30, 30);

        details.Reduce_Overlap_Pref = false;
        details.Minimum_Gap_Pref = 0;

        List<Session> ls = new ArrayList<>();
        ls.add(new Session(0, "Session A", 1, Arrays.asList(3)));
        ls.add(new Session(1, "Session B", 1, Arrays.asList(1, 3)));
        ls.add(new Session(2, "Session C", 1, Arrays.asList(2)));
        ls.add(new Session(3, "Session D", 1, Arrays.asList(2, 4)));
        ls.add(new Session(4, "Session E", 1, Arrays.asList(1, 4)));
        ls.add(new Session(5, "Session F", 1, Arrays.asList(1, 4)));

        List<KeyIndividual> keyls = new ArrayList<>();
        keyls.add(new KeyIndividual("Person A", 2, Arrays.asList(0, 1)));
        keyls.add(new KeyIndividual("Person B", 1, Arrays.asList(2, 3)));
        keyls.add(new KeyIndividual("Person C", 2, Arrays.asList(1, 2)));
        keyls.add(new KeyIndividual("Person D", 3, Arrays.asList(2, 3)));
        keyls.add(new KeyIndividual("Person E", 1, Arrays.asList(0, 1)));
        keyls.add(new KeyIndividual("Person F", 1, Arrays.asList(1)));
        details.KeyInd_Details = keyls;

        List<PredeterminedSession> pdsls = new ArrayList<>();
        pdsls.add(new PredeterminedSession(6, "Session A*", 2, Arrays.asList(3), 0, 0, 0));

        ls.add(pdsls.get(0));
        details.Session_Details = ls;
        details.PDS_Details = pdsls;

        return details;
    }

    public static Timetable test_timetable_A() {
        List<Session> ls = test_details_A().Session_Details;
        Timetable tt = new Timetable(5,8,4);

        tt.set(0,3,0, ls.get(0));
        tt.set(1,2,1, ls.get(1));
        tt.set(2,6,0, ls.get(2));

        return tt;
    }

    public static Timetable test_timetable_B() {
        Timetable tt = new Timetable(3,8,2);

        tt.set(0,3,0, 4, 0);
        tt.set(0,4,0, 4, 1);

        tt.set(1,2,1, 1, 0);
        tt.set(1,3,1, 1, 1);
        tt.set(1,4,1, 1, 2);

        tt.set(2,6,0, 2, 0);

        return tt;
    }

    public static List<Session> test_session_list_B() {
        List<Session> ls = new ArrayList<>();

        ls.add(new Session(0));
        ls.add(new Session(1, 3));
        ls.add(new Session(2, 1));
        ls.add(new Session(3, 2));
        ls.add(new Session(4, 2));

        return ls;
    }

    public static SchedulingProblem test_details_C() {
        SchedulingProblem details = new SchedulingProblem();

        // Create fake details for testing

        details.Maximum_Days = 5;
        details.Hours_Per_Day = 8;
        details.Maximum_Rooms = 5;

        details.Room_Occupancy_Limits = Arrays.asList(5, 5, 30, 30, 2);

        details.Reduce_Overlap_Pref = false;
        details.Minimum_Gap_Pref = 0;

        List<Session> ls = new ArrayList<>();
        ls.add(new Session(0, "Session A", 2, Arrays.asList(0, 3)));
        ls.add(new Session(1, "Session B", 4, Arrays.asList(1, 3)));
        ls.add(new Session(2, "Session C", 3, Arrays.asList(2)));
        ls.add(new Session(3, "Session D", 1, Arrays.asList(2, 4)));
        ls.add(new Session(4, "Session E", 1, Arrays.asList(1, 3, 7)));
        ls.add(new Session(5, "Session F", 2, Arrays.asList(1, 4)));
        ls.add(new Session(6, "Session G", 5, Arrays.asList(2, 6)));
        ls.add(new Session(7, "Session H", 2, Arrays.asList(3, 4)));
        ls.add(new Session(8, "Session I", 1, Arrays.asList(3, 4, 5)));
        ls.add(new Session(9, "Session J", 6, Arrays.asList(0, 5, 7)));

        List<KeyIndividual> keyls = new ArrayList<>();
        keyls.add(new KeyIndividual("Person A", 2, Arrays.asList(0, 1)));
        keyls.add(new KeyIndividual("Person B", 1, Arrays.asList(3)));
        keyls.add(new KeyIndividual("Person C", 2, Arrays.asList(1, 2, 4)));
        keyls.add(new KeyIndividual("Person D", 3, Arrays.asList(2, 3)));
        keyls.add(new KeyIndividual("Person E", 1, Arrays.asList(0, 2)));
        keyls.add(new KeyIndividual("Person F", 1, Arrays.asList(1, 4)));
        keyls.add(new KeyIndividual("Person G", 1, Arrays.asList(1)));
        keyls.add(new KeyIndividual("Person H", 1, Arrays.asList(4)));
        details.KeyInd_Details = keyls;

        List<PredeterminedSession> pdsls = new ArrayList<>();
        //pdsls.add(new PredeterminedSession(6, "Session A*", 2, Arrays.asList(3), 0, 0, 0));

        //ls.add(pdsls.get(0));
        details.Session_Details = ls;
        details.PDS_Details = pdsls;

        return details;
    }

    public static SchedulingProblem test_details_D() {
        SchedulingProblem details = new SchedulingProblem();

        // Create fake details for testing

        details.Maximum_Days = 2;
        details.Hours_Per_Day = 4;
        details.Maximum_Rooms = 2;

        details.Room_Occupancy_Limits = Arrays.asList(2, 50);

        details.Reduce_Overlap_Pref = false;
        details.Minimum_Gap_Pref = 0;

        List<Session> ls = new ArrayList<>();
        ls.add(new Session(0, "Session A", 1, Arrays.asList(0)));
        ls.add(new Session(1, "Session B", 3, Arrays.asList(0, 2)));
        ls.add(new Session(2, "Session C", 2, Arrays.asList(1, 2)));

        List<KeyIndividual> keyls = new ArrayList<>();
        keyls.add(new KeyIndividual("Person A", 2, Arrays.asList(0)));
        keyls.add(new KeyIndividual("Person B", 2, Arrays.asList(1)));
        keyls.add(new KeyIndividual("Person C", 2, Arrays.asList(0)));
        details.KeyInd_Details = keyls;

        List<PredeterminedSession> pdsls = new ArrayList<>();
        //pdsls.add(new PredeterminedSession(6, "Session A*", 2, Arrays.asList(3), 0, 0, 0));
        //ls.add(pdsls.get(0));

        details.Session_Details = ls;
        details.PDS_Details = pdsls;

        return details;
    }

    @Test
    public void randomizer_sanity_check() {
        Main.randomized_test_details(21, 50, 40, 25);
        for (int i=0; i<100; i++) {
            //Main.randomized_test_details(10, 5, 20, 15);
        }
    }

    // TODO implement real tests for the randomised data

}
