////////////////////////////////////////////////////////////////////////////////
//
//  ADOBE SYSTEMS INCORPORATED
//  Copyright 2012 Adobe Systems Incorporated
//  All Rights Reserved.
//
//  NOTICE: Adobe permits you to use, modify, and distribute this file
//  in accordance with the terms of the license agreement accompanying it.
//
////////////////////////////////////////////////////////////////////////////////
package au.com.centrumsystems.hudson.plugin.buildpipeline.dashboard;

import au.com.centrumsystems.hudson.plugin.buildpipeline.DownstreamProjectGridBuilder;
import au.com.centrumsystems.hudson.plugin.buildpipeline.ProjectGridBuilder;
import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import hudson.plugins.view.dashboard.DashboardPortlet;

import java.util.ArrayList;
import java.util.List;

import org.kohsuke.stapler.DataBoundConstructor;

import au.com.centrumsystems.hudson.plugin.buildpipeline.BuildPipelineView;
import au.com.centrumsystems.hudson.plugin.buildpipeline.Strings;


/**
 * This class provides the entry point to use this plugin in the dashboard-plugin
 *
 * @author Ingo Richter (irichter@adobe.com)
 * @since 03/30/2012
 */
public class BuildPipelineDashboard extends DashboardPortlet {
    /**
     * @deprecated
     *      For backward compatibility. Back when we didn't have {@link #gridBuilder},
     *      this field stored the first job to display.
     */
    @Deprecated
    private String selectedJob;

    /** Controls the layout. */
    private ProjectGridBuilder gridBuilder;

    /**
     * noOfDisplayedBuilds.
     */
    private String noOfDisplayedBuilds;

    /**
     * a brief description of this portlet.
     */
    private String description;

    /**
     * URL for custom CSS file.
     */
    private String cssUrl;

    /**
     * Constructor
     *
     * @param name
     *            the name of this view
     * @param description
     *            a brief description of this view
     * @param gridBuilder
     *            controls the layout
     * @param noOfDisplayedBuilds
     *            how many builds will be displayed for this job
     */
    @DataBoundConstructor
    public BuildPipelineDashboard(final String name, final String description,
                                  final ProjectGridBuilder gridBuilder, final String noOfDisplayedBuilds) {
        super(name);
        this.description = description;
        this.gridBuilder = gridBuilder;
        this.noOfDisplayedBuilds = noOfDisplayedBuilds;
    }

    public ProjectGridBuilder getGridBuilder() {
        return gridBuilder;
    }

    public void setGridBuilder(ProjectGridBuilder gridBuilder) {
        this.gridBuilder = gridBuilder;
    }

    /**
     * @return
     *      always this.
     */
    protected Object readResolve() {
        if (gridBuilder == null && selectedJob != null) {
            gridBuilder = new DownstreamProjectGridBuilder(selectedJob);
            selectedJob = null;
        }
        return this;


    }

    public String getNoOfDisplayedBuilds() {
        return noOfDisplayedBuilds;
    }

    public void setNoOfDisplayedBuilds(final String noOfDisplayedBuilds) {
        this.noOfDisplayedBuilds = noOfDisplayedBuilds;
    }

    public String getSelectedJob() {
        return selectedJob;
    }

    public void setSelectedJob(final String selectedJob) {
        this.selectedJob = selectedJob;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public void setCssUrl(final String cssUrl) {
        this.cssUrl = cssUrl;
    }

    public String getCssUrl() {
        return cssUrl;
    }

    public BuildPipelineView getBuildPipelineView() {
        return new ReadOnlyBuildPipelineView(getDisplayName(), getDescription(), getGridBuilder(),
                getNoOfDisplayedBuilds(), false, getCssUrl());
    }

    /**
     * Extension point registration.
     */
    // TODO: create a class and use this code also in BuildPipelineView
    @Extension(optional = true)
    public static class BuildPipelineDashboardDescriptor extends Descriptor<DashboardPortlet> {

        @Override
        public String getDisplayName() {
            return Strings.getString("Portlet.BuildPipelineDashboardDescriptor");
        }

        /**
         * Display Job List Item in the Edit View Page
         *
         * @return ListBoxModel
         */
        public hudson.util.ListBoxModel doFillSelectedJobItems() {
            final hudson.util.ListBoxModel options = new hudson.util.ListBoxModel();
            for (final String jobName : Hudson.getInstance().getJobNames()) {
                options.add(jobName);
            }
            return options;
        }

        /**
         * Display No Of Builds Items in the Edit View Page
         *
         * @return ListBoxModel
         */
        public hudson.util.ListBoxModel doFillNoOfDisplayedBuildsItems() {
            final hudson.util.ListBoxModel options = new hudson.util.ListBoxModel();
            final List<String> noOfBuilds = new ArrayList<String>();
            noOfBuilds.add("1");
            noOfBuilds.add("2");
            noOfBuilds.add("3");
            noOfBuilds.add("5");
            noOfBuilds.add("10");
            noOfBuilds.add("20");
            noOfBuilds.add("50");
            noOfBuilds.add("100");
            noOfBuilds.add("200");
            noOfBuilds.add("500");

            for (final String noOfBuild : noOfBuilds) {
                options.add(noOfBuild);
            }
            return options;
        }
    }
}
