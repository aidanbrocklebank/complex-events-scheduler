package uk.ac.cam.agb67.dissertation;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;
import uk.ac.cam.agb67.dissertation.*;
import uk.ac.cam.agb67.dissertation.algorithm.one.Coordinator;
import uk.ac.cam.agb67.dissertation.algorithm.one.GreedyMethod;

public class TimetableSatisfactionMeasurerTest {

    @Test
    public void object_can_be_created() {
        // ARRANGE
        TimetableSatisfactionMeasurer tsm;
        // ACT
        tsm = new TimetableSatisfactionMeasurer();
        // ASSERT
        System.out.println(tsm.hashCode());
        assertThat(true);
    }

    /*
    @Test
    public void temp_unoptimised_score_test() {
        // ARRANGE
        Coordinator co = new Coordinator();
        SchedulingProblem details = MainTest.test_details_A();
        TimetableVerifier ttv = new TimetableVerifier();

        // ACT
        Timetable tt = co.generate(details);

        // ASSERT
        assertThat(ttv.timetable_is_valid(tt, details)).isEqualTo(true);

        TimetableSatisfactionMeasurer tsm = new TimetableSatisfactionMeasurer(Main.DEBUG);
        int pref_score = tsm.timetable_preference_satisfaction(tt, details);
        if (Main.DEBUG) System.out.println("First Score: " + pref_score + "\n");
    }

    @Test
    public void temp_optimised_score_test() {
        // ARRANGE
        Coordinator opt = new Coordinator(false, true);
        SchedulingProblem details = MainTest.test_details_A();
        TimetableVerifier ttv = new TimetableVerifier();

        // ACT
        Timetable ott = opt.generate(details);

        // ASSERT
        assertThat(ttv.timetable_is_valid(ott, details)).isEqualTo(true);

        TimetableSatisfactionMeasurer tsm = new TimetableSatisfactionMeasurer(Main.DEBUG);
        int new_pref_score = tsm.timetable_preference_satisfaction(ott, details);
        if (Main.DEBUG) System.out.println("Optimised Score: " + new_pref_score);
    }
    */

    @Test
    public void calculates_correct_gap_pref_score() {
        // ARRANGE
        SchedulingProblem details = MainTest.test_details_C();
        Timetable tt = MainTest.test_timetable_C();
        TimetableSatisfactionMeasurer tsm = new TimetableSatisfactionMeasurer(true);

        // ACT
        int gap_score = tsm.gap_preference_satisfaction(tt, details);
        System.out.println("Calculated Gap Score: " + gap_score + "%");

        // ASSERT
        assertThat(gap_score).isAtLeast(90);
        assertThat(gap_score).isAtMost(90);

        // ACT
        details.Minimum_Gap_Pref = 3;
        int harsher_gap_score = tsm.gap_preference_satisfaction(tt, details);
        System.out.println("Calculated Harsher Gap Score: " + harsher_gap_score + "%");

        // ASSERT
        assertThat(harsher_gap_score).isAtLeast(70);
        assertThat(harsher_gap_score).isAtMost(70);
    }

    @Test
    public void calculates_correct_overlap_pref_score() {
        // ARRANGE
        SchedulingProblem details = MainTest.test_details_C();
        Timetable tt = MainTest.test_timetable_C();
        TimetableSatisfactionMeasurer tsm = new TimetableSatisfactionMeasurer(true);

        // ACT
        int overlap_score = tsm.overlap_preference_satisfaction(tt, details);
        System.out.println("Calculated Overlap Score: " + overlap_score + "%");

        // ASSERT
        assertThat(overlap_score).isAtLeast(100 - 36);
        assertThat(overlap_score).isAtMost(100 - 36);
    }

    @Test
    public void calculates_correct_room_pref_score() {
        // ARRANGE
        SchedulingProblem details = MainTest.test_details_C();
        Timetable tt = MainTest.test_timetable_C();
        //System.err.println(tt.toString());
        TimetableSatisfactionMeasurer tsm = new TimetableSatisfactionMeasurer(true);

        // ACT
        int room_score = tsm.room_preference_satisfaction(tt, details);
        System.out.println("Calculated Overlap Score: " + room_score + "%");

        // ASSERT
        assertThat(room_score).isAtLeast(53);
        assertThat(room_score).isAtMost(54);
    }

    @Test
    public void calculates_correct_limit_pref_score() {
        // ARRANGE
        SchedulingProblem details = MainTest.test_details_C();
        Timetable tt = MainTest.test_timetable_C();
        TimetableSatisfactionMeasurer tsm = new TimetableSatisfactionMeasurer(true);

        // ACT
        int limit_score = tsm.limit_preference_satisfaction(tt, details);
        System.out.println("Calculated Overlap Score: " + limit_score + "%");

        // ASSERT
        assertThat(limit_score).isAtLeast(70);
        assertThat(limit_score).isAtMost(71);
    }

    @Test
    public void fully_calculates_pref_score() {
        // ARRANGE
        SchedulingProblem details = MainTest.test_details_C();
        Timetable tt = MainTest.test_timetable_C();
        TimetableSatisfactionMeasurer tsm = new TimetableSatisfactionMeasurer(true);

        // ACT
        int score = tsm.timetable_preference_satisfaction(tt, details);

        // ASSERT
        assertThat(score).isAtLeast(69);
        assertThat(score).isAtMost(70);

        // ACT
        details.Minimum_Gap_Pref = 3;
        int harsher_score = tsm.timetable_preference_satisfaction(tt, details);

        // ASSERT
        assertThat(harsher_score).isAtLeast(64);
        assertThat(harsher_score).isAtMost(65);
    }
}
