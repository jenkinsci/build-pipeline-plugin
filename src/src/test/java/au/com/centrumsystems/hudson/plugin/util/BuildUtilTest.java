/*
 * The MIT License
 *
 * Copyright (c) 2011, Centrumsystems Pty Ltd
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 */
package au.com.centrumsystems.hudson.plugin.util;

import hudson.model.FreeStyleBuild;
import hudson.model.AbstractBuild;
import hudson.model.Cause.UserCause;
import hudson.model.FreeStyleProject;
import hudson.model.Hudson;
import hudson.model.ParametersAction;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.StringParameterDefinition;
import hudson.model.StringParameterValue;
import hudson.tasks.BuildTrigger;

import org.junit.Before;
import org.junit.Test;
import org.jvnet.hudson.test.HudsonTestCase;

public class BuildUtilTest extends HudsonTestCase {

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void testGetDownstreamBuild() throws Exception {
        final String proj1 = "Proj1";
        final String proj2 = "Proj2";
        final String proj3 = "Proj3";

        FreeStyleProject project1, project2, project3;
        FreeStyleBuild build1, build2, build3;
        final FreeStyleProject project4 = null;
        final FreeStyleBuild build4 = null;

        // Create test projects and associated builders
        project1 = createFreeStyleProject(proj1);
        project2 = createFreeStyleProject(proj2);
        project3 = createFreeStyleProject(proj3);

        // Add project2 as a post build action: build other project
        project1.getPublishersList().add(new BuildTrigger(proj2, true));
        project2.getPublishersList().add(new BuildTrigger(proj3, true));

        // Important; we must do this step to ensure that the dependency graphs are updated
        Hudson.getInstance().rebuildDependencyGraph();

        // Build project1, upon completion project2 will be built
        build1 = buildAndAssertSuccess(project1);
        // When all building is complete retrieve the last build from project2
        waitUntilNoActivity();
        build2 = project2.getLastBuild();
        build3 = project3.getLastBuild();

        AbstractBuild<?, ?> nextBuild = BuildUtil.getDownstreamBuild(project2, build1);
        assertEquals("The next build should be " + proj1 + build2.number, build2, nextBuild);

        nextBuild = BuildUtil.getDownstreamBuild(project3, nextBuild);
        assertEquals("The next build should be " + proj1 + build3.number, build3, nextBuild);

        nextBuild = BuildUtil.getDownstreamBuild(project4, build4);
        assertNull(nextBuild);
    }

    @Test
    public void testGetAllBuildParametersAction() throws Exception {
        final String proj1 = "Proj1";
        final String proj2 = "Proj2";
        final String key1 = "testKey";
        final String key2 = "testKey2";
        final String value1 = "testValue";
        final String value2 = "testValue2";
        final String value3 = "testValue3";

        FreeStyleProject project1, project2;
        FreeStyleBuild build1;

        // Create test projects and associated builders
        project1 = createFreeStyleProject(proj1);
        project2 = createFreeStyleProject(proj2);
        // Add a String parameter
        project1.addProperty((new ParametersDefinitionProperty(new StringParameterDefinition(key1, value1))));
        project1.addProperty((new ParametersDefinitionProperty(new StringParameterDefinition(key2, value3))));
        project2.addProperty((new ParametersDefinitionProperty(new StringParameterDefinition(key1, value2))));

        // Add project2 as a post build action: build other project
        project1.getPublishersList().add(new BuildTrigger(proj2, true));

        // Important; we must do this step to ensure that the dependency graphs are updated
        Hudson.getInstance().rebuildDependencyGraph();

        // Build project1, upon completion project2 will be built
        // build1 = buildAndAssertSuccess(project1);
        build1 = project1.scheduleBuild2(0, new UserCause(),
                new ParametersAction(new StringParameterValue(key1, value1), new StringParameterValue(key2, value3))).get();
        // When all building is complete retrieve the last build from project2
        waitUntilNoActivity();

        final ParametersAction params = (ParametersAction) BuildUtil.getAllBuildParametersAction(build1, project2);
        assertEquals(((StringParameterValue) params.getParameter(key1)).value, value2);
        assertEquals(((StringParameterValue) params.getParameter(key2)).value, value3);
    }

    @Test
    public void testGetBuildParametersAction() throws Exception {
        final String proj1 = "Proj1";
        final String key1 = "testKey";
        final String key2 = "testKey2";
        final String value1 = "testValue";
        final String value3 = "testValue3";

        FreeStyleProject project1;
        FreeStyleBuild build1;
        final FreeStyleBuild build2 = null;

        // Create test projects and associated builders
        project1 = createFreeStyleProject(proj1);

        // Add a String parameter
        project1.addProperty((new ParametersDefinitionProperty(new StringParameterDefinition(key1, value1))));
        project1.addProperty((new ParametersDefinitionProperty(new StringParameterDefinition(key2, value3))));

        // Important; we must do this step to ensure that the dependency graphs are updated
        Hudson.getInstance().rebuildDependencyGraph();

        // Build project1 with the two StringParameterValues
        build1 = project1.scheduleBuild2(0, new UserCause(),
                new ParametersAction(new StringParameterValue(key1, value1), new StringParameterValue(key2, value3))).get();
        waitUntilNoActivity();

        ParametersAction params = BuildUtil.getBuildParametersAction(build1);
        assertEquals(((StringParameterValue) params.getParameter(key1)).value, value1);
        assertEquals(((StringParameterValue) params.getParameter(key2)).value, value3);

        params = BuildUtil.getBuildParametersAction(build2);
        assertNull(params);
    }

    @Test
    public void testMergeParameters() throws Exception {
        final String key1 = "testKey";
        final String key2 = "testKey2";
        final String value1 = "testValue";
        final String value2 = "testValue2";
        final String value3 = "testValue3";

        ParametersAction baseParams = new ParametersAction(new StringParameterValue(key1, value1), new StringParameterValue(key2, value3));
        ParametersAction extraParams = new ParametersAction(new StringParameterValue(key2, value2));

        ParametersAction params = BuildUtil.mergeParameters(baseParams, extraParams);
        assertEquals(((StringParameterValue) params.getParameter(key1)).value, value1);
        assertEquals(((StringParameterValue) params.getParameter(key2)).value, value2);

        baseParams = null;
        extraParams = null;
        params = BuildUtil.mergeParameters(baseParams, extraParams);
        assertEquals(params.getParameters().size(), 0);
    }
}
