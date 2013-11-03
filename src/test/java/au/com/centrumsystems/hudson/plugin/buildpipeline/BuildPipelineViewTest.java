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

import hudson.model.Action;
import hudson.model.FreeStyleBuild;
import hudson.model.ItemGroup;
import hudson.model.TopLevelItem;
import hudson.model.Cause.UpstreamCause;
import hudson.model.FreeStyleProject;
import hudson.model.Hudson;
import hudson.model.Job;
import hudson.model.Run;
import jenkins.model.Jenkins;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.jvnet.hudson.test.HudsonTestCase;

import au.com.centrumsystems.hudson.plugin.buildpipeline.trigger.BuildPipelineTrigger;

/**
 * Test Build Pipeline View
 *
 * @author Centrum Systems
 *
 */
public class BuildPipelineViewTest extends HudsonTestCase {

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
	}

	@Test
	public void testGetSelectedProject() throws IOException {
		final String bpViewName = "MyTestView";
		final String bpViewTitle = "MyTestViewTitle";
		final String proj1 = "Proj1";
		final String noOfBuilds = "5";
		createFreeStyleProject(proj1);

		// Test a valid case
        DownstreamProjectGridBuilder gridBuilder = new DownstreamProjectGridBuilder(proj1);
		BuildPipelineView testView = BuildPipelineViewFactory.getBuildPipelineView(bpViewName, bpViewTitle, gridBuilder, noOfBuilds, false);

		Job<?, ?> testSelectedProject = gridBuilder.getFirstJob(testView);

		assertEquals(proj1, testSelectedProject.getName());

		// Test the null case
        gridBuilder = new DownstreamProjectGridBuilder("");
		testView = BuildPipelineViewFactory.getBuildPipelineView(bpViewName, bpViewTitle, gridBuilder, noOfBuilds, false);
		testSelectedProject = gridBuilder.getFirstJob(testView);

		assertNull(testSelectedProject);
	}

	@Test
	public void testHasBuildPermission() throws IOException {
		final String bpViewName = "MyTestView";
		final String bpViewTitle = "MyTestViewTitle";
		final String proj1 = "Proj1";
		final String noOfBuilds = "5";
		final FreeStyleProject project1 = createFreeStyleProject(proj1);

		final BuildPipelineView testView = new BuildPipelineView(bpViewName, bpViewTitle,
                new DownstreamProjectGridBuilder(proj1), noOfBuilds, false, null);
		assertTrue(testView.hasBuildPermission());
	}

	@Test
	public void testTriggerOnlyLatestJob() throws IOException {
		final String bpViewName = "MyTestView";
		final String bpViewTitle = "MyTestViewTitle";
		final String proj1 = "Proj1";
		final String noOfBuilds = "5";
		createFreeStyleProject(proj1);

		// True
		BuildPipelineView testView = new BuildPipelineView(bpViewName, bpViewTitle, new DownstreamProjectGridBuilder(proj1), noOfBuilds, true, null);
		assertTrue(proj1, testView.isTriggerOnlyLatestJob());

		// False
		testView = new BuildPipelineView(bpViewName, bpViewTitle, new DownstreamProjectGridBuilder(""), noOfBuilds, false, null);
		assertFalse(proj1, testView.isTriggerOnlyLatestJob());
	}

	@Test
	public void testAlwaysAllowManualTrigger() throws IOException {
		final String bpViewName = "MyTestView";
		final String bpViewTitle = "MyTestViewTitle";
		final String proj1 = "Proj1";
		final String noOfBuilds = "5";
		createFreeStyleProject(proj1);

		// True
		BuildPipelineView testView = new BuildPipelineView(bpViewName, bpViewTitle, new DownstreamProjectGridBuilder(proj1), noOfBuilds, true, true, false, false, false, 2, null);
		assertTrue("Failed to set AlwaysAllowManualTrigger flag", testView.isAlwaysAllowManualTrigger());

		// False
		testView = new BuildPipelineView(bpViewName, bpViewTitle, new DownstreamProjectGridBuilder(""), noOfBuilds, true, false, false, false, false, 2, null);
		assertFalse("Failed to unset AlwaysAllowManualTrigger flag", testView.isAlwaysAllowManualTrigger());
	}

	@Test
	public void testShowPipelineDefinitionHeader() throws IOException {
		final String bpViewName = "MyTestView";
		final String bpViewTitle = "MyTestViewTitle";
		final String proj1 = "Proj1";
		final String noOfBuilds = "5";
		createFreeStyleProject(proj1);

		// True
		BuildPipelineView testView = new BuildPipelineView(bpViewName, bpViewTitle, new DownstreamProjectGridBuilder(proj1), noOfBuilds, true, false, false, false, true, 2, null);
		assertTrue("Failed to set ShowPipelineDefinitionHeader flag", testView.isShowPipelineDefinitionHeader());

		// False
		testView = new BuildPipelineView(bpViewName, bpViewTitle, new DownstreamProjectGridBuilder(""), noOfBuilds, true, false, false, false, false, 2, null);
		assertFalse("Failed to unset ShowPipelineDefinitionHeader flag", testView.isShowPipelineDefinitionHeader());
	}

	@Test
	public void testShowPipelineParameters() throws IOException {
		final String bpViewName = "MyTestView";
		final String bpViewTitle = "MyTestViewTitle";
		final String proj1 = "Proj1";
		final String noOfBuilds = "5";
		createFreeStyleProject(proj1);

		// True
		BuildPipelineView testView = new BuildPipelineView(bpViewName, bpViewTitle, new DownstreamProjectGridBuilder(proj1), noOfBuilds, true, false, true, false, false, 2, null);
		assertTrue("Failed to set ShowPipelineParameters flag", testView.isShowPipelineParameters());

		// False
		testView = new BuildPipelineView(bpViewName, bpViewTitle, new DownstreamProjectGridBuilder(""), noOfBuilds, true, false, false, false, false, 2, null);
		assertFalse("Failed to unset ShowPipelineParameters flag", testView.isShowPipelineParameters());
	}

    @Test
    public void testShowPipelineParametersInHeaders() throws IOException {
        final String bpViewName = "MyTestView";
        final String bpViewTitle = "MyTestViewTitle";
        final String proj1 = "Proj1";
        final String noOfBuilds = "5";
        createFreeStyleProject(proj1);

        BuildPipelineView testView = new BuildPipelineView(bpViewName, bpViewTitle, new DownstreamProjectGridBuilder(proj1), noOfBuilds, true, false, true, true, false, 2, null);
        assertTrue("Failed to set ShowPipelineParametersInHeaders flag", testView.isShowPipelineParametersInHeaders());

        testView = new BuildPipelineView(bpViewName, bpViewTitle, new DownstreamProjectGridBuilder(proj1), noOfBuilds, true, false, true, false, false, 2, null);
        assertFalse("Failed to unset ShowPipelineParametersInHeaders flag", testView.isShowPipelineParametersInHeaders());
    }

	@Test
	public void testHasDownstreamProjects() throws IOException {
		final String bpViewName = "MyTestView";
		final String bpViewTitle = "MyTestViewTitle";
		final String proj1 = "Proj1";
		final String proj2 = "Proj2";
		final String noOfBuilds = "5";
		final FreeStyleProject project1 = createFreeStyleProject(proj1);
		final FreeStyleProject project2 = createFreeStyleProject(proj2);

		// Add project2 as a post build action: build other project
		project1.getPublishersList().add(new BuildPipelineTrigger(proj2, null));

		// Important; we must do this step to ensure that the dependency graphs are updated
		Hudson.getInstance().rebuildDependencyGraph();

		// Test a valid case
		final BuildPipelineView testView = new BuildPipelineView(bpViewName, bpViewTitle, new DownstreamProjectGridBuilder(proj1), noOfBuilds, false, null);

		assertTrue(testView.hasDownstreamProjects(project1));
		assertFalse(testView.hasDownstreamProjects(project2));
	}

	@Test
	public void testGetDownstreamProjects() throws IOException {
		final String bpViewName = "MyTestView";
		final String bpViewTitle = "MyTestViewTitle";
		final String proj1 = "Proj1";
		final String proj2 = "Proj2";
		final String noOfBuilds = "5";
		final FreeStyleProject project1 = createFreeStyleProject(proj1);
		final FreeStyleProject project2 = createFreeStyleProject(proj2);

		// Add project2 as a post build action: build other project
		project1.getPublishersList().add(new BuildPipelineTrigger(proj2, null));

		// Important; we must do this step to ensure that the dependency graphs are updated
		Hudson.getInstance().rebuildDependencyGraph();

		// Test a valid case
		final BuildPipelineView testView = new BuildPipelineView(bpViewName, bpViewTitle, new DownstreamProjectGridBuilder(proj1), noOfBuilds, false, null);

		assertEquals(testView.getDownstreamProjects(project1).get(0), project2);
		assertEquals(testView.getDownstreamProjects(project2).size(), 0);
	}

	@Test
	public void testGetBuildPipelineForm() throws Exception {
		final String bpViewName = "MyTestView";
		final String bpViewTitle = "MyTestViewTitle";
		final String proj1 = "Proj1";
		final String proj2 = "Proj2";
		final String proj3 = "Proj3";
		FreeStyleBuild build1;
		final String noOfBuilds = "5";
		final FreeStyleProject project1 = createFreeStyleProject(proj1);
		final FreeStyleProject project2 = createFreeStyleProject(proj2);
		final FreeStyleProject project3 = createFreeStyleProject(proj3);

		// Add project2 as a post build action: build other project
		project1.getPublishersList().add(new BuildPipelineTrigger(proj2, null));
		project2.getPublishersList().add(new BuildPipelineTrigger(proj3, null));

		// Important; we must do this step to ensure that the dependency graphs are updated
		Hudson.getInstance().rebuildDependencyGraph();

		// Build project1
		build1 = buildAndAssertSuccess(project1);
		waitUntilNoActivity();

		// Test a valid case
		final BuildPipelineView testView = BuildPipelineViewFactory.getBuildPipelineView(bpViewName, bpViewTitle, new DownstreamProjectGridBuilder(proj1), noOfBuilds, false);

		UpstreamCause upstreamCause = new hudson.model.Cause.UpstreamCause((Run<?, ?>) build1);
		final List<Action> buildActions = new ArrayList<Action>();
		project2.scheduleBuild(0, upstreamCause, buildActions.toArray(new Action[buildActions.size()]));
		waitUntilNoActivity();

		upstreamCause = new hudson.model.Cause.UpstreamCause((Run<?, ?>) project2.getBuildByNumber(1));
		project3.scheduleBuild(0, upstreamCause, buildActions.toArray(new Action[buildActions.size()]));
		waitUntilNoActivity();

		final BuildPipelineForm testForm = testView.getBuildPipelineForm();

		assertEquals(testForm.getProjectGrid().get(0,0).getName(), proj1);
		assertEquals(testForm.getProjectGrid().get(0,1).getName(), proj2);
		assertEquals(testForm.getProjectGrid().get(0,2).getName(), proj3);

		// Test a null case
		testView.setGridBuilder(new DownstreamProjectGridBuilder(""));
		assertNull(testView.getBuildPipelineForm());
	}

	@Test
	public void testOnJobRenamed() throws IOException {
		final String bpViewName = "MyTestView";
		final String bpViewTitle = "MyTestViewTitle";
		final String proj1 = "Proj1";
		final String proj2 = "Proj2";
		final String proj3 = "Proj3";
		final String noOfBuilds = "5";
		final FreeStyleProject project1 = createFreeStyleProject(proj1);

		// Add project2 as a post build action: build other project
		project1.getPublishersList().add(new BuildPipelineTrigger(proj2, null));

		// Important; we must do this step to ensure that the dependency graphs are updated
		Hudson.getInstance().rebuildDependencyGraph();

		// Test a valid case
		final BuildPipelineView testView = BuildPipelineViewFactory.getBuildPipelineView(bpViewName, bpViewTitle, new DownstreamProjectGridBuilder(proj1), noOfBuilds, false);

		assertEquals(testView.getJob(proj1), project1);
		project1.renameTo(proj3);
		assertEquals(testView.getJob(proj3), project1);
	}

	@Test
	public void testGetItems() throws IOException {
		final String bpViewName = "MyTestView";
		final String bpViewTitle = "MyTestViewTitle";
		final String proj1 = "Proj1";
		final String noOfBuilds = "5";
		final FreeStyleProject project1 = createFreeStyleProject(proj1);

		final BuildPipelineView testView = new BuildPipelineView(bpViewName, bpViewTitle,
                new DownstreamProjectGridBuilder(proj1), noOfBuilds, false, null);
		TopLevelItem item1 = Jenkins.getInstance().getItem(proj1);
		assertNotNull(item1);
		assertTrue(testView.getItems().contains(item1));

		final String proj2 = "Proj2";
		final FreeStyleProject project2 = createFreeStyleProject(proj2);
		TopLevelItem item2 = Jenkins.getInstance().getItem(proj2);
		assertNotNull(item2);
		assertFalse(testView.getItems().contains(item2));
	}

    /**
     * This is a factory to create an instance of the class under test. This helps to avoid a NPE in View.java when calling
     * getOwnerItemGroup and it's not set. This doesn't solve the root cause and it't only intended to make our tests succeed.
     */
    static class BuildPipelineViewFactory {
        public static BuildPipelineView getBuildPipelineView(final String bpViewName, final String bpViewTitle, final ProjectGridBuilder gridBuilder,
                final String noOfBuilds, final boolean triggerOnlyLatestJob) {
            return new BuildPipelineView(bpViewName, bpViewTitle, gridBuilder, noOfBuilds, triggerOnlyLatestJob, null) {

                @Override
                public ItemGroup<? extends TopLevelItem> getOwnerItemGroup() {
                    return Hudson.getInstance();
                }
            };
        }
    }
}
