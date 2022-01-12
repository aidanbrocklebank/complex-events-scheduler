package uk.ac.cam.agb67.dissertation.ui;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import uk.ac.cam.agb67.dissertation.*;

@RunWith(JUnit4.class)
public class InterfaceXMLTest {

    // TODO create some proper tests

    @Test
    public void object_can_be_created(){
        // ARRANGE
        InterfaceXML ui;
        // ACT
        ui = new InterfaceXML();
        // ASSERT
        System.out.println(ui.hashCode());
        assertThat(true);
    }

    @Test
    public void temp() {
        // ARRANGE
        InterfaceXML ui;
        // ACT
        ui = new InterfaceXML();
        SchedulingProblem details = ui.XML_to_Problem("samples\\FullInput.xml");

        System.out.println(details.Session_Details.toString());
        System.out.println(details.KeyInd_Details.toString());

        System.out.println("\n" + details.Maximum_Days);
        System.out.println(details.Hours_Per_Day);
        System.out.println(details.Maximum_Rooms);

        System.out.println("\n" + details.Room_Occupancy_Limits.toString());

        System.out.println("\n" + details.Reduce_Overlap_Pref);
        System.out.println(details.Minimum_Gap_Pref);

    }

}
