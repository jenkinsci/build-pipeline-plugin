package au.com.centrumsystems.hudson.plugin.buildpipeline.testsupport;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.time.Duration;

import static au.com.centrumsystems.hudson.plugin.buildpipeline.testsupport.TestUtils.elementIsPresent;
import static au.com.centrumsystems.hudson.plugin.buildpipeline.testsupport.TestUtils.waitForElement;

public class BuildCardComponent {

    private static final String TRIGGER_SPAN_XPATH = ".//span[@class='pointer trigger']";
    private static final String RETRY_IMG_XPATH = ".//span[@class='pointer trigger']/img[@alt='retry']";
    private static final String RERUN_IMG_XPATH = ".//span[@class='pointer trigger']/img[@alt='re-run']";

    private final WebDriver webDriver;
    private final int pipelineGroup;
    private final int pipeline;
    private final int card;

    private WebElement cardWebElement;

    public BuildCardComponent(WebDriver webDriver, int pipelineGroup, int pipeline, int card) {
        this.webDriver = webDriver;
        this.pipelineGroup = pipelineGroup;
        this.pipeline = pipeline;
        this.card = card;
    }

    public BuildCardComponent waitFor() {
        cardWebElement = waitForElement(By.xpath(cardXPath(pipelineGroup, pipeline, card)), webDriver);
        return this;
    }

    public BuildCardComponent waitForBuildToStart() {
        waitForElement(By.xpath(".//table[@class='progress-bar']"), webDriver);
        return this;
    }

    public BuildCardComponent waitForFailure() {
        return waitForStatus("FAILURE");
    }

    public BuildCardComponent waitForStatus(String status) {
        waitForElement(
                By.xpath(".//table[contains(@class, '" + status + "')]"),
                cardWebElement, Duration.ofSeconds(20));
        return this;
    }

    public boolean hasManualTriggerButton() {
        return elementIsPresent(By.xpath(TRIGGER_SPAN_XPATH), webDriver);
    }

    public boolean hasDisplayName(String displayName) {
        return elementIsPresent(By.xpath(".//a[@title='" + displayName + "']"), webDriver);
    }

    public boolean hasRetryButton() {
        return elementIsPresent(By.xpath(RETRY_IMG_XPATH), webDriver);
    }

    public BuildCardComponent clickTriggerButton() {
        triggerButtonHtmlElement().click();
        return this;
    }

    private WebElement triggerButtonHtmlElement() {
        return cardWebElement.findElement(By.xpath(TRIGGER_SPAN_XPATH));
    }

    private String cardXPath(int pipelineGroup, int pipeline, int card) {
        return String.format("//table[@id = 'pipelines']/tbody[%d]/tr[@class='build-pipeline'][%d]/td[starts-with(@id,'build-')][%d]",
                pipelineGroup, pipeline, card);
    }
}
