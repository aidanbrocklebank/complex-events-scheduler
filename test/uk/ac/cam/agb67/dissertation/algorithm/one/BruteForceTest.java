package uk.ac.cam.agb67.dissertation.algorithm.one;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import uk.ac.cam.agb67.dissertation.*;

import java.util.*;

import static com.google.common.truth.Truth.assertThat;

@RunWith(JUnit4.class)
public class BruteForceTest {

    @Test
    public void object_can_be_created(){
        // ARRANGE
        BruteForce bf;
        SchedulingProblem det = MainTest.test_details_A();
        // ACT
        bf = new BruteForce(det.Maximum_Days, det.Hours_Per_Day, det.Maximum_Rooms, det.KeyInd_Details, det.Room_Occupancy_Limits, det.Session_Details);
        // ASSERT
        System.out.println(bf.hashCode());
        assertThat(true);
    }

    @Test
    public void insert_sessions_base_case(){
        // ARRANGE
        SchedulingProblem details = MainTest.test_details_A();
        BruteForce bf = new BruteForce(details.Maximum_Days, details.Hours_Per_Day, details.Maximum_Rooms, details.KeyInd_Details, details.Room_Occupancy_Limits,
                details.Session_Details);
        Timetable tt = new Timetable(details.Maximum_Days, details.Hours_Per_Day, details.Maximum_Rooms);



        // ACT
        tt.set(2, 2, 2, 1, 0);
        tt = bf.insert_sessions(tt, Collections.emptyList());

        // ASSERT
        assertThat(tt.get_id(2,2,2)).isEqualTo(1);
        assertThat(tt.get_hour(2,2,2)).isEqualTo(0);
    }

    @Test
    public void insert_sessions_step_case(){
        // ARRANGE
        SchedulingProblem details = MainTest.test_details_A();
        Timetable tt = MainTest.test_timetable_A();
        BruteForce bf = new BruteForce(details.Maximum_Days, details.Hours_Per_Day, details.Maximum_Rooms, details.KeyInd_Details, details.Room_Occupancy_Limits,
                details.Session_Details);

        // ACT
        tt = bf.insert_sessions(tt, Arrays.asList(details.Session_Details.get(3)));

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

    @Test
    public void copy_session_list_works(){
        // ARRANGE
        SchedulingProblem det = MainTest.test_details_A();
        BruteForce bf = new BruteForce(det.Maximum_Days, det.Hours_Per_Day, det.Maximum_Rooms, det.KeyInd_Details, det.Room_Occupancy_Limits, det.Session_Details);

        // ACT
        List<Session> ls = bf.copy_session_list(det.Session_Details);

        // ASSERT
        assertThat(ls == det.Session_Details).isNotEqualTo(true);
        assertThat(ls.hashCode()).isEqualTo(det.Session_Details.hashCode());
        assertThat(ls.size()).isEqualTo(det.Session_Details.size());
        assertThat(ls.get(0)).isEqualTo(det.Session_Details.get(0));
        assertThat(ls.get(3)).isEqualTo(det.Session_Details.get(3));
        assertThat(ls.get(5)).isEqualTo(det.Session_Details.get(5));
    }

    @Test
    public void union_room_preferences_works(){
        // ARRANGE
        Timetable tt = MainTest.test_timetable_A();
        SchedulingProblem det = MainTest.test_details_A();
        BruteForce bf = new BruteForce(det.Maximum_Days, det.Hours_Per_Day, det.Maximum_Rooms, det.KeyInd_Details, det.Room_Occupancy_Limits, det.Session_Details);

        // ACT
        List<Integer> union = new ArrayList<>(Arrays.asList(1, 2, 7, 9));
        bf.union_room_preferences(3, union);

        // ASSERT
        Set<Integer> set = Set.copyOf(union);
        assertThat(union.size()).isEqualTo(5);
        assertThat(set.hashCode()).isEqualTo(Set.copyOf(Arrays.asList(1,2,3,7,9)).hashCode());
    }

}
