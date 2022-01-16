package uk.ac.cam.agb67.dissertation.algorithm.two;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import uk.ac.cam.agb67.dissertation.*;
import uk.ac.cam.agb67.dissertation.TimetableVerifier;
import uk.ac.cam.agb67.dissertation.algorithm.one.Coordinator;

import static com.google.common.truth.Truth.assertThat;

@RunWith(JUnit4.class)
public class CoordinatorTwoTest {

    @Test
    public void object_can_be_created() {
        // ARRANGE
        CoordinatorTwo co;
        SchedulingProblem det = MainTest.test_details_A();
        // ACT
        co = new CoordinatorTwo();
        // ASSERT
        System.out.println(co.hashCode());
        assertThat(true);
    }

    @Test
    public void algorithm_generates_coherent_schedule_0() {
        // ARRANGE
        CoordinatorTwo co = new CoordinatorTwo();
        SchedulingProblem details = MainTest.test_details_D();
        TimetableVerifier ttv = new TimetableVerifier();

        // ACT
        Timetable tt = co.generate(details);
        boolean correct = ttv.timetable_is_valid(tt, details);

        // ASSERT
        assertThat(correct).isEqualTo(true);
    }

    @Test
    public void algorithm_generates_coherent_schedule_1() {
        // ARRANGE
        CoordinatorTwo co = new CoordinatorTwo();
        SchedulingProblem details = MainTest.test_details_A();
        TimetableVerifier ttv = new TimetableVerifier();

        // ACT
        Timetable tt = co.generate(details);
        boolean correct = ttv.timetable_is_valid(tt, details);
        //correct = correct & ttv.sessions_are_scheduled_in_large_enough_rooms(tt, details.Session_Details, details.Room_Occupancy_Limits);

        // ASSERT
        assertThat(correct).isEqualTo(true);
    }

    @Test
    public void algorithm_generates_coherent_schedule_2() {
        // ARRANGE
        CoordinatorTwo co = new CoordinatorTwo();
        SchedulingProblem details = MainTest.test_details_C();
        TimetableVerifier ttv = new TimetableVerifier();

        // ACT
        Timetable tt = co.generate(details);
        boolean correct = ttv.timetable_is_valid(tt, details);
        //correct = correct & ttv.sessions_are_scheduled_in_large_enough_rooms(tt, details.Session_Details, details.Room_Occupancy_Limits);

        // ASSERT
        assertThat(correct).isEqualTo(true);

        TimetableSatisfactionMeasurer tsm = new TimetableSatisfactionMeasurer();
        int pref_score = tsm.timetable_preference_satisfaction(tt, details);
        System.out.println("Overall Score: " + pref_score);
    }

    @Test
    public void algorithm_generates_coherent_schedule_edge_case() {
        // ARRANGE
        CoordinatorTwo co = new CoordinatorTwo();
        SchedulingProblem details = MainTest.test_details_E();
        TimetableVerifier ttv = new TimetableVerifier();

        // ACT
        Timetable tt = co.generate(details);
        boolean correct = ttv.timetable_is_valid(tt, details);

        // ASSERT
        assertThat(correct).isEqualTo(true);
    }

    @Test
    public void algorithm_generates_coherent_schedule_edge_case_2() {
        // ARRANGE
        CoordinatorTwo co = new CoordinatorTwo();
        SchedulingProblem details = MainTest.test_details_F();
        TimetableVerifier ttv = new TimetableVerifier();

        // ACT
        Timetable tt = co.generate(details);
        boolean correct = ttv.timetable_is_valid(tt, details);

        // ASSERT
        assertThat(correct).isEqualTo(false);
    }

}
