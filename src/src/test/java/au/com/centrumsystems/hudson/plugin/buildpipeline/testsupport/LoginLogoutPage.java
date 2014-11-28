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

    public <T extends Page> void login(String username) {
        driver.get(baseUrl + "login");

        usernameField().sendKeys(username);
        passwordField().sendKeys(username);
        passwordField().submit();
    }

    private WebElement usernameField() {
        return waitForElement(By.name("j_username"), driver);
    }

    private WebElement passwordField() {
        return waitForElement(By.name("j_password"), driver);
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
