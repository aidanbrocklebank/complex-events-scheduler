package uk.ac.cam.agb67.dissertation;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Arrays;

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
        //System.err.println(tt2.toString());
        assertThat(tt2.get_id(5,6,7)).isEqualTo(101);
        assertThat(tt2.get_hour(5,6,7)).isEqualTo(2);
        assertThat(tt2 == tt).isEqualTo(false);
    }

    @Test
    public void to_string_works() {
        // ARRANGE
        Timetable tt;
        tt = new Timetable(1, 3,2);
        tt.set(0, 2, 1, 101, 0);

        // ACT
        String represent = tt.toString();

        // ASSERT
        System.err.println(represent);
        assertThat(represent).isEqualTo("\nDAY 0:\n   Room 0: [   ,   ,   ,] \n   Room 1: [   ,   ,101,] \n");
    }

    @Test
    public void set_session_works() {
        // ARRANGE
        Timetable tt;
        tt = new Timetable(4, 8,5);
        Session s = new Session(202, "Ses 202", 3, Arrays.asList(1,2,3));

        // ACT
        tt.set(2, 3, 4, s);

        // ASSERT
        //System.err.println(tt2.toString());
        assertThat(tt.get_id(2,3,4)).isEqualTo(202);
        assertThat(tt.get_hour(2,3,4)).isEqualTo(0);
        assertThat(tt.get_id(2,4,4)).isEqualTo(202);
        assertThat(tt.get_hour(2,4,4)).isEqualTo(1);
        assertThat(tt.get_id(2,5,4)).isEqualTo(202);
        assertThat(tt.get_hour(2,5,4)).isEqualTo(2);
    }

}
