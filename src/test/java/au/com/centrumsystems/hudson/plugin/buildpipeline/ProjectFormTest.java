package au.com.centrumsystems.hudson.plugin.buildpipeline;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import au.com.centrumsystems.hudson.plugin.buildpipeline.extension.NullColumnHeader;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.tasks.BuildTrigger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class ProjectFormTest {

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
        final FreeStyleProject project2 = jenkins.createFreeStyleProject(proj2);
        project1.getPublishersList().add(new BuildTrigger(proj2, false));
        jenkins.getInstance().rebuildDependencyGraph();
        final FreeStyleBuild build1 = jenkins.buildAndAssertSuccess(project1);
        jenkins.waitUntilNoActivity();

        final PipelineBuild pb = new PipelineBuild(build1, project1, null);
        final ProjectForm pf = new ProjectForm(project1, new NullColumnHeader());
        assertEquals(project1.getName(), pf.getName());
        assertEquals(pb.getCurrentBuildResult(), pf.getResult());
        assertEquals(pb.getProjectURL(), pf.getUrl());
        assertEquals(pb.getProject().getBuildHealth().getIconUrl().replaceAll("\\.gif", "\\.png"), pf.getHealth());
        assertThat(pf.getDependencies().get(0).getName(), is(project2.getName()));
    }

    @Test
    void testEquals() throws Exception {
        final String proj1 = "Project1";
        final String proj2 = "Project2";
        final FreeStyleProject project1 = jenkins.createFreeStyleProject(proj1);
        final FreeStyleProject project2 = jenkins.createFreeStyleProject(proj2);
        project1.getPublishersList().add(new BuildTrigger(proj2, false));
        jenkins.getInstance().rebuildDependencyGraph();

        final ProjectForm pf = new ProjectForm(project1, new NullColumnHeader());
        final ProjectForm pf1 = new ProjectForm(project1, new NullColumnHeader());
        final ProjectForm pf2 = new ProjectForm(project2, new NullColumnHeader());
        final String proj3 = null;
        final ProjectForm pf3 = new ProjectForm(proj3);

        assertEquals(pf, pf1);
        assertNotEquals(pf, pf2);
        assertNotNull(pf);
        assertNotEquals(pf, pf3);

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

        final ProjectForm form1 = new ProjectForm(project1, new NullColumnHeader());
        assertThat(form1.getDependencies(), hasSize(1));
        assertThat(form1.getDependencies().get(0).getDependencies(), hasSize(0));
    }
}
