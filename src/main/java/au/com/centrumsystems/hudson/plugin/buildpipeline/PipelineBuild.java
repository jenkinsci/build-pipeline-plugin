/*
 * The MIT License
 *
 * Copyright (c) 2011, Centrumsystems Pty Ltd
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

import hudson.Util;
import hudson.model.Item;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.FreeStyleProject;
import hudson.model.Hudson;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import au.com.centrumsystems.hudson.plugin.util.BuildUtil;
import au.com.centrumsystems.hudson.plugin.util.HudsonResult;
import au.com.centrumsystems.hudson.plugin.util.ProjectUtil;
import hudson.plugins.parameterizedtrigger.BlockableBuildTriggerConfig;
import hudson.plugins.parameterizedtrigger.SubProjectsAction;

/**
 * @author Centrum Systems
 *
 */
public class PipelineBuild {
    /** Represents the current build */
    private AbstractBuild<?, ?> currentBuild;
    /** Represents the current project */
    private AbstractProject<?, ?> project;
    /** Represents the upstream build */
    private AbstractBuild<?, ?> upstreamBuild;
    /**
     * Contains the upstreamBuild result. Can be one of the following: - BUILDING - SUCCESS - FAILURE - UNSTABLE - NOT_BUILT - ABORT -
     * PENDING - MANUAL
     */
    private String upstreamBuildResult;
    /**
     * Contains the currentBuild result. Can be one of the following: - BUILDING - SUCCESS - FAILURE - UNSTABLE - NOT_BUILT - ABORT -
     * PENDING - MANUAL
     */
    private String currentBuildResult;

    /** A Logger object is used to log messages */
    private static final Logger LOGGER = Logger.getLogger(PipelineBuild.class.getName());

    /**
     * Default constructor
     */
    public PipelineBuild() {
    }

    /**
     * Creates a new PipelineBuild with currentBuild, project and upstreamBuild set.
     *
     * @param build
     *            - current build
     * @param project
     *            - current project
     * @param previousBuild
     *            - upstream build
     */
    public PipelineBuild(final AbstractBuild<?, ?> build, final AbstractProject<?, ?> project, final AbstractBuild<?, ?> previousBuild) {
        this.currentBuild = build;
        this.project = project;
        this.upstreamBuild = previousBuild;
        this.currentBuildResult = ""; //$NON-NLS-1$
        this.upstreamBuildResult = ""; //$NON-NLS-1$
    }

    /**
     * Convenience method to create {@link PipelineBuild} from a build.
     *
     * @param build
     *      The object to be wrapped.
     */
    public PipelineBuild(final AbstractBuild<?, ?> build) {
        this(build, build.getProject(), build.getPreviousBuild());
    }

    /**
     * @param project
     *            project
     */
    public PipelineBuild(final FreeStyleProject project) {
        this(null, project, null);
    }

    public AbstractBuild<?, ?> getCurrentBuild() {
        return currentBuild;
    }

    public void setCurrentBuild(final AbstractBuild<?, ?> currentBuild) {
        this.currentBuild = currentBuild;
    }

    public AbstractBuild<?, ?> getUpstreamBuild() {
        return upstreamBuild;
    }

    public void setUpstreamBuild(final AbstractBuild<?, ?> upstreamBuild) {
        this.upstreamBuild = upstreamBuild;
    }

    public void setProject(final AbstractProject<?, ?> currentProject) {
        this.project = currentProject;
    }

    /**
     * Returns the project name. If the current project is null the project name is determined using the current build.
     *
     * @return - Project name
     */
    public AbstractProject<?, ?> getProject() {
        final AbstractProject<?, ?> currentProject;
        if (this.project == null && this.currentBuild != null) {
            currentProject = this.currentBuild.getProject();
        } else {
            currentProject = this.project;
        }
        return currentProject;
    }

    /**
     * Returns the current build number.
     *
     * @return - Current build number or empty String is the current build is null.
     */
    public String getCurrentBuildNumber() {
        if (this.currentBuild != null) {
            return Integer.toString(currentBuild.getNumber());
        } else {
            return ""; //$NON-NLS-1$
        }
    }

    /**
     * Constructs a List of downstream PipelineBuild objects that make up the current pipeline.
     *
     * @return - List of downstream PipelineBuild objects that make up the current pipeline.
     */
    public List<PipelineBuild> getDownstreamPipeline() {
        final List<PipelineBuild> pbList = new ArrayList<PipelineBuild>();

        final AbstractProject<?, ?> currentProject;
        currentProject = getProject();

        final List<AbstractProject<?, ?>> downstreamProjects = ProjectUtil.getDownstreamProjects(currentProject);
        for (final AbstractProject<?, ?> proj : downstreamProjects) {
            AbstractBuild<?, ?> returnedBuild = null;
            if (this.currentBuild != null) {
                returnedBuild = BuildUtil.getDownstreamBuild(proj, currentBuild);
            }
            final PipelineBuild newPB = new PipelineBuild(returnedBuild, proj, this.currentBuild);
            pbList.add(newPB);
        }
        if (Hudson.getInstance().getPlugin("parameterized-trigger") != null) {
            for (SubProjectsAction action : Util.filter(currentProject.getActions(), SubProjectsAction.class)) {
                for (BlockableBuildTriggerConfig config : action.getConfigs()) {
                    for (final AbstractProject<?, ?> dependency : config.getProjectList(currentProject.getParent(), null)) {
                        AbstractBuild<?, ?> returnedBuild = null;
                        if (this.currentBuild != null) {
                            returnedBuild = BuildUtil.getDownstreamBuild(dependency, currentBuild);
                        }
                        final PipelineBuild candidate = new PipelineBuild(returnedBuild, dependency, this.currentBuild);
                        // if subprojects come back as downstreams someday, no duplicates wanted
                        if (!pbList.contains(candidate)) {
                            pbList.add(candidate);
                        }
                    }
                }
            }
        }

        return pbList;
    }

    /**
     * Build a URL of the currentBuild
     *
     * @return URL of the currentBuild
     */
    public String getBuildResultURL() {
        return currentBuild != null ? currentBuild.getUrl() : ""; //$NON-NLS-1$
    }

    /**
     * Builds a URL of the current project
     *
     * @return URL - of the project
     */
    public String getProjectURL() {
        return project != null ? project.getUrl() : ""; //$NON-NLS-1$
    }

    /**
     * Determines the result of the current build.
     *
     * @return - String representing the build result
     * @see PipelineBuild#getBuildResult(AbstractBuild)
     */
    public String getCurrentBuildResult() {
        this.currentBuildResult = getBuildResult(this.currentBuild);
        return this.currentBuildResult;
    }

    /**
     * Determines the result of the upstream build.
     *
     * @return - String representing the build result
     * @see PipelineBuild#getBuildResult(AbstractBuild)
     */
    public String getUpstreamBuildResult() {
        if (this.upstreamBuildResult.length() == 0) {
            this.upstreamBuildResult = getBuildResult(this.upstreamBuild);
        }
        return this.upstreamBuildResult;
    }

    /**
     * Determines the result for a particular build. Can be one of the following: - BUILDING - SUCCESS - FAILURE - UNSTABLE - NOT_BUILT -
     * ABORT - PENDING - MANUAL
     *
     * @param build
     *            - The build for which a result is requested.
     * @return - String representing the build result
     */
    private String getBuildResult(final AbstractBuild<?, ?> build) {
        String buildResult;
        // If AbstractBuild exists determine its current status
        if (build != null) {
            if (build.isBuilding()) {
                buildResult = HudsonResult.BUILDING.toString();
            } else {
                if (build.getResult() == null) {
                    throw new IllegalStateException("Build with a null result after build has finished");
                }
                buildResult = HudsonResult.values()[build.getResult().ordinal].toString();
            }
        } else {
            // Otherwise determine its pending status
            buildResult = getPendingStatus();
        }

        return buildResult;
    }

    /**
     * Determines the pending currentBuild status of a currentBuild in the pipeline that has not been completed. (i.e. the currentBuild is
     * null)
     *
     * @return - PENDING: Current currentBuild is pending the execution of upstream builds. MANUAL: Current currentBuild requires a manual
     *         trigger
     */

    private String getPendingStatus() {
        String pendingStatus = HudsonResult.PENDING.toString();
        final PipelineBuild upstreamPB = getUpstreamPipelineBuild();

        if (upstreamPB != null) {
            if (this.getUpstreamBuild() != null) {
                if (getUpstreamBuildResult().equals(HudsonResult.SUCCESS.toString())
            ||
            getUpstreamBuildResult().equals(HudsonResult.UNSTABLE.toString())) {
                    if (ProjectUtil.isManualTrigger(this.upstreamBuild.getProject(), this.project)) {
                        pendingStatus = HudsonResult.MANUAL.toString();
                    }
                }
            }
        }
        return pendingStatus;
    }


    /**
     * Returns the upstream PipelineBuild object from the current PipelineBuild object.
     *
     * @return - Upstream PipelineBuild object from the current PipelineBuild object
     */
    public PipelineBuild getUpstreamPipelineBuild() {
        @SuppressWarnings("rawtypes")
        final List<AbstractProject> upstreamProjects = getProject().getUpstreamProjects();
        AbstractProject<?, ?> previousProject = null;
        String upstreamBuildName;
        final PipelineBuild previousPB = new PipelineBuild();
        if (this.upstreamBuild != null) {
            upstreamBuildName = this.upstreamBuild.getProject().getName();
        } else {
            upstreamBuildName = "";
        }
        if (upstreamProjects.size() > 0) {
            if (isManualTrigger()) {
                for (AbstractProject upstreamProject : upstreamProjects) {
                    if (upstreamProject.getName().equals(upstreamBuildName)) {
                        previousProject = upstreamProject;
                      break;
                    }
                }
            }
            if (previousProject == null) {
                previousProject = upstreamProjects.get(0);
            }
            previousPB.setCurrentBuild(this.getUpstreamBuild());
            previousPB.setProject(previousProject);
        }
        return previousPB;
    }

    /**
     * Returns the current build duration.
     *
     * @return - Current build duration or an empty String if the current build is null.
     */
    public String getBuildDuration() {
        if (this.currentBuild != null) {
            return this.currentBuild.getDurationString();
        } else {
            return ""; //$NON-NLS-1$
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Project: " + getProject().getName() + " : Build: " + getCurrentBuildNumber();
    }

    /**
     * Returns the current build description.
     *
     * @return - Current build description or the project name if the current build is null.
     */
    public String getBuildDescription() {
        if (this.currentBuild != null) {
            return this.currentBuild.toString();
        } else {
            return Strings.getString("PipelineBuild.PendingBuildOfProject") + this.getProject().getName(); //$NON-NLS-1$
        }
    }

    /**
     * Returns the estimated percentage complete of the current build.
     *
     * @return - Estimated percentage complete of the current build.
     */
    public long getBuildProgress() {
        if (this.currentBuild != null && this.currentBuild.isBuilding()) {
            final long duration = new Date().getTime() - this.currentBuild.getTimestamp().getTimeInMillis();
            return calculatePercentage(duration, this.currentBuild.getEstimatedDuration());
        } else {
            return 0;
        }
    }

    /**
     * Calculates percentage of the current duration to the estimated duration. Caters for the possibility that current duration will be
     * longer than estimated duration
     *
     * @param duration
     *            - Current running time in milliseconds
     * @param estimatedDuration
     *            - Estimated running time in milliseconds
     * @return - Percentage of current duration to estimated duration
     */
    protected long calculatePercentage(final long duration, final long estimatedDuration) {
        if (duration > estimatedDuration) {
            return 100;
        }
        if (estimatedDuration > 0) {
            return (long) ((float) duration / (float) estimatedDuration * 100);
        }
        return 100;
    }

    /**
     * Return pipeline version which is simply the first build's number
     *
     * @return pipeline verison
     */
    public String getPipelineVersion() {
        String version;
        if (currentBuild != null) {
            final String displayName = currentBuild.getDisplayName();
            if (displayName == null || displayName.trim().length() == 0) {
                version = currentBuild.getNumber() > 0 ? String.valueOf(currentBuild.getNumber()) : Strings
                    .getString("PipelineBuild.RevisionNotAvailable");
            } else {
                version = displayName;
            }
        } else {
            version = Strings.getString("PipelineBuild.RevisionNotAvailable");
        }
        return version;
    }

    /**
     * Checks whether the user has Build permission for the current project.
     *
     * @return - true: Has Build permission; false: Does not have Build permission
     * @see hudson.model.Item
     */
    public boolean hasBuildPermission() {
        boolean buildPermission = false;
        // If no security is enabled then allow builds
        if (!Hudson.getInstance().isUseSecurity()) {
            LOGGER.fine("Security is not enabled.");
            buildPermission = true;
        } else if (this.project != null) {
            // If security is enabled check if BUILD is enabled
            buildPermission = this.project.hasPermission(Item.BUILD);
        }
        LOGGER.fine("Is user allowed to build? -> " + buildPermission);
        return buildPermission;
    }

    /**
     * @return is ready to be manually built.
     */
    public boolean isReadyToBeManuallyBuilt() {
      return  this.currentBuild == null && (upstreamBuildSucceeded() || upstreamBuildUnstable()) && hasBuildPermission();
    }

    public boolean isRerunnable() {
        return !isReadyToBeManuallyBuilt()
                && !"PENDING".equals(getCurrentBuildResult())
                && !"BUILDING".equals(getCurrentBuildResult())
                && hasBuildPermission();
    }

    /**
     * @return upstream build is existed and successful.
     */
    private boolean upstreamBuildSucceeded() {
        return this.getUpstreamBuild() != null && HudsonResult.SUCCESS.toString().equals(getBuildResult(this.upstreamBuild));
    }

   /**
    * @return upstream build exists and unstable.
    */
   private boolean upstreamBuildUnstable() {
       return this.getUpstreamBuild() != null && HudsonResult.UNSTABLE.toString().equals(getBuildResult(this.upstreamBuild));
   }


    /**
     * Determine if the project is triggered manually, regardless of the state of its upstream builds
     *
     * @return true if it is manual
     */
    public boolean isManualTrigger() {
        boolean manualTrigger = false;
        if (this.upstreamBuild != null) {
            manualTrigger = ProjectUtil.isManualTrigger(this.upstreamBuild.getProject(), this.project);
        }
        return manualTrigger;
    }

    /**
     * Start time of build
     *
     * @return start time
     */
    public Date getStartTime() {
        return currentBuild != null ? currentBuild.getTime() : null;
    }

    /**
     * @return Formatted start time
     */
    public String getFormattedStartTime() {
        String formattedStartTime = ""; //$NON-NLS-1$
        if (getStartTime() != null) {
            formattedStartTime = DateFormat.getTimeInstance(DateFormat.MEDIUM).format(getStartTime());
        }
        return formattedStartTime;
    }

    /**
     * @return Formatted start date
     */
    public String getFormattedStartDate() {
        String formattedStartTime = ""; //$NON-NLS-1$
        if (getStartTime() != null) {
            formattedStartTime = DateFormat.getDateInstance(DateFormat.MEDIUM).format(getStartTime());
        }
        return formattedStartTime;
    }

    /**
     * @return a map including build parameters
     */
    public Map<String, String> getBuildParameters() {
        return BuildUtil.getUnsensitiveParameters(currentBuild);
    }

    public boolean isProjectDisabled() {
        return getProject().isDisabled();
    }

    public String getProjectHealth() {
        return project.getBuildHealth().getIconUrl().replaceAll("\\.gif", "\\.png");
    }
}
