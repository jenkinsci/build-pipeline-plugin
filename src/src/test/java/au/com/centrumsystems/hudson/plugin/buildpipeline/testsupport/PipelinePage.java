package au.com.centrumsystems.hudson.plugin.buildpipeline.testsupport;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.net.URL;

import static au.com.centrumsystems.hudson.plugin.buildpipeline.testsupport.TestUtils.elementIsPresent;
import static au.com.centrumsystems.hudson.plugin.buildpipeline.testsupport.TestUtils.waitForElement;

public class PipelinePage implements Page {

    private static final String TRIGGER_PIPELINE_BUTTON = "trigger-pipeline-button";

    private final String pipelineName;
    private final WebDriver webDriver;
    private final URL baseUrl;

    public PipelinePage(WebDriver webDriver, String pipelineName, URL baseUrl) {
        this.webDriver = webDriver;
        this.pipelineName = pipelineName;
        this.baseUrl = baseUrl;
    }

    public PipelinePage open() {
        webDriver.get(baseUrl + getRelativeUrl());
        return this;
    }

    public boolean runButtonIsPresent() {
        return triggerPipelineButton() != null;
    }

    public boolean runButtonIsAbsent() {
        waitForElement(By.xpath("//img[@alt='Pipeline History']"), webDriver);
        return !elementIsPresent(By.id(TRIGGER_PIPELINE_BUTTON), webDriver);
    }

    public PipelinePage clickRunButton() {
        triggerPipelineButton().click();
        return this;
    }

    public void reload() throws Exception {
        webDriver.navigate().refresh();
    }

    public BuildCardComponent buildCard(int pipelineGroup, int pipeline, int card) {
        return new BuildCardComponent(webDriver, pipelineGroup, pipeline, card).waitFor();
    }

    private WebElement triggerPipelineButton() {
        return waitForElement(By.id(TRIGGER_PIPELINE_BUTTON), webDriver);
    }

    @Override
    public String getRelativeUrl() {
        return "view/" + pipelineName;
    }
}
