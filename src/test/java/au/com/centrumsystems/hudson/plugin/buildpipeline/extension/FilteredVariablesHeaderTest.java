/*
 * The MIT License
 *
 * Copyright (c) 2016 the Jenkins project
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
package au.com.centrumsystems.hudson.plugin.buildpipeline.extension;

import hudson.model.AbstractBuild;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author dalvizu
 */
@WithJenkins
class FilteredVariablesHeaderTest {

    private JenkinsRule jenkins;

    @BeforeEach
    void beforeEach(JenkinsRule rule) {
        jenkins = rule;
    }

    @Test
    void testGetParameters() {
        FilteredVariablesHeader.FilteredVariable fooVariable = new FilteredVariablesHeader.FilteredVariable("foo");
        FilteredVariablesHeader.FilteredVariable passwordVariable =
                new FilteredVariablesHeader.FilteredVariable("password");
        FilteredVariablesHeader provider = new FilteredVariablesHeader(Arrays.asList(fooVariable, passwordVariable));

        Map<String, String> buildVariables = new HashMap<>();

        buildVariables.put("foo", "bar");
        buildVariables.put("bar", "baz");
        buildVariables.put("password", "password");

        AbstractBuild build = mock(AbstractBuild.class);
        when(build.getBuildVariables()).thenReturn(buildVariables);
        when(build.getSensitiveBuildVariables()).thenReturn(new HashSet(Arrays.asList("password")));
        assertFalse(provider.getParameters(build).isEmpty());
        assertThat(provider.getParameters(build), hasKey("foo"));
        assertThat(provider.getParameters(build), hasKey("password"));
        assertEquals("********", provider.getParameters(build).get("password"));
    }

    @Test
    void testNullConfig() {
        FilteredVariablesHeader provider = new FilteredVariablesHeader(null);
        AbstractBuild build = mock(AbstractBuild.class);
        when(build.getBuildVariables()).thenReturn(new HashMap<String, String>());
        assertNotNull(provider.getParameters(build));
        assertTrue(provider.getParameters(build).isEmpty());
    }
}
