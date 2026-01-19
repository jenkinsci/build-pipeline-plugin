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
package au.com.centrumsystems.hudson.plugin.util;

import hudson.model.AbstractProject;
import hudson.model.FreeStyleProject;
import hudson.model.ParametersAction;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.StringParameterDefinition;
import hudson.tasks.BuildTrigger;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import au.com.centrumsystems.hudson.plugin.buildpipeline.PipelineBuild;
import au.com.centrumsystems.hudson.plugin.buildpipeline.trigger.BuildPipelineTrigger;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@WithJenkins
class ProjectUtilTest {

    private JenkinsRule jenkins;

    @BeforeEach
    void beforeEach(JenkinsRule rule) {
        jenkins = rule;
    }

    @Test
    void testGetDownstreamProjects() throws Exception {
        final String proj1 = "Proj1";
        final String proj2 = "Proj2";
        final String proj3 = "Proj3";

        // Create a test project
        final FreeStyleProject project1 = jenkins.createFreeStyleProject(proj1);
        final FreeStyleProject project2 = jenkins.createFreeStyleProject(proj2);

        // Add project2 as a post build action: build other project
        project1.getPublishersList().add(new BuildPipelineTrigger(proj2, null));
        project1.getPublishersList().add(new BuildPipelineTrigger(proj3, null));

        // Important; we must do this step to ensure that the dependency graphs are updated
        jenkins.getInstance().rebuildDependencyGraph();

        // Test the method
        final List<AbstractProject<?, ?>> dsProjects = ProjectUtil.getDownstreamProjects(project1);
        assertEquals(project2, dsProjects.get(0), project1.getName() + " should have a downstream project " + project2.getName());
    }

    @Test
    void testIsManualTrigger() throws Exception {
        final String proj1 = "Proj1";
        final String proj2 = "Proj2";
        final String proj3 = "Proj3";

        // Create a test project
        final FreeStyleProject project1 = jenkins.createFreeStyleProject(proj1);
        final FreeStyleProject project2 = jenkins.createFreeStyleProject(proj2);
        final FreeStyleProject project3 = jenkins.createFreeStyleProject(proj3);

        // Add TEST_PROJECT2 as a Manually executed pipeline project
        // Add TEST_PROJECT3 as a Post-build action -> build other projects
        project1.getPublishersList().add(new BuildPipelineTrigger(proj2, null));
        project1.getPublishersList().add(new BuildTrigger(proj3, true));

        // Important; we must do this step to ensure that the dependency graphs are updated
        jenkins.getInstance().rebuildDependencyGraph();

        // Test the method
        assertTrue(ProjectUtil.isManualTrigger(project1, project2), proj2 + " should be a manual trigger");
        assertFalse(ProjectUtil.isManualTrigger(project1, project3), proj3 + " should be an automatic trigger");

        assertFalse(ProjectUtil.isManualTrigger(null, null));
    }

    @Test
    void testHasDownstreamProjects() throws Exception {
        final String proj1 = "Proj1";
        final String proj2 = "Proj2";
        final String proj3 = "Proj3";

        // Create a test project
        final FreeStyleProject project1 = jenkins.createFreeStyleProject(proj1);
        jenkins.createFreeStyleProject(proj2);
        jenkins.createFreeStyleProject(proj3);

        // Add project2 as a post build action: build other project
        project1.getPublishersList().add(new BuildPipelineTrigger(proj2, null));
        project1.getPublishersList().add(new BuildTrigger(proj3, true));

        // Important; we must do this step to ensure that the dependency graphs are updated
        jenkins.getInstance().rebuildDependencyGraph();

        // Test the method
        assertTrue(ProjectUtil.hasDownstreamProjects(project1), project1.getName() + " should have downstream projects");
    }

    @Test
    void testGetProjectURL() throws Exception {
        final String proj1 = "Proj 1";
        final String proj1Url = "job/Proj%201/";

        // Create a test project
        final FreeStyleProject project1 = jenkins.createFreeStyleProject(proj1);
        final PipelineBuild pipelineBuild = new PipelineBuild(project1);

        assertEquals(proj1Url, pipelineBuild.getProjectURL(), "The project URL should have been " + proj1Url);
    }

    @Test
    void testGetProjectParametersAction() throws Exception {
        final String proj1 = "Proj1";
        final String proj2 = "Proj2";
        final String paramKey = "testKey";
        final String paramValue = "testValue";

        // Create a test project
        final FreeStyleProject project1 = jenkins.createFreeStyleProject(proj1);
        // Add a String parameter
        project1.addProperty((new ParametersDefinitionProperty(new StringParameterDefinition(paramKey, paramValue))));
        final FreeStyleProject project2 = jenkins.createFreeStyleProject(proj2);

        // Important; we must do this step to ensure that the dependency graphs are updated
        jenkins.getInstance().rebuildDependencyGraph();

        // Test the method
        ParametersAction params = ProjectUtil.getProjectParametersAction(project1);
        assertEquals(paramKey, params.getParameter(paramKey).getName());
        params = ProjectUtil.getProjectParametersAction(project2);
        assertNull(params);

        assertNull(ProjectUtil.getProjectParametersAction(null));
    }
}
