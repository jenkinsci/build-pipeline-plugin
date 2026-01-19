package au.com.centrumsystems.hudson.plugin.buildpipeline.functionaltest;

import au.com.centrumsystems.hudson.plugin.buildpipeline.BuildPipelineView;
import au.com.centrumsystems.hudson.plugin.buildpipeline.DownstreamProjectGridBuilder;
import au.com.centrumsystems.hudson.plugin.buildpipeline.testsupport.BuildCardComponent;
import au.com.centrumsystems.hudson.plugin.buildpipeline.testsupport.PipelinePage;
import hudson.model.FreeStyleProject;
import hudson.tasks.BuildTrigger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;
import org.openqa.selenium.WebDriver;

import static org.junit.jupiter.api.Assertions.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

@WithJenkins
class BuildPipelineViewTest {
    protected WebDriver webDriver;

    private JenkinsRule jenkins;

    @BeforeEach
    void beforeEach(JenkinsRule rule) {
        jenkins = rule;
        webDriver = new FirefoxDriver();
        WebDriverWait wait = new WebDriverWait(webDriver, Duration.ofSeconds(30));
    }

    @AfterEach
    void afterEach() {
        if (webDriver != null) {
            webDriver.close();
            webDriver.quit();
        }
    }

    /**
     * checks that UI re-run button works
     */
    @Disabled
    @Test
    void testReRunButton() throws Exception {
        final FreeStyleProject freestyle1 = jenkins.createFreeStyleProject("freestyle1");
        final FreeStyleProject freestyle2 = jenkins.createFreeStyleProject("freestyle2");
        freestyle1.getPublishersList().add(new BuildTrigger("freestyle2", true));
        final FreeStyleProject freestyle3 = jenkins.createFreeStyleProject("freestyle3");
        freestyle2.getPublishersList().add(new BuildTrigger("freestyle3", true));

        freestyle1.scheduleBuild();
        jenkins.waitUntilNoActivity();

        BuildPipelineView pipeline = new BuildPipelineView("pipeline", "",
                new DownstreamProjectGridBuilder(freestyle1.getFullName()),
                "1", //num displayed
                false, //trigger only latest
                true,  // manual trigger
                false, // parameters
                false, //params in header
                false, //definition header
                1, null, null, null, null, null);

        jenkins.getInstance().addView(pipeline);

        PipelinePage pipelinePage = new PipelinePage(webDriver, pipeline.getViewName(), jenkins.getURL());
        pipelinePage.open();

        BuildCardComponent buildCardComponent = pipelinePage.buildCard(1, 1, 2);
        buildCardComponent.clickTriggerButton();

        jenkins.waitUntilNoActivity();
        pipelinePage.reload();

        assertEquals(1, freestyle1.getBuilds().size());
        assertEquals(2, freestyle2.getBuilds().size());
        assertEquals(2, freestyle3.getBuilds().size());
    }
}
