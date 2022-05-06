package uk.ac.cam.agb67.dissertation.ui;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import uk.ac.cam.agb67.dissertation.*;

import java.util.Arrays;
import java.util.List;

@RunWith(JUnit4.class)
public class InterfaceXMLTest {

    @Test
    public void object_can_be_created() {
        // ARRANGE
        InterfaceXML ui;
        // ACT
        ui = new InterfaceXML();
        // ASSERT
        System.out.println(ui.hashCode());
        assertThat(true);
    }

    @Test
    public void string_to_list_works() {
        // ARRANGE
        String nums = "2,5,6,3,3";
        List<Integer> true_list = Arrays.asList(2, 5, 6, 3, 3);
        InterfaceXML ui = new InterfaceXML();

        // ACT
        List<Integer> num_list = ui.string_to_list(nums);

        // ASSERT
        assertThat(num_list.hashCode()).isEqualTo(true_list.hashCode());
    }

    @Test
    public void string_to_list_fails_gracefully() {
        // ARRANGE
        String nums = "two, five, six, three, three";
        List<Integer> true_list = Arrays.asList(2, 5, 6, 3, 3);
        InterfaceXML ui = new InterfaceXML();

        // ACT
        try {
            List<Integer> num_list = ui.string_to_list(nums);
        } catch (Exception e) {
            // ASSERT
            assertThat(e.getClass()).isEqualTo(NumberFormatException.class);
        }
    }

    @Test
    public void XML_decoder_works() {
        // ARRANGE
        InterfaceXML ui = new InterfaceXML();

        // ACT
        SchedulingProblem details = ui.XML_to_Problem("samples/FullInput.xml");

        if (Main.DEBUG) {
            System.out.println(details.Session_Details.toString());
            System.out.println(details.KeyInd_Details.toString());
            System.out.println("\n" + details.Maximum_Days);
            System.out.println(details.Hours_Per_Day);
            System.out.println(details.Maximum_Rooms);
            System.out.println("\n" + details.Room_Occupancy_Limits.toString());
            System.out.println("\n" + details.Reduce_Overlap_Pref);
            System.out.println(details.Minimum_Gap_Pref);
        }

        // ASSERT
        assertThat(details.Session_Details.size()).isEqualTo(4);
        assertThat(details.KeyInd_Details.size()).isEqualTo(2);
        assertThat(details.PDS_Details.size()).isEqualTo(1);

        assertThat(details.Session_Details.get(0).Session_Name).isEqualTo("Group Learning");
        assertThat(details.Session_Details.get(0).Session_KeyInds.toString()).isEqualTo("[0, 6, 15]");
        assertThat(details.KeyInd_Details.get(1).KeyInd_Name).isEqualTo("Jim Baxter");
        assertThat(details.KeyInd_Details.get(1).KeyInd_Daily_Limit_Pref).isEqualTo(2);
        assertThat(details.Session_Details.get(3).getClass()).isEqualTo(PredeterminedSession.class);

        //boolean valid = details.check_validity();
        //assertThat(valid).isTrue();
    }

    @Test
    public void XML_decoder_fails_gracefully() {
        // ARRANGE
        InterfaceXML ui;

        // ACT
        ui = new InterfaceXML();
        SchedulingProblem details = ui.XML_to_Problem("samples/IncorrectInput.xml");

        // ASSERT
        assertThat(details).isEqualTo(null);
    }

    @Test
    public void XML_encoder_manual_test() {
        // ARRANGE
        SchedulingProblem details = MainTest.test_details_A();

        Timetable tt = new Timetable(5,8,4);
        tt.set(0,3,0, details.Session_Details.get(0));
        tt.set(1,0,0, details.Session_Details.get(1));
        tt.set(0,0,1, details.Session_Details.get(2));
        tt.set(0,2,1, details.Session_Details.get(3));
        tt.set(2,0,0, details.Session_Details.get(4));
        tt.set(2,1,0, details.Session_Details.get(5));
        tt.set(0,0,0, details.Session_Details.get(6));

        // ACT
        InterfaceXML ui = new InterfaceXML();
        String filename = ui.Schedule_to_XML(tt, details, "example1");
        assertThat(filename).isNotEqualTo(null);
    }

}
