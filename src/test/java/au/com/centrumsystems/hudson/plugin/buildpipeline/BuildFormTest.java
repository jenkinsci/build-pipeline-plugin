package au.com.centrumsystems.hudson.plugin.buildpipeline;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.ParameterDefinition;
import hudson.model.ParameterValue;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.StringParameterDefinition;
import hudson.tasks.BuildTrigger;
import net.sf.json.JSONObject;

import org.junit.Before;
import org.junit.Test;
import org.jvnet.hudson.test.HudsonTestCase;
import org.kohsuke.stapler.StaplerRequest;

public class BuildFormTest extends HudsonTestCase {
    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void testConstructor() throws Exception {
        final String proj1 = "Project1";
        final String proj2 = "Project2";
        final FreeStyleProject project1 = createFreeStyleProject(proj1);
        project1.getPublishersList().add(new BuildTrigger(proj2, false));
        hudson.rebuildDependencyGraph();
        final FreeStyleBuild build1 = buildAndAssertSuccess(project1);
        waitUntilNoActivity();

        final PipelineBuild pb = new PipelineBuild(build1, project1, null);
        final BuildForm bf = new BuildForm(pb);

        assertThat(bf.getStatus(), is(pb.getCurrentBuildResult()));
    }
    
    @Test
    public void testGetParameterList() throws Exception {
        final String proj1 = "Project1";
        final String proj2 = "Project2";
        final FreeStyleProject project1 = createFreeStyleProject(proj1);
        project1.getPublishersList().add(new BuildTrigger(proj2, false));
        
        final List<ParameterDefinition> pds = new ArrayList<ParameterDefinition>();
        pds.add(new StringParameterDefinition("tag",""));
        pds.add(new StringParameterDefinition("branch",""));
        
        project1.addProperty(new ParametersDefinitionProperty(pds));
        hudson.rebuildDependencyGraph();
        final FreeStyleBuild build1 = buildAndAssertSuccess(project1);
        waitUntilNoActivity();
        final ArrayList<String> paramList = new ArrayList<String>();
        paramList.add("tag");
        paramList.add("branch");

        final PipelineBuild pb = new PipelineBuild(build1, project1, null);
        final BuildForm bf = new BuildForm(pb);

        assertEquals(paramList, bf.getParameterList());
    }
}
