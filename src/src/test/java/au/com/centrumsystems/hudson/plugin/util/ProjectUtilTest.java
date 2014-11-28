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
import hudson.model.Hudson;
import hudson.model.ParametersAction;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.StringParameterDefinition;
import hudson.tasks.BuildTrigger;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.junit.Before;
import org.junit.Test;
import org.jvnet.hudson.test.HudsonTestCase;

import au.com.centrumsystems.hudson.plugin.buildpipeline.PipelineBuild;
import au.com.centrumsystems.hudson.plugin.buildpipeline.trigger.BuildPipelineTrigger;

public class ProjectUtilTest extends HudsonTestCase {

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void testGetDownstreamProjects() throws IOException {
        final String proj1 = "Proj1";
        final String proj2 = "Proj2";
        final String proj3 = "Proj3";

        // Create a test project
        final FreeStyleProject project1 = createFreeStyleProject(proj1);
        final FreeStyleProject project2 = createFreeStyleProject(proj2);

        // Add project2 as a post build action: build other project
        project1.getPublishersList().add(new BuildPipelineTrigger(proj2, null));
        project1.getPublishersList().add(new BuildPipelineTrigger(proj3, null));

        // Important; we must do this step to ensure that the dependency graphs are updated
        Hudson.getInstance().rebuildDependencyGraph();

        // Test the method
        final List<AbstractProject<?, ?>> dsProjects = ProjectUtil.getDownstreamProjects(project1);
        assertEquals(project1.getName() + " should have a downstream project " + project2.getName(), project2, dsProjects.get(0));
    }

    @Test
    public void testIsManualTrigger() throws IOException {
        final String proj1 = "Proj1";
        final String proj2 = "Proj2";
        final String proj3 = "Proj3";

        // Create a test project
        final FreeStyleProject project1 = createFreeStyleProject(proj1);
        final FreeStyleProject project2 = createFreeStyleProject(proj2);
        final FreeStyleProject project3 = createFreeStyleProject(proj3);

        // Add TEST_PROJECT2 as a Manually executed pipeline project
        // Add TEST_PROJECT3 as a Post-build action -> build other projects
        project1.getPublishersList().add(new BuildPipelineTrigger(proj2, null));
        project1.getPublishersList().add(new BuildTrigger(proj3, true));

        // Important; we must do this step to ensure that the dependency graphs are updated
        Hudson.getInstance().rebuildDependencyGraph();

        // Test the method
        assertTrue(proj2 + " should be a manual trigger", ProjectUtil.isManualTrigger(project1, project2));
        assertFalse(proj3 + " should be an automatic trigger", ProjectUtil.isManualTrigger(project1, project3));

        assertFalse(ProjectUtil.isManualTrigger(null, null));
    }

    @Test
    public void testHasDownstreamProjects() throws IOException {
        final String proj1 = "Proj1";
        final String proj2 = "Proj2";
        final String proj3 = "Proj3";

        // Create a test project
        final FreeStyleProject project1 = createFreeStyleProject(proj1);
        createFreeStyleProject(proj2);
        createFreeStyleProject(proj3);

        // Add project2 as a post build action: build other project
        project1.getPublishersList().add(new BuildPipelineTrigger(proj2, null));
        project1.getPublishersList().add(new BuildTrigger(proj3, true));

        // Important; we must do this step to ensure that the dependency graphs are updated
        Hudson.getInstance().rebuildDependencyGraph();

        // Test the method
        assertTrue(project1.getName() + " should have downstream projects", ProjectUtil.hasDownstreamProjects(project1));
    }

    @Test
    public void testGetProjectURL() throws URISyntaxException, IOException {
        final String proj1 = "Proj 1";
        final String proj1Url = "job/Proj%201/";

        // Create a test project
        final FreeStyleProject project1 = createFreeStyleProject(proj1);
        final PipelineBuild pipelineBuild = new PipelineBuild(project1);

        assertEquals("The project URL should have been " + proj1Url, proj1Url, pipelineBuild.getProjectURL());
    }

    @Test
    public void testGetProjectParametersAction() throws IOException, InterruptedException, ExecutionException {
        final String proj1 = "Proj1";
        final String proj2 = "Proj2";
        final String paramKey = "testKey";
        final String paramValue = "testValue";

        // Create a test project
        final FreeStyleProject project1 = createFreeStyleProject(proj1);
        // Add a String parameter
        project1.addProperty((new ParametersDefinitionProperty(new StringParameterDefinition(paramKey, paramValue))));
        final FreeStyleProject project2 = createFreeStyleProject(proj2);

        // Important; we must do this step to ensure that the dependency graphs are updated
        Hudson.getInstance().rebuildDependencyGraph();

        // Test the method
        ParametersAction params = ProjectUtil.getProjectParametersAction(project1);
        assertEquals(params.getParameter(paramKey).getName(), paramKey);
        params = ProjectUtil.getProjectParametersAction(project2);
        assertNull(params);

        assertNull(ProjectUtil.getProjectParametersAction(null));
    }
}
