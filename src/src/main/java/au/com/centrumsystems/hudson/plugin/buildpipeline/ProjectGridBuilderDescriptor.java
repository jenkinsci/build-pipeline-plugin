package au.com.centrumsystems.hudson.plugin.buildpipeline;

import hudson.DescriptorExtensionList;
import hudson.model.Descriptor;
import jenkins.model.Jenkins;

/**
 * {@link Descriptor} for {@link ProjectGridBuilder}.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class ProjectGridBuilderDescriptor extends Descriptor<ProjectGridBuilder> {
    /**
     * For {@link Descriptor}s that explicitly specify {@link ProjectGridBuilder}
     *
     * @param clazz
     *      The type of the {@link ProjectGridBuilder}.
     */
    public ProjectGridBuilderDescriptor(Class<? extends ProjectGridBuilder> clazz) {
        super(clazz);
    }

    /**
     * For {@link Descriptor}s that are enclosed in their {@link ProjectGridBuilder}s.
     */
    public ProjectGridBuilderDescriptor() {
    }

    /**
     * Returns all the registered {@link ProjectGridBuilder} descriptors.
     *
     * @return
     *  always non-null
     */
    public static DescriptorExtensionList<ProjectGridBuilder, ProjectGridBuilderDescriptor> all() {
        return Jenkins.getInstance().<ProjectGridBuilder, ProjectGridBuilderDescriptor>getDescriptorList(ProjectGridBuilder.class);
    }

}
