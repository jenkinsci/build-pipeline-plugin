package au.com.centrumsystems.hudson.plugin.buildpipeline;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hudson.model.ItemGroup;
import hudson.model.TopLevelItem;

import jenkins.model.Jenkins;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class BuildPipelineViewConstructorTest {

    private static final String BP_VIEW_NAME = "MyTestView";
    private static final String BP_VIEW_TITLE = "MyTestViewTitle";
    private static final String PROJ_1 = "Proj1";
    private final DownstreamProjectGridBuilder gridBuilder = new DownstreamProjectGridBuilder(PROJ_1);
    private final DownstreamProjectGridBuilder nullGridBuilder = new DownstreamProjectGridBuilder("");
    private static final String NO_OF_BUILDS = "5";

    private JenkinsRule jenkins;

    @BeforeEach
    void beforeEach(JenkinsRule rule) {
        jenkins = rule;
    }

    @Test
    void testAlwaysAllowManualTrigger() {
        // True
        BuildPipelineView testView = new BuildPipelineView(BP_VIEW_NAME, BP_VIEW_TITLE, gridBuilder, NO_OF_BUILDS, true, true, false, false, false, 2, null, null, null, null, null);
        assertTrue(testView.isAlwaysAllowManualTrigger());

        // False
        testView = new BuildPipelineView(BP_VIEW_NAME, BP_VIEW_TITLE, nullGridBuilder, NO_OF_BUILDS, true, false, false, false, false, 2, null, null, null, null, null);
        assertFalse(testView.isAlwaysAllowManualTrigger());
    }

    @Test
    void testRefreshFrequency() {

        // False
        final BuildPipelineView testView = new BuildPipelineView(BP_VIEW_NAME, BP_VIEW_TITLE, nullGridBuilder, NO_OF_BUILDS, true, false, false, false, false, 2, null, null, null, null, null);
        assertThat(testView.getRefreshFrequency(), is(2));
        assertThat(testView.getRefreshFrequencyInMillis(), is(2000));
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
                    return Jenkins.get();
                }
            };
        }
    }
}
