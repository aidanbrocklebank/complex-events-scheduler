package uk.ac.cam.agb67.dissertation.algorithm.one;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import uk.ac.cam.agb67.dissertation.*;

import java.util.ArrayList;
import java.util.List;

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

    @Test
    public void comprehensive_checker_basic_examples(){
        // ARRANGE
        TimetableVerifier ttv = new TimetableVerifier();
        Timetable tt = new Timetable(2,8,2);
        List<Session> ls = new ArrayList<>();

        // ACT
        ls.add(new Session(0));
        ls.add(new Session(1));
        ls.add(new Session(2));
        ls.add(new Session(3));

        tt.set(0,3,0, 0, 0);
        tt.set(1,7,0, 1, 0);
        tt.set(1,4,1, 2, 0);

        // ASSERT
        assertThat(!ttv.timetable_is_comprehensive(tt, ls));

        // Add the missing session
        tt.set(0,5,1, 3, 0);
        assertThat(ttv.timetable_is_comprehensive(tt, ls));
    }

    @Test
    public void duplicates_checker_basic_examples(){
        // ARRANGE
        TimetableVerifier ttv = new TimetableVerifier();
        Timetable tt = new Timetable(2,8,2);
        List<Session> ls = new ArrayList<>();

        // ACT
        ls.add(new Session(0));
        ls.add(new Session(1));
        ls.add(new Session(2));
        ls.add(new Session(3));

        tt.set(0,3,0, 0, 0);
        tt.set(0,4,0, 0, 1);
        tt.set(1,2,1, 2, 0);
        tt.set(1,5,1, 3, 0);

        // ASSERT
        assertThat(ttv.timetable_excludes_duplicates(tt, ls));

        // Add a duplicate
        tt.set(0,5,1, 2, 0);
        assertThat(!ttv.timetable_excludes_duplicates(tt, ls));
    }

}
