package uk.ac.cam.agb67.dissertation.ui;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import part.norfolk.xml.ReadXmlDomParser;

import uk.ac.cam.agb67.dissertation.*;

import java.util.ArrayList;
import java.util.List;

public class InterfaceXML {

    public InterfaceXML() {}

    // Takes the location of an XML file representing a user input and returns the decoded SchedulingProblem
    public SchedulingProblem XML_to_Problem(String filename) {
        try {

            // Create the scheduling problem to populate
            SchedulingProblem details = new SchedulingProblem();
            details.Session_Details = new ArrayList<>();
            details.KeyInd_Details = new ArrayList<>();
            details.PDS_Details = new ArrayList<>();
            int sid = 0, pdsid = 0;

            // Get every object from the XML diagram, each of which represents either a session or an individual
            ReadXmlDomParser readXMLDomParser = new ReadXmlDomParser(filename);
            NodeList list = readXMLDomParser.getElementsByTagName("object");

            // Iterate through and turn them into the necessary lists
            for (int i = 0; i < list.getLength(); i++) {
                Node object_node = list.item(i);
                if (object_node.getNodeType() == Node.ELEMENT_NODE) {
                    Element object = (Element) object_node;

                    // Determine if the object is a session, a key individual, or the overall details
                    NodeList innerCell = object.getElementsByTagName("mxCell");
                    String object_style_info = innerCell.item(0).getAttributes().getNamedItem("style").getTextContent();
                    //if (Main.DEBUG) System.out.println("Style info for object "+i+" was: "+object_style_info);

                    //NOTE: Event's will have rounded=0, while key individuals will have rounded=1
                    if (object_style_info.substring(0, 9).equals("rounded=1")) {
                        // Decode a key individual
                        if (Main.DEBUG) System.out.println("Found one key individual.");

                        String individual_name = object.getAttribute("label");
                        String individual_DLP = object.getAttribute("DailyLimitPreference");
                        String individual_room_prefs = object.getAttribute("RoomPreferences");

                        KeyIndividual keyInd = new KeyIndividual(individual_name, Integer.parseInt(individual_DLP), string_to_list(individual_room_prefs));
                        details.KeyInd_Details.add(keyInd);

                    } else if (object_style_info.equals("rounded=0;whiteSpace=wrap;html=1;strokeWidth=2;")) {
                        // Decode a predetermined event
                        if (Main.DEBUG) System.out.println("Found one PDS event.");

                        String session_name = object.getAttribute("label");
                        String session_length = object.getAttribute("length");
                        String session_participants = object.getAttribute("participants");

                        String PDS_day = object.getAttribute("day");
                        String PDS_time = object.getAttribute("hour");
                        String PDS_room = object.getAttribute("room");

                        PredeterminedSession pds = new PredeterminedSession(pdsid, session_name, Integer.parseInt(session_length), string_to_list(session_participants),
                                Integer.parseInt(PDS_day), Integer.parseInt(PDS_time), Integer.parseInt(PDS_room));
                        details.PDS_Details.add(pds);
                        pdsid++;

                    } else if (object_style_info.substring(0, 9).equals("rounded=0")) {
                        // Decode a normal event
                        if (Main.DEBUG) System.out.println("Found one event.");

                        String session_name = object.getAttribute("label");
                        String session_length = object.getAttribute("length");
                        String session_participants = object.getAttribute("participants");

                        Session sesh = new Session(sid, session_name, Integer.parseInt(session_length), string_to_list(session_participants));
                        details.Session_Details.add(sesh);
                        sid++;

                    } else if (object_style_info.substring(0, 14).equals("shape=process;")) {
                        // Decode the overall event details
                        if (Main.DEBUG) System.out.println("Found the overall event details.");

                        details.Maximum_Days = Integer.parseInt(object.getAttribute("Days"));
                        details.Hours_Per_Day = Integer.parseInt(object.getAttribute("HoursPerDay"));
                        details.Maximum_Rooms = Integer.parseInt(object.getAttribute("Rooms"));

                        details.Room_Occupancy_Limits = string_to_list(object.getAttribute("RoomCapacities"));

                        details.Reduce_Overlap_Pref = Boolean.parseBoolean(object.getAttribute("ReduceOverlapPreference"));
                        details.Minimum_Gap_Pref = Integer.parseInt(object.getAttribute("BookingGapPreference"));

                    } else {
                        System.err.println("Discovered an object in the XML file which does not correspond to input data.");
                    }
                }
            }

            // Then add the predetermined sessions to the sessions list
            for (int i = 0; i < pdsid; i++) {
                // Update all PDS to have IDs at the end of the list
                details.PDS_Details.get(i).Session_ID += sid;
                details.Session_Details.add(details.PDS_Details.get(i));
            }

            return details;

        } catch (NumberFormatException e) {
            System.err.println("There were incorrectly formated values in the XML input.");
            return null;
        }
    }

    // Takes a finished Timetable and returns the location a an XML file which now contains a visual representation of the schedule
    public String Schedule_to_XML(Timetable tt, SchedulingProblem details) {
        // TODO implement this method

        return null;
    }

    // Takes a string of digits separated by commas and returns a list of numbers
    List<Integer> string_to_list(String numbers) throws NumberFormatException {
        List<Integer> list = new ArrayList<>();

        int length = numbers.length();
        while (length > 0) {
            // Find the location of the next comma
            int loc;
            for (loc=0; loc<length; loc++) {
                if (numbers.substring(loc, loc+1).equals(",")) break;
            }
            //System.out.println("loc was "+loc);

            // Decode the number before the comma
            //System.out.println("substring is : "+numbers.substring(0,loc));
            try {
                int num = Integer.parseInt(numbers.substring(0, loc));
                list.add(num);
            } catch (NumberFormatException e ) {
                System.err.println("Tried to read in a list which contained non-integer items. \n   Offending Item: '"+numbers.substring(0, loc)+"'");
                throw e;
            }

            // Remove the number and its comma
            if (loc+1 > length) break;
            numbers = numbers.substring(loc+1,length);
            length = numbers.length();
            //System.out.println("New string: "+ numbers);
        }

        return list;
    }

}
