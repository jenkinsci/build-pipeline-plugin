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

import au.com.centrumsystems.hudson.plugin.buildpipeline.BuildPipelineView;
import au.com.centrumsystems.hudson.plugin.buildpipeline.ProjectGridBuilder;
import hudson.security.Permission;

/**
 * This class provides a read-only view for the existing build-pipeline view. All calls checking permissions return false. The other reason
 * for this class is that it's used in a different context and not as a child of the view tab.
 *
 * @author Ingo Richter (irichter@adobe.com)
 * @since 04/01/2012
 */
public class ReadOnlyBuildPipelineView extends BuildPipelineView {
    /**
     *
     * @param displayName
     *            display name of build pipeline view
     * @param description
     *            description of build pipeline view
     * @param gridBuilder
     *            controls the data to be displayed.
     * @param noOfDisplayedBuilds
     *            number of displayed build of build pipeline view
     * @param triggerOnlyLatestJob
     *            is trigger only latest job?
     * @param cssUrl
     *            URL for the custom CSS file.
     */
    public ReadOnlyBuildPipelineView(final String displayName, final String description, final ProjectGridBuilder gridBuilder,
            final String noOfDisplayedBuilds, final boolean triggerOnlyLatestJob, final String cssUrl, final boolean startsWithParameters) {
        super(displayName, displayName, gridBuilder, noOfDisplayedBuilds, triggerOnlyLatestJob, cssUrl, startsWithParameters);
        // this is ugly, but there is no other way to set the description of the view
        super.description = description;
    }

    @Override
    public boolean hasBuildPermission() {
        // we are not a 'real view' in this case and we don't care in R/O mode
        return false;
    }

    @Override
    public boolean hasPermission(final Permission p) {
        return false;
    }
}
