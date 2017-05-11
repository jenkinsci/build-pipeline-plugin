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

import au.com.centrumsystems.hudson.plugin.buildpipeline.extension.NullColumnHeader;
import au.com.centrumsystems.hudson.plugin.buildpipeline.extension.PipelineHeaderExtension;
import au.com.centrumsystems.hudson.plugin.buildpipeline.extension.BuildVariablesHeader;
import au.com.centrumsystems.hudson.plugin.buildpipeline.extension.PipelineHeaderExtensionDescriptor;
import au.com.centrumsystems.hudson.plugin.buildpipeline.extension.SimpleColumnHeader;
import au.com.centrumsystems.hudson.plugin.buildpipeline.extension.SimpleRowHeader;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;

import com.google.common.collect.Sets;
import hudson.Extension;
import hudson.model.Action;
import hudson.model.Item;
import hudson.model.ItemGroup;
import hudson.model.ParameterValue;
import hudson.model.TopLevelItem;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Cause;
import hudson.model.Cause.UserIdCause;
import hudson.model.CauseAction;
import hudson.model.Descriptor;
import hudson.model.Descriptor.FormException;
import hudson.model.Hudson;
import hudson.model.ParametersAction;
import hudson.model.Run;
import hudson.model.View;
import hudson.model.ViewDescriptor;
import hudson.plugins.parameterizedtrigger.AbstractBuildParameters;
import hudson.plugins.parameterizedtrigger.BuildTrigger;
import hudson.plugins.parameterizedtrigger.BuildTriggerConfig;
import hudson.security.Permission;
import hudson.tasks.Publisher;
import hudson.util.DescribableList;
import hudson.util.LogTaskListener;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
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
 * This view displays the set of jobs that are related
 * based on their upstream\downstream relationships as a pipeline. Each
 * build pipeline becomes a row on the view.
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

    /**
     * Whether to show pipeline parameter in the revision box
     * @deprecated - replaced with revisionBoxParameterProvider, keep in place
     * to migrate data
     */
    @Deprecated
    private boolean showPipelineParameters = true;

    /**
     * What to show as a row header for pipelines
     */
    private PipelineHeaderExtension rowHeaders;

    /**
     * Whether to show pipeline parameters in the header box
     * @deprecated  - replaced with headerBoxParameterProvider, keep in place
     * to migrate data
     */
    @Deprecated
    private boolean showPipelineParametersInHeaders;

    /**
     * What to show as a column headers for jobs in the pipeline
     */
    private PipelineHeaderExtension columnHeaders;

    /**
     * @deprecated
     *
     * Don't need an input from UI store this
     */
    @Deprecated    
    private boolean startsWithParameters;

    /**
     * Frequency at which the Build Pipeline Plugin updates the build cards in seconds
     */
    private int refreshFrequency = 3;

    /** showPipelineDefinitionHeader. */
    private boolean showPipelineDefinitionHeader;

    /** A Logger object is used to log messages */
    private static final Logger LOGGER = Logger.getLogger(BuildPipelineView.class.getName());

    /**
     * An instance of {@link Cause.UserIdCause} related to the current user.
     * Just kept for backwards comparability.
     * @deprecated Use Cause.UserIdCause instead
     */
    @Deprecated
    private class MyUserIdCause extends Cause.UserIdCause {
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
        this.rowHeaders = new NullColumnHeader();
        this.columnHeaders = new SimpleRowHeader();
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
     * @param selectedJob
     *            the first job name in the pipeline. it can be set to null when gridBuilder is passed.
     * @param columnHeaders
     *            see {@link #columnHeaders}
     * @param rowHeaders
     *            see {@link #rowHeaders}
     */
    @DataBoundConstructor
    public BuildPipelineView(final String name, final String buildViewTitle, final ProjectGridBuilder gridBuilder,
            final String noOfDisplayedBuilds,
            final boolean triggerOnlyLatestJob, final boolean alwaysAllowManualTrigger, final boolean showPipelineParameters,
            final boolean showPipelineParametersInHeaders, final boolean showPipelineDefinitionHeader,
            final int refreshFrequency, final String cssUrl, final String selectedJob,
            final PipelineHeaderExtension columnHeaders,
            final PipelineHeaderExtension rowHeaders) {
        this(name, buildViewTitle, gridBuilder, noOfDisplayedBuilds, triggerOnlyLatestJob, cssUrl);
        this.alwaysAllowManualTrigger = alwaysAllowManualTrigger;
        this.showPipelineParameters = showPipelineParameters;
        this.showPipelineParametersInHeaders = showPipelineParametersInHeaders;
        this.showPipelineDefinitionHeader = showPipelineDefinitionHeader;
        this.selectedJob = selectedJob;
        this.columnHeaders = columnHeaders;
        this.rowHeaders = rowHeaders;
        //not exactly understanding the lifecycle here, but I want a default of 3
        //(this is what the class variable is set to 3, if it's 0, set it to default, refresh of 0 does not make sense anyway)
        if (refreshFrequency < 1) {
            this.refreshFrequency = 3;
        } else {
            this.refreshFrequency = refreshFrequency;
        }

        //for remote api support
        if (this.gridBuilder == null) {
            if (this.selectedJob != null) {
                this.gridBuilder = new DownstreamProjectGridBuilder(this.selectedJob);
            }
        }
        
        if (this.selectedJob == null) {
            if (this.gridBuilder != null && this.gridBuilder instanceof DownstreamProjectGridBuilder) {
                this.selectedJob = ((DownstreamProjectGridBuilder) this.gridBuilder).getFirstJob();
            }
        }
    }

    /**
     * Migrate old data, set new fields
     *
     * @see
     *      <a href="https://wiki.jenkins-ci.org/display/JENKINS/Hint+on+retaining+backward+compatibility">
     *          Jenkins wiki entry on the subject</a>
     *
     * @return
     *      must be always 'this'
     */
    protected Object readResolve() {
        if (gridBuilder == null) {
            if (selectedJob != null) {
                gridBuilder = new DownstreamProjectGridBuilder(selectedJob);
            }
        } else {
            // safe to assume an existing install, check if we need to migrate
            // header config
            if (columnHeaders == null) {
                if (!showPipelineDefinitionHeader) {
                    columnHeaders = new NullColumnHeader();
                } else if (showPipelineParametersInHeaders) {
                    columnHeaders = new BuildVariablesHeader();
                } else {
                    columnHeaders = new SimpleColumnHeader();
                }
            }
            if (rowHeaders == null) {
                if (showPipelineParameters) {
                    rowHeaders = new BuildVariablesHeader();
                } else {
                    rowHeaders = new SimpleRowHeader();
                }
            }
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
     * Checks if this build starts with parameters
     * @return - true: The build has parameters; false: Does not have parameters
     */ 
    public boolean isProjectParameterized() {
        return getGridBuilder().startsWithParameters(this);
    }

    public PipelineHeaderExtension getColumnHeaders() {
        return columnHeaders;
    }

    public void setColumnHeaders(PipelineHeaderExtension columnHeaders) {
        this.columnHeaders = columnHeaders;
    }

    public PipelineHeaderExtension getRowHeaders() {
        return rowHeaders;
    }

    public void setRowHeaders(PipelineHeaderExtension rowHeaders) {
        this.rowHeaders = rowHeaders;
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
     */
    public BuildPipelineForm getBuildPipelineForm() {
        if (noOfDisplayedBuilds == null) {
          return null;
        }

        final int maxNoOfDisplayBuilds = Integer.valueOf(noOfDisplayedBuilds);

        if (gridBuilder == null)  {
          return null;
        }

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
        final ItemGroup context = getOwnerItemGroup();
        final AbstractProject<?, ?> triggerProject = (AbstractProject<?, ?>) Jenkins.getInstance().getItem(triggerProjectName, context);
        final AbstractProject<?, ?> upstreamProject = (AbstractProject<?, ?>) Jenkins.getInstance().getItem(upstreamProjectName, context);

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
     * Re-run a project, passing in the CauseActions from the previous completed {@link Run} so
     * that the new run will appear in the same pipeline.
     *
     * @param externalizableId
     *            the externalizableId of the Run. See {@link Run#getExternalizableId()}
     * @return the number of re-run build
     */
    @JavaScriptMethod
    public int rerunBuild(final String externalizableId) {
        LOGGER.info("Running build again: " + externalizableId); //$NON-NLS-1$
        final AbstractBuild<?, ?> triggerBuild = (AbstractBuild<?, ?>) Run.fromExternalizableId(externalizableId);
        final AbstractProject<?, ?> triggerProject = triggerBuild.getProject();
        final Future<?> future = triggerProject.scheduleBuild2(triggerProject.getQuietPeriod(), new MyUserIdCause(),
                filterActions(triggerBuild.getActions()));

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
        final List<Action> buildActions = new ArrayList<Action>();
        final List<Cause> causes = new ArrayList<Cause>();
        causes.add(new UserIdCause());
        if (upstreamBuild != null) {
            causes.add(new Cause.UpstreamCause((Run<?, ?>) upstreamBuild));
        }
        final CauseAction causeAction = new CauseAction(causes);
        buildActions.add(causeAction);
        ParametersAction parametersAction =
                buildParametersAction instanceof ParametersAction
                        ? (ParametersAction) buildParametersAction : new ParametersAction();

        if (upstreamBuild != null) {


            final List<AbstractBuildParameters> configs = retrieveUpstreamProjectTriggerConfig(triggerProject, upstreamBuild);

            if (configs == null) {
                LOGGER.log(Level.SEVERE, "No upstream trigger found for this project: " + triggerProject.getFullDisplayName());
                throw new IllegalStateException("No upstream trigger found for this project: " + triggerProject.getFullDisplayName());
            }

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
     * Used to retrieve the parameters from the upstream project build trigger relative to the given downstream project
     * @param project the downstream project
     * @param upstreamBuild the upstream project build
     * @return the trigger config relative to the given downstream project
     */
    private List<AbstractBuildParameters> retrieveUpstreamProjectTriggerConfig(final AbstractProject<?, ?> project,
                                                                               final AbstractBuild<?, ?> upstreamBuild) {
        final DescribableList<Publisher, Descriptor<Publisher>> upstreamProjectPublishersList =
                upstreamBuild.getProject().getPublishersList();

        List<AbstractBuildParameters> configs = null;

        final BuildPipelineTrigger manualTrigger = upstreamProjectPublishersList.get(BuildPipelineTrigger.class);
        if (manualTrigger != null) {
            LOGGER.fine("Found Manual Trigger (BuildPipelineTrigger) found in upstream project publisher list ");
            final Set<String> downstreamProjectsNames =
                    Sets.newHashSet(Splitter.on(",").trimResults().split(manualTrigger.getDownstreamProjectNames()));
            LOGGER.fine("Downstream project names: " + downstreamProjectsNames);
            // defect: requires full name in the trigger. But downstream is just fine!
            if (downstreamProjectsNames.contains(project.getFullName())) {
                configs = manualTrigger.getConfigs();
            } else {
                LOGGER.warning("Upstream project had a Manual Trigger for projects [" + downstreamProjectsNames
                        + "], but that did not include our project [" + project.getFullName() + "]");
            }
        }

        final BuildTrigger autoTrigger = upstreamProjectPublishersList.get(BuildTrigger.class);
        if (autoTrigger != null) {
            for (BuildTriggerConfig config : autoTrigger.getConfigs()) {
                final Set<String> downstreamProjectsNames = Sets.newHashSet(Splitter.on(",").trimResults().split(config.getProjects()));
                if (downstreamProjectsNames.contains(project.getFullName())) {
                    configs = config.getConfigs();
                }
            }
        }

        return configs;
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
     * Filter out the list of actions so that it only includes {@link ParametersAction} and
     * CauseActions, removing the UserIdAction from the CauseAction's list of Causes.
     *
     * We want to include CauseAction because that includes upstream cause actions, which
     * are inherited in downstream builds.
     *
     * We do not want to inherit the UserId cause, because the user initiating a retry may
     * be different than the user who originated the upstream build, and so should be
     * re-identified.
     *
     * We do not want to inherit any other CauseAction because that will result in duplicating
     * actions from publishers, and builders from previous builds corrupting the retriggered build.
     *
     * @param actions
     *            a collection of build actions.
     * @return a collection of build actions with all UserId causes removed.
     */
    private List<Action> filterActions(final List<Action> actions) {
        final List<Action> retval = new ArrayList<Action>();
        for (final Action action : actions) {
            if (action instanceof CauseAction) {
                final CauseAction causeAction  = filterOutUserIdCause((CauseAction) action);
                if (!causeAction.getCauses().isEmpty()) {
                    retval.add(causeAction);
                }
            } else if (action instanceof ParametersAction) {
                retval.add(action);
            } else if ("hudson.plugins.git.RevisionParameterAction".equals(action.getClass().getName())) {
                 retval.add(action);
            }
        }
        return retval;
    }

    /**
     * Filter out {@link UserIdCause} from the given {@link CauseAction}.
     *
     * We want to do this because re-run will want to contribute its own
     * {@link UserIdCause}, not copy it from the previous run.
     *
     * @param causeAction
     *  the causeAction to remove UserIdCause from
     * @return a causeAction with UserIdCause removed
     */
    private CauseAction filterOutUserIdCause(CauseAction causeAction) {
        final List<Cause> causes = new ArrayList<Cause>();
        final Iterator<Cause> it = causeAction.getCauses().iterator();
        while (it.hasNext()) {
            final Cause cause = it.next();
            if (!(cause instanceof UserIdCause)) {
                causes.add(cause);
            }
        }
        return new CauseAction(causes);
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
         * @param condition
         *       if true, return it as part of the returned list
         * @return
         *      a filtered and ordered list of descriptors matching the condition
         */
        public List<PipelineHeaderExtensionDescriptor> filter(Function<PipelineHeaderExtensionDescriptor, Boolean> condition) {
            final List<PipelineHeaderExtensionDescriptor> result = new ArrayList<PipelineHeaderExtensionDescriptor>();
            final List<PipelineHeaderExtension> applicableExtensions = new ArrayList<PipelineHeaderExtension>();
            for (PipelineHeaderExtensionDescriptor descriptor : PipelineHeaderExtensionDescriptor.all()) {
                if (condition.apply(descriptor)) {
                    result.add(descriptor);
                }
            }
            Collections.sort(result);
            return result;
        }

        /**
         * @return a list of PipelineHeaderExtension descriptors which can be used as a row header
         */
        public List<PipelineHeaderExtensionDescriptor> getRowHeaderDescriptors() {
            return filter(new Function<PipelineHeaderExtensionDescriptor, Boolean>() {
                @Override
                public Boolean apply(PipelineHeaderExtensionDescriptor extension) {
                    return extension.appliesToRows();
                }
            });
        }

        /**
         * @return a list of PipelineHeaderExtension descriptors which can be used as column headers
         */
        public List<PipelineHeaderExtensionDescriptor> getColumnHeaderDescriptors() {
            return filter(new Function<PipelineHeaderExtensionDescriptor, Boolean>() {

                @Override
                public Boolean apply(PipelineHeaderExtensionDescriptor extension) {
                    return extension.appliesToColumns();
                }
            });
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

    /**
     * A function which accepts an argument and returns a result. Necessary to parameterize behavior,
     * because we do not require JDK8 yet.
     *
     * @see <a href="http://docs.oracle.com/javase/8/docs/api/java/util/function/Function.html">
     *     JDK8 java.lang.Function
     *     </a>
     */
    public interface Function<F, T> {

        /**
         * Applies this function to the argument
         * @param input
         *  an input
         * @return
         *  a result
         */
        T apply(F input);
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
        final Collection<TopLevelItem> items = new ArrayList<TopLevelItem>();
        final BuildPipelineForm buildPipelineForm = getBuildPipelineForm();
        if (buildPipelineForm != null) {
            final ProjectGrid grid = buildPipelineForm.getProjectGrid();
            for (int row = 0; row < grid.getRows(); row++) {
                for (int col = 0; col < grid.getColumns(); col++) {
                    final ProjectForm form = grid.get(row, col);
                    if (form != null) {
                        final Item item = Jenkins.getInstance().getItem(form.getName(), getOwnerItemGroup());
                        if (item != null && item instanceof TopLevelItem) {
                            items.add((TopLevelItem) item);
                        }
                    }
                }
            }
        }
        return items;
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
            if (gridBuilder != null) {
                gridBuilder.onJobRenamed(this, item, oldName, newName);
            }
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
    
    @Override
    public boolean hasPermission(final Permission p) {
        boolean display = true;
        //tester la liste vide seulement en lecture
        if (READ.name.equals(p.name)) {
            if (isEmpty()) {
                display = false;
            }
        } else {
            //Pas en lecture => permission standard
            display = super.hasPermission(p);
        }

        return display;
    }

    /**
     * determine if this view is empty
     * @return true if this view contains zero items
     */
    private boolean isEmpty() {
        if (noOfDisplayedBuilds == null || gridBuilder == null) {
            return true;
        }
        return gridBuilder.build(this).isEmpty();
    }
}
