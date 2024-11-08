package au.com.centrumsystems.hudson.plugin.buildpipeline;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.ItemGroup;
import hudson.model.ParametersDefinitionProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.logging.Logger;

import org.kohsuke.stapler.bind.JavaScriptMethod;

/**
 * @author Centrum Systems
 * 
 *         Representation of a build results pipeline
 * 
 */
public class BuildForm {
    /**
     * logger
     */
    private static final Logger LOGGER = Logger.getLogger(BuildForm.class.getName());

    /**
     * status
     */
    private String status = "";

    /**
     * pipeline build
     */
    private PipelineBuild pipelineBuild;

    /**
     * id
     */
    private final Integer id;

    /**
     * project id used to update project cards
     */
    // TODO refactor to get rid of this coupling
    private final Integer projectId;

    /**
     * downstream builds
     */
    private List<BuildForm> dependencies = new ArrayList<BuildForm>();

    
    /**
     * project stringfied list of parameters for the project
     * */
    private final ArrayList<String> parameters;

    /**
     * The item group pipeline view belongs to
     */
    private final ItemGroup context;

    /**
     * @param pipelineBuild
     *            pipeline build domain used to see the form
     * @param context
     *            item group pipeline view belongs to, used to compute relative item names
     */
    public BuildForm(ItemGroup context, final PipelineBuild pipelineBuild) {
        this(context, pipelineBuild, new LinkedHashSet<AbstractProject<?, ?>>(Arrays.asList(pipelineBuild.getProject())));
    }

    /**
     * @param pipelineBuild
     *            pipeline build domain used to see the form
     * @param context
     *            item group pipeline view belongs to, used to compute relative item names
     * @param parentPath
     *            already traversed projects
     */
    private BuildForm(ItemGroup context, final PipelineBuild pipelineBuild, final Collection<AbstractProject<?, ?>> parentPath) {
        this.context = context;
        this.pipelineBuild = pipelineBuild;
        status = pipelineBuild.getCurrentBuildResult();
        dependencies = new ArrayList<BuildForm>();
        for (final PipelineBuild downstream : pipelineBuild.getDownstreamPipeline()) {
            final Collection<AbstractProject<?, ?>> forkedPath = new LinkedHashSet<AbstractProject<?, ?>>(parentPath);
            if (forkedPath.add(downstream.getProject())) {
                dependencies.add(new BuildForm(context, downstream, forkedPath));
            }
        }
        id = hashCode();
        final AbstractProject<?, ?> project = pipelineBuild.getProject();
        projectId = project.getFullName().hashCode() & 0x7FFFFFFF;
        final ParametersDefinitionProperty params = project.getProperty(ParametersDefinitionProperty.class);
        final ArrayList<String> paramList = new ArrayList<String>();
        if (params != null && params.getParameterDefinitionNames() != null) {
            for (String p : params.getParameterDefinitionNames()) {
                paramList.add(p);
            }
        }
        parameters = paramList;
    }

    public String getStatus() {
        return status;
    }

    public List<BuildForm> getDependencies() {
        return dependencies;
    }

    /**
     * @return All ids for existing depencies.
     */
    public List<Integer> getDependencyIds() {
        final List<Integer> ids = new ArrayList<Integer>();
        for (final BuildForm dependency : dependencies) {
            ids.add(dependency.getId());
        }
        return ids;
    }

    /**
     * @return convert pipelineBuild as json format.
     */
    @JavaScriptMethod
    public String asJSON() {
        return BuildJSONBuilder.asJSON(context, pipelineBuild, id, projectId, getDependencyIds(), getParameterList());
    }

    public int getId() {
        return id;
    }

    /**
     * 
     * @param nextBuildNumber
     *            nextBuildNumber
     * @return is the build pipeline updated.
     */
    @JavaScriptMethod
    public boolean updatePipelineBuild(final int nextBuildNumber) {
        boolean updated = false;
        final AbstractBuild<?, ?> newBuild = pipelineBuild.getProject().getBuildByNumber(nextBuildNumber);
        if (newBuild != null) {
            updated = true;
            pipelineBuild = new PipelineBuild(newBuild, newBuild.getProject(), pipelineBuild.getUpstreamBuild());
        }
        return updated;
    }

    public int getNextBuildNumber() {
        return pipelineBuild.getProject().getNextBuildNumber();
    }

    public String getRevision() {
        return pipelineBuild.getPipelineVersion();
    }

    @JavaScriptMethod
    public boolean isManualTrigger() {
        return pipelineBuild.isManualTrigger();
    }

    public ArrayList<String> getParameterList() {
        return parameters;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public AbstractBuild<?, ?> getCurrentBuild() {
        return pipelineBuild.getCurrentBuild();
    }

}
