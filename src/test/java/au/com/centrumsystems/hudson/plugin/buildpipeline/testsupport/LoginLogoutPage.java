package au.com.centrumsystems.hudson.plugin.buildpipeline.testsupport;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.net.URL;
import java.net.URLEncoder;

import static au.com.centrumsystems.hudson.plugin.buildpipeline.testsupport.TestUtils.waitForElement;

public class LoginLogoutPage implements Page {

    private final URL baseUrl;
    private final WebDriver driver;

    public LoginLogoutPage(WebDriver driver, URL baseUrl) {
        this.driver = driver;
        this.baseUrl = baseUrl;
    }

    private void sleep() {
        // Selenium web driver 4.39.0 with Google Chrome 143.0.7499.169
        // fails to login to Jenkins unless it is allowed some time to
        // render the page.  The half second sleep was enough on my
        // Red Hat Enterprise Linux 8.10 computer with 32 GB of memory
        // and an AMD Ryzen 5 5600X 6-Core Processor
        try {
            Thread.sleep(537);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public <T extends Page> void login(String username) {
        driver.get(baseUrl + "login");
        sleep();

        usernameField().sendKeys(username);
        sleep();
        passwordField().sendKeys(username);
        sleep();
        submitButton().click();
    }

    private WebElement usernameField() {
        return waitForElement(By.name("j_username"), driver);
    }

    private WebElement passwordField() {
        return waitForElement(By.name("j_password"), driver);
    }

    private WebElement submitButton() {
        return waitForElement(By.name("Submit"), driver);
    }

    public void logout() {
        driver.get(baseUrl + "logout");
    }

    public String getRelativeUrl() {
        return "login";
    }

    public <T extends Page> String getUrl(T nextPage) {
        return baseUrl + "login?from=" + encodeSafely(nextPage.getRelativeUrl());
    }

    private String encodeSafely(String s) {
        try {
            return URLEncoder.encode(s, "utf-8");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
