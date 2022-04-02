package uk.ac.cam.agb67.dissertation;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import uk.ac.cam.agb67.dissertation.algorithm.one.Coordinator;

import java.io.IOException;
import java.security.Key;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RunWith(JUnit4.class)
public class AnalyserTest {

    @Test
    public void writes_correctly_to_CSV_file() {
        // ARRANGE
        int len = 10;
        boolean[][] VALID = new boolean[5][len];
        int[][] SCORE = new int[5][len];
        long[][] RAM = new long[5][len];
        long[][] TIME = new long[5][len];

        for (int i=0; i<len; i++) {
            for (int j=0; j<5; j++) {
                VALID[j][i] = true;
                SCORE[j][i] = i + j;
                RAM[j][i] = i + j + 100;
                TIME[j][i] = i + j + 200;
            }
        }

        // ACT
        try {
            Analyser.save_to_spreadsheet("test.csv", len, VALID, SCORE, RAM, TIME);
        } catch (IOException e) {
            e.printStackTrace();
            assertThat(false).isTrue();
        }

        // ASSERT
        assertThat(true).isTrue();
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
        details = Analyser.guaranteed_randomized_test_details(20, 5, 150, 40);

        Coordinator algoOne = new Coordinator(false, false);
        Timetable tt = algoOne.generate(details);
        System.out.println(tt);

        // ASSERT
        assertThat(details.check_validity()).isTrue();
    }

}
