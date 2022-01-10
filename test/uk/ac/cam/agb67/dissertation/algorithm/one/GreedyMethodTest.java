package uk.ac.cam.agb67.dissertation.algorithm.one;

import org.junit.Test;
import uk.ac.cam.agb67.dissertation.MainTest;
import uk.ac.cam.agb67.dissertation.SchedulingProblem;
import uk.ac.cam.agb67.dissertation.Timetable;

import java.util.Arrays;

import static com.google.common.truth.Truth.assertThat;

public class GreedyMethodTest {

    // TODO create tests

    @Test
    public void object_can_be_created() {
        // ARRANGE
        GreedyMethod gm;
        SchedulingProblem det = MainTest.test_details_A();
        // ACT
        gm = new GreedyMethod(det.Maximum_Days, det.Hours_Per_Day, det.Maximum_Rooms, det.KeyInd_Details, det.Room_Occupancy_Limits, det.Session_Details);
        // ASSERT
        System.out.println(gm.hashCode());
        assertThat(true);
    }

    @Test
    public void insert_sessions_places_new_session() {
        // ARRANGE
        SchedulingProblem details = MainTest.test_details_A();
        Timetable tt = MainTest.test_timetable_A();
        GreedyMethod gm = new GreedyMethod(details.Maximum_Days, details.Hours_Per_Day, details.Maximum_Rooms, details.KeyInd_Details, details.Room_Occupancy_Limits,
                details.Session_Details);

        // ACT
        tt = gm.insert_sessions(tt, Arrays.asList(details.Session_Details.get(3)));

        // ASSERT
        boolean found = false;
        for (int day = 0; day < details.Maximum_Days; day++) {
            for (int hour = 0; hour < details.Hours_Per_Day; hour++) {
                for (int room = 0; room < details.Maximum_Rooms; room++) {
                    if(tt.get_id(day, hour, room) == 3) found = true;
                }
            }
        }
        assertThat(found).isEqualTo(true);
    }

}
