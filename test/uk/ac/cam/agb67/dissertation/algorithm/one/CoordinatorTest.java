package uk.ac.cam.agb67.dissertation.algorithm.one;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import uk.ac.cam.agb67.dissertation.*;

import static com.google.common.truth.Truth.assertThat;

@RunWith(JUnit4.class)
public class CoordinatorTest {

    @Test
    public void object_can_be_created(){
        // ARRANGE
        Coordinator co;
        SchedulingProblem det = MainTest.test_details_A();
        // ACT
        co = new Coordinator();
        // ASSERT
        System.out.println(co.hashCode());
        assertThat(true);
    }

    @Test
    public void algorithm_generates_coherent_schedule_0(){
        // ARRANGE
        Coordinator co = new Coordinator();
        SchedulingProblem details = MainTest.test_details_D();
        TimetableVerifier ttv = new TimetableVerifier();

        // ACT
        Timetable tt = co.generate(details);
        boolean correct = ttv.timetable_is_valid(tt, details);

        // ASSERT
        assertThat(correct).isEqualTo(true);
    }

    @Test
    public void algorithm_generates_coherent_schedule_1(){
        // ARRANGE
        Coordinator co = new Coordinator();
        SchedulingProblem details = MainTest.test_details_A();
        TimetableVerifier ttv = new TimetableVerifier();

        // ACT
        Timetable tt = co.generate(details);
        boolean correct = ttv.timetable_is_valid(tt, details);

        // ASSERT
        assertThat(correct).isEqualTo(true);
    }

    @Test
    public void algorithm_generates_coherent_schedule_2(){
        // ARRANGE
        Coordinator co = new Coordinator();
        SchedulingProblem details = MainTest.test_details_C();
        TimetableVerifier ttv = new TimetableVerifier();

        // ACT
        Timetable tt = co.generate(details);
        boolean correct = ttv.timetable_is_valid(tt, details);

        // ASSERT
        assertThat(correct).isEqualTo(true);
    }

    // TODO copmprehensive tests

}
