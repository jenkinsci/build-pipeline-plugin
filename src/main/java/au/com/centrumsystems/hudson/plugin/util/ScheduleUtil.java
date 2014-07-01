package au.com.centrumsystems.hudson.plugin.util;

import hudson.model.ParameterValue;
import hudson.model.ParametersAction;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import ch.srsx.timetable.model.IntervalParameterValue;

/**
 * Utitily class to help scheduling Jobs in the context of the build-pipeline
 * plugin.
 * 
 * @author rhirt
 * 
 */
public final class ScheduleUtil {

    /** A Logger object is used to log messages */
    private static final Logger LOGGER = Logger.getLogger(ScheduleUtil.class.getName());

    /** The format used for specifying time */
    private static DateTimeFormatter fmt = DateTimeFormat.forPattern("HH:mm");

    /** Delimiter for time-range on one line */
    public static final String TIMES_DELIMITER = "-";

    /** Define a comparator for intervals */
    public static class IntervalComparator implements Comparator<Interval>, Serializable {
        /**
         * Serial ID
         */
        private static final long serialVersionUID = -1905472266853377782L;

        @Override
        public int compare(Interval o1, Interval o2) {
            return o1.getStart().compareTo(o2.getStart());
        }
    }

    /**
     * Calculate a delay, depending on the windows defined for the job.
     * 
     * @param action
     *            - the action
     * @return int - the calculated delay for the job-execution
     */
    public static int calcDelay(ParametersAction action) {

        // Check for instances of IntervalParameterValue on the job-definition
        if (action == null || (!hasIntervalParameterValues(action))) {
            LOGGER.info("No action or intervals present, delay 0");
            return 0;
        }

        // Now, determine the deployment-windows on the job
        // Derive it from the IntervalParameterValues:
        final List<Interval> intervals = getIntervals(action);

        // Check the intervals for a match
        final DateTime now = new DateTime();
        final int nowOfDay = now.getMillisOfDay();
        for (Interval interval : intervals) {
            // check for interval being after now
            if (nowOfDay < interval.getStart().getMillisOfDay()) {
                // the interval is after now, take the delay from there and
                // return
                final int delay = (interval.getStart().getMillisOfDay() - nowOfDay) / 1000;
                LOGGER.info("Now before the next interval, delay is " + delay);
                return delay;
            } else {
                // check for interval containing now
                if (nowOfDay >= interval.getStart().getMillisOfDay() && nowOfDay < interval.getEnd().getMillisOfDay()) {
                    // now is inside an interval, no delay necessary
                    LOGGER.info("Now inside an interval, delay is 0");
                    return 0;
                }
            }
        }

        // We are after the last interval, but have at least one interval
        // now take the start of the first interval and add a day to it,
        // than take the time from then - now
        final int delayNextDay = intervals.get(0).getStart().getMillisOfDay();
        final int delay = (int) (delayNextDay + 86400000 - nowOfDay) / 1000;
        LOGGER.info("Now before the first interval next day, delay is " + delay);
        return delay;
    }

    /**
     * Read the provided intervals from the action of the job
     * 
     * @param action
     *            the action
     * @return List<Interval> - the list of the intervals read from the action,
     *         sorted ascending
     */
    private static List<Interval> getIntervals(ParametersAction action) {
        final List<Interval> intervals = new ArrayList<Interval>();
        final List<ParameterValue> pvs = action.getParameters();
        for (ParameterValue pv : pvs) {
            if (pv instanceof IntervalParameterValue) {
                intervals.add(((IntervalParameterValue) pv).getValue());
            }
        }
        Collections.sort(intervals, new ScheduleUtil.IntervalComparator());
        return intervals;
    }

    /**
     * Parse a given time of the format HH:mm and convert it into a DateTime
     * today
     * 
     * @param value
     *            - the value to parse
     * @return DateTime - the parsed time, moved into today
     * */
    public static DateTime parseTime(String value) {
        final DateTime today = new DateTime();
        try {
            return fmt.parseDateTime(value.trim()).withDate(today.getYear(), today.getMonthOfYear(), today.getDayOfMonth());
        } catch (IllegalArgumentException iaex) {
            LOGGER.warning("Time could not be parsed: " + value);
        }
        return null;
    }

    /**
     * Parse the given String to create an interval from a compact
     * representation in the form (HH:mm - HH:mm)
     * 
     * @param value
     *            - the value in the format HH:mm - HH:mm
     * @return Interval - the interval parsed
     */
    public static Interval parseInterval(String value) {
        final String[] timeValues = value.split(TIMES_DELIMITER);
        if (timeValues.length == 2) {
            return new Interval(parseTime(timeValues[0]), parseTime(timeValues[1]));
        }
        return null;
    }

    /**
     * Format the start time of the given interval in the form (HH:mm)
     * 
     * @param value
     *            - the interval from which the start-time will be formatted
     * @return String - the formatted String
     */
    public static String formatStartTime(Interval value) {
        if (value == null) {
            return "";
        }
        return fmt.print(value.getStart());
    }

    /**
     * Format the end time of the given interval in the form (HH:mm)
     * 
     * @param value
     *            - the interval from which the end-time will be formatted
     * @return String - the formatted String
     */
    public static String formatEndTime(Interval value) {
        if (value == null) {
            return "";
        }
        return fmt.print(value.getEnd());
    }

    /**
     * Format the given interval for a compact representation in the form (HH:mm
     * - HH:mm)
     * 
     * @param value
     *            - the interval to be formatted
     * @return String - the formatted String
     */
    public static String formatInterval(Interval value) {
        if (value == null) {
            return "";
        }
        return fmt.print(value.getStart()) + TIMES_DELIMITER + fmt.print(value.getEnd());
    }

    /**
     * Check whether an IntervalParameterValue is present on the job
     * 
     * @param action
     *            - the action
     * @return boolean - whether IntervalParameterValue(s) are present
     */
    private static boolean hasIntervalParameterValues(ParametersAction action) {
        final List<ParameterValue> pvs = action.getParameters();
        for (ParameterValue pv : pvs) {
            if (pv instanceof IntervalParameterValue) {
                return true;
            }
        }
        return false;
    }
}
