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

import au.com.centrumsystems.hudson.plugin.buildpipeline.extension.BuildCardExtension;
import au.com.centrumsystems.hudson.plugin.buildpipeline.extension.NullColumnHeader;
import au.com.centrumsystems.hudson.plugin.buildpipeline.extension.PipelineHeaderExtension;
import au.com.centrumsystems.hudson.plugin.buildpipeline.extension.BuildVariablesHeader;
import au.com.centrumsystems.hudson.plugin.buildpipeline.extension.PipelineHeaderExtensionDescriptor;
import au.com.centrumsystems.hudson.plugin.buildpipeline.extension.SimpleColumnHeader;
import au.com.centrumsystems.hudson.plugin.buildpipeline.extension.SimpleRowHeader;
import au.com.centrumsystems.hudson.plugin.buildpipeline.extension.StandardBuildCard;
import com.google.common.collect.Iterables;

import hudson.Extension;
import hudson.model.Item;
import hudson.model.TopLevelItem;
import hudson.model.AbstractProject;
import hudson.model.Cause;
import hudson.model.Descriptor;
import hudson.model.Descriptor.FormException;
import hudson.model.Hudson;
import hudson.model.View;
import hudson.model.ViewDescriptor;
import hudson.security.Permission;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.bind.JavaScriptMethod;

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
     * @deprecated  - replaced with columnHeaders, keep in place
     * to migrate data
     */
    @Deprecated
    private boolean showPipelineParametersInHeaders;

    /**
     * What to show as a column headers for jobs in the pipeline
     */
    private PipelineHeaderExtension columnHeaders;

    /**
     * What to show as a build card in the pipeline
     */
    private BuildCardExtension buildCard;

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
    private static class MyUserIdCause extends Cause.UserIdCause {
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
        this.rowHeaders = new SimpleRowHeader();
        this.columnHeaders = new NullColumnHeader();
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
     * @param buildCard
     *            see {@link #buildCard}
     */
    @DataBoundConstructor
    public BuildPipelineView(final String name, final String buildViewTitle, final ProjectGridBuilder gridBuilder,
            final String noOfDisplayedBuilds,
            final boolean triggerOnlyLatestJob, final boolean alwaysAllowManualTrigger, final boolean showPipelineParameters,
            final boolean showPipelineParametersInHeaders, final boolean showPipelineDefinitionHeader,
            final int refreshFrequency, final String cssUrl, final String selectedJob,
            final PipelineHeaderExtension columnHeaders,
            final PipelineHeaderExtension rowHeaders,
            final BuildCardExtension buildCard) {
        this(name, buildViewTitle, gridBuilder, noOfDisplayedBuilds, triggerOnlyLatestJob, cssUrl);
        this.alwaysAllowManualTrigger = alwaysAllowManualTrigger;
        this.showPipelineParameters = showPipelineParameters;
        this.showPipelineParametersInHeaders = showPipelineParametersInHeaders;
        this.showPipelineDefinitionHeader = showPipelineDefinitionHeader;
        this.selectedJob = selectedJob;
        this.columnHeaders = columnHeaders;
        this.rowHeaders = rowHeaders;
        this.buildCard = buildCard;
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
        }
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
        if (buildCard == null) {
            buildCard = new StandardBuildCard();
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

    public BuildCardExtension getBuildCard() {
        return buildCard;
    }

    public void setBuildCard(BuildCardExtension buildCard) {
        this.buildCard = buildCard;
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
     * @throws URISyntaxException on error
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
        return buildCard.triggerManualBuild(getOwnerItemGroup(), upstreamBuildNumber, triggerProjectName, upstreamProjectName);
    }

    /**
     * Re-run a project, passing in the CauseActions from the previous completed {@link hudson.model.Run} so
     * that the new run will appear in the same pipeline.
     *
     * @param externalizableId
     *            the externalizableId of the Run. See {@link hudson.model.Run#getExternalizableId()}
     * @return the number of re-run build
     */
    @JavaScriptMethod
    public int rerunBuild(final String externalizableId) {
        LOGGER.info("Running build again: " + externalizableId); //$NON-NLS-1$
        return buildCard.rerunBuild(externalizableId);
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
         * @return a list of BuildCardExtension descriptors
         */
        public List<Descriptor<BuildCardExtension>> getBuildCardDescriptors() {
            final List<Descriptor<BuildCardExtension>> result = new ArrayList<Descriptor<BuildCardExtension>>();
            for (BuildCardExtension extension : BuildCardExtension.all()) {
                result.add(extension.getDescriptor());
            }
            return result;
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
     * @param <F> type of the function
     * @param <T> type returned by apply
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
        try {
            return super.hasPermission(p);
        } catch (Throwable t) {
            // JENKINS-44324This can be called from jenkins just determinig if it needs to show the
            // pipeline tab, so if there are any errors don't blow up
            LOGGER.log(Level.SEVERE, "Error in hasPermission: ", t);
            return false;
        }
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
