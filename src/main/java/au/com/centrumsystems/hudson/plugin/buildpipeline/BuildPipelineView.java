/*
 * The MIT License
 *
 * Copyright (c) 2011, Centrum Systems Pty Ltd
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
 *
 */
package au.com.centrumsystems.hudson.plugin.buildpipeline;

import com.google.common.collect.Iterables;
import hudson.Extension;
import hudson.model.Action;
import hudson.model.Item;
import hudson.model.ParameterValue;
import hudson.model.TaskListener;
import hudson.model.TopLevelItem;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Cause;
import hudson.model.Cause.UserIdCause;
import hudson.model.CauseAction;
import hudson.model.Descriptor.FormException;
import hudson.model.Hudson;
import hudson.model.ParametersAction;
import hudson.model.Run;
import hudson.model.User;
import hudson.model.View;
import hudson.model.ViewDescriptor;
import hudson.plugins.parameterizedtrigger.AbstractBuildParameters;
import hudson.util.LogTaskListener;
import hudson.util.ListBoxModel;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.bind.JavaScriptMethod;

import au.com.centrumsystems.hudson.plugin.buildpipeline.trigger.BuildPipelineTrigger;
import au.com.centrumsystems.hudson.plugin.util.BuildUtil;
import au.com.centrumsystems.hudson.plugin.util.ProjectUtil;

/**
 * This view displays the set of jobs that are related based on their upstream\downstream relationships as a pipeline. Each build pipeline
 * becomes a row on the view.
 *
 * @author Centrum Systems
 *
 */
public class BuildPipelineView extends View {

    /**
     * @deprecated
     *      For backward compatibility. Back when we didn't have {@link #gridBuilder},
     *      this field stored the first job to display.
     */
    @Deprecated
    private volatile String selectedJob;

    /** Builds {@link ProjectGrid} */
    private ProjectGridBuilder gridBuilder;

    /** noOfDisplayedBuilds. */
    private String noOfDisplayedBuilds;

    /** buildViewTitle. */
    private String buildViewTitle = ""; //$NON-NLS-1$

    /** consoleOutputLinkStyle. */
    private String consoleOutputLinkStyle = LinkStyle.LIGHTBOX;

    /** URL for custom CSS file */
    private String cssUrl = "";

    /** Indicates whether only the latest job will be triggered. **/
    private boolean triggerOnlyLatestJob;

    /** alwaysAllowManualTrigger. */
    private boolean alwaysAllowManualTrigger = true;

    /** showPipelineParameters. */
    private boolean showPipelineParameters = true;

    /** showPipelineParametersInHeaders */
    private boolean showPipelineParametersInHeaders;

    /**
     * Frequency at which the Build Pipeline Plugin updates the build cards in seconds
     */
    private int refreshFrequency = 3;

    /** showPipelineDefinitionHeader. */
    private boolean showPipelineDefinitionHeader;

    /*
     * Keep feature flag properties in one place so that it is easy to refactor them out later.
     */
    /* Feature flags - START */

    /* Feature flags - END */

    /** A Logger object is used to log messages */
    private static final Logger LOGGER = Logger.getLogger(BuildPipelineView.class.getName());

    /**
     * An instance of {@link Cause.UserIdCause} related to the current user. Must be transient, or xstream will include it in the
     * serialization
     */
    private class MyUserIdCause extends Cause.UserIdCause {
        /**
         * user
         */
        private User user;

        /**
         *
         */
        public MyUserIdCause() {
            try {
                // this block can generate a CyclicGraphDetector.CycleDetectedException
                // in cases that I haven't quite figured out yet
                // also an org.acegisecurity.AccessDeniedException when the user
                // is not logged in
                user = Hudson.getInstance().getMe();
            } catch (final Exception e) {
                // do nothing
                LOGGER.fine(e.getMessage());
            }
        }

        @Override
        public String getUserId() {
            return (null == user) ? null : user.getId();
        }

        @Override
        public String getUserName() {
            return (null == user) ? null : user.getDisplayName();
        }

        @Override
        public String toString() {
            return getUserName();
        }

        @Override
        public int hashCode() {
            if (getUserId() == null) {
                return super.hashCode();
            } else {
                return getUserId().hashCode();
            }
        }

        @Override
        public boolean equals(final Object o) {
            if (null == o) {
                return false;
            }
            if (!(o instanceof Cause.UserIdCause)) {
                return false;
            }

            return hashCode() == o.hashCode();
        }

        @Override
        public void print(final TaskListener listener) {
            // do nothing
        }
    }

    /**
     *
     * @param name
     *            the name of the pipeline build view.
     * @param buildViewTitle
     *            the build view title.
     * @param gridBuilder
     *            controls the data to be displayed.
     * @param noOfDisplayedBuilds
     *            a count of the number of builds displayed on the view
     * @param triggerOnlyLatestJob
     *            Indicates whether only the latest job will be triggered.
     * @param cssUrl
     *            URL for the custom CSS file.
     */
    public BuildPipelineView(final String name, final String buildViewTitle,
             final ProjectGridBuilder gridBuilder, final String noOfDisplayedBuilds,
             final boolean triggerOnlyLatestJob, final String cssUrl) {
        super(name, Hudson.getInstance());
        this.buildViewTitle = buildViewTitle;
        this.gridBuilder = gridBuilder;
        this.noOfDisplayedBuilds = noOfDisplayedBuilds;
        this.triggerOnlyLatestJob = triggerOnlyLatestJob;
        this.cssUrl = cssUrl;
    }

    /**
     *
     * @param name
     *            the name of the pipeline build view.
     * @param buildViewTitle
     *            the build view title.
     * @param gridBuilder
     *            controls the data to be displayed.
     * @param noOfDisplayedBuilds
     *            a count of the number of builds displayed on the view
     * @param triggerOnlyLatestJob
     *            Indicates whether only the latest job will be triggered.
     * @param alwaysAllowManualTrigger
     *            Indicates whether manual trigger will always be available.
     * @param showPipelineParameters
     *            Indicates whether pipeline parameter values should be shown.
     * @param showPipelineParametersInHeaders
     *            Indicates whether the pipeline headers should show the
     *            pipeline parameter values for the last successful instance.
     * @param showPipelineDefinitionHeader
     *            Indicates whether the pipeline headers should be shown.
     * @param refreshFrequency
     *            Frequency at which the build pipeline plugin refreshes build cards
     * @param cssUrl
     *            URL for the custom CSS file.
     */
    @DataBoundConstructor
    public BuildPipelineView(final String name, final String buildViewTitle, final ProjectGridBuilder gridBuilder,
            final String noOfDisplayedBuilds,
            final boolean triggerOnlyLatestJob, final boolean alwaysAllowManualTrigger, final boolean showPipelineParameters,
            final boolean showPipelineParametersInHeaders, final boolean showPipelineDefinitionHeader,
            final int refreshFrequency, final String cssUrl) {
        this(name, buildViewTitle, gridBuilder, noOfDisplayedBuilds, triggerOnlyLatestJob, cssUrl);
        this.alwaysAllowManualTrigger = alwaysAllowManualTrigger;
        this.showPipelineParameters = showPipelineParameters;
        this.showPipelineParametersInHeaders = showPipelineParametersInHeaders;
        this.showPipelineDefinitionHeader = showPipelineDefinitionHeader;
        //not exactly understanding the lifecycle here, but I want a default of 3
        //(this is what the class variable is set to 3, if it's 0, set it to default, refresh of 0 does not make sense anyway)
        if (refreshFrequency < 1) {
            this.refreshFrequency = 3;
        } else {
            this.refreshFrequency = refreshFrequency;
        }
    }

    /**
     * @return
     *      must be always 'this'
     */
    protected Object readResolve() {
        if (gridBuilder == null && selectedJob != null) {
            gridBuilder = new DownstreamProjectGridBuilder(selectedJob);
            selectedJob = null;
        }
        return this;
    }

    /**
     * Handles the configuration submission
     *
     * @param req
     *            Stapler Request
     * @throws FormException
     *             Form Exception
     * @throws IOException
     *             IO Exception
     * @throws ServletException
     *             Servlet Exception
     */
    @Override
    protected void submit(final StaplerRequest req) throws IOException, ServletException, FormException {
        req.bindJSON(this, req.getSubmittedForm());
    }

    /**
     * Checks whether the user has a permission to start a new instance of the pipeline.
     *
     * @return - true: Has Build permission; false: Does not have Build permission
     * @see hudson.model.Item
     */
    public boolean hasBuildPermission() {
        return getGridBuilder().hasBuildPermission(this);
    }

    /**
     * Checks whether the user has Configure permission for the current project.
     *
     * @return - true: Has Configure permission; false: Does not have Configure permission
     */
    public boolean hasConfigurePermission() {
        return this.hasPermission(CONFIGURE);
    }

    public ProjectGridBuilder getGridBuilder() {
        return gridBuilder;
    }

    public void setGridBuilder(ProjectGridBuilder gridBuilder) {
        this.gridBuilder = gridBuilder;
    }

    /**
     * Get a List of downstream projects.
     *
     * @param currentProject
     *            - The project from which we want the downstream projects
     * @return - A List of downstream projects
     */
    public List<AbstractProject<?, ?>> getDownstreamProjects(final AbstractProject<?, ?> currentProject) {
        return ProjectUtil.getDownstreamProjects(currentProject);
    }

    /**
     * Determines if the current project has any downstream projects
     *
     * @param currentProject
     *            - The project from which we are testing.
     * @return - true; has downstream projects; false: does not have downstream projects
     */
    public boolean hasDownstreamProjects(final AbstractProject<?, ?> currentProject) {
        return (getDownstreamProjects(currentProject).size() > 0);
    }

    /**
     * Returns BuildPipelineForm containing the build pipeline to display.
     *
     * @return - Representation of the projects and their related builds making up the build pipeline view
     * @throws URISyntaxException
     *             {@link URISyntaxException}
     */
    public BuildPipelineForm getBuildPipelineForm() throws URISyntaxException {
        final int maxNoOfDisplayBuilds = Integer.valueOf(noOfDisplayedBuilds);

        final ProjectGrid project = gridBuilder.build(this);
        if (project.isEmpty()) {
            return null;
        }
        return new BuildPipelineForm(
                project,
                Iterables.limit(project.builds(), maxNoOfDisplayBuilds));
    }

    /**
     * Retrieves the project URL
     *
     * @param project
     *            - The project
     * @return URL - of the project
     * @throws URISyntaxException
     * @throws URISyntaxException
     *             {@link URISyntaxException}
     */
    public String getProjectURL(final AbstractProject<?, ?> project) throws URISyntaxException {
        return project.getUrl();
    }

    /**
     * Trigger a manual build
     *
     * @param upstreamBuildNumber
     *            upstream build number
     * @param triggerProjectName
     *            project that is triggered
     * @param upstreamProjectName
     *            upstream project
     * @return next build number that has been scheduled
     */
    @JavaScriptMethod
    public int triggerManualBuild(final Integer upstreamBuildNumber, final String triggerProjectName, final String upstreamProjectName) {
        final AbstractProject<?, ?> triggerProject = (AbstractProject<?, ?>) super.getJob(triggerProjectName);
        final AbstractProject<?, ?> upstreamProject = (AbstractProject<?, ?>) super.getJob(upstreamProjectName);

        final AbstractBuild<?, ?> upstreamBuild = retrieveBuild(upstreamBuildNumber, upstreamProject);

        // Get parameters from upstream build
        if (upstreamBuild != null) {
            LOGGER.fine("Getting parameters from upstream build " + upstreamBuild.getExternalizableId()); //$NON-NLS-1$
        }
        Action buildParametersAction = null;
        if (upstreamBuild != null) {
            buildParametersAction = BuildUtil.getAllBuildParametersAction(upstreamBuild, triggerProject);
        }

        return triggerBuild(triggerProject, upstreamBuild, buildParametersAction);
    }

    /**
     * @param triggerProjectName
     *            the triggerProjectName
     * @return the number of re-tried build
     */
    @JavaScriptMethod
    public int retryBuild(final String triggerProjectName) {
        LOGGER.fine("Retrying build again: " + triggerProjectName); //$NON-NLS-1$
        final AbstractProject<?, ?> triggerProject = (AbstractProject<?, ?>) super.getJob(triggerProjectName);
        triggerProject.scheduleBuild(new MyUserIdCause());

        return triggerProject.getNextBuildNumber();
    }

    /**
     * @param externalizableId
     *            the externalizableId
     * @return the number of re-run build
     */
    @JavaScriptMethod
    public int rerunBuild(final String externalizableId) {
        LOGGER.fine("Running build again: " + externalizableId); //$NON-NLS-1$
        final AbstractBuild<?, ?> triggerBuild = (AbstractBuild<?, ?>) Run.fromExternalizableId(externalizableId);
        final AbstractProject<?, ?> triggerProject = triggerBuild.getProject();
        final Future<?> future = triggerProject.scheduleBuild2(triggerProject.getQuietPeriod(), new MyUserIdCause(),
                removeUserIdCauseActions(triggerBuild.getActions()));

        AbstractBuild<?, ?> result = triggerBuild;
        try {
            result = (AbstractBuild<?, ?>) future.get();
        } catch (final InterruptedException e) {
            e.printStackTrace();
        } catch (final ExecutionException e) {
            e.printStackTrace();
        }

        return result.getNumber();
    }

    /**
     * Given an AbstractProject and a build number the associated AbstractBuild will be retrieved.
     *
     * @param buildNo
     *            - Build number
     * @param project
     *            - AbstractProject
     * @return The AbstractBuild associated with the AbstractProject and build number.
     */
    @SuppressWarnings("unchecked")
    private AbstractBuild<?, ?> retrieveBuild(final int buildNo, final AbstractProject<?, ?> project) {
        AbstractBuild<?, ?> build = null;

        if (project != null) {
            for (final AbstractBuild<?, ?> tmpUpBuild : (List<AbstractBuild<?, ?>>) project.getBuilds()) {
                if (tmpUpBuild.getNumber() == buildNo) {
                    build = tmpUpBuild;
                    break;
                }
            }
        }

        return build;
    }

    /**
     * Schedules a build to start.
     *
     * The build will take an upstream build as its Cause and a set of ParametersAction from the upstream build.
     *
     * @param triggerProject
     *            - Schedule a build to start on this AbstractProject
     * @param upstreamBuild
     *            - The upstream AbstractBuild that will be used as a Cause for the triggerProject's build.
     * @param buildParametersAction
     *            - The upstream ParametersAction that will be used as an Action for the triggerProject's build.
     * @return next build number
     */
    private int triggerBuild(final AbstractProject<?, ?> triggerProject, final AbstractBuild<?, ?> upstreamBuild,
            final Action buildParametersAction) {
        LOGGER.fine("Triggering build for project: " + triggerProject.getFullDisplayName()); //$NON-NLS-1$
        final Cause.UpstreamCause upstreamCause = (null == upstreamBuild) ? null : new Cause.UpstreamCause((Run<?, ?>) upstreamBuild);
        final List<Action> buildActions = new ArrayList<Action>();
        final CauseAction causeAction = new CauseAction(new MyUserIdCause());
        // TODO hack obsolete as of 1.531 when CauseAction.<init>(Cause...) available:
        causeAction.getCauses().add(upstreamCause);
        buildActions.add(causeAction);
        ParametersAction parametersAction =
                buildParametersAction instanceof ParametersAction
                        ? (ParametersAction) buildParametersAction : new ParametersAction();

        if (upstreamBuild != null) {

            final BuildPipelineTrigger trigger = upstreamBuild.getProject().getPublishersList().get(BuildPipelineTrigger.class);

            final List<AbstractBuildParameters> configs = trigger.getConfigs();

            for (final AbstractBuildParameters config : configs) {
                try {
                    final Action action = config.getAction(upstreamBuild, new LogTaskListener(LOGGER, Level.INFO));
                    if (action instanceof ParametersAction) {
                        parametersAction = mergeParameters(parametersAction, (ParametersAction) action);
                    } else {
                        buildActions.add(action);
                    }
                } catch (final IOException e) {
                    LOGGER.log(Level.SEVERE, "I/O exception while adding build parameter", e); //$NON-NLS-1$
                } catch (final InterruptedException e) {
                    LOGGER.log(Level.SEVERE, "Adding build parameter was interrupted", e); //$NON-NLS-1$
                } catch (final AbstractBuildParameters.DontTriggerException e) {
                    LOGGER.log(Level.FINE, "Not triggering : " + config); //$NON-NLS-1$
                }
            }
        }

        buildActions.add(parametersAction);

        triggerProject.scheduleBuild(triggerProject.getQuietPeriod(), null, buildActions.toArray(new Action[buildActions.size()]));
        return triggerProject.getNextBuildNumber();
    }

    /**
     * From parameterized trigger plugin src/main/java/hudson/plugins/parameterizedtrigger/BuildTriggerConfig.java
     *
     * @param base
     *      One of the two parameters to merge.
     * @param overlay
     *      The other parameters to merge
     * @return
     *      Result of the merge.
     */
    private static ParametersAction mergeParameters(final ParametersAction base, final ParametersAction overlay) {
        final LinkedHashMap<String, ParameterValue> params = new LinkedHashMap<String, ParameterValue>();
        for (final ParameterValue param : base.getParameters()) {
            params.put(param.getName(), param);
        }
        for (final ParameterValue param : overlay.getParameters()) {
            params.put(param.getName(), param);
        }
        return new ParametersAction(params.values().toArray(new ParameterValue[params.size()]));
    }

    /**
     * Checks whether the given {@link Action} contains a reference to a {@link UserIdCause} object.
     *
     * @param buildAction
     *            the action to check.
     * @return <code>true</code> if the action has a reference to a userId cause.
     */
    private boolean isUserIdCauseAction(final Action buildAction) {
        boolean retval = false;
        if (buildAction instanceof CauseAction) {
            for (final Cause cause : ((CauseAction) buildAction).getCauses()) {
                if (cause instanceof UserIdCause) {
                    retval = true;
                    break;
                }
            }
        }
        return retval;
    }

    /**
     * Removes any UserId cause action from the given actions collection. This is used by downstream builds that inherit upstream actions.
     * The downstream build can be initiated by another user that is different from the user who initiated the upstream build, so the
     * downstream build needs to remove the old user action inherited from upstream, and add its own.
     *
     * @param actions
     *            a collection of build actions.
     * @return a collection of build actions with all UserId causes removed.
     */
    private List<Action> removeUserIdCauseActions(final List<Action> actions) {
        final List<Action> retval = new ArrayList<Action>();
        for (final Action action : actions) {
            if (!isUserIdCauseAction(action)) {
                retval.add(action);
            }
        }
        return retval;
    }

    /**
     * This descriptor class is required to configure the View Page
     *
     */
    @Extension
    public static final class DescriptorImpl extends ViewDescriptor {

        /**
         * descriptor impl constructor This empty constructor is required for stapler. If you remove this constructor, text name of
         * "Build Pipeline View" will be not displayed in the "NewView" page
         */
        public DescriptorImpl() {
            super();
        }

        /**
         * get the display name
         *
         * @return display name
         */
        @Override
        public String getDisplayName() {
            return Strings.getString("BuildPipelineView.DisplayText"); //$NON-NLS-1$
        }

        /**
         * Display No Of Builds Items in the Edit View Page
         *
         * @return ListBoxModel
         */
        public ListBoxModel doFillNoOfDisplayedBuildsItems() {
            final hudson.util.ListBoxModel options = new hudson.util.ListBoxModel();
            final List<String> noOfBuilds = new ArrayList<String>();
            noOfBuilds.add("1"); //$NON-NLS-1$
            noOfBuilds.add("2"); //$NON-NLS-1$
            noOfBuilds.add("3"); //$NON-NLS-1$
            noOfBuilds.add("5"); //$NON-NLS-1$
            noOfBuilds.add("10"); //$NON-NLS-1$
            noOfBuilds.add("20"); //$NON-NLS-1$
            noOfBuilds.add("50"); //$NON-NLS-1$
            noOfBuilds.add("100"); //$NON-NLS-1$
            noOfBuilds.add("200"); //$NON-NLS-1$
            noOfBuilds.add("500"); //$NON-NLS-1$

            for (final String noOfBuild : noOfBuilds) {
                options.add(noOfBuild);
            }
            return options;
        }

        /**
         * Display Console Output Link Style Items in the Edit View Page
         *
         * @return ListBoxModel
         */
        public ListBoxModel doFillConsoleOutputLinkStyleItems() {
            final hudson.util.ListBoxModel options = new hudson.util.ListBoxModel();
            options.add(LinkStyle.LIGHTBOX);
            options.add(LinkStyle.NEW_WINDOW);
            options.add(LinkStyle.THIS_WINDOW);
            return options;
        }
    }

    public String getBuildViewTitle() {
        return buildViewTitle;
    }

    public void setBuildViewTitle(final String buildViewTitle) {
        this.buildViewTitle = buildViewTitle;
    }

    public String getCssUrl() {
        return cssUrl;
    }

    public void setCssUrl(final String cssUrl) {
        this.cssUrl = cssUrl;
    }

    public String getNoOfDisplayedBuilds() {
        return noOfDisplayedBuilds;
    }

    public void setNoOfDisplayedBuilds(final String noOfDisplayedBuilds) {
        this.noOfDisplayedBuilds = noOfDisplayedBuilds;
    }

    public String getConsoleOutputLinkStyle() {
        return consoleOutputLinkStyle;
    }

    public void setConsoleOutputLinkStyle(String consoleOutputLinkStyle) {
        this.consoleOutputLinkStyle = consoleOutputLinkStyle;
    }

    public boolean isNewWindowConsoleOutputLinkStyle() {
        return LinkStyle.NEW_WINDOW.equals(consoleOutputLinkStyle);
    }

    public boolean isThisWindowConsoleOutputLinkStyle() {
        return LinkStyle.THIS_WINDOW.equals(consoleOutputLinkStyle);
    }

    public boolean isTriggerOnlyLatestJob() {
        return triggerOnlyLatestJob;
    }

    public String getTriggerOnlyLatestJob() {
        return Boolean.toString(triggerOnlyLatestJob);
    }

    public void setTriggerOnlyLatestJob(final boolean triggerOnlyLatestJob) {
        this.triggerOnlyLatestJob = triggerOnlyLatestJob;
    }

    public boolean isAlwaysAllowManualTrigger() {
        return alwaysAllowManualTrigger;
    }

    public String getAlwaysAllowManualTrigger() {
        return Boolean.toString(alwaysAllowManualTrigger);
    }

    public void setAlwaysAllowManualTrigger(final boolean alwaysAllowManualTrigger) {
        this.alwaysAllowManualTrigger = alwaysAllowManualTrigger;
    }

    public boolean isShowPipelineParameters() {
        return showPipelineParameters;
    }

    public String getShowPipelineParameters() {
        return Boolean.toString(showPipelineParameters);
    }

    public void setShowPipelineParameters(final boolean showPipelineParameters) {
        this.showPipelineParameters = showPipelineParameters;
    }

    public boolean isShowPipelineParametersInHeaders() {
        return showPipelineParametersInHeaders;
    }

    public String getShowPipelineParametersInHeaders() {
        return Boolean.toString(showPipelineParametersInHeaders);
    }

    public void setShowPipelineParametersInHeaders(final boolean showPipelineParametersInHeaders) {
        this.showPipelineParametersInHeaders = showPipelineParametersInHeaders;
    }

    public int getRefreshFrequency() {
        return refreshFrequency;
    }

    public void setRefreshFrequency(final int refreshFrequency) {
        this.refreshFrequency = refreshFrequency;
    }

    public int getRefreshFrequencyInMillis() {
        return refreshFrequency * 1000;
    }

    public boolean isShowPipelineDefinitionHeader() {
        return showPipelineDefinitionHeader;
    }

    public String getShowPipelineDefinitionHeader() {
        return Boolean.toString(showPipelineDefinitionHeader);
    }

    public void setShowPipelineDefinitionHeader(final boolean showPipelineDefinitionHeader) {
        this.showPipelineDefinitionHeader = showPipelineDefinitionHeader;
    }

    @Override
    public Collection<TopLevelItem> getItems() {
        return Hudson.getInstance().getItems();
    }

    @Override
    public boolean contains(final TopLevelItem item) {
        return this.getItems().contains(item);
    }

    /**
     * If a project name is changed we check if the selected job for this view also needs to be changed.
     *
     * @param item
     *            - The Item that has been renamed
     * @param oldName
     *            - The old name of the Item
     * @param newName
     *            - The new name of the Item
     *
     */
    @Override
    public void onJobRenamed(final Item item, final String oldName, final String newName) {
        LOGGER.fine(String.format("Renaming job: %s -> %s", oldName, newName));
        try {
            gridBuilder.onJobRenamed(this, item, oldName, newName);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to handle onJobRenamed", e);
        }
    }

    @Override
    public Item doCreateItem(final StaplerRequest req, final StaplerResponse rsp) throws IOException, ServletException {
        return Hudson.getInstance().doCreateItem(req, rsp);
    }

    /**
     * A class that groups together the console output link style options
     */
    private static final class LinkStyle {
        /** lightbox link style option */
        static final String LIGHTBOX = "Lightbox"; //$NON-NLS-1$
        /** new window link style option */
        static final String NEW_WINDOW = "New Window"; //$NON-NLS-1$
        /** this window link style option */
        static final String THIS_WINDOW = "This Window"; //$NON-NLS-1$
    }
}
