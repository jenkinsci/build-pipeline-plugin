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
package au.com.centrumsystems.hudson.plugin.buildpipeline.trigger;

import hudson.Extension;
import hudson.Launcher;
import hudson.PluginWrapper;
import hudson.Util;
import hudson.model.BuildListener;
import hudson.model.DependecyDeclarer;
import hudson.model.DependencyGraph;
import hudson.model.Item;
import hudson.model.Items;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import hudson.model.Project;
import hudson.model.listeners.ItemListener;
import hudson.plugins.parameterizedtrigger.AbstractBuildParameters;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Messages;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import hudson.util.FormValidation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import au.com.centrumsystems.hudson.plugin.buildpipeline.Strings;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.AncestorInPath;

/**
 * The build pipeline trigger allows the creation of downstream jobs which aren't triggered automatically. This allows us to have manual
 * "approval" steps in the process where jobs are manually promoted along the pipeline by a user pressing a button on the view.
 *
 * @author Centrum Systems
 */
public class BuildPipelineTrigger extends Notifier implements DependecyDeclarer {
    /**
     * logger
     */
    private static final Logger LOGGER = Logger.getLogger(BuildPipelineTrigger.class.getName());

    /**
     * Build parameters
     */
    private final List<AbstractBuildParameters> configs;

    /**
     * downstream project name
     */
    private String downstreamProjectNames;

    public String getDownstreamProjectNames() {
        return downstreamProjectNames;
    }

    public void setDownstreamProjectNames(final String downstreamProjectNames) {
        this.downstreamProjectNames = downstreamProjectNames;
    }

    public List<AbstractBuildParameters> getConfigs() {
        return configs;
    }

    /**
     * Construct the trigger setting the project name and manual build promotion option
     *
     * @param downstreamProjectNames - the job name of the downstream build
     * @param configs                - the build parameters
     */
    @DataBoundConstructor
    public BuildPipelineTrigger(final String downstreamProjectNames, final List<AbstractBuildParameters> configs) {
        if (downstreamProjectNames == null) {
            throw new IllegalArgumentException();
        }

        this.downstreamProjectNames = downstreamProjectNames;
        this.configs = new ArrayList<AbstractBuildParameters>(Util.fixNull(configs));
    }

    /**
     * this method is required to rebuild the dependency graph of the downstream project
     *
     * @param owner owner
     * @param graph graph
     */
    @Override
    @SuppressWarnings("rawtypes")
    public void buildDependencyGraph(final AbstractProject owner, final DependencyGraph graph) {
        if ((downstreamProjectNames != null) && (downstreamProjectNames.length() > 0)) {
            for (final Object o : Items.fromNameList(owner.getParent(), downstreamProjectNames, AbstractProject.class)) {
                final AbstractProject downstream = (AbstractProject) o;

                if (owner != downstream) {
                    graph.addDependency(createDownstreamDependency(owner, downstream));
                } else {
                    removeDownstreamTrigger(this, owner, downstream.getName());
                }
            }
        }
    }

    /**
     * Create a new DownstreamDependency
     *
     * @param owner      - upstream project
     * @param downstream - downstream project
     * @return downstream dependency
     */
    private DownstreamDependency createDownstreamDependency(final AbstractProject<?, ?> owner, final AbstractProject<?, ?> downstream) {
        return new DownstreamDependency(owner, downstream);
    }

    @Override
    public boolean needsToRunAfterFinalized() {
        return true;
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Override
    public boolean perform(final AbstractBuild<?, ?> build, final Launcher launcher, final BuildListener listener)
            throws InterruptedException, IOException {
        return true;
    }

    /**
     * Renames a project contained in downstreamProjectNames
     *
     * @param oldName - The old name of the project
     * @param newName - The new name of the project
     * @return - true: A downstream project has been renamed; false No downstream projects were renamed
     */
    // TODO should these names be relative names? cf. onDownstreamProjectDeleted, removeDownstreamTrigger, onRenamed, onDeleted
    public boolean onDownstreamProjectRenamed(final String oldName, final String newName) {
        LOGGER.fine(String.format("Renaming project %s -> %s", oldName, newName)); //$NON-NLS-1$
        boolean changed = false;
        String[] existingDownstreamProjects = new String[5];
        if (this.getDownstreamProjectNames() != null) {
            existingDownstreamProjects = this.getDownstreamProjectNames().split(",");
            for (int i = 0; i < existingDownstreamProjects.length; i++) {
                if (existingDownstreamProjects[i].trim().equals(oldName)) {
                    existingDownstreamProjects[i] = newName;
                    changed = true;
                }
            }
        }
        if (changed) {
            final StringBuilder newDownstreamProjects = new StringBuilder();
            for (int i = 0; i < existingDownstreamProjects.length; i++) {
                if (existingDownstreamProjects[i] == null) {
                    continue;
                }
                if (newDownstreamProjects.length() > 0) {
                    newDownstreamProjects.append(',');
                }
                newDownstreamProjects.append(existingDownstreamProjects[i].trim());
            }
            this.setDownstreamProjectNames(newDownstreamProjects.toString());
        }
        return changed;
    }

    /**
     * Deletes a project from downstreamProjectNames.
     *
     * @param oldName - Project to be deleted
     * @return - true; project deleted: false; project not deleted {@link #onDownstreamProjectRenamed(String, String)}
     */
    public boolean onDownstreamProjectDeleted(final String oldName) {
        LOGGER.fine("Downstram project deleted: " + oldName); //$NON-NLS-1$
        return onDownstreamProjectRenamed(oldName, null);
    }

    /**
     * Removes a downstream trigger (BuildPipelineTrigger) from a project. This removes both: - The downstream project name from the
     * downstreamProjectNames attribute - The BuildPipelineTrigger from the AbstractProject publishers list
     *
     * @param bpTrigger             - The BuildPipelineTrigger to be removed
     * @param ownerProject          - The AbstractProject from which to removed the BuildPipelineTrigger
     * @param downstreamProjectName - The name of the AbstractProject associated with the BuildPipelineTrigger
     */
    public void removeDownstreamTrigger(final BuildPipelineTrigger bpTrigger, final AbstractProject<?, ?> ownerProject,
                                        final String downstreamProjectName) {
        if (bpTrigger != null) {
            boolean changed = false;

            if (bpTrigger.onDownstreamProjectDeleted(downstreamProjectName)) {
                changed = true;
            }

            if (changed) {
                try {
                    if (bpTrigger.getDownstreamProjectNames().length() == 0) {
                        ownerProject.getPublishersList().remove(bpTrigger);
                    }
                    ownerProject.save();
                } catch (final IOException e) {
                    Logger.getLogger(BuildPipelineTrigger.class.getName()).log(
                            Level.SEVERE,
                            au.com.centrumsystems.hudson.plugin.buildpipeline.Strings
                                    .getString("BuildPipelineTrigger.FailedPersistDuringRemoval") + downstreamProjectName, e); //$NON-NLS-1$
                }
            }
        }
    }

    /**
     * Set the descriptor for build pipeline trigger class This descriptor is only attached to Build Trigger Post Build action in JOB
     * configuration page
     *
     * @author Centrum Systems
     */
    @Extension
    public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        /**
         * Returns true if it is possible to add parameters to the trigger. This is the case when the
         * parameterized-trigger plugin is both installed and active.
         *
         * @return true if it is possible to add parameters to the trigger
         */
        public boolean canAddParameters() {
            final PluginWrapper plugin = Hudson.getInstance().getPluginManager().getPlugin("parameterized-trigger"); //$NON-NLS-1$
            return plugin != null && plugin.isActive();
        }

        @Override
        public boolean isApplicable(@SuppressWarnings("rawtypes") final Class<? extends AbstractProject> jobType) {
            return true;
        }

        /**
         * set the display name in post build action section of the job configuration page
         *
         * @return display name
         */
        @Override
        public String getDisplayName() {
            return Strings.getString("BuildPipelineTrigger.DisplayText"); //$NON-NLS-1$
        }

        /**
         * Set help text to "Build Pipeline Plugin -> Manually Execute Downstream Project" Post Build action in JOB configuration page
         *
         * @return location of the help file
         */
        @Override
        public String getHelpFile() {
            final String filePath = "/descriptor/au.com.centrumsystems.hudson.plugin.buildpipeline.trigger.BuildPipelineTrigger/help";
            final String fileName = "buildPipeline.html";
            return String.format("%s/%s", filePath, fileName);
        }

        /**
         * Validates that the downstream project names entered are valid projects.
         *
         * @param value   - The entered project names
         * @param project - the containing project
         * @return hudson.util.FormValidation
         */
        public FormValidation doCheckDownstreamProjectNames(@AncestorInPath AbstractProject project,
                                                            @QueryParameter("downstreamProjectNames") final String value) {
            final StringTokenizer tokens = new StringTokenizer(Util.fixNull(value), ","); //$NON-NLS-1$
            boolean some = false;
            while (tokens.hasMoreTokens()) {
                final String projectName = tokens.nextToken().trim();
                if ("".equals(projectName)) { //$NON-NLS-1$
                    continue;
                }
                some = true;
                final Item item = Jenkins.getInstance().getItemByFullName(projectName);
                if (item == null) {
                    return FormValidation.error(Messages.BuildTrigger_NoSuchProject(projectName,
                            AbstractProject.findNearest(projectName, project.getParent()).getRelativeNameFrom(project)));
                }
                if (!(item instanceof AbstractProject)) {
                    return FormValidation.error(Messages.BuildTrigger_NotBuildable(projectName));
                }
            }
            if (!some) {
                return FormValidation.error(Messages.BuildTrigger_NoProjectSpecified());
            }
            return FormValidation.ok();
        }

        public List<Descriptor<AbstractBuildParameters>> getBuilderConfigDescriptors() {
            return Hudson.getInstance().<AbstractBuildParameters,
                    Descriptor<AbstractBuildParameters>>getDescriptorList(AbstractBuildParameters.class);
        }

        /**
         * If a job is renamed, update all BuildPipelineTriggers with the new name.
         */
        @Extension
        public static final class ItemListenerImpl extends ItemListener {
            @Override
            public void onRenamed(final Item item, final String oldName, final String newName) {
                for (final Project<?, ?> p : Jenkins.getInstance().getAllItems(Project.class)) {
                    final BuildPipelineTrigger bpTrigger = p.getPublishersList().get(BuildPipelineTrigger.class);
                    if (bpTrigger != null) {
                        boolean changed = false;
                        changed = bpTrigger.onDownstreamProjectRenamed(oldName, newName);

                        if (changed) {
                            try {
                                p.save();
                            } catch (final IOException e) {
                                Logger.getLogger(ItemListenerImpl.class.getName()).log(Level.SEVERE,
                                        String.format(Strings.getString("BuildPipelineTrigger.FailedPersistDuringRename_FMT"), //$NON-NLS-1$
                                                oldName, newName), e);
                            }
                        }
                    }
                }
            }

            @Override
            public void onDeleted(final Item item) {
                for (final Project<?, ?> p : Jenkins.getInstance().getAllItems(Project.class)) {
                    final String oldName = item.getName();
                    final BuildPipelineTrigger bpTrigger = p.getPublishersList().get(BuildPipelineTrigger.class);
                    if (bpTrigger != null) {
                        bpTrigger.removeDownstreamTrigger(bpTrigger, p, oldName);
                    }
                }
            }
        }
    }
}
