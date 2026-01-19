/*
 * The MIT License
 *
 * Copyright (c) 2016 the Jenkins project
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
package au.com.centrumsystems.hudson.plugin.buildpipeline.extension;

import au.com.centrumsystems.hudson.plugin.buildpipeline.BuildPipelineView;
import au.com.centrumsystems.hudson.plugin.buildpipeline.BuildPipelineViewTest;
import au.com.centrumsystems.hudson.plugin.buildpipeline.DownstreamProjectGridBuilder;
import au.com.centrumsystems.hudson.plugin.buildpipeline.trigger.BuildPipelineTrigger;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.Cause;
import hudson.model.FreeStyleProject;
import hudson.model.ParametersAction;
import hudson.model.Run;
import hudson.model.StringParameterValue;
import hudson.triggers.SCMTrigger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.TestBuilder;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.Mockito.mock;

/**
 * @author dalvizu
 */
@WithJenkins
class BuildCardExtensionTest {

    private JenkinsRule jenkins;

    @BeforeEach
    void beforeEach(JenkinsRule rule) {
        jenkins = rule;
    }

    @Test
    void testBuildCards() {
        assertEquals(1, BuildCardExtension.all().size());
    }

    @Test
    @Issue("JENKINS-30801")
    void testRetriggerSuccessfulBuild() throws Exception {
        final FreeStyleProject upstreamBuild = jenkins.createFreeStyleProject("upstream");
        final FreeStyleProject downstreamBuild = jenkins.createFreeStyleProject("downstream");
        upstreamBuild.getPublishersList().add(new BuildPipelineTrigger("downstream", null));
        downstreamBuild.getBuildersList().add(new TestBuilder() {
            @Override
            public boolean perform(AbstractBuild<?, ?> abstractBuild, Launcher launcher, BuildListener buildListener) {
                abstractBuild.addAction(new MockAction());
                return true;
            }
        });

        // Important; we must do this step to ensure that the dependency graphs
        // are updated
        jenkins.getInstance().rebuildDependencyGraph();

        // mock the upstream build as being caused by SCM trigger
        Cause mockScmTriggerCause = new SCMTrigger.SCMTriggerCause("mock");
        upstreamBuild.scheduleBuild2(0, mockScmTriggerCause);
        jenkins.waitUntilNoActivity();

        // mock trigger the downstream build as being triggered by upstream
        ParametersAction parametersAction = new ParametersAction(
                Arrays.asList(new StringParameterValue("foo", "bar")));
        Cause.UpstreamCause upstreamCause = new hudson.model.Cause.UpstreamCause(
                (Run<?, ?>) upstreamBuild.getLastBuild());
        downstreamBuild.scheduleBuild2(0, upstreamCause, parametersAction);
        jenkins.waitUntilNoActivity();

        BuildPipelineView pipeline = BuildPipelineViewTest.BuildPipelineViewFactory.getBuildPipelineView("pipeline", "",
                new DownstreamProjectGridBuilder(upstreamBuild.getFullName()), "1", false);
        pipeline.setBuildCard(new StandardBuildCard());
        jenkins.getInstance().addView(pipeline);
        assertNotNull(downstreamBuild.getLastBuild());
        // re-run the build as if we clicked re-run in the UI
        pipeline.rerunBuild(downstreamBuild.getLastBuild().getExternalizableId());
        jenkins.waitUntilNoActivity();

        // MockAction is not copied from one run to another
        assertEquals(1, downstreamBuild.getLastBuild().getActions(MockAction.class).size());
        // upstream cause copied
        assertEquals(1, downstreamBuild.getLastBuild().getCauses().size());
        // parametersAction copied
        assertNotNull(downstreamBuild.getLastBuild().getAction(ParametersAction.class));
        StringParameterValue stringParam = (StringParameterValue) downstreamBuild.getLastBuild()
                .getAction(ParametersAction.class).getParameter("foo");
        assertEquals("bar", stringParam.value);
        assertEquals(upstreamCause, downstreamBuild.getLastBuild().getCauses().get(0));
        assertEquals(mockScmTriggerCause, upstreamCause.getUpstreamCauses().get(0));
    }

    @Test
    void testFilterUserIdCause() throws Exception {
        final FreeStyleProject upstreamBuild = jenkins.createFreeStyleProject("upstream");
        final FreeStyleProject downstreamBuild = jenkins.createFreeStyleProject("downstream");
        upstreamBuild.getPublishersList().add(new BuildPipelineTrigger("downstream", null));
        // Important; we must do this step to ensure that the dependency graphs
        // are updated
        jenkins.getInstance().rebuildDependencyGraph();
        Cause mockUserIdCause = mock(Cause.UserIdCause.class);
        upstreamBuild.scheduleBuild2(0, mockUserIdCause);
        jenkins.waitUntilNoActivity();
        Cause.UpstreamCause upstreamCause = new hudson.model.Cause.UpstreamCause(
                (Run<?, ?>) upstreamBuild.getLastBuild());
        downstreamBuild.scheduleBuild2(0, upstreamCause);
        jenkins.waitUntilNoActivity();

        BuildPipelineView pipeline = BuildPipelineViewTest.BuildPipelineViewFactory.getBuildPipelineView("pipeline", "",
                new DownstreamProjectGridBuilder(upstreamBuild.getFullName()), "1", false);
        pipeline.setBuildCard(new StandardBuildCard());
        jenkins.getInstance().addView(pipeline);
        assertNotNull(downstreamBuild.getLastBuild());
        // re-run the build as if we clicked re-run in the UI
        pipeline.rerunBuild(upstreamBuild.getLastBuild().getExternalizableId());
        jenkins.waitUntilNoActivity();
        assertEquals(2, upstreamBuild.getBuilds().size());
        assertNotNull(upstreamBuild.getLastBuild().getCause(Cause.UserIdCause.class));
        assertNotSame(upstreamBuild.getLastBuild().getCause(Cause.UserIdCause.class),
                mockUserIdCause);
    }

    public static class MockAction implements Action, Serializable {

        @Serial
        private static final long serialVersionUID = 5677631606354259250L;

        @Override
        public String getIconFileName() {
            return null;
        }

        @Override
        public String getDisplayName() {
            return null;
        }

        @Override
        public String getUrlName() {
            return null;
        }
    }
}
