package uk.ac.cam.agb67.dissertation;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.security.Key;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RunWith(JUnit4.class)
public class AnalyserTest {

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

}
