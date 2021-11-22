package uk.ac.cam.agb67.dissertation.algorithm.one;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TimetableVerifierTest {

    @Test
    public void object_can_be_created(){
        // ARRANGE
        TimetableVerifier ttv;
        // ACT
        ttv = new TimetableVerifier();
        // ASSERT
        System.out.println(ttv.hashCode());
        assertThat(true);
    }

}
