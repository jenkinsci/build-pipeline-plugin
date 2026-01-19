package au.com.centrumsystems.hudson.plugin.buildpipeline.dashboard;

import au.com.centrumsystems.hudson.plugin.buildpipeline.BuildPipelineView;
import au.com.centrumsystems.hudson.plugin.buildpipeline.DownstreamProjectGridBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

import static org.junit.jupiter.api.Assertions.*;

@WithJenkins
class BuildPipelineDashboardTest {

	private JenkinsRule jenkins;

	private BuildPipelineDashboard cut;

    @BeforeEach
    void beforeEach(JenkinsRule rule) {
		jenkins = rule;
		cut = new BuildPipelineDashboard(
			"TestProject",
			"Test Description",
			new DownstreamProjectGridBuilder("Job10"),
			"5"
		);
	}

    @Test
    void shouldReturnANewBuildPipelineView() {
		BuildPipelineView bpv = cut.getBuildPipelineView();
		assertNotNull(bpv);
        assertInstanceOf(ReadOnlyBuildPipelineView.class, bpv);
		assertEquals("Job10", ((DownstreamProjectGridBuilder) bpv.getGridBuilder()).getFirstJob());
		assertEquals("5", bpv.getNoOfDisplayedBuilds());
		assertEquals("TestProject", bpv.getBuildViewTitle());
	}

    @Test
    void shouldNotHaveBuildPermissions() {
		BuildPipelineView bpv = cut.getBuildPipelineView();
		assertFalse(bpv.hasBuildPermission());
	}

    @Test
    void shouldNotHaveAnyPermission() {
		BuildPipelineView bpv = cut.getBuildPipelineView();
		assertFalse(bpv.hasPermission(null));
	}
}
