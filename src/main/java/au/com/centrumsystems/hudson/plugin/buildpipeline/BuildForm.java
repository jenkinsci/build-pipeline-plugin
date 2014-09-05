package au.com.centrumsystems.hudson.plugin.buildpipeline;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.ParametersDefinitionProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.kohsuke.stapler.bind.JavaScriptMethod;

import au.com.centrumsystems.hudson.plugin.util.QueueEntry;
import au.com.centrumsystems.hudson.plugin.util.QueueUtil;

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
     * @param pipelineBuild
     *            pipeline build domain used to see the form
     */
    public BuildForm(final PipelineBuild pipelineBuild) {
        this.pipelineBuild = pipelineBuild;
        status = pipelineBuild.getCurrentBuildResult();
        dependencies = new ArrayList<BuildForm>();
        for (final PipelineBuild downstream : pipelineBuild.getDownstreamPipeline()) {
            dependencies.add(new BuildForm(downstream));
        }
        id = hashCode();
        final AbstractProject<?, ?> project = pipelineBuild.getProject();
        projectId = project.getFullName().hashCode();
        final ParametersDefinitionProperty params = project.getProperty(ParametersDefinitionProperty.class);
        final ArrayList<String> paramList = new ArrayList<String>();
        if (params != null) {
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
        return BuildJSONBuilder.asJSON(pipelineBuild, id, projectId, getDependencyIds(), getParameterList());
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
        } else {
            // try to see if the build went into queueing by searching for the queue-item from the upstream-build
            final AbstractBuild<?, ?> upstreamBuild = pipelineBuild.getUpstreamBuild();
            if (upstreamBuild != null) {
                final QueueEntry qentry = QueueUtil.getQueueEntry(upstreamBuild);
                if (QueueUtil.getQueuedItem(pipelineBuild.getProject(), qentry) != null) {
                    updated = true;
                }
            }
        }
        return updated;
    }

    /**
     * 
     * @param queueId the id of the queue-item that was cancelled
     * 
     * @return is the queued item cancelled.
     */
    @JavaScriptMethod
    public boolean cancelQueued(final int queueId) {
        boolean updated = false;
        final AbstractBuild<?, ?> upstreamBuild = pipelineBuild.getUpstreamBuild();
        if (upstreamBuild != null) {
            final QueueEntry qentry = QueueUtil.getQueueEntry(upstreamBuild);
            if (qentry.getQueueId() == queueId) {
                updated = true;
            }
        }
        return updated;
    }

    public int getNextBuildNumber() {
        return pipelineBuild.getProject().getNextBuildNumber();
    }

    public String getRevision() {
        return pipelineBuild.getPipelineVersion();
    }

    public String getFullBuildName() {
        return pipelineBuild.getProject().getDisplayName() + " #" + pipelineBuild.getCurrentBuildNumber();
    }
    
    @JavaScriptMethod
    public boolean isManualTrigger() {
        return pipelineBuild.isManualTrigger();
    }

    public Map<String, String> getParameters() {
        return pipelineBuild.getBuildParameters();
    }

    public Map<String, String> getFilteredParameters() {
        return filterSensitiveBuildVariables(pipelineBuild.getCurrentBuild());
    }
    
    public ArrayList<String> getParameterList() {
        return parameters;
    }

    public Integer getProjectId() {
        return projectId;
    }

    /**
     * Filter last successful build variables with sensitive information.
     * 
     * @param build
     *            the Build object to get the Variables from
     * 
     * @return Map<String, String> the vars, pixeled out sensitive information
     */
    private Map<String, String> filterSensitiveBuildVariables(AbstractBuild<?, ?> build) {
        final Map<String, String> allVars = build.getBuildVariables();
        final Set<String> sensitives = build.getSensitiveBuildVariables();
        final HashMap<String, String> resultVars = new HashMap<String, String>();
        for (Entry<String, String> item : allVars.entrySet()) {
            if (sensitives.contains(item.getKey())) {
                resultVars.put(item.getKey(), "******");
            } else {
                resultVars.put(item.getKey(), item.getValue());
            }
        }
        return resultVars;
    }
}
