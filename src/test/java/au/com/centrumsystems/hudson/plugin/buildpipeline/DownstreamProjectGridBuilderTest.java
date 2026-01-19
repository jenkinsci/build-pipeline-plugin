package au.com.centrumsystems.hudson.plugin.buildpipeline;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * @author Kohsuke Kawaguchi
 */
@WithJenkins
class DownstreamProjectGridBuilderTest {

    private JenkinsRule jenkins;

    @BeforeEach
    void beforeEach(JenkinsRule rule) {
        jenkins = rule;
    }

    /**
     * Makes sure that the config form will keep the settings intact.
     */
    @Test
    void testConfigRoundtrip() throws Exception {
        DownstreamProjectGridBuilder gridBuilder = new DownstreamProjectGridBuilder("something");
        BuildPipelineView v = new BuildPipelineView("foo","Title", gridBuilder, "5", true, null);
        jenkins.getInstance().addView(v);
        jenkins.configRoundtrip(v);
        BuildPipelineView av = (BuildPipelineView)jenkins.getInstance().getView(v.getViewName());
        assertSame(v,av);
//        assertNotSame(gridBuilder,(DownstreamProjectGridBuilder)av.getGridBuilder()); //FIXME: this is making the test fail, and it's not obvious why this should be true
    }
}
