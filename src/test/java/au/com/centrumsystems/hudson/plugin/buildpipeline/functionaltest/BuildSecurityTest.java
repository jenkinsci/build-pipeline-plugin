package au.com.centrumsystems.hudson.plugin.buildpipeline.functionaltest;

import au.com.centrumsystems.hudson.plugin.buildpipeline.testsupport.BuildCardComponent;
import au.com.centrumsystems.hudson.plugin.buildpipeline.testsupport.PipelineWebDriverTestBase;
import au.com.centrumsystems.hudson.plugin.buildpipeline.trigger.BuildPipelineTrigger;
import hudson.model.FreeStyleProject;
import hudson.model.Item;
import hudson.security.GlobalMatrixAuthorizationStrategy;
import hudson.security.Permission;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BuildSecurityTest extends PipelineWebDriverTestBase {

    private static final String UNPRIVILEGED_USER = "unprivilegeduser";
    private static final String PRIVILEGED_USER = "privilegeduser";

    private FreeStyleProject secondJob;

    @BeforeEach
    void beforeEach() throws Exception {
        GlobalMatrixAuthorizationStrategy authorizationStrategy = new GlobalMatrixAuthorizationStrategy();
        authorizationStrategy.add(Permission.READ, UNPRIVILEGED_USER);
        authorizationStrategy.add(Permission.READ, PRIVILEGED_USER);
        authorizationStrategy.add(Item.BUILD, PRIVILEGED_USER);
        authorizationStrategy.add(Item.CONFIGURE, PRIVILEGED_USER);
        jenkins.jenkins.setAuthorizationStrategy(authorizationStrategy);

        secondJob = createFailingJob(SECOND_JOB);
        initialJob.getPublishersList().add(new BuildPipelineTrigger(secondJob.getName(), Collections.emptyList()));
        jenkins.jenkins.rebuildDependencyGraph();
    }

    @Test
    void pipelineShouldNotShowRunButtonIfUserNotPermittedToTriggerBuild() {
        loginLogoutPage.login(UNPRIVILEGED_USER);
        pipelinePage.open();

        assertTrue(pipelinePage.runButtonIsAbsent(),
                "The Run button should not be present");
    }

    @Test
    void pipelineShouldShowRunButtonIfUserPermittedToTriggerBuild() {
        loginLogoutPage.login(PRIVILEGED_USER);
        pipelinePage.open();

        assertTrue(pipelinePage.runButtonIsPresent(),
                "The Run button should be present");
    }

    @Disabled
    @Test
    void manualBuildTriggerShouldNotBeShownIfNotPeritted() throws Exception {
        jenkins.buildAndAssertSuccess(initialJob);

        loginLogoutPage.login(UNPRIVILEGED_USER);
        pipelinePage.open();

        assertFalse(pipelinePage.buildCard(1, 1, 2).hasManualTriggerButton(),
                "Second card in pipeline should not have a trigger button");
    }

    @Disabled
    @Test
    void manualBuildTriggerShouldBeShownIfPermitted() throws Exception {
        jenkins.buildAndAssertSuccess(initialJob);

        loginLogoutPage.login(PRIVILEGED_USER);
        pipelinePage.open();

        assertTrue(pipelinePage.buildCard(1, 1, 2).hasManualTriggerButton(),
                "Second card in pipeline should have a trigger button");
    }

    @Disabled
    @Test
    void retryButtonShouldNotBeShownIfNotPermitted() throws Exception {
        jenkins.buildAndAssertSuccess(initialJob);
        loginLogoutPage.login(PRIVILEGED_USER);
        pipelinePage.open();
        BuildCardComponent secondBuildCard = pipelinePage.buildCard(1, 1, 2);
        secondBuildCard.clickTriggerButton();
        secondBuildCard.waitForFailure();

        loginLogoutPage.logout();
        loginLogoutPage.login(UNPRIVILEGED_USER);
        pipelinePage.open();

        assertFalse(pipelinePage.buildCard(1, 1, 2).hasRetryButton(),
                "Second card in pipeline should not have a retry button");
    }

    @Disabled
    @Test
    void retryButtonShouldBeShownIfPermitted() throws Exception {
        jenkins.buildAndAssertSuccess(initialJob);
        jenkins.waitUntilNoActivity();

        loginLogoutPage.login(PRIVILEGED_USER);
        pipelinePage.open();

        BuildCardComponent secondBuildCard = pipelinePage.buildCard(1, 1, 2);
        secondBuildCard.clickTriggerButton();
        secondBuildCard.waitForFailure();

        assertTrue(pipelinePage.buildCard(1, 1, 2).hasRetryButton(),
                "Second card in pipeline should have a retry button");
    }
}
