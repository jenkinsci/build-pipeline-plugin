package au.com.centrumsystems.hudson.plugin.buildpipeline;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import hudson.model.Hudson;
import hudson.model.ItemGroup;
import hudson.model.TopLevelItem;

import java.io.IOException;

import org.junit.Test;

public class BuildPipelineViewConstructorTest {

    final String bpViewName = "MyTestView";
    final String bpViewTitle = "MyTestViewTitle";
    final String proj1 = "Proj1";
    final DownstreamProjectGridBuilder gridBuilder = new DownstreamProjectGridBuilder(proj1);
    final DownstreamProjectGridBuilder nullGridBuilder = new DownstreamProjectGridBuilder("");
    final String noOfBuilds = "5";

    @Test
    public void testAlwaysAllowManualTrigger() throws IOException {
        // True
        BuildPipelineView testView = new BuildPipelineView(bpViewName, bpViewTitle, gridBuilder, noOfBuilds, true, true, false, false, false, 2, null, null, false);
        assertTrue(testView.isAlwaysAllowManualTrigger());

        // False
        testView = new BuildPipelineView(bpViewName, bpViewTitle, nullGridBuilder, noOfBuilds, true, false, false, false, false, 2, null, null, false);
        assertFalse(testView.isAlwaysAllowManualTrigger());
    }

    @Test
    public void testShowPipelineParameters() throws IOException {

        // True
        BuildPipelineView testView = new BuildPipelineView(bpViewName, bpViewTitle, gridBuilder, noOfBuilds, true, false, true, false, false, 2, null, null, false);
        assertTrue(testView.isShowPipelineParameters());

        // False
        testView = new BuildPipelineView(bpViewName, bpViewTitle, nullGridBuilder, noOfBuilds, true, false, false, false, false, 2, null, null, false);
        assertFalse(testView.isShowPipelineParameters());
    }

    @Test
    public void testShowPipelineParametersInHeaders() throws IOException {

        // True
        BuildPipelineView testView = new BuildPipelineView(bpViewName, bpViewTitle, gridBuilder, noOfBuilds, true, false, true, true, false, 2, null, null, false);
        assertTrue(testView.isShowPipelineParametersInHeaders());

        // False
        testView = new BuildPipelineView(bpViewName, bpViewTitle, nullGridBuilder, noOfBuilds, true, false, false, false, false, 2, null, null, false);
        assertFalse(testView.isShowPipelineParametersInHeaders());
    }

    @Test
    public void testRefreshFrequency() throws IOException {

        // False
        final BuildPipelineView testView = new BuildPipelineView(bpViewName, bpViewTitle, nullGridBuilder, noOfBuilds, true, false, false, false, false, 2, null, null, false);
        assertThat(testView.getRefreshFrequency(), is(2));
        assertThat(testView.getRefreshFrequencyInMillis(), is(2000));
    }
    
    @Test
    public void testStartsWithParameters() throws IOException {

        // True
        BuildPipelineView testView = new BuildPipelineView(bpViewName, bpViewTitle, gridBuilder, noOfBuilds, true, false, true, false, false, 2, null, null, true);
        assertTrue(testView.isStartsWithParameters());

        // False
        testView = new BuildPipelineView(bpViewName, bpViewTitle, nullGridBuilder, noOfBuilds, true, false, false, false, false, 2, null, null, false);
        assertFalse(testView.isStartsWithParameters());
    }
    
    /**
     * This is a factory to create an instance of the class under test. This helps to avoid a NPE in View.java when calling
     * getOwnerItemGroup and it's not set. This doesn't solve the root cause and it't only intended to make our tests succeed.
     */
    static class BuildPipelineViewFactory {
        public static BuildPipelineView getBuildPipelineView(final String bpViewName, final String bpViewTitle, final ProjectGridBuilder gridBuilder,
                final String noOfBuilds, final boolean triggerOnlyLatestJob, final boolean startsWithParameters) {
            return new BuildPipelineView(bpViewName, bpViewTitle, gridBuilder, noOfBuilds, triggerOnlyLatestJob, null, startsWithParameters) {

                @Override
                public ItemGroup<? extends TopLevelItem> getOwnerItemGroup() {
                    return Hudson.getInstance();
                }
            };
        }
    }
}
