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
package au.com.centrumsystems.hudson.plugin.util;

import hudson.model.DependencyGraph;
import hudson.model.ParameterValue;
import hudson.model.AbstractProject;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import hudson.model.ParameterDefinition;
import hudson.model.ParametersAction;
import hudson.model.ParametersDefinitionProperty;
import hudson.tasks.Publisher;
import hudson.util.DescribableList;

import java.util.ArrayList;
import java.util.List;

import au.com.centrumsystems.hudson.plugin.buildpipeline.trigger.BuildPipelineTrigger;
import jenkins.model.Jenkins;

/**
 * Provides helper methods for #hudson.model.AbstractProject
 * 
 * @author Centrum Systems
 * 
 */
public final class ProjectUtil {

    /**
     * Default constructor
     */
    public ProjectUtil() {

    }

    /**
     * Given a Project get a List of all of its Downstream projects
     * 
     * @param currentProject
     *            Current project
     * @return List of Downstream projects
     */
    public static List<AbstractProject<?, ?>> getDownstreamProjects(final AbstractProject<?, ?> currentProject) {
        final DependencyGraph myDependencyGraph = Hudson.getInstance().getDependencyGraph();

        final List<AbstractProject<?, ?>> downstreamProjectsList = new ArrayList<AbstractProject<?, ?>>();
        for (final AbstractProject<?, ?> proj : myDependencyGraph.getDownstream(currentProject)) {
            downstreamProjectsList.add(proj);
        }
        return downstreamProjectsList;
    }

    /**
     * Determines whether a project has any downstream projects.
     * 
     * @param currentProject
     *            - The project in question
     * @return - true: Current project has downstream projects; false: Current project does not have any downstream projects
     */
    public static boolean hasDownstreamProjects(final AbstractProject<?, ?> currentProject) {
        return (getDownstreamProjects(currentProject).size() > 0);
    }

    /**
     * Determines if a manual trigger of the downstream project from the current upstream project is required.
     * 
     * @param upstreamProject
     *            - The upstream project
     * @param downstreamProject
     *            - The downstream project
     * 
     * @return - true: Manual trigger required; false: Manual trigger not required
     */
    public static boolean isManualTrigger(final AbstractProject<?, ?> upstreamProject, final AbstractProject<?, ?> downstreamProject) {

        boolean manualTrigger = false;
        if ((upstreamProject != null) && (downstreamProject != null)) {
            final DescribableList<Publisher, Descriptor<Publisher>> upstreamPublishersLists = upstreamProject.getPublishersList();

            for (final Publisher upstreamPub : upstreamPublishersLists) {
                if (upstreamPub instanceof BuildPipelineTrigger) {
                    final String manualDownstreamProjects = ((BuildPipelineTrigger) upstreamPub).getDownstreamProjectNames();
                    final String[] downstreamProjs = manualDownstreamProjects.split(",");
                    for (final String nextProj : downstreamProjs) {
                        if (Jenkins.getInstance().getItem(nextProj.trim(), upstreamProject) == downstreamProject) {
                            manualTrigger = true;
                            break;
                        }
                    }
                }
            }
        }

        return manualTrigger;

    }

    /**
     * Gets the ParametersAction of an AbstractProject
     * 
     * @param project
     *            - The AbstractProject
     * @return The ParametersAction of the AbstractProject
     */
    public static ParametersAction getProjectParametersAction(final AbstractProject<?, ?> project) {
        if (project != null) {
            final ParametersDefinitionProperty property = project.getProperty(ParametersDefinitionProperty.class);
            if (property == null) {
                return null;
            }

            final List<ParameterValue> parameters = new ArrayList<ParameterValue>();
            for (final ParameterDefinition pd : property.getParameterDefinitions()) {
                final ParameterValue param = pd.getDefaultParameterValue();
                if (param != null) {
                    parameters.add(param);
                }
            }
            return new ParametersAction(parameters);
        } else {
            return null;
        }
    }
}
