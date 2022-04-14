package uk.ac.cam.agb67.dissertation;

import org.chocosolver.solver.constraints.nary.nvalue.amnv.differences.D;

import java.util.List;

public class TimetableSatisfactionMeasurer {

    boolean DEBUG = false;

    public TimetableSatisfactionMeasurer() {}
    public TimetableSatisfactionMeasurer(boolean debug) { DEBUG = debug; }

    // Returns a score between 0 and 100 indicating how well the preferences for a scheduling problem are satisfied by the given timetable
    public int timetable_preference_satisfaction(Timetable tt, SchedulingProblem details) {
        // Note: This class assumes all provided timetables are valid for the given scheduling problems

        if (DEBUG) System.out.println("Global Preferences: Min_Gap="+details.Minimum_Gap_Pref+" Overlap?="+details.Reduce_Overlap_Pref+".");

        // Obtain the individuals scores
        int gap_score = gap_preference_satisfaction(tt, details);
        int overlap_score = overlap_preference_satisfaction(tt, details);
        int room_score = room_preference_satisfaction(tt, details);
        int limit_score = limit_preference_satisfaction(tt, details);

        if (DEBUG) System.out.println("\nThe preference satisfaction metrics for the given timetable:\n(Overall) Gap Pref: "+ gap_score+"\n(Overall) Overlap Pref:" +
                " "+ overlap_score+" \n(Individuals') Room Pref: "+ room_score+" \n(Individuals') Daily Limit Pref: "+ limit_score+"\n");

        // Combine the scores
        int score = (gap_score + overlap_score + room_score + limit_score) / 4;

        return score;
    }

    // Returns a score between 0 and 100 indicating how well the gap preference has been met
    int gap_preference_satisfaction(Timetable tt, SchedulingProblem details) {
        // (#"gaps longer than or eqto pref" / #"sessions") * 100
        int minimum = details.Minimum_Gap_Pref;
        int gaps_over_min = 0;

        // Iterate through all timetable slots
        for (int r = 0; r < tt.Total_Rooms; r++) {
            for (int d = 0; d < tt.Total_Days; d++) {
                // Trace through the hours of the day tracking gaps between sessions
                int gap = -1000000000;
                int previous_sid = -1;

                for (int h = 0; h < tt.Hours_Per_Day; h++) {
                    if (tt.get_id(d, h, r) == -1) {
                        // A gap starts or continues
                        gap += 1;
                    } else if (tt.get_id(d, h, r) != -1 && previous_sid == -1) {
                        // We reach the end of a gap
                        if (gap >= minimum) gaps_over_min += 1;
                        gap = 0;
                    } else if (tt.get_id(d, h, r) != -1 && previous_sid != tt.get_id(d, h, r)) {
                        // There are two sessions back to back with no gap
                        if (minimum == 0) gaps_over_min += 1;
                        gap = 0;
                    } else if (tt.get_id(d, h, r) != -1 && previous_sid == tt.get_id(d, h, r)) {
                        // A session continues (no effect)
                        gap = 0;
                    }

                    // Update previous session id
                    previous_sid = tt.get_id(d, h, r);
                }

                // Add a gap over min for any session which was not followed by another session, e.g. last session before the end of the day
                if (gap > 0 || previous_sid != -1) {
                    gaps_over_min += 1;
                }

            }
        }

        // Calculate and return the score
        if (DEBUG) System.out.println("Calculating Gap Score: #gaps_longer_than_pref="+gaps_over_min+" / #sessions="+details.Session_Details.size());
        return ((100 * gaps_over_min) / details.Session_Details.size());
    }

    // Returns a score between 0 and 100 indicating how well the overlap preference has been met
    int overlap_preference_satisfaction(Timetable tt, SchedulingProblem details) {
        // ((([Sum for X] #"sessions at time X") / #"time-slots with any booked session") / #"rooms") * 100
        int total_booked = 0;
        int timeslots_with_booking = 0;

        // Iterate through all timetable slots
        for (int d = 0; d < tt.Total_Days; d++) {
            for (int h = 0; h < tt.Hours_Per_Day; h++) {
                // Iterate through the rooms and count how many sessions are booked
                int booked = 0;
                for (int r = 0; r < tt.Total_Rooms; r++) {
                    if (tt.get_id(d, h, r) != -1) booked += 1;
                }
                // Update the total bookings, and if there were any then increment the number of timeslots with a booking
                if (booked > 0) timeslots_with_booking += 1;
                total_booked += booked;
            }
        }

        // Calculate and return the score
        if (DEBUG) System.out.println("Calculating Overlap Score: (#total_hours_booked="+total_booked+" / #timeslots_with_one_booking="+timeslots_with_booking+") / " +
                "#rooms="+tt.Total_Rooms);
        int overlap_score = ((100 * total_booked / timeslots_with_booking) / tt.Total_Rooms);

        if (details.Reduce_Overlap_Pref) {
            if (DEBUG) System.out.println("Recalculating Overlap Score: 100 - "+overlap_score);
            return 100 - overlap_score;
        } else {
            return overlap_score;
        }
    }

    // Returns a score between 0 and 100 indicating how well individuals' room preferences have been met
    int room_preference_satisfaction(Timetable tt, SchedulingProblem details) {
        // [Sum for X] (#"participants in session X who were satisfied with room"/ #"participants in session X") / #"sessions" * 100

        double[] session_ratios = new double[details.Session_Details.size()];

        // Iterate through all timetable slots
        for (int r = 0; r < tt.Total_Rooms; r++) {
            for (int d = 0; d < tt.Total_Days; d++) {
                for (int h = 0; h < tt.Hours_Per_Day; h++) {

                    int sid = tt.get_id(d, h, r);
                    if (sid == -1) continue;

                    // Iterate through every participant and count those who were satisfied with the room
                    double satisfied_individuals = 0;
                    List<Integer> KeyIDs = details.Session_Details.get(sid).Session_KeyInds;

                    for (int KeyID : KeyIDs) {
                        // Determine if this participant had their preference met
                        List<Integer> room_prefs = details.KeyInd_Details.get(KeyID).KeyInd_Room_Prefs;
                        if (room_prefs.isEmpty() || room_prefs.contains(r)) satisfied_individuals+= 1;
                    }

                    // Divide the count by the total number of participants and record the ratio
                    session_ratios[sid] = satisfied_individuals / (double)KeyIDs.size();
                    // Forcibly advance the loop
                    h += details.Session_Details.get(sid).Session_Length;
                }
            }
        }

        // Calculate the score by averaging the ratios
        if (DEBUG) System.out.print("Calculating Room Pref Score: #satisfied_participants / #participants per session: [");
        double total = 0;
        for (double ratio : session_ratios) {
            if (DEBUG) System.out.print(" "+ratio+",");
            total += ratio;
        }
        if (DEBUG) System.out.println("]\n Then (#total_ratios="+total+" / #sessions="+details.Session_Details.size()+")");
        return (int) (100 * (total / details.Session_Details.size()));
    }

    // Returns a score between 0 and 100 indicating how well individuals' daily limit preferences have been met
    int limit_preference_satisfaction(Timetable tt, SchedulingProblem details) {
        // [Sum for X] (#"hours over their limit for individual X" / #"possible hours over limit for ind X") / #"individuals" * 100

        int[] hours_over_limit = new int[details.KeyInd_Details.size()];

        // Iterate through the days
        for (int d = 0; d < tt.Total_Days; d++) {
            int[] hours_today = new int[details.KeyInd_Details.size()];

            // Iterate through the time-slots in the day
            for (int r = 0; r < tt.Total_Rooms; r++) {
                for (int h = 0; h < tt.Hours_Per_Day; h++) {

                    int sid = tt.get_id(d, h, r);
                    if (sid == -1) continue;

                    // Iterate through every participant and add to their hours today
                    List<Integer> KeyIDs = details.Session_Details.get(sid).Session_KeyInds;
                    for (int KeyID : KeyIDs) {
                        hours_today[KeyID] += 1;
                    }

                }
            }

            // Iterate through all individuals and add their over-limit hours from this day
            for (int KeyID=0; KeyID < details.KeyInd_Details.size(); KeyID++) {
                hours_over_limit[KeyID] += Math.max(0, hours_today[KeyID] - details.KeyInd_Details.get(KeyID).KeyInd_Daily_Limit_Pref);
            }
        }

        // Find each individuals total potential overspill (their overall hours - their daily limit)
        int[] potential_overspill = new int[details.KeyInd_Details.size()];
        for (int KeyID=0; KeyID < details.KeyInd_Details.size(); KeyID++) {
            int overall_hours = 0;
            for (Session s : details.Session_Details) {
                if (s.Session_KeyInds.contains(KeyID)) overall_hours += s.Session_Length;
            }
            potential_overspill[KeyID] = overall_hours - details.KeyInd_Details.get(KeyID).KeyInd_Daily_Limit_Pref;
        }

        // Then calculate the score by averaging these ratios
        if (DEBUG) System.out.print("Calculating Daily Limit Pref Score: #hours_over_limit / #possible_hours_over_limit per individual: \n [");
        double total = 0;
        int excemptions = 0;
        for (int KeyID=0; KeyID < details.KeyInd_Details.size(); KeyID++) {
            if (DEBUG) System.out.print(" "+details.KeyInd_Details.get(KeyID).KeyInd_Name+" "+((double)hours_over_limit[KeyID] / (double)potential_overspill[KeyID])+" <"+(double)hours_over_limit[KeyID]+
                    "/"+(double)potential_overspill[KeyID]+">,");
            if (potential_overspill[KeyID] == 0) {
                excemptions += 1;
            } else {
                total += (double) hours_over_limit[KeyID] / (double) potential_overspill[KeyID];
            }
        }
        if (DEBUG) System.out.println("]\n Then (#total_ratios="+total+" / #individuals="+(details.KeyInd_Details.size() - excemptions)+")");
        return (int) (100 * (total / (details.KeyInd_Details.size() - excemptions)));
    }



}
