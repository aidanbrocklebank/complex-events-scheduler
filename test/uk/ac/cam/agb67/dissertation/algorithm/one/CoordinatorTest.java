package uk.ac.cam.agb67.dissertation.algorithm.one;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import uk.ac.cam.agb67.dissertation.*;
import uk.ac.cam.agb67.dissertation.ui.InterfaceXML;

import java.sql.Time;

import static com.google.common.truth.Truth.assertThat;

@RunWith(JUnit4.class)
public class CoordinatorTest {

    // TODO differentiate tests for the greedy variant

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

    @Test
    public void greedy_optimiser_maintains_validity_0() {
        // ARRANGE
        Coordinator co = new Coordinator();
        Coordinator opt = new Coordinator(false, true);
        SchedulingProblem details = MainTest.test_details_D();
        TimetableVerifier ttv = new TimetableVerifier();

        // ACT
        Timetable tt = co.generate(details);
        Timetable ott = opt.generate(details);

        // ASSERT
        assertThat(ttv.timetable_is_valid(tt, details)).isEqualTo(true);
        assertThat(ttv.timetable_is_valid(ott, details)).isEqualTo(true);


        TimetableSatisfactionMeasurer tsm = new TimetableSatisfactionMeasurer();
        int pref_score = tsm.timetable_preference_satisfaction(tt, details);

        int new_pref_score = tsm.timetable_preference_satisfaction(ott, details);
        assertThat(new_pref_score).isAtLeast(pref_score);
    }

    @Test
    public void greedy_optimiser_maintains_validity_1() {
        // ARRANGE
        Coordinator co = new Coordinator();
        Coordinator opt = new Coordinator(false, true);
        SchedulingProblem details = MainTest.test_details_A();
        TimetableVerifier ttv = new TimetableVerifier();

        // ACT
        Timetable tt = co.generate(details);
        Timetable ott = opt.generate(details);

        // ASSERT
        assertThat(ttv.timetable_is_valid(tt, details)).isEqualTo(true);
        assertThat(ttv.timetable_is_valid(ott, details)).isEqualTo(true);


        TimetableSatisfactionMeasurer tsm = new TimetableSatisfactionMeasurer(Main.DEBUG);
        int pref_score = tsm.timetable_preference_satisfaction(tt, details);
        if (Main.DEBUG) System.out.println("First Score: " + pref_score + "\n");

        int new_pref_score = tsm.timetable_preference_satisfaction(ott, details);
        if (Main.DEBUG) System.out.println("Optimised Score: " + new_pref_score);
        assertThat(new_pref_score).isAtLeast(pref_score);
    }

    @Test
    public void greedy_optimiser_maintains_validity_2() {
        // ARRANGE
        Coordinator co = new Coordinator();
        Coordinator opt = new Coordinator(false, true);
        SchedulingProblem details = MainTest.test_details_C();
        TimetableVerifier ttv = new TimetableVerifier();

        // ACT
        Timetable tt = co.generate(details);
        Timetable ott = opt.generate(details);

        // ASSERT
        assertThat(ttv.timetable_is_valid(tt, details)).isEqualTo(true);
        assertThat(ttv.timetable_is_valid(ott, details)).isEqualTo(true);


        TimetableSatisfactionMeasurer tsm = new TimetableSatisfactionMeasurer(Main.DEBUG);
        int pref_score = tsm.timetable_preference_satisfaction(tt, details);
        if (Main.DEBUG) System.out.println("First Score: " + pref_score + "\n");

        int new_pref_score = tsm.timetable_preference_satisfaction(ott, details);
        if (Main.DEBUG) System.out.println("Optimised Score: " + new_pref_score);
        assertThat(new_pref_score).isAtLeast(pref_score);
    }

    @Test
    public void algorithm_generates_coherent_schedule_edge_case_1(){
        // ARRANGE
        Coordinator co = new Coordinator();
        SchedulingProblem details = MainTest.test_details_E();
        TimetableVerifier ttv = new TimetableVerifier();

        // ACT
        Timetable tt = co.generate(details);
        boolean correct = ttv.timetable_is_valid(tt, details);

        // ASSERT
        assertThat(correct).isEqualTo(true);
    }

    @Test
    public void algorithm_generates_coherent_schedule_edge_case_2(){
        // ARRANGE
        Coordinator co = new Coordinator();
        SchedulingProblem details = MainTest.test_details_F();
        TimetableVerifier ttv = new TimetableVerifier();

        // ACT
        Timetable tt = co.generate(details);
        boolean correct = ttv.timetable_is_valid(tt, details);

        // ASSERT
        assertThat(correct).isEqualTo(false);
    }

    @Test
    public void check_session_doesnt_clash_catches_parallel_individuals(){
        // ARRANGE
        Timetable tt = MainTest.test_timetable_A();
        SchedulingProblem det = MainTest.test_details_A();

        // ACT
        // Place session 3, which shares a participant with session 2, at the same time as session 2
        boolean noClash = Coordinator.check_session_doesnt_clash(tt, 2,6,1, det.Session_Details.get(3), det.Session_Details);

        // ASSERT
        assertThat(noClash).isEqualTo(false);
    }

    @Test
    public void check_session_doesnt_clash_catches_direct_clash(){
        // ARRANGE
        Timetable tt = MainTest.test_timetable_A();
        SchedulingProblem det = MainTest.test_details_A();

        // ACT
        // Place session 4 at the same time as session 2
        boolean noClash = Coordinator.check_session_doesnt_clash(tt, 2,6,0, det.Session_Details.get(4), det.Session_Details);

        // ASSERT
        assertThat(noClash).isEqualTo(false);
    }

    @Test
    public void check_session_doesnt_clash_allows_acceptable_sessions(){
        // ARRANGE
        Timetable tt = MainTest.test_timetable_A();
        SchedulingProblem det = MainTest.test_details_A();

        // ACT
        // Place session 5
        boolean noClash = Coordinator.check_session_doesnt_clash(tt, 1,3,0, det.Session_Details.get(5), det.Session_Details);

        // ASSERT
        assertThat(noClash).isEqualTo(true);
    }

}
