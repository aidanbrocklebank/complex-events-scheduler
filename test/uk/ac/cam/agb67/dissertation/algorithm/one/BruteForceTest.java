package uk.ac.cam.agb67.dissertation.algorithm.one;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import uk.ac.cam.agb67.dissertation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;

@RunWith(JUnit4.class)
public class BruteForceTest {

    // TODO create tests

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


        // ACT


        // ASSERT
        assertThat(1).isEqualTo(0);
    }

    @Test
    public void insert_sessions_step_case(){
        // ARRANGE


        // ACT


        // ASSERT
        assertThat(1).isEqualTo(0);
    }

    @Test
    public void copy_session_list_works(){
        // ARRANGE


        // ACT


        // ASSERT
        assertThat(1).isEqualTo(0);
    }

    @Test
    public void union_room_preferences_basic_examples(){
        // ARRANGE


        // ACT


        // ASSERT
        assertThat(1).isEqualTo(0);
    }

    @Test
    public void check_session_doesnt_clash_catches_A(){
        // ARRANGE


        // ACT


        // ASSERT
        assertThat(1).isEqualTo(0);
    }

    @Test
    public void check_session_doesnt_clash_catches_B(){
        // ARRANGE


        // ACT


        // ASSERT
        assertThat(1).isEqualTo(0);
    }

}
