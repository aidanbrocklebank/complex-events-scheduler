package uk.ac.cam.agb67.dissertation.algorithm.one;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import uk.ac.cam.agb67.dissertation.MainTest;
import uk.ac.cam.agb67.dissertation.SchedulingProblem;

import static com.google.common.truth.Truth.assertThat;

@RunWith(JUnit4.class)
public class CoordinatorTest {

    // TODO create tests

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
    public void algorithm_generates_schedules(){
        // ARRANGE


        // ACT


        // ASSERT
        assertThat(1).isEqualTo(1);
    }

}
