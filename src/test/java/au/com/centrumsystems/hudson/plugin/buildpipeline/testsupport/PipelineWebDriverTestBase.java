package au.com.centrumsystems.hudson.plugin.buildpipeline.testsupport;

import io.github.bonigarcia.wdm.WebDriverManager;
import au.com.centrumsystems.hudson.plugin.buildpipeline.BuildPipelineView;
import au.com.centrumsystems.hudson.plugin.buildpipeline.DownstreamProjectGridBuilder;
import au.com.centrumsystems.hudson.plugin.buildpipeline.extension.SimpleColumnHeader;
import au.com.centrumsystems.hudson.plugin.buildpipeline.extension.SimpleRowHeader;
import au.com.centrumsystems.hudson.plugin.buildpipeline.extension.StandardBuildCard;
import hudson.model.FreeStyleProject;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import org.jvnet.hudson.test.FailureBuilder;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;
import org.openqa.selenium.NoSuchSessionException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

@WithJenkins
public class PipelineWebDriverTestBase {

    protected static final String INITIAL_JOB = "initial-job";
    protected static final String SECOND_JOB = "second-job";

    protected JenkinsRule jenkins;

    protected FreeStyleProject initialJob;

    protected JenkinsRule.DummySecurityRealm realm;
    protected BuildPipelineView pipelineView;
    protected LoginLogoutPage loginLogoutPage;
    protected PipelinePage pipelinePage;
    protected WebDriver webDriver;

    protected static boolean isCi() {
        return System.getenv("CI") != null && !System.getenv("CI").isBlank();
    }

    @BeforeAll
    static void beforeAll() {
        if (isCi()) {
            // The browserVersion needs to match what is provided by the Jenkins Infrastructure
            // If you see an exception like this:
            //
            // org.openqa.selenium.SessionNotCreatedException: Could not start a new session. Response code 500. Message: session not created: This version of ChromeDriver only supports Chrome version 114
            // Current browser version is 112.0.5615.49 with binary path /usr/bin/chromium-browser
            //
            // Then that means you need to update the version here to match the current browser version.
            WebDriverManager.chromedriver().browserVersion("112").setup();
        } else {
            WebDriverManager.chromedriver().setup();
        }
    }

    @BeforeEach
    void beforeEach(JenkinsRule rule) throws Exception {
        jenkins = rule;
        realm = jenkins.createDummySecurityRealm();
        jenkins.jenkins.setSecurityRealm(realm);

        initialJob = jenkins.createFreeStyleProject(INITIAL_JOB);

        pipelineView = new BuildPipelineView("pipeline", "Pipeline", new DownstreamProjectGridBuilder(INITIAL_JOB), "5", false, true, false, false, false, 1, null, null, new SimpleColumnHeader(), new SimpleRowHeader(), new StandardBuildCard());
        jenkins.jenkins.addView(pipelineView);

        if (isCi()) {
            webDriver = new ChromeDriver(new ChromeOptions().addArguments("--headless", "--disable-dev-shm-usage", "--no-sandbox"));
        } else {
            webDriver = new ChromeDriver(new ChromeOptions());
        }
        loginLogoutPage = new LoginLogoutPage(webDriver, jenkins.getURL());
        pipelinePage = new PipelinePage(webDriver, pipelineView.getViewName(), jenkins.getURL());
    }

    @AfterEach
    void afterEach() {
        if (webDriver != null) {
            webDriver.close();
            try {
                webDriver.quit();
            } catch (NoSuchSessionException e) {
                // Ignore
            }
        }
    }

    protected FreeStyleProject createFailingJob(String name) throws Exception{
        FreeStyleProject failingJob = jenkins.createFreeStyleProject(name);
        failingJob.getBuildersList().add(new FailureBuilder());
        return failingJob;
    }
}
