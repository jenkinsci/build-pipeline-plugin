import au.com.centrumsystems.hudson.plugin.buildpipeline.DownstreamProjectGridBuilder
import spock.lang.*

import au.com.centrumsystems.hudson.plugin.buildpipeline.dashboard.BuildPipelineDashboard
import au.com.centrumsystems.hudson.plugin.buildpipeline.dashboard.ReadOnlyBuildPipelineView

class BuildPipelineDashboardTest extends Specification {
    def cut
    def setup() {
        cut = new BuildPipelineDashboard('TestProject', 'Test Description', new DownstreamProjectGridBuilder('Job10'), '5')
    }
    
    def "should return a new BuildPipelineView"() {
        def bpv = cut.getBuildPipelineView()

        expect:
            bpv != null
            bpv instanceof ReadOnlyBuildPipelineView
            bpv.getGridBuilder().getFirstJob() == 'Job10'
            bpv.getNoOfDisplayedBuilds() == '5'
            bpv.getBuildViewTitle() == 'TestProject'
    }

    def "should not have build permissions"() {
        def bpv = cut.getBuildPipelineView()

        expect:
            !bpv.hasBuildPermission()
    }

    def "should not have any permission"() {
        def bpv = cut.getBuildPipelineView()

        expect:
            !bpv.hasPermission(null)
    }
}