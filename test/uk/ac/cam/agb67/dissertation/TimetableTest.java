package uk.ac.cam.agb67.dissertation;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import uk.ac.cam.agb67.dissertation.*;
import uk.ac.cam.agb67.dissertation.algorithm.one.TimetableVerifier;

import java.sql.Time;

@RunWith(JUnit4.class)
public class TimetableTest {

    @Test
    public void object_can_be_created(){
        // ARRANGE
        Timetable tt;
        // ACT
        tt = new Timetable(7, 8,10);
        // ASSERT
        System.out.println(tt.hashCode());
        assertThat(true);
    }

    @Test
    public void deep_copy_works() {
        // ARRANGE
        Timetable tt;
        tt = new Timetable(7, 8,10);
        tt.set(5, 6, 7, 101, 2);

        // ACT
        Timetable tt2 = tt.deep_copy();

        // ASSERT
        assertThat(tt2.get_id(5,6,7)).isEqualTo(101);
        assertThat(tt2.get_hour(5,6,7)).isEqualTo(2);
        assertThat(tt2.hashCode()).isNotEqualTo(tt.hashCode());
    }

}
