package uk.ac.cam.agb67.dissertation.algorithm.one;

import org.junit.Test;
import uk.ac.cam.agb67.dissertation.*;

import static com.google.common.truth.Truth.assertThat;

public class NaiveOptimiserTest {

    @Test
    public void object_can_be_created() {
        // ARRANGE
        NaiveOptimiser no;
        SchedulingProblem det = MainTest.test_details_A();

        // ACT
        no = new NaiveOptimiser();

        // ASSERT
        System.out.println(no.hashCode());
        assertThat(true);
    }

    @Test
    public void optimisation_pass_maintains_validity() {
        // ARRANGE
        NaiveOptimiser no = new NaiveOptimiser();
        Coordinator co = new Coordinator();
        SchedulingProblem details = MainTest.test_details_A();

        // ACT
        Timetable tt = co.generate(details);
        Timetable improved = no.optimisation_pass(tt, details);

        // ASSERT
        TimetableVerifier ttv = new TimetableVerifier();
        boolean was_valid = ttv.timetable_is_valid(tt, details);
        boolean is_valid = ttv.timetable_is_valid(improved, details);

        assertThat(was_valid && is_valid);
    }

    @Test
    public void optimisation_pass_does_not_reduce_score() {
        // ARRANGE
        NaiveOptimiser no = new NaiveOptimiser();
        Coordinator co = new Coordinator();
        SchedulingProblem details = MainTest.test_details_A();

        // ACT
        Timetable tt = co.generate(details);
        Timetable improved = no.optimisation_pass(tt, details);

        // ASSERT
        TimetableSatisfactionMeasurer tsm = new TimetableSatisfactionMeasurer(Main.DEBUG);
        int old_score = tsm.timetable_preference_satisfaction(tt, details);
        int new_score = tsm.timetable_preference_satisfaction(improved, details);

        assertThat(new_score).isAtLeast(old_score);
    }

}
