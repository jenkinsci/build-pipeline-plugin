package au.com.centrumsystems.hudson.plugin.buildpipeline;

import au.com.centrumsystems.hudson.plugin.buildpipeline.testsupport.BuildCardComponent;
import au.com.centrumsystems.hudson.plugin.buildpipeline.testsupport.PipelinePage;
import hudson.model.FreeStyleProject;
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
class BuildPipelineViewDisplayNameTest {
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
     * checks that pipeline box uses displayName
     */
    @Disabled
    @Test
    void testDisplayName() throws Exception {
        final FreeStyleProject freestyle1 = jenkins.createFreeStyleProject("freestyle1");

        freestyle1.setDisplayName("fancyname1");

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

        assertTrue(buildCardComponent.hasDisplayName("fancyname1"),
                "The displayName should be visible");
    }
}
