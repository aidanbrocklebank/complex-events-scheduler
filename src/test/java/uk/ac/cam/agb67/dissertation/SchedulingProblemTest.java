package uk.ac.cam.agb67.dissertation;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Arrays;

@RunWith(JUnit4.class)
public class SchedulingProblemTest {

    @Test
    public void testObject_can_be_created(){
        // ARRANGE
        SchedulingProblem details;
        // ACT
        details = new SchedulingProblem();
        // ASSERT
        System.out.println(details.hashCode());
        assertThat(true);
    }

    @Test
    public void check_validity_allows_valid_details() {
        // ARRANGE
        SchedulingProblem details = MainTest.test_details_A();

        // ACT
        boolean valid = details.potentially_schedulable();
        System.out.println(details.toString());

        // ASSERT
        assertThat(valid).isEqualTo(true);
    }

    @Test
    public void check_validity_catches_session_id_mismatch() {
        // ARRANGE
        SchedulingProblem details = MainTest.test_details_A();

        // ACT
        details.Session_Details.get(3).Session_ID = 1;
        boolean valid = details.potentially_schedulable();

        // ASSERT
        assertThat(valid).isEqualTo(false);
    }

    @Test
    public void check_validity_catches_PDS_missing_from_list() {
        // ARRANGE
        SchedulingProblem details = MainTest.test_details_A();

        // ACT
        details.PDS_Details.add(new PredeterminedSession(7, "Session B*", 2, Arrays.asList(3, 1), 1, 0, 0));
        boolean valid = details.potentially_schedulable();

        // ASSERT
        assertThat(valid).isEqualTo(false);
    }

    @Test
    public void check_validity_catches_erroneous_key_ids() {
        // ARRANGE
        SchedulingProblem details = MainTest.test_details_A();

        // ACT
        details.Session_Details.get(0).Session_KeyInds.set(0, 25);
        boolean valid = details.potentially_schedulable();

        // ASSERT
        assertThat(valid).isEqualTo(false);
    }

    @Test
    public void check_validity_catches_erroneous_room_ids() {
        // ARRANGE
        SchedulingProblem details = MainTest.test_details_A();

        // ACT
        details.KeyInd_Details.get(0).KeyInd_Room_Prefs.set(0, 25);
        boolean valid = details.potentially_schedulable();

        // ASSERT
        assertThat(valid).isEqualTo(false);
    }

    @Test
    public void check_validity_catches_overlong_sessions() {
        // ARRANGE
        SchedulingProblem details = MainTest.test_details_A();

        // ACT
        details.Session_Details.get(0).Session_Length = 12;
        boolean valid = details.potentially_schedulable();

        // ASSERT
        assertThat(valid).isEqualTo(false);
    }

    @Test
    public void check_validity_catches_overfull_sessions() {
        // ARRANGE
        SchedulingProblem details = MainTest.test_details_A();

        // ACT
        details.Room_Occupancy_Limits = Arrays.asList(3, 3, 3, 3);
        details.Session_Details.get(1).Session_KeyInds = Arrays.asList(1, 2, 3, 4);
        boolean valid = details.potentially_schedulable();

        // ASSERT
        assertThat(valid).isEqualTo(false);
    }

}
