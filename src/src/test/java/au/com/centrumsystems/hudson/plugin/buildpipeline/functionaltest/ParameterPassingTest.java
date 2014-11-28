package au.com.centrumsystems.hudson.plugin.buildpipeline.functionaltest;

import au.com.centrumsystems.hudson.plugin.buildpipeline.testsupport.PipelineWebDriverTestBase;
import au.com.centrumsystems.hudson.plugin.buildpipeline.trigger.BuildPipelineTrigger;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.ParametersAction;
import hudson.model.StringParameterValue;
import hudson.plugins.parameterizedtrigger.AbstractBuildParameters;
import hudson.plugins.parameterizedtrigger.PredefinedBuildParameters;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.support.ui.FluentWait;

import java.util.Arrays;

import static hudson.model.Result.FAILURE;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class ParameterPassingTest extends PipelineWebDriverTestBase {

    FreeStyleProject secondJob;

    @Before
    public void init() throws Exception {
        secondJob = createFailingJob(SECOND_JOB);
        initialJob.getPublishersList().add(
                new BuildPipelineTrigger(secondJob.getName(),
                Arrays.<AbstractBuildParameters>asList(new PredefinedBuildParameters("myProp=some-value"))));
        jr.jenkins.rebuildDependencyGraph();
    }

    @Test
    public void shouldPassParametersFromFirstJobToSecond() throws Exception {
        jr.buildAndAssertSuccess(initialJob);
        pipelinePage.open()
                .buildCard(1, 1, 2)
                    .clickTriggerButton()
                    .waitForFailure();

        assertParameterValueIsPresentInBuild(secondJob.getBuilds().getFirstBuild());
    }

    @Test
    public void secondJobShouldRetainParameterWhenRetried() throws Exception {
        jr.buildAndAssertSuccess(initialJob);
        pipelinePage.open()
                .buildCard(1, 1, 2)
                    .clickTriggerButton()
                    .waitForFailure()
                    .clickTriggerButton();

        waitForBuild2ToFail();

        assertParameterValueIsPresentInBuild(secondJob.getBuilds().getLastBuild());
    }

    private void waitForBuild2ToFail() {
        new FluentWait<FreeStyleProject>(secondJob)
                .ignoring(IllegalStateException.class)
                .withTimeout(10, SECONDS)
                .until(new Predicate<FreeStyleProject>() {
                    public boolean apply(FreeStyleProject input) {
                        return buildNumbered(2, input).getResult() == FAILURE;
                    }
                });
    }

    private void assertParameterValueIsPresentInBuild(FreeStyleBuild build) {
        assertThat(getMyPropParameterFrom(build).or(absentParameter()).value, is("some-value"));
    }

    private Optional<StringParameterValue> getMyPropParameterFrom(FreeStyleBuild build) {
        ParametersAction parametersAction = build.getAction(ParametersAction.class);
        if (parametersAction != null) {
            return Optional.fromNullable((StringParameterValue) parametersAction.getParameter("myProp"));
        }

        return Optional.absent();
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
