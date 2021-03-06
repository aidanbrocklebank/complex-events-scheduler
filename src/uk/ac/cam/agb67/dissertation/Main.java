package uk.ac.cam.agb67.dissertation;

import uk.ac.cam.agb67.dissertation.algorithm.one.*;
import uk.ac.cam.agb67.dissertation.algorithm.two.*;
import uk.ac.cam.agb67.dissertation.ui.InterfaceXML;

import java.io.File;

public class Main {

    // Globally used debug flag
    public static boolean DEBUG = false;

    // Takes user input in the form of the name of an XML file, selects an algorithm from their parameters,
    // schedules the input details with that algorithm and saves it to a file.
    public static void main(String[] args) {

        // Get the name of the input file, and the params for the algorithm
        String location = args[0];
        int algorithm_choice = Integer.parseInt(args[1]);
        boolean optimise_choice = Boolean.parseBoolean(args[2]);

        // Retrieve the input details from the file
        InterfaceXML ui = new InterfaceXML();
        File check_existance = new File("samples\\" + location);
        if (!check_existance.exists()) {
            System.err.println("The input file was not found.");
            return;
        }
        SchedulingProblem details = ui.XML_to_Problem("samples\\" + location);

        // Check that they are a usable set of input details
        if (!details.potentially_schedulable()) {
            System.err.println("The given event details are not a valid input.");
            return;
        }

        // Put together the algorithm to use from the parameters
        SchedulingAlgorithm algorithm;
        switch (algorithm_choice) {
            case 0: algorithm = new Coordinator(true, optimise_choice); break;
            case 1: algorithm = new Coordinator(false, optimise_choice); break;
            case 2: algorithm = new CoordinatorTwo(optimise_choice); break;
            default: algorithm = new Coordinator(false, optimise_choice);
        }

        // Generate a schedule for the details, check it is valid
        Timetable schedule = algorithm.generate(details);
        TimetableVerifier ttv = new TimetableVerifier();
        if (!ttv.timetable_is_valid(schedule, details)) {
            System.err.println("Failed to create a valid schedule with the given input and desired algorithm."); return;
        }

        // Print the schedule to the console
        System.out.println("SCHEDULE: \n" + schedule.toString());

        // Determine the suffix for the filename
        String suffix;
        if (optimise_choice) {
            suffix = algorithm_choice + "opt";
        } else{
            suffix = algorithm_choice + "base";
        }

        // Then save the schedule to a file
        ui.Schedule_to_XML(schedule, details, location.substring(0, location.length() - 4) + "_schedule_" + suffix);
    }

}
