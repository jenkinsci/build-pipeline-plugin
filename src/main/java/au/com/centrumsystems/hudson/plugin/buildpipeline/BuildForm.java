package au.com.centrumsystems.hudson.plugin.buildpipeline;

import com.cloudbees.plugins.flow.FlowRun;
import com.cloudbees.plugins.flow.JobInvocation;
import com.google.common.primitives.Ints;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.ItemGroup;
import hudson.model.ParametersDefinitionProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.logging.Logger;
import java.util.Map;
import java.util.Set;


import org.jgrapht.DirectedGraph;
import org.jgrapht.traverse.DepthFirstIterator;
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
        BuildForm flowRunFirstLongestDistanceBuildForm = null;
        if (pipelineBuild.getCurrentBuild() instanceof FlowRun) {
            final FlowRun flowRun = (FlowRun) pipelineBuild.getCurrentBuild();
            flowRunFirstLongestDistanceBuildForm = traverseBuildFlowRunDownstreams(context, dependencies, flowRun.getJobsGraph(), 
                                                             flowRun.getStartJob(), parentPath, flowRunFirstLongestDistanceBuildForm);
        }

        for (final PipelineBuild downstream : pipelineBuild.getDownstreamPipeline()) {
            final Collection<AbstractProject<?, ?>> forkedPath = new LinkedHashSet<AbstractProject<?, ?>>(parentPath);
            if (forkedPath.add(downstream.getProject())) {
                if (flowRunFirstLongestDistanceBuildForm == null) {
                    dependencies.add(new BuildForm(context, downstream, forkedPath));
                } else {
                    flowRunFirstLongestDistanceBuildForm.dependencies.add(new BuildForm(context, downstream, forkedPath));
                }
            }
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

    /**
     * trasverse all of downstreams of the build flow run
     * @param context
     *          item group pipeline view belongs to, used to compute relative item names
     * @param dependencies
     *          the current dependencies for holding downstreams
     * @param allJobsGraphs
     *          build flow run jobs graph (jobs relationship)
     * @param jobInvocation
     *          build flow run
     * @param parentPath
     *          already traversed projects
     * @param firstLongestDistanceBuildForm
     *          the first longest distance build form (of build flow)
     * @return the first longest distance build form (of build flow) or null
     */
    private BuildForm traverseBuildFlowRunDownstreams(ItemGroup context, List<BuildForm> dependencies,
        final DirectedGraph<JobInvocation, FlowRun.JobEdge> allJobsGraphs, final JobInvocation jobInvocation,
        final Collection<AbstractProject<?, ?>> parentPath, BuildForm firstLongestDistanceBuildForm) {
        final Collection<AbstractProject<?, ?>> forkedPath = new LinkedHashSet<AbstractProject<?, ?>>(parentPath);
        final Set<FlowRun.JobEdge> edges = allJobsGraphs.outgoingEdgesOf(jobInvocation);
        for (FlowRun.JobEdge edge : edges) {
            if (needTrasverse(allJobsGraphs, edge)) {
                try {
                    final PipelineBuild downstream = new PipelineBuild((AbstractBuild<?, ?>) edge.getTarget().getBuild(),
                            edge.getTarget().getProject(), (AbstractBuild<?, ?>) jobInvocation.getBuild());
                    if (forkedPath.add(downstream.getProject())) {
                        final BuildForm bf = new BuildForm(context, downstream, forkedPath);
                        if (firstLongestDistanceBuildForm == null) { 
                            final int maxLongestDistance = Ints.max(Ints.toArray(
                                                               getJobGraphVertexsLongestDistance(allJobsGraphs).values()));
                            if (maxLongestDistance == getJobGraphVertexsLongestDistance(allJobsGraphs).get(edge.getTarget())) {
                               firstLongestDistanceBuildForm =  bf;
                            }
                        }
                        firstLongestDistanceBuildForm = traverseBuildFlowRunDownstreams(context, bf.dependencies, allJobsGraphs, 
                                                                  edge.getTarget(), parentPath, firstLongestDistanceBuildForm);
                        dependencies.add(bf);
                    }
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        return firstLongestDistanceBuildForm;
    }

    /**
     * continue trasversing or not
     * @param jobsGraph
     *          build flow run jobs graph (jobs relationship)
     * @param edge
     *          a outgoing edge in jobs graph
     * @return true - continuing trasversing, false - not
     */
    private boolean needTrasverse(DirectedGraph<JobInvocation, FlowRun.JobEdge> jobsGraph, FlowRun.JobEdge edge) {
        final Map<JobInvocation, Integer> vLd = getJobGraphVertexsLongestDistance(jobsGraph);
        final int sDistance = vLd.get(edge.getSource());
        final int tDistance = vLd.get(edge.getTarget());
        //first element: distance, the rest: all of staring sources jobs with the same longest distance
        final List<JobInvocation> firstSources = new ArrayList<JobInvocation>();
        for (FlowRun.JobEdge incomingEdeg : jobsGraph.incomingEdgesOf(edge.getTarget())) {
            final int inSourceDistance = vLd.get(incomingEdeg.getSource());
            if ((inSourceDistance + 1) == tDistance) {
                firstSources.add(incomingEdeg.getSource());
            }
        }
        //only add this downstream only, even when multiple starting source with the same longest distance
        if ((sDistance + 1) == tDistance && (firstSources.size() == 0 || firstSources.get(0).equals(edge.getSource()))) {
            return true;
        }
        return false;
    }

    /**
     * @param jobsGraph build flow run jobs graph (jobs relationship)
     *
     * @return get the longest distance (from the builf flow run vertex) for each vertex in the jobgraphs
     */
    private Map<JobInvocation, Integer> getJobGraphVertexsLongestDistance(DirectedGraph<JobInvocation, FlowRun.JobEdge> jobsGraph) {
        final DepthFirstIterator<JobInvocation, FlowRun.JobEdge> iter =
                new DepthFirstIterator<JobInvocation, FlowRun.JobEdge>(jobsGraph);

        final Map<JobInvocation, Integer> vLd = new HashMap<JobInvocation, Integer>();

        while (iter.hasNext()) {
            int distance = 0;
            final JobInvocation vertex = iter.next();
            final List<Integer> ivds = new ArrayList<Integer>(); // for calculate maximum distance
            for (FlowRun.JobEdge incommingEdge : jobsGraph.incomingEdgesOf(vertex)) {
                if (vLd.containsKey(incommingEdge.getSource())) {
                    ivds.add(vLd.get(incommingEdge.getSource()));
                }
            }
            if (!ivds.isEmpty()) {
                distance = Ints.max(Ints.toArray(ivds)) + 1;
            }
            vLd.put(vertex, distance);
        }
        return vLd;
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

    public Map<String, String> getParameters() {
        return pipelineBuild.getBuildParameters();
    }
    
    public ArrayList<String> getParameterList() {
        return parameters;
    }

    public Integer getProjectId() {
        return projectId;
    }

}
