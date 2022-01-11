package uk.ac.cam.agb67.dissertation.ui;

import uk.ac.cam.agb67.dissertation.*;

import java.sql.Time;

public class InterfaceXML {

    // TODO import the XML decoding library
    public InterfaceXML() {}

    // Takes the location of an XML file representing a user input and returns the decoded SchedulingProblem
    public SchedulingProblem XML_to_Problem(String filename) {
        // TODO implement this method

        return null;

    /*
    try {
        // Reading the object from a file
        FileInputStream file = new FileInputStream(filename);
        ObjectInputStream in = new ObjectInputStream(file);

        deserialsed = (CLASS HERE) in.readObject();

        file.close();
        in.close();

    } catch (IOException | ClassNotFoundException e) {
        System.err.println("Failed to deserialise the object with location: " + location);
        e.printStackTrace();
        return null;
    }
    */

    }

    // Takes a finished Timetable and returns the location a an XML file which now contains a visual representation of the schedule
    public String Schedule_to_XML(Timetable tt, SchedulingProblem details) {
        // TODO implement this method

        return null;
    }

}
