package uk.ac.cam.agb67.dissertation.algorithm.two;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import uk.ac.cam.agb67.dissertation.*;
import uk.ac.cam.agb67.dissertation.algorithm.one.TimetableVerifier;

import static com.google.common.truth.Truth.assertThat;

@RunWith(JUnit4.class)
public class CoordinatorTwoTest {

    // TODO create more tests

    @Test
    public void object_can_be_created(){
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
    public void algorithm_generates_coherent_schedule_1(){
        // ARRANGE
        CoordinatorTwo co = new CoordinatorTwo();
        SchedulingProblem details = MainTest.test_details_A();
        TimetableVerifier ttv = new TimetableVerifier();

        // ACT
        Timetable tt = co.generate(details);
        boolean correct = ttv.timetable_is_coherent(tt, details.Session_Details);

        // ASSERT
        assertThat(correct).isEqualTo(true);
    }

    @Test
    public void algorithm_generates_coherent_schedule_2(){
        // ARRANGE
        CoordinatorTwo co = new CoordinatorTwo();
        SchedulingProblem details = MainTest.test_details_C();
        TimetableVerifier ttv = new TimetableVerifier();

        // ACT
        Timetable tt = co.generate(details);
        boolean correct = ttv.timetable_is_coherent(tt, details.Session_Details);

        // ASSERT
        assertThat(correct).isEqualTo(true);
    }

}
