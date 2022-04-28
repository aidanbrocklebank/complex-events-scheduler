package uk.ac.cam.agb67.dissertation;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import uk.ac.cam.agb67.dissertation.algorithm.one.Coordinator;

import java.io.File;
import java.io.IOException;
import java.util.List;

@RunWith(JUnit4.class)
public class AnalyserTest {

    @Test
    public void writes_correctly_to_CSV_file() {
        // ARRANGE
        boolean success_1 = false;
        boolean success_2 = false;

        int len = 10;
        boolean[][] VALID = new boolean[5][len];
        int[][] SCORE = new int[5][len];
        long[][] RAM = new long[5][len];
        long[][] TIME = new long[5][len];

        int[] PARAM = new int[len];
        long[][] SEGMENTS = null;

        for (int i=0; i<len; i++) {
            for (int j=0; j<5; j++) {
                VALID[j][i] = true;
                SCORE[j][i] = i + j;
                RAM[j][i] = i + j + 100;
                TIME[j][i] = i + j + 200;
            }
            PARAM[i] = i;
        }

        File test = new File("test.csv");
        test.delete();
        File ind_test = new File("ind_test.csv");
        ind_test.delete();

        // ACT
        try {
            success_1 = Analyser.save_to_spreadsheet("test.csv", len, VALID, SCORE, RAM, TIME);
        } catch (IOException e) {
            e.printStackTrace();
            assertThat(false).isTrue();
        }

        try {
            success_2 = Analyser.save_to_spreadsheet("ind_test.csv", len, "Algorithm Name", VALID[0], SCORE[0], RAM[0], TIME[0], SEGMENTS, PARAM, "#Param");
        } catch (IOException e) {
            e.printStackTrace();
            assertThat(false).isTrue();
        }

        // ASSERT
        assertThat(success_1).isTrue();
        assertThat(success_2).isTrue();
    }

    @Test
    public void generate_number_returns_int_within_range() {
        // ARRANGE
        int max = 105; int min = 7;

        for (int i=0; i<100; i++) {
            // ACT

            int random = Analyser.generate_number(max, min);

            // ASSERT
            assertThat(random).isAtMost(max);
            assertThat(random).isAtLeast(min);
        }
    }

    @Test
    public void generate_numbers_returns_ints_within_range() {
        // ARRANGE
        int max = 1205; int min = 78;

        // ACT
        List<Integer> random = Analyser.generate_numbers(max, min, 100);

        // ASSERT
        for (int i=0; i<100; i++) {
            assertThat(random.get(i)).isAtMost(max);
            assertThat(random.get(i)).isAtLeast(min);
        }
    }

    @Test
    public void generate_numbers_returns_no_duplicates() {
        // ARRANGE
        int max = 1205; int min = 78;
        int[] occurrences = new int[max-min+1];

        // ACT
        List<Integer> random = Analyser.generate_numbers(max, min, 100);

        // ASSERT
        for (int i=0; i<100; i++) {
            occurrences[random.get(i) - min] += 1;
        }
        for (int i=0; i<max-min; i++) {
            assertThat(occurrences[i]).isAtMost(1);
        }
    }

    @Test
    public void guaranteed_randomized_test_details_works() {
        // ARRANGE
        SchedulingProblem details;

        // ACT
        details = Analyser.guaranteed_randomized_test_details(50, 50, 1000, 50);

        Coordinator algoOne = new Coordinator(false, false);
        Timetable tt = algoOne.generate(details);
        System.out.println(tt);

        // ASSERT
        assertThat(details.potentially_schedulable()).isTrue();
    }

    @Test
    public void testing_all_algorithms_runs() {
        // ARRANGE
        File test = new File("results/all_test_1.csv");
        test.delete();



        // ACT
        Analyser.main(new String[]{"1", "all_test"});

        // ASSERT
        test = new File("results/all_test_1.csv");
        assertThat(test.exists()).isTrue();
    }

    @Test
    public void testing_two_algorithms_runs() {
        // ARRANGE
        File test = new File("results/compare_test_1.csv");
        test.delete();

        // ACT
        Analyser.main(new String[]{"10", "compare_test", "#10#10", "0", "1"});

        // ASSERT
        test = new File("results/compare_test_1.csv");
        assertThat(test.exists()).isTrue();
    }

    @Test
    public void testing_CSP_algorithms_runs() {
        // ARRANGE
        File test = new File("results/csp_compare_test_1.csv");
        test.delete();

        // ACT
        Analyser.main(new String[]{"10", "csp_compare_test", "#10#10", "3", "4"});

        // ASSERT
        test = new File("results/csp_compare_test_1.csv");
        assertThat(test.exists()).isTrue();
    }

    @Test
    public void testing_algorithm_with_parameter_increasing_runs() {
        // ARRANGE
        File test = new File("results/param_test_1.csv");
        test.delete();

        // ACT
        Analyser.main(new String[]{"10", "param_test", "3", "s", "1"});

        // ASSERT
        test = new File("results/param_test_1.csv");
        assertThat(test.exists()).isTrue();
    }

    // TODO new test for test_algorithm_with_details() which uses ALGO 2 and gives an invalid set of details

}
