package au.com.centrumsystems.hudson.plugin.buildpipeline;

import hudson.model.AbstractBuild;

/**
 * {@link Grid} of {@link BuildForm}s, which represents one instance
 * of a pipeline execution.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class BuildGrid extends Grid<BuildForm> {
    /**
     * Search for a BuildForm by a given Build in the current BuildGrid
     * 
     * @param build - the build to search for in the BuildGrid
     * @return BuildForm the form containing the given build
     */
    public abstract BuildForm findBuildForm(AbstractBuild<?, ?> build);    
}
