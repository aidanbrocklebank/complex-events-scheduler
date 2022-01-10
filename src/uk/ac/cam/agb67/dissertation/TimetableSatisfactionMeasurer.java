package uk.ac.cam.agb67.dissertation;

public class TimetableSatisfactionMeasurer {

    // Numeric Model for the Preference Satisfaction Metrics:

    // Minimum Gaps
    // (#"gaps shorter than pref" / #"sessions") * 100

    // Overlap Reduction
    // (([Sum for X] #"sessions at time X") / #"time-slots with any booked session") * 100

    // Room Preferences
    // SIMPLE: (#"session with at least one participant satisfied with room"/#"sessions") * 100
    // COMPLEX: [Sum for X] (#"participants in session X who were satisfied with room"/ #"participants in session X") / #"sessions" * 100

    // Personal Daily Limits
    // SIMPLE: (#"individuals who were booked for more than their limit" / #"individuals") * 100
    // COMPLEX: ([Sum for X] #"hours over their limit for individual X") / #"individuals" * 100

    // TODO implement this class to measure these metrics for a given Schedule & set of Details


}
