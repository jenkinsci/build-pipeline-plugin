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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import hudson.model.FreeStyleBuild;
import hudson.model.AbstractBuild;
import hudson.model.FreeStyleProject;
import hudson.tasks.BuildTrigger;

import java.util.Calendar;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import au.com.centrumsystems.hudson.plugin.buildpipeline.trigger.BuildPipelineTrigger;
import au.com.centrumsystems.hudson.plugin.util.HudsonResult;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class PipelineBuildTest {

    private JenkinsRule jenkins;

    @BeforeEach
    void beforeEach(JenkinsRule rule) {
        jenkins = rule;
    }

    @Test
    void testGetBuildProgress() {
        final AbstractBuild<?, ?> mockBuild = mock(AbstractBuild.class);
        when(mockBuild.isBuilding()).thenReturn(true);
        final Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, -1);
        when(mockBuild.getTimestamp()).thenReturn(calendar);
        when(mockBuild.getEstimatedDuration()).thenReturn(120000L);

        final PipelineBuild pb = new PipelineBuild(mockBuild, null, null);
        final long progress = pb.getBuildProgress();
        assertTrue(progress > 0);
        assertTrue(progress < 100);
    }

    @Test
    void testCalculatePercentage() {
        final PipelineBuild pb = new PipelineBuild();

        assertEquals(10, pb.calculatePercentage(10, 100));
        assertEquals(100, pb.calculatePercentage(100, 100));
        assertEquals(100, pb.calculatePercentage(110, 100));
        assertEquals(66, pb.calculatePercentage(2, 3));
        assertEquals(100, pb.calculatePercentage(2, 0));
    }

    @Test
    void testGetDownstreamPipeline() throws Exception {
        final String proj1 = "Proj1";
        final String proj2 = "Proj2";
        final String proj3 = "Proj3";
        final String proj4 = "Proj4";
        final String proj5 = "Proj5";
        final String RESULT1 = "-Project: " + proj1 + " : Build: 1\n" + "--Project: " + proj2 + " : Build: 1\n" + "---Project: " + proj4
                + " : Build: 1\n" + "--Project: " + proj3 + " : Build: 1\n";
        final String RESULT2 = "-Project: " + proj1 + " : Build: 2\n" + "--Project: " + proj2 + " : Build: 2\n" + "--Project: " + proj3
                + " : Build: 2\n" + "---Project: " + proj4 + " : Build: 2\n";
        final String RESULT3 = "-Project: " + proj1 + " : Build: 3\n" + "--Project: " + proj2 + " : Build: 3\n" + "--Project: " + proj3
                + " : Build: 3\n" + "---Project: " + proj4 + " : Build: 3\n" + "---Project: " + proj5 + " : Build: 1\n";

        final FreeStyleProject project1 = jenkins.createFreeStyleProject(proj1);
        final FreeStyleProject project2 = jenkins.createFreeStyleProject(proj2);
        final BuildTrigger trigger2 = new BuildTrigger(proj2, true);
        final FreeStyleProject project3 = jenkins.createFreeStyleProject(proj3);
        final BuildTrigger trigger3 = new BuildTrigger(proj3, true);
        jenkins.createFreeStyleProject(proj4);
        final BuildTrigger trigger4 = new BuildTrigger(proj4, true);
        jenkins.createFreeStyleProject(proj5);
        final BuildTrigger trigger5 = new BuildTrigger(proj5, true);

        // Project 1 -> Project 2 -> Project 4
        // -> Project 3
        project1.getPublishersList().add(trigger2);
        project1.getPublishersList().add(trigger3);
        project2.getPublishersList().add(trigger4);
        // Important; we must do this step to ensure that the dependency graphs
        // are updated
        jenkins.getInstance().rebuildDependencyGraph();

        // Build project1
        FreeStyleBuild build1 = jenkins.buildAndAssertSuccess(project1);
        // When all building is complete retrieve the last builds
        jenkins.waitUntilNoActivity();
        PipelineBuild pb1 = new PipelineBuild(build1, null, null);
        final StringBuffer result = new StringBuffer();
        printDownstreamPipeline("", pb1, result);
        assertEquals(RESULT1, result.toString());

        // Project 1 -> Project 2
        // -> Project 3 -> Project 4
        project1.getPublishersList().add(trigger2);
        project1.getPublishersList().add(trigger3);
        project2.getPublishersList().remove(trigger4);
        project3.getPublishersList().add(trigger4);
        // Important; we must do this step to ensure that the dependency graphs
        // are updated
        jenkins.getInstance().rebuildDependencyGraph();

        // Build project1
        build1 = jenkins.buildAndAssertSuccess(project1);
        // When all building is complete retrieve the last builds
        jenkins.waitUntilNoActivity();
        pb1 = new PipelineBuild(build1, null, null);
        result.delete(0, result.length());
        printDownstreamPipeline("", pb1, result);
        assertEquals(RESULT2, result.toString());

        // Project 1 -> Project 2
        // -> Project 3 -> Project 4
        // -> Project 5
        project1.getPublishersList().add(trigger2);
        project1.getPublishersList().add(trigger3);
        project3.getPublishersList().add(trigger4);
        project3.getPublishersList().add(trigger5);
        // Important; we must do this step to ensure that the dependency graphs
        // are updated
        jenkins.getInstance().rebuildDependencyGraph();

        // Build project1
        build1 = jenkins.buildAndAssertSuccess(project1);
        // When all building is complete retrieve the last builds
        jenkins.waitUntilNoActivity();
        pb1 = new PipelineBuild(build1, null, null);
        result.delete(0, result.length());
        printDownstreamPipeline("", pb1, result);
        assertEquals(RESULT3, result.toString());
    }

    private void printDownstreamPipeline(final String prefix, final PipelineBuild pb, final StringBuffer result) {
        final String newPrefix = prefix + "-";

        result.append(newPrefix + pb.toString() + "\n");
        for (final PipelineBuild child : pb.getDownstreamPipeline()) {
            printDownstreamPipeline(newPrefix, child, result);
        }
    }

    @Test
    void testGetCurrentBuildResult() throws Exception {
        final String proj1 = "Proj1";
        final String proj2 = "Proj2";
        BuildPipelineTrigger trigger2;

        final FreeStyleProject project1 = jenkins.createFreeStyleProject(proj1);
        trigger2 = new BuildPipelineTrigger(proj2, null);

        project1.getPublishersList().add(trigger2);
        // Important; we must do this step to ensure that the dependency graphs
        // are updated
        jenkins.getInstance().rebuildDependencyGraph();

        // Build project1
        final FreeStyleBuild build1 = jenkins.buildAndAssertSuccess(project1);
        // When all building is complete retrieve the last builds
        jenkins.waitUntilNoActivity();

        final PipelineBuild pb1 = new PipelineBuild(build1, null, null);
        assertEquals(HudsonResult.SUCCESS.toString(), pb1.getCurrentBuildResult(), "Build result is incorrect.");
    }

    @Test
    void testGetUpstreamPipelineBuild() throws Exception {
        final String proj1 = "Proj1";
        final String proj2 = "Proj2";

        final FreeStyleProject project1 = jenkins.createFreeStyleProject(proj1);
        final FreeStyleProject project2 = jenkins.createFreeStyleProject(proj2);

        project1.getPublishersList().add(new BuildTrigger(proj2, false));
        // Important; we must do this step to ensure that the dependency graphs
        // are updated
        jenkins.getInstance().rebuildDependencyGraph();

        // Build project1
        final FreeStyleBuild build1 = jenkins.buildAndAssertSuccess(project1);
        // When all building is complete retrieve the last builds
        jenkins.waitUntilNoActivity();
        final FreeStyleBuild build2 = project2.getLastBuild();

        final PipelineBuild pb1 = new PipelineBuild(build1, null, null);
        final PipelineBuild pb2 = new PipelineBuild(build2, null, build1);
        assertEquals(pb1.toString(), pb2.getUpstreamPipelineBuild().toString(), "Upstream PipelineBuild is incorrect.");
    }

    @Test
    void testGetUpstreamBuildResult() throws Exception {
        final String proj1 = "Proj1";
        final String proj2 = "Proj2";

        final FreeStyleProject project1 = jenkins.createFreeStyleProject(proj1);
        final FreeStyleProject project2 = jenkins.createFreeStyleProject(proj2);
        final BuildPipelineTrigger trigger2 = new BuildPipelineTrigger(proj2, null);

        project1.getPublishersList().add(trigger2);
        // Important; we must do this step to ensure that the dependency graphs
        // are updated
        jenkins.getInstance().rebuildDependencyGraph();

        // Build project1
        final FreeStyleBuild build1 = jenkins.buildAndAssertSuccess(project1);
        // When all building is complete retrieve the last builds
        jenkins.waitUntilNoActivity();
        final FreeStyleBuild build2 = project2.getLastBuild();

        final PipelineBuild pb1 = new PipelineBuild(build2, null, build1);
        assertEquals(HudsonResult.SUCCESS.toString(), pb1.getUpstreamBuildResult(), "Upstream build result is incorrect.");
    }

    @Test
    void testToString() throws Exception {
        final String proj1 = "Proj1";
        final String proj1ToString = "Project: " + proj1 + " : Build: 1";
        FreeStyleBuild build1;
        final FreeStyleProject project1 = jenkins.createFreeStyleProject(proj1);
        build1 = jenkins.buildAndAssertSuccess(project1);
        // When all building is complete retrieve the last builds
        jenkins.waitUntilNoActivity();

        final PipelineBuild pb = new PipelineBuild(build1, null, null);

        assertEquals(proj1ToString, pb.toString(), "PipelineBuild.toString is incorrect.");
    }

    @Test
    void testGetBuildDescription() throws Exception {
        final String proj1 = "Proj1";
        final String proj1BuildDescFail = "Pending build of project: " + proj1;
        final String proj1BuildDescSuccess = proj1 + " #1";
        FreeStyleBuild build1;
        final FreeStyleProject project1 = jenkins.createFreeStyleProject(proj1);
        final PipelineBuild pb = new PipelineBuild(null, project1, null);

        assertEquals(proj1BuildDescFail, pb.getBuildDescription(), "The build description is incorrect.");

        build1 = jenkins.buildAndAssertSuccess(project1);
        // When all building is complete retrieve the last builds
        jenkins.waitUntilNoActivity();
        pb.setCurrentBuild(build1);

        assertEquals(proj1BuildDescSuccess, pb.getBuildDescription(), "The build description is incorrect.");
    }

    @Test
    void testGetBuildDuration() throws Exception {
        final String proj1 = "Proj1";
        FreeStyleBuild build1;
        final FreeStyleProject project1 = jenkins.createFreeStyleProject(proj1);

        build1 = jenkins.buildAndAssertSuccess(project1);
        jenkins.waitUntilNoActivity();
        final PipelineBuild pb = new PipelineBuild(build1, project1, null);

        assertEquals(build1.getDurationString(), pb.getBuildDuration(), "The build duration is incorrect.");
    }

    @Test
    void testHasBuildPermission() throws Exception {
        final String proj1 = "Proj1";
        final FreeStyleProject project1 = jenkins.createFreeStyleProject(proj1);
        final PipelineBuild pb = new PipelineBuild(null, project1, null);

        // Since no Hudson security is in place this method should return true
        assertTrue(pb.hasBuildPermission());
    }

    @Test
    void getPipelineVersion() throws Exception {
        final String proj1 = "Proj1";
        FreeStyleProject project1;
        FreeStyleBuild build1;
        project1 = jenkins.createFreeStyleProject(proj1);
        build1 = jenkins.buildAndAssertSuccess(project1);
        // When all building is complete retrieve the last builds
        jenkins.waitUntilNoActivity();

        final PipelineBuild pb = new PipelineBuild(build1, project1, null);
        assertEquals("#" + build1.getNumber(), pb.getPipelineVersion());
    }

    @Test
    void testIsReadyToBeManuallyBuilt() throws Exception {
        String upstreamProjectName = "Proj1";
        String downstreamProjectName = "Proj2";

        FreeStyleProject upstreamProject = jenkins.createFreeStyleProject(upstreamProjectName);
        FreeStyleProject downstreamProject = jenkins.createFreeStyleProject(downstreamProjectName);

        upstreamProject.getPublishersList().add(new BuildPipelineTrigger(downstreamProjectName, null));
        jenkins.getInstance().rebuildDependencyGraph();

        FreeStyleBuild upstreamBuild = jenkins.buildAndAssertSuccess(upstreamProject);
        PipelineBuild pipelineBuildWithPermission = new PipelineBuild(null, downstreamProject, upstreamBuild) {
            @Override
            public boolean hasBuildPermission() { return true; }
        };
        assertTrue(pipelineBuildWithPermission.isReadyToBeManuallyBuilt());

        PipelineBuild pipelineBuildWithoutPermission = new PipelineBuild(null, downstreamProject, upstreamBuild) {
            @Override
            public boolean hasBuildPermission() { return false; }
        };
        assertFalse(pipelineBuildWithoutPermission.isReadyToBeManuallyBuilt());
    }

    @Test
    void testTwoUpstreamRebuild() throws Exception {
        FreeStyleProject a = jenkins.createFreeStyleProject("A");
        FreeStyleProject b = jenkins.createFreeStyleProject("B");
        FreeStyleProject c = jenkins.createFreeStyleProject("C");

        a.getPublishersList().add(new BuildTrigger("C", false));
        b.getPublishersList().add(new BuildTrigger("C", false));

        jenkins.getInstance().rebuildDependencyGraph();

        FreeStyleBuild buildB = jenkins.buildAndAssertSuccess(b);

        jenkins.waitUntilNoActivity();


        FreeStyleBuild buildC = c.getLastBuild();

        assertNotNull(buildC);

        PipelineBuild pipelineBuild = new PipelineBuild(buildC, c, buildB);
        PipelineBuild upstream = pipelineBuild.getUpstreamPipelineBuild();
        assertEquals("1", upstream.getCurrentBuildNumber());
        assertEquals(b.getFullName(),  upstream.getProject().getFullName());
        assertFalse(upstream.isManualTrigger());
    }
}



