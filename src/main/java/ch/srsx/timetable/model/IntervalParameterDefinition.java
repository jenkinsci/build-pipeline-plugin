package ch.srsx.timetable.model;

import hudson.Extension;
import hudson.model.ParameterValue;
import hudson.model.ParameterDefinition;
import hudson.util.FormValidation;

import java.util.Date;

import net.sf.json.JSONObject;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.export.Exported;

import au.com.centrumsystems.hudson.plugin.util.ScheduleUtil;
import ch.srsx.timetable.Messages;

/**
 * @author rhirt
 */
public class IntervalParameterDefinition extends ParameterDefinition {
    /** Serial ID */
    private static final long serialVersionUID = -8142303813745641667L;

    /** Default value */
    private Interval defaultValue;

    /**
     * Constructor requesting name, startTime, endTime and description
     * 
     * @param name
     *            the name
     * @param startTime
     *            - string in the format HH:mm for the start of the interval
     * @param endTime
     *            - string in the format HH:mm for the end of the interval
     * @param description
     *            the description
     */
    @DataBoundConstructor
    public IntervalParameterDefinition(String name, String startTime, String endTime, String description) {
        super(name, description);
        this.defaultValue = new Interval(ScheduleUtil.parseTime(startTime), ScheduleUtil.parseTime(endTime));
    }

    /**
     * Constructor requesting name, interval and description
     * 
     * @param name
     *            the name
     * @param value
     *            - interval to derive this interval from
     * @param description
     *            the description
     */
    public IntervalParameterDefinition(String name, Interval value, String description) {
        super(name, description);
        this.defaultValue = value;
    }

    @Override
    public ParameterDefinition copyWithDefaultValue(ParameterValue defaultValue) {
        if (defaultValue instanceof IntervalParameterValue) {
            final IntervalParameterValue value = (IntervalParameterValue) defaultValue;
            return new IntervalParameterDefinition(getName(), value.getValue(), getDescription());
        } else {
            return this;
        }
    }

    /**
     * @return String - text representation of the time value at the start of
     *         the interval
     */
    public String getFormattedInterval() {
        return ScheduleUtil.formatInterval(defaultValue);
    }

    /**
     * @return Date - the start of the interval as a date
     */
    @Exported
    public Date getIntervalFrom() {
        if (defaultValue == null) {
            return null;
        }
        return new Date(defaultValue.getStartMillis());
    }

    /**
     * @return String - text representation of the time value at the start of
     *         the interval
     */
    public String getIntervalFromText() {
        return ScheduleUtil.formatStartTime(defaultValue);
    }

    /**
     * @return Date - the end of the interval as a date
     */
    @Exported
    public Date getIntervalTo() {
        if (defaultValue == null) {
            return null;
        }
        return new Date(defaultValue.getEndMillis());
    }

    /**
     * @return String - text representation of the time value at the end of the
     *         interval
     */
    public String getIntervalToText() {
        return ScheduleUtil.formatEndTime(defaultValue);
    }

    @Override
    public IntervalParameterValue getDefaultParameterValue() {
        return new IntervalParameterValue(getName(), defaultValue, getDescription());
    }

    @Override
    public ParameterValue createValue(StaplerRequest req) {
        // TODO Auto-generated method stub
        // FileItem src;
        // try {
        // src = req.getFileItem( getName() );
        // } catch (ServletException e) {
        // // Not sure what to do here. We might want to raise this
        // // but that would involve changing the throws for this call
        // return null;
        // } catch (IOException e) {
        // // ditto above
        // return null;
        // }
        // if ( src == null ) {
        // // the requested file parameter wasn't uploaded
        // return null;
        // }
        // FileParameterValue p = new FileParameterValue(getName(), src,
        // getFileName(src.getName()));
        // p.setDescription(getDescription());
        // p.setLocation(getName());
        // return p;
        return null;
    }

    @Override
    public ParameterValue createValue(StaplerRequest req, JSONObject jo) {
        final IntervalParameterValue value = req.bindJSON(IntervalParameterValue.class, jo);
        value.setDescription(getDescription());
        return value;
    }

    /**
     * @param value
     *            the value
     * @return the interval parameter value object created
     */
    public IntervalParameterValue createValue(Interval value) {
        return new IntervalParameterValue(getName(), value, getDescription());
    }

    /**
     * DescriptorImpl for this parameter definition
     * 
     * @author rhirt
     * 
     */
    @Extension
    public static class DescriptorImpl extends ParameterDescriptor {
        @Override
        public String getDisplayName() {
            return Messages.IntervalParameterDefinition_DisplayName();
        }

        @Override
        public String getHelpFile() {
            return "/plugin/build-pipeline-plugin/help/parameter/interval.html";
        }

        /**
         * Checks if parameterised build intervals are valid.
         * 
         * @param value
         *            - the value to check
         * @return FormValidation - status whether validation succeeded
         */
        public FormValidation doCheckTime(@QueryParameter String value) {
            if (ScheduleUtil.parseTime(value) == null) {
                return FormValidation.error(Messages.IntervalParameterDefinition_IllegalTime());
            }
            return FormValidation.ok();
        }

        /**
         * Checks if parameterised build intervals are valid.
         * 
         * @param fromValue
         *            - the value (from) to check
         * @param toValue
         *            - the value (to) to check
         * @return FormValidation - status whether validation succeeded
         */
        public FormValidation doCheckInterval(@QueryParameter String fromValue, @QueryParameter String toValue) {
            final DateTime from = ScheduleUtil.parseTime(fromValue);
            final DateTime to = ScheduleUtil.parseTime(toValue);
            if (from == null || to == null) {
                return FormValidation.error(Messages.IntervalParameterDefinition_IllegalTime());
            } else if (!from.isBefore(to)) {
                return FormValidation.error(Messages.IntervalParameterDefinition_IllegalInterval());
            }
            return FormValidation.ok();
        }
    }
}