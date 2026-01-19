package au.com.centrumsystems.hudson.plugin.buildpipeline;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.ParameterDefinition;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.StringParameterDefinition;
import hudson.tasks.BuildTrigger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class BuildFormTest {

    private JenkinsRule jenkins;

    @BeforeEach
    void beforeEach(JenkinsRule rule) {
        jenkins = rule;
    }

    @Test
    void testConstructor() throws Exception {
        final String proj1 = "Project1";
        final String proj2 = "Project2";
        final FreeStyleProject project1 = jenkins.createFreeStyleProject(proj1);
        project1.getPublishersList().add(new BuildTrigger(proj2, false));
        jenkins.getInstance().rebuildDependencyGraph();
        final FreeStyleBuild build1 = jenkins.buildAndAssertSuccess(project1);
        jenkins.waitUntilNoActivity();

        final PipelineBuild pb = new PipelineBuild(build1, project1, null);
        final BuildForm bf = new BuildForm(jenkins.getInstance(), pb);

        assertThat(bf.getStatus(), is(pb.getCurrentBuildResult()));
    }

    @Test
    void testGetParameterList() throws Exception {
        final String proj1 = "Project1";
        final String proj2 = "Project2";
        final FreeStyleProject project1 = jenkins.createFreeStyleProject(proj1);
        project1.getPublishersList().add(new BuildTrigger(proj2, false));
        
        final List<ParameterDefinition> pds = new ArrayList<>();
        pds.add(new StringParameterDefinition("tag",""));
        pds.add(new StringParameterDefinition("branch",""));
        
        project1.addProperty(new ParametersDefinitionProperty(pds));
        jenkins.getInstance().rebuildDependencyGraph();
        final FreeStyleBuild build1 = jenkins.buildAndAssertSuccess(project1);
        jenkins.waitUntilNoActivity();
        final ArrayList<String> paramList = new ArrayList<>();
        paramList.add("tag");
        paramList.add("branch");

        final PipelineBuild pb = new PipelineBuild(build1, project1, null);
        final BuildForm bf = new BuildForm(jenkins.getInstance(), pb);

        assertEquals(paramList, bf.getParameterList());
    }

    @Test
    void testNoInfiniteRecursion() throws Exception {
        final String proj1 = "Project1";
        final String proj2 = "Project2";
        final FreeStyleProject project1 = jenkins.createFreeStyleProject(proj1);
        final FreeStyleProject project2 = jenkins.createFreeStyleProject(proj2);
        project1.getPublishersList().add(new BuildTrigger(proj2, false));
        project2.getPublishersList().add(new BuildTrigger(proj1, false));
        jenkins.getInstance().rebuildDependencyGraph();

        final BuildForm form1 = new BuildForm(jenkins.getInstance(), new PipelineBuild(null, project1, null));
        assertThat(form1.getDependencies(), hasSize(1));
        assertThat(form1.getDependencies().get(0).getDependencies(), hasSize(0));
    }
}
