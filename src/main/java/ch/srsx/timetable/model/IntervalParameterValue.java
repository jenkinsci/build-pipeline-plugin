/*
 * The MIT License
 * 
 * Copyright (c) 2004-2009, Sun Microsystems, Inc., Kohsuke Kawaguchi, Luca Domenico Milanesio, Tom Huybrechts
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package ch.srsx.timetable.model;

import hudson.EnvVars;
import hudson.model.ParameterValue;
import hudson.model.AbstractBuild;
import hudson.util.VariableResolver;

import java.util.Locale;

import org.joda.time.Interval;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.export.Exported;

import au.com.centrumsystems.hudson.plugin.util.ScheduleUtil;

/**
 * {@link ParameterValue} created from {@link IntervalParameterDefinition}.
 * 
 * @author rhirt
 */
public class IntervalParameterValue extends ParameterValue {
    /** The serial id */
    private static final long serialVersionUID = 1L;

    /** the interval in question */
    @Exported(visibility = 4)
    private final Interval value;

    /**
     * Constructor taking name and value.
     * 
     * @param name
     *            the name
     * @param value
     *            the value, given as a formatted String (HH:mm - HH:mm)
     */
    @DataBoundConstructor
    public IntervalParameterValue(String name, String value) {
        this(name, value, null);
    }

    /**
     * Constructor taking name, value and description.
     * 
     * @param name
     *            the name
     * @param value
     *            the value, given as a formatted String (HH:mm - HH:mm)
     * @param description
     *            the description
     */
    public IntervalParameterValue(String name, String value, String description) {
        super(name, description);
        this.value = ScheduleUtil.parseInterval(value);
    }

    /**
     * Constructor taking name, interval and description.
     * 
     * @param name
     *            the name
     * @param interval
     *            the value, given as an interval object
     * @param description
     *            the description
     */
    public IntervalParameterValue(String name, Interval interval, String description) {
        super(name, description);
        this.value = interval;
    }


    @Override
    public void buildEnvVars(AbstractBuild<?, ?> build, EnvVars env) {
        env.put(name, value.toString());
        env.put(name.toUpperCase(Locale.ENGLISH), value.toString());
    }

    @Override
    public VariableResolver<String> createVariableResolver(AbstractBuild<?, ?> build) {
        return new VariableResolver<String>() {
            public String resolve(String name) {
                return IntervalParameterValue.this.name.equals(name) ? ScheduleUtil.formatInterval(value) : null;
            }
        };
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final IntervalParameterValue other = (IntervalParameterValue) obj;
        if (value == null) {
            if (other.value != null) {
                return false;
            }
        } else if (!value.equals(other.value)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "(IntervalParameterValue) " + getName() + "='" + value.toString() + "'";
    }

    @Override
    public String getShortDescription() {
        return name + '=' + ScheduleUtil.formatInterval(value);
    }

    public Interval getValue() {
        return value;
    }
}
