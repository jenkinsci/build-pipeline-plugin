package au.com.centrumsystems.hudson.plugin.buildpipeline;

import hudson.model.AbstractDescribableImpl;
import hudson.model.Item;

import java.io.IOException;

import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;

/**
 * Encapsulates the definition of how to layout projects into a {@link ProjectGrid}.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class ProjectGridBuilder extends AbstractDescribableImpl<ProjectGridBuilder> {

    /**
     * Builds the grid.
     *
     * @param owner
     *      The view for which this builder is working. Never null.
     *      If the {@link ProjectGridBuilder} takes user-supplied job name,
     *      this parameter should be used as a context to resolve relative names.
     *      See {@link jenkins.model.Jenkins#getItem(String, hudson.model.ItemGroup)} (where you obtain
     *      {@link hudson.model.ItemGroup} by {@link BuildPipelineView#getOwnerItemGroup()}.
     * @return
     *      Never null, although the obtained {@link ProjectGrid} can be empty.
     */
    public abstract ProjectGrid build(BuildPipelineView owner);

    /**
     * Called by {@link BuildPipelineView} when one of its members are renamed.
     *
     * @param owner
     *      View that this builder is operating under.
     * @param oldName
     *      Old short name of the job
     * @param newName
     *      New short name of the job
     * @param item
     *      Job being renamed.
     */
    public void onJobRenamed(BuildPipelineView owner, Item item, String oldName, String newName) throws IOException {
        // no-op
    }

    /**
     * If the grid produced by this builder supports the notion of "starting a new pipeline instance",
     * and if the current user has a permission to do so, then return true.
     *
     * @param owner
     *      View that this builder is operating under.
     * @return
     *      True if the user has a permission.
     */
    public abstract boolean hasBuildPermission(BuildPipelineView owner);

    /**
     * If the first job of the grid produced by this builder has parameters
     *
     * @param owner
     *      View that this builder is operating under.
     * @return
     *      True if the first job has parameters.
     */
    public abstract boolean startsWithParameters(BuildPipelineView owner);

    /**
     * Called to start a new pipeline instance
     * (normally by triggering some job.)
     *
     * @param req
     *      Current HTTP request
     * @param owner
     *      View that this builder is operating under.
     * @return
     *      The HTTP response.
     */
    public abstract HttpResponse doBuild(StaplerRequest req, @AncestorInPath BuildPipelineView owner) throws IOException;

    /**
     * {@inheritDoc}
     */
    @Override
    public ProjectGridBuilderDescriptor getDescriptor() {
        return (ProjectGridBuilderDescriptor) super.getDescriptor();
    }
}
