package au.com.centrumsystems.hudson.plugin.buildpipeline.testsupport;

import com.google.common.base.Function;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.SECONDS;

public class TestUtils {

    public static WebElement waitForElement(final By findBy, WebDriver driver) {
        return waitForElement(findBy, driver, 10, SECONDS);
    }

    public static WebElement waitForElement(final By findBy, WebDriver driver, long timeout, TimeUnit timeUnit) {
        return new WebDriverWait(driver, 10)
                .withTimeout(timeout, timeUnit)
                .until(new Function<WebDriver, WebElement>() {
                    public WebElement apply(WebDriver driver) {
                        return driver.findElement(findBy);
                    }
                });
    }

    public static WebElement waitForElement(final By findBy, WebElement parentElement) {
        return waitForElement(findBy, parentElement, 10, SECONDS);
    }

    public static WebElement waitForElement(final By findBy, WebElement parentElement, long timeout, TimeUnit timeUnit) {
        return new FluentWait<WebElement>(parentElement)
                .withTimeout(timeout, timeUnit)
                .ignoring(NoSuchElementException.class)
                .until(new Function<WebElement, WebElement>() {
            public WebElement apply(WebElement element) {
                return element.findElement(findBy);
            }
        });
    }

    public static void checkState(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

    public static boolean elementIsPresent(By locator, WebDriver driver) {
        try {
            driver.findElement(locator);
        } catch (NoSuchElementException e) {
            return false;
        }

        return true;
    }
}
