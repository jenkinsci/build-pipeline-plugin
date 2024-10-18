package au.com.centrumsystems.hudson.plugin.buildpipeline.functionaltest;

import au.com.centrumsystems.hudson.plugin.buildpipeline.testsupport.BuildCardComponent;
import au.com.centrumsystems.hudson.plugin.buildpipeline.testsupport.PipelineWebDriverTestBase;
import au.com.centrumsystems.hudson.plugin.buildpipeline.trigger.BuildPipelineTrigger;
import hudson.model.FreeStyleProject;
import hudson.model.Item;
import hudson.plugins.parameterizedtrigger.AbstractBuildParameters;
import hudson.security.GlobalMatrixAuthorizationStrategy;
import hudson.security.Permission;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BuildSecurityTest extends PipelineWebDriverTestBase {

    static final String UNPRIVILEGED_USER = "unprivilegeduser";
    static final String PRIVILEGED_USER = "privilegeduser";

    FreeStyleProject secondJob;

    @Before
    public void init() throws Exception {
        GlobalMatrixAuthorizationStrategy authorizationStrategy = new GlobalMatrixAuthorizationStrategy();
        jr.jenkins.setAuthorizationStrategy(authorizationStrategy);
        authorizationStrategy.add(Permission.READ, UNPRIVILEGED_USER);
        authorizationStrategy.add(Permission.READ, PRIVILEGED_USER);
        authorizationStrategy.add(Item.BUILD, PRIVILEGED_USER);
        authorizationStrategy.add(Item.CONFIGURE, PRIVILEGED_USER);

        secondJob = createFailingJob(SECOND_JOB);
        initialJob.getPublishersList().add(new BuildPipelineTrigger(secondJob.getName(), Collections.<AbstractBuildParameters>emptyList()));
        jr.jenkins.rebuildDependencyGraph();
    }

    @Test
    public void pipelineShouldNotShowRunButtonIfUserNotPermittedToTriggerBuild() throws Exception {
        loginLogoutPage.login(UNPRIVILEGED_USER);
        pipelinePage.open();

        assertTrue("The Run button should not be present",
                pipelinePage.runButtonIsAbsent());
    }

    @Test
    public void pipelineShouldShowRunButtonIfUserPermittedToTriggerBuild() throws Exception {
        loginLogoutPage.login(PRIVILEGED_USER);
        pipelinePage.open();

        assertTrue("The Run button should be present",
                pipelinePage.runButtonIsPresent());
    }

    @Ignore
    @Test
    public void manualBuildTriggerShouldNotBeShownIfNotPeritted() throws Exception {
        jr.buildAndAssertSuccess(initialJob);

        loginLogoutPage.login(UNPRIVILEGED_USER);
        pipelinePage.open();

        assertFalse("Second card in pipeline should not have a trigger button",
                pipelinePage.buildCard(1, 1, 2).hasManualTriggerButton());
    }

    @Ignore
    @Test
    public void manualBuildTriggerShouldBeShownIfPermitted() throws Exception {
        jr.buildAndAssertSuccess(initialJob);

        loginLogoutPage.login(PRIVILEGED_USER);
        pipelinePage.open();

        assertTrue("Second card in pipeline should have a trigger button",
                pipelinePage.buildCard(1, 1, 2).hasManualTriggerButton());
    }

    @Ignore
    @Test
    public void retryButtonShouldNotBeShownIfNotPermitted() throws Exception {
        jr.buildAndAssertSuccess(initialJob);
        loginLogoutPage.login(PRIVILEGED_USER);
        pipelinePage.open();
        BuildCardComponent secondBuildCard = pipelinePage.buildCard(1, 1, 2);
        secondBuildCard.clickTriggerButton();
        secondBuildCard.waitForFailure();

        loginLogoutPage.logout();
        loginLogoutPage.login(UNPRIVILEGED_USER);
        pipelinePage.open();

        assertFalse("Second card in pipeline should not have a retry button",
                pipelinePage.buildCard(1, 1, 2).hasRetryButton());
    }

    @Ignore
    @Test
    public void retryButtonShouldBeShownIfPermitted() throws Exception {
        jr.buildAndAssertSuccess(initialJob);
        jr.waitUntilNoActivity();

        loginLogoutPage.login(PRIVILEGED_USER);
        pipelinePage.open();

        BuildCardComponent secondBuildCard = pipelinePage.buildCard(1, 1, 2);
        secondBuildCard.clickTriggerButton();
        secondBuildCard.waitForFailure();

        assertTrue("Second card in pipeline should have a retry button",
                pipelinePage.buildCard(1, 1, 2).hasRetryButton());
    }
}
