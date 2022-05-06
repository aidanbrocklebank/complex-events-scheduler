package uk.ac.cam.agb67.dissertation.ui;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import uk.ac.cam.agb67.dissertation.ui.ReadXmlDomParser;

import uk.ac.cam.agb67.dissertation.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
    public String Schedule_to_XML(Timetable tt, SchedulingProblem details, String name) {

        // Prepare the header and the footer for the file
        String header =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<mxfile host=\"app.diagrams.net\" modified=\"2022-01-13T17:24:35.419Z\" agent=\"5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.110 Safari/537.36\" version=\"16.0.0\" etag=\"OqdQl5cTmx5Hxo7dFLoE\" type=\"google\">\n" +
                "  <diagram id=\"S1rT-Umi_qtr9FfrOm0y\">\n" +
                "    <mxGraphModel dx=\"1093\" dy=\"515\" grid=\"1\" gridSize=\"10\" guides=\"1\" tooltips=\"1\" connect=\"1\" arrows=\"1\" fold=\"1\" page=\"1\" pageScale=\"1\" pageWidth=\"827\" pageHeight=\"1169\" math=\"0\" shadow=\"0\">\n" +
                "      <root>\n" +
                "        <mxCell id=\"0\" />\n" +
                "        <mxCell id=\"1\" parent=\"0\" />\n";

        String footer =
                "      </root>\n" +
                "    </mxGraphModel>\n" +
                "  </diagram>\n" +
                "</mxfile>\n";


        // Create the geometric objects which lay out the information
        int mxCellID = 2;
        StringBuilder layout_data = new StringBuilder();

        // Iterate through the days
        for (int day=0; day<tt.Total_Days; day++) {
            int y_offset = day * (tt.Total_Rooms + 2) * 40;

            // Create the day label
            layout_data.append(create_mxCell(mxCellID++, "DAY: "+day, 40, y_offset, 80, 40,
                    "text;html=1;strokeColor=none;fillColor=none;align=center;verticalAlign=middle;whiteSpace=wrap;rounded=0;shadow=0;glass=0;sketch=0;fontStyle=1;fontSize=18;"));

            // Create the room text
            layout_data.append(create_mxCell(mxCellID++, "ROOMS", -20, y_offset+60, 80, 40,
                    "text;html=1;strokeColor=none;fillColor=none;align=center;verticalAlign=middle;whiteSpace=wrap;rounded=0;shadow=0;glass=0;sketch=0;fontStyle=1;fontSize=18;rotation=-90;"));

            // Create the room labels
            for (int r=0; r<tt.Total_Rooms; r++){
                layout_data.append(create_mxCell(mxCellID++, "R"+r+":", 40, y_offset+40+(r*40), 40, 40,
                        "text;html=1;strokeColor=none;fillColor=none;align=center;verticalAlign=middle;whiteSpace=wrap;rounded=0;shadow=0;glass=0;sketch=0;fontStyle=1;fontSize=18;"));
            }

            // Create the background
            layout_data.append(create_mxCell(mxCellID++, "", 80, y_offset+40, tt.Hours_Per_Day*40, tt.Total_Rooms*40,
                    "rounded=0;whiteSpace=wrap;html=1;shadow=0;glass=0;sketch=0;fontSize=18;strokeWidth=1;fillColor=#f5f5f5;fontColor=#333333;strokeColor=#666666;"));

            // Create the room line
            layout_data.append(
                    "        <mxCell id=\""+(mxCellID++)+"\" value=\"\" style=\"endArrow=none;html=1;rounded=0;fontSize=18;\" edge=\"1\" parent=\"1\">\n" +
                    "          <mxGeometry width=\"50\" height=\"50\" relative=\"1\" as=\"geometry\">\n" +
                    "            <mxPoint x=\"40\" y=\""+(y_offset+40)+"\" as=\"sourcePoint\" />\n" +
                    "            <mxPoint x=\"40\" y=\""+(y_offset+40+(tt.Total_Rooms*40))+"\" as=\"targetPoint\" />\n" +
                    "          </mxGeometry>\n" +
                    "        </mxCell>\n"
            );

        }

        // Create the geometric objects for the timetabled sessions
        StringBuilder timetable_data = new StringBuilder();

        // Iterate through the days
        for (int day=0; day<tt.Total_Days; day++) {
            int y_offset = day * (tt.Total_Rooms + 2) * 40;

            // Iterate through rooms
            for (int room=0; room<tt.Total_Rooms; room++) {
                int room_y_offset = y_offset+(40*room)+40;

                // Iterate through hours
                for (int time=0; time<tt.Hours_Per_Day; time++) {
                    int sid = tt.get_id(day, time, room);
                    if (sid == -1) continue;

                    int seshLength = details.Session_Details.get(sid).Session_Length;
                    String seshName = details.Session_Details.get(sid).Session_Name;

                    timetable_data.append(create_mxCell(mxCellID++, seshName, 80+(time*40), room_y_offset, seshLength*40, 40,
                            "rounded=0;whiteSpace=wrap;html=1;shadow=0;glass=0;sketch=0;strokeWidth=1;"));

                    time += seshLength - 1;
                }

            }
        }

        String complete_file_data = header + layout_data + timetable_data + footer;

        String filename = create_file(complete_file_data, name);
        if (filename == null) {
            System.err.println("Failed to save the XML file.");
        }

        return filename;
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

            // Decode the number before the comma
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

    // Takes a string and a filename, and creates a new file with the string as the contents
    private String create_file(String contents, String name) {
        String filename = "samples\\"+name+".xml";
        try {
            File newFile = new File(filename);
            if (newFile.createNewFile()) {
                System.out.println("File created: " + newFile.getName());
            } else {
                System.err.println("An XML file with this name already existed here. (Overwriting it.)");
            }

            FileWriter writer = new FileWriter(filename);
            writer.write(contents);
            writer.close();

        } catch (IOException e) {
            return null;
        }
        return filename;
    }

    // Creates a string representing an mxCell from certain values
    private String create_mxCell(int id, String text, int x, int y, int width, int height, String style) {
        return
        "        <mxCell id=\""+id+"\" value=\""+text+"\" style=\""+style+"\" vertex=\"1\" parent=\"1\">\n" +
        "          <mxGeometry x=\""+x+"\" y=\""+y+"\" width=\""+width+"\" height=\""+height+"\" as=\"geometry\" />\n" +
        "        </mxCell>\n";
    }

}
