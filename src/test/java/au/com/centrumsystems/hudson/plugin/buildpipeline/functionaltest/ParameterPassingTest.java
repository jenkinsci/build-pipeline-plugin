package au.com.centrumsystems.hudson.plugin.buildpipeline.functionaltest;

import au.com.centrumsystems.hudson.plugin.buildpipeline.testsupport.PipelineWebDriverTestBase;
import au.com.centrumsystems.hudson.plugin.buildpipeline.trigger.BuildPipelineTrigger;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.ParametersAction;
import hudson.model.StringParameterValue;
import hudson.plugins.parameterizedtrigger.PredefinedBuildParameters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import org.openqa.selenium.support.ui.FluentWait;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;

import static hudson.model.Result.FAILURE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

class ParameterPassingTest extends PipelineWebDriverTestBase {

    private FreeStyleProject secondJob;

    @BeforeEach
    void beforeEach() throws Exception {
        secondJob = createFailingJob(SECOND_JOB);
        initialJob.getPublishersList().add(
                new BuildPipelineTrigger(secondJob.getName(),
                Arrays.asList(new PredefinedBuildParameters("myProp=some-value"))));
        jenkins.jenkins.rebuildDependencyGraph();
    }

    @Disabled
    @Test
    void shouldPassParametersFromFirstJobToSecond() throws Exception {
        jenkins.buildAndAssertSuccess(initialJob);
        pipelinePage.open()
                .buildCard(1, 1, 2)
                    .clickTriggerButton()
                    .waitForFailure();

        assertParameterValueIsPresentInBuild(secondJob.getBuilds().getFirstBuild());
    }

    @Disabled
    @Test
    void secondJobShouldRetainParameterWhenRetried() throws Exception {
        jenkins.buildAndAssertSuccess(initialJob);
        pipelinePage.open()
                .buildCard(1, 1, 2)
                    .clickTriggerButton()
                    .waitForFailure()
                    .clickTriggerButton();

        waitForBuild2ToFail();

        assertParameterValueIsPresentInBuild(secondJob.getBuilds().getLastBuild());
    }

    private void waitForBuild2ToFail() {
        new FluentWait<>(secondJob)
                .ignoring(IllegalStateException.class)
                .withTimeout(Duration.ofSeconds(10))
                .until(input -> buildNumbered(2, input).getResult() == FAILURE);
    }

    private void assertParameterValueIsPresentInBuild(FreeStyleBuild build) {
        assertThat(getMyPropParameterFrom(build).orElse(absentParameter()), is("some-value"));
    }

    private Optional<StringParameterValue> getMyPropParameterFrom(FreeStyleBuild build) {
        ParametersAction parametersAction = build.getAction(ParametersAction.class);
        if (parametersAction != null) {
            return Optional.ofNullable((StringParameterValue) parametersAction.getParameter("myProp"));
        }

        return Optional.empty();
    }

    private FreeStyleBuild buildNumbered(int number, FreeStyleProject job) {
        for (FreeStyleBuild build: job.getBuilds()) {
            if (build.getNumber() == number) {
                return build;
            }
        }

        throw new IllegalStateException("No build numbered " + number + " in " + job);
    }

    private StringParameterValue absentParameter() {
        return new StringParameterValue("myProp", "[absent]");
    }
}
