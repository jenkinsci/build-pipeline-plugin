package au.com.centrumsystems.hudson.plugin.buildpipeline;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertThat;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.tasks.BuildTrigger;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.jvnet.hudson.test.HudsonTestCase;

public class ProjectFormTest extends HudsonTestCase {
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
        final FreeStyleProject project2 = createFreeStyleProject(proj2);
        project1.getPublishersList().add(new BuildTrigger(proj2, false));
        hudson.rebuildDependencyGraph();
        final FreeStyleBuild build1 = buildAndAssertSuccess(project1);
        waitUntilNoActivity();

        final PipelineBuild pb = new PipelineBuild(build1, project1, null);
        final ProjectForm pf = new ProjectForm(project1);
        assertEquals(project1.getName(), pf.getName());
        assertEquals(pb.getCurrentBuildResult(), pf.getResult());
        assertEquals(pb.getProjectURL(), pf.getUrl());
        assertEquals(pb.getProject().getBuildHealth().getIconUrl().replaceAll("\\.gif", "\\.png"), pf.getHealth());
        assertThat(pf.getDependencies().get(0).getName(), is(project2.getName()));
    }

    @Test
    public void testEquals() throws IOException {
        final String proj1 = "Project1";
        final String proj2 = "Project2";
        final FreeStyleProject project1 = createFreeStyleProject(proj1);
        final FreeStyleProject project2 = createFreeStyleProject(proj2);
        project1.getPublishersList().add(new BuildTrigger(proj2, false));
        hudson.rebuildDependencyGraph();

        final ProjectForm pf = new ProjectForm(project1);
        final ProjectForm pf1 = new ProjectForm(project1);
        final ProjectForm pf2 = new ProjectForm(project2);
        final String proj3 = null;
        final ProjectForm pf3 = new ProjectForm(proj3);

        assertTrue(pf.equals(pf1));
        assertFalse(pf.equals(pf2));
        assertNotNull(pf);
        assertFalse(pf.equals(pf3));

    }

    @Test
    public void testNoInfiniteRecursion() throws IOException {
        final String proj1 = "Project1";
        final String proj2 = "Project2";
        final FreeStyleProject project1 = createFreeStyleProject(proj1);
        final FreeStyleProject project2 = createFreeStyleProject(proj2);
        project1.getPublishersList().add(new BuildTrigger(proj2, false));
        project2.getPublishersList().add(new BuildTrigger(proj1, false));
        hudson.rebuildDependencyGraph();

        final ProjectForm form1 = new ProjectForm(project1);
        assertThat(form1.getDependencies(), hasSize(1));
        assertThat(form1.getDependencies().get(0).getDependencies(), hasSize(0));
    }
}
