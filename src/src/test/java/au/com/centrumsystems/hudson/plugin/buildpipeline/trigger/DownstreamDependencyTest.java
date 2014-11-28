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
 */package au.com.centrumsystems.hudson.plugin.buildpipeline.trigger;

import hudson.model.FreeStyleProject;
import hudson.model.Hudson;
import hudson.tasks.BuildTrigger;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.jvnet.hudson.test.HudsonTestCase;

/**
 * @author Centrum Systems
 * 
 */
public class DownstreamDependencyTest extends HudsonTestCase {

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void testDownstreamDependency() throws IOException {
        final String proj1 = "Proj1";
        final String proj2 = "Proj2";
        final FreeStyleProject project1 = createFreeStyleProject(proj1);
        final FreeStyleProject project2 = createFreeStyleProject(proj2);

        final DownstreamDependency myDD = new DownstreamDependency(project1, project2);
        assertEquals("Upstream project should be " + proj1, project1, myDD.getUpstreamProject());
        assertEquals("Downstream project should be " + proj2, project2, myDD.getDownstreamProject());
    }

    @Test
    public void testShouldTriggerBuild() throws Exception {
        final String proj1 = "Proj1";
        final String proj2 = "Proj2";
        final String proj3 = "Proj3";
        final FreeStyleProject project1 = createFreeStyleProject(proj1);
        final FreeStyleProject project2 = createFreeStyleProject(proj2);
        final FreeStyleProject project3 = createFreeStyleProject(proj3);

        // Add TEST_PROJECT2 as a Manually executed pipeline project
        // Add TEST_PROJECT3 as a Post-build action -> build other projects
        project1.getPublishersList().add(new BuildPipelineTrigger(proj2, null));
        project1.getPublishersList().add(new BuildTrigger(proj3, true));

        // Important; we must do this step to ensure that the dependency graphs are updated
        Hudson.getInstance().rebuildDependencyGraph();

        // Build project1 and wait until completion
        buildAndAssertSuccess(project1);
        waitUntilNoActivity();

        assertNull(project2.getLastBuild());
        assertNotNull(project3.getLastBuild());
    }

}
