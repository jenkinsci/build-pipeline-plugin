package au.com.centrumsystems.hudson.plugin.buildpipeline;

/**
 * Two-dimensional placement of {@link ProjectForm}s into a grid/matrix layout.
 *
 * This class is also responsible for producing a sequence of {@link BuildGrid}s
 * that are the instances of the pipelines.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class ProjectGrid extends Grid<ProjectForm> {
    /**
     * Iterates instances of the pipeline grid view from this project layout.
     *
     * The caller is only going to iterate {@link BuildGrid}s up to a certain number
     * that the user has configured.
     *
     *
     * @return never null.
     */
    public abstract Iterable<BuildGrid> builds();
}
