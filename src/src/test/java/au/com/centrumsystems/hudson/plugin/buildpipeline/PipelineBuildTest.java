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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import hudson.model.FreeStyleBuild;
import hudson.model.AbstractBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Hudson;
import hudson.tasks.BuildTrigger;

import java.util.Calendar;

import org.junit.Before;
import org.junit.Test;
import org.jvnet.hudson.test.HudsonTestCase;

import au.com.centrumsystems.hudson.plugin.buildpipeline.trigger.BuildPipelineTrigger;
import au.com.centrumsystems.hudson.plugin.util.HudsonResult;

public class PipelineBuildTest extends HudsonTestCase {

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void testGetBuildProgress() {
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
    public void testCalculatePercentage() throws Exception {
        final PipelineBuild pb = new PipelineBuild();

        assertEquals(10, pb.calculatePercentage(10, 100));
        assertEquals(100, pb.calculatePercentage(100, 100));
        assertEquals(100, pb.calculatePercentage(110, 100));
        assertEquals(66, pb.calculatePercentage(2, 3));
        assertEquals(100, pb.calculatePercentage(2, 0));
    }

    @Test
    public void testGetDownstreamPipeline() throws Exception {
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

        final FreeStyleProject project1 = createFreeStyleProject(proj1);
        final FreeStyleProject project2 = createFreeStyleProject(proj2);
        final BuildTrigger trigger2 = new BuildTrigger(proj2, true);
        final FreeStyleProject project3 = createFreeStyleProject(proj3);
        final BuildTrigger trigger3 = new BuildTrigger(proj3, true);
        createFreeStyleProject(proj4);
        final BuildTrigger trigger4 = new BuildTrigger(proj4, true);
        createFreeStyleProject(proj5);
        final BuildTrigger trigger5 = new BuildTrigger(proj5, true);

        // Project 1 -> Project 2 -> Project 4
        // -> Project 3
        project1.getPublishersList().add(trigger2);
        project1.getPublishersList().add(trigger3);
        project2.getPublishersList().add(trigger4);
        // Important; we must do this step to ensure that the dependency graphs
        // are updated
        Hudson.getInstance().rebuildDependencyGraph();

        // Build project1
        FreeStyleBuild build1 = buildAndAssertSuccess(project1);
        // When all building is complete retrieve the last builds
        waitUntilNoActivity();
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
        Hudson.getInstance().rebuildDependencyGraph();

        // Build project1
        build1 = buildAndAssertSuccess(project1);
        // When all building is complete retrieve the last builds
        waitUntilNoActivity();
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
        Hudson.getInstance().rebuildDependencyGraph();

        // Build project1
        build1 = buildAndAssertSuccess(project1);
        // When all building is complete retrieve the last builds
        waitUntilNoActivity();
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
    public void testGetCurrentBuildResult() throws Exception {
        final String proj1 = "Proj1";
        final String proj2 = "Proj2";
        BuildPipelineTrigger trigger2;

        final FreeStyleProject project1 = createFreeStyleProject(proj1);
        trigger2 = new BuildPipelineTrigger(proj2, null);

        project1.getPublishersList().add(trigger2);
        // Important; we must do this step to ensure that the dependency graphs
        // are updated
        Hudson.getInstance().rebuildDependencyGraph();

        // Build project1
        final FreeStyleBuild build1 = buildAndAssertSuccess(project1);
        // When all building is complete retrieve the last builds
        waitUntilNoActivity();

        final PipelineBuild pb1 = new PipelineBuild(build1, null, null);
        assertEquals("Build result is incorrect.", HudsonResult.SUCCESS.toString(), pb1.getCurrentBuildResult());
    }

    @Test
    public void testGetUpstreamPipelineBuild() throws Exception {
        final String proj1 = "Proj1";
        final String proj2 = "Proj2";

        final FreeStyleProject project1 = createFreeStyleProject(proj1);
        final FreeStyleProject project2 = createFreeStyleProject(proj2);

        project1.getPublishersList().add(new BuildTrigger(proj2, false));
        // Important; we must do this step to ensure that the dependency graphs
        // are updated
        Hudson.getInstance().rebuildDependencyGraph();

        // Build project1
        final FreeStyleBuild build1 = buildAndAssertSuccess(project1);
        // When all building is complete retrieve the last builds
        waitUntilNoActivity();
        final FreeStyleBuild build2 = project2.getLastBuild();

        final PipelineBuild pb1 = new PipelineBuild(build1, null, null);
        final PipelineBuild pb2 = new PipelineBuild(build2, null, build1);
        assertEquals("Upstream PipelineBuild is incorrect.", pb1.toString(), pb2.getUpstreamPipelineBuild().toString());
    }

    @Test
    public void testGetUpstreamBuildResult() throws Exception {
        final String proj1 = "Proj1";
        final String proj2 = "Proj2";

        final FreeStyleProject project1 = createFreeStyleProject(proj1);
        final FreeStyleProject project2 = createFreeStyleProject(proj2);
        final BuildPipelineTrigger trigger2 = new BuildPipelineTrigger(proj2, null);

        project1.getPublishersList().add(trigger2);
        // Important; we must do this step to ensure that the dependency graphs
        // are updated
        Hudson.getInstance().rebuildDependencyGraph();

        // Build project1
        final FreeStyleBuild build1 = buildAndAssertSuccess(project1);
        // When all building is complete retrieve the last builds
        waitUntilNoActivity();
        final FreeStyleBuild build2 = project2.getLastBuild();

        final PipelineBuild pb1 = new PipelineBuild(build2, null, build1);
        assertEquals("Upstream build result is incorrect.", HudsonResult.SUCCESS.toString(), pb1.getUpstreamBuildResult());
    }

    @Test
    public void testToString() throws Exception {
        final String proj1 = "Proj1";
        final String proj1ToString = "Project: " + proj1 + " : Build: 1";
        FreeStyleBuild build1;
        final FreeStyleProject project1 = createFreeStyleProject(proj1);
        build1 = buildAndAssertSuccess(project1);
        // When all building is complete retrieve the last builds
        waitUntilNoActivity();

        final PipelineBuild pb = new PipelineBuild(build1, null, null);

        assertEquals("PipelineBuild.toString is incorrect.", proj1ToString, pb.toString());
    }

    @Test
    public void testGetBuildDescription() throws Exception {
        final String proj1 = "Proj1";
        final String proj1BuildDescFail = "Pending build of project: " + proj1;
        final String proj1BuildDescSuccess = proj1 + " #1";
        FreeStyleBuild build1;
        final FreeStyleProject project1 = createFreeStyleProject(proj1);
        final PipelineBuild pb = new PipelineBuild(null, project1, null);

        assertEquals("The build description is incorrect.", proj1BuildDescFail, pb.getBuildDescription());

        build1 = buildAndAssertSuccess(project1);
        // When all building is complete retrieve the last builds
        waitUntilNoActivity();
        pb.setCurrentBuild(build1);

        assertEquals("The build description is incorrect.", proj1BuildDescSuccess, pb.getBuildDescription());
    }

    @Test
    public void testGetBuildDuration() throws Exception {
        final String proj1 = "Proj1";
        FreeStyleBuild build1;
        final FreeStyleProject project1 = createFreeStyleProject(proj1);

        build1 = buildAndAssertSuccess(project1);
        waitUntilNoActivity();
        final PipelineBuild pb = new PipelineBuild(build1, project1, null);

        assertEquals("The build duration is incorrect.", build1.getDurationString(), pb.getBuildDuration());
    }

    @Test
    public void testHasBuildPermission() throws Exception {
        final String proj1 = "Proj1";
        final FreeStyleProject project1 = createFreeStyleProject(proj1);
        final PipelineBuild pb = new PipelineBuild(null, project1, null);

        // Since no Hudson security is in place this method should return true
        assertTrue(pb.hasBuildPermission());
    }

    @Test
    public void getPipelineVersion() throws Exception {
        final String proj1 = "Proj1";
        FreeStyleProject project1;
        FreeStyleBuild build1;
        project1 = createFreeStyleProject(proj1);
        build1 = buildAndAssertSuccess(project1);
        // When all building is complete retrieve the last builds
        waitUntilNoActivity();

        final PipelineBuild pb = new PipelineBuild(build1, project1, null);
        assertEquals(String.valueOf(build1.getNumber()), pb.getPipelineVersion());
    }

    @Test
    public void testIsReadyToBeManuallyBuilt() throws Exception {
        String upstreamProjectName = "Proj1";
        String downstreamProjectName = "Proj2";

        FreeStyleProject upstreamProject = createFreeStyleProject(upstreamProjectName);
        FreeStyleProject downstreamProject = createFreeStyleProject(downstreamProjectName);

        upstreamProject.getPublishersList().add(new BuildPipelineTrigger(downstreamProjectName, null));
        Hudson.getInstance().rebuildDependencyGraph();

        FreeStyleBuild upstreamBuild = buildAndAssertSuccess(upstreamProject);
        PipelineBuild pipelineBuildWithPermission = new PipelineBuild(null, downstreamProject, upstreamBuild) {
            public boolean hasBuildPermission() { return true; }
        };
        assertTrue(pipelineBuildWithPermission.isReadyToBeManuallyBuilt());

        PipelineBuild pipelineBuildWithoutPermission = new PipelineBuild(null, downstreamProject, upstreamBuild) {
            public boolean hasBuildPermission() { return false; }
        };
        assertFalse(pipelineBuildWithoutPermission.isReadyToBeManuallyBuilt());
    }

}



