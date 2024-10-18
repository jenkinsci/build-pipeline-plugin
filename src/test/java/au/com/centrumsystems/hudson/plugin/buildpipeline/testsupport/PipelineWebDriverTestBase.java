package au.com.centrumsystems.hudson.plugin.buildpipeline.testsupport;

import au.com.centrumsystems.hudson.plugin.buildpipeline.BuildPipelineView;
import au.com.centrumsystems.hudson.plugin.buildpipeline.DownstreamProjectGridBuilder;
import au.com.centrumsystems.hudson.plugin.buildpipeline.extension.SimpleColumnHeader;
import au.com.centrumsystems.hudson.plugin.buildpipeline.extension.SimpleRowHeader;
import au.com.centrumsystems.hudson.plugin.buildpipeline.extension.StandardBuildCard;
import hudson.model.FreeStyleProject;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.jvnet.hudson.test.FailureBuilder;
import org.jvnet.hudson.test.JenkinsRule;
import org.openqa.selenium.NoSuchSessionException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

public class PipelineWebDriverTestBase {

    protected static final String INITIAL_JOB = "initial-job";
    protected static final String SECOND_JOB = "second-job";

    @Rule
    public JenkinsRule jr = new JenkinsRule();

    protected FreeStyleProject initialJob;

    protected JenkinsRule.DummySecurityRealm realm;
    protected BuildPipelineView pipelineView;
    protected LoginLogoutPage loginLogoutPage;
    protected PipelinePage pipelinePage;
    protected WebDriver webDriver;

    @Before
    public void initSharedComponents() throws Exception {
        realm = jr.createDummySecurityRealm();
        jr.jenkins.setSecurityRealm(realm);

        initialJob = jr.createFreeStyleProject(INITIAL_JOB);

        pipelineView = new BuildPipelineView("pipeline", "Pipeline", new DownstreamProjectGridBuilder(INITIAL_JOB), "5", false, true, false, false, false, 1, null, null, new SimpleColumnHeader(), new SimpleRowHeader(), new StandardBuildCard());
        jr.jenkins.addView(pipelineView);

        webDriver = new FirefoxDriver();
        loginLogoutPage = new LoginLogoutPage(webDriver, jr.getURL());
        pipelinePage = new PipelinePage(webDriver, pipelineView.getViewName(), jr.getURL());
    }

    @After
    public void cleanUpWebDriver() {
        webDriver.close();
        try {
          webDriver.quit();
        } catch (NoSuchSessionException e) {
            // Ignore
        }
    }

    protected FreeStyleProject createFailingJob(String name) throws Exception{
        FreeStyleProject failingJob = jr.createFreeStyleProject(name);
        failingJob.getBuildersList().add(new FailureBuilder());
        return failingJob;
    }
}
