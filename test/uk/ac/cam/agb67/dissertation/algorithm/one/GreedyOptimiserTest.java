package uk.ac.cam.agb67.dissertation.algorithm.one;

import org.junit.Test;
import uk.ac.cam.agb67.dissertation.MainTest;
import uk.ac.cam.agb67.dissertation.SchedulingProblem;
import uk.ac.cam.agb67.dissertation.Timetable;

import java.util.Arrays;

import static com.google.common.truth.Truth.assertThat;

public class GreedyOptimiserTest {

    // TODO create tests

    @Test
    public void object_can_be_created() {
        // ARRANGE
        GreedyOptimiser go;
        SchedulingProblem det = MainTest.test_details_A();
        // ACT
        go = new GreedyOptimiser();
        // ASSERT
        System.out.println(go.hashCode());
        assertThat(true);
    }


}
