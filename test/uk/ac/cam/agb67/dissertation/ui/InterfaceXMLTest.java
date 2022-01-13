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

    // TODO create some proper tests

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
        InterfaceXML ui;

        // ACT
        ui = new InterfaceXML();
        SchedulingProblem details = ui.XML_to_Problem("samples\\FullInput.xml");

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
    }

    @Test
    public void XML_decoder_fails_gracefully() {
        // ARRANGE
        InterfaceXML ui;

        // ACT
        ui = new InterfaceXML();
        SchedulingProblem details = ui.XML_to_Problem("samples\\IncorrectInput.xml");

        // ASSERT
        assertThat(details).isEqualTo(null);
    }

}
