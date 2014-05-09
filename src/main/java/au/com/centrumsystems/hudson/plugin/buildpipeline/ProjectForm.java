package au.com.centrumsystems.hudson.plugin.buildpipeline;

import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.plugins.parameterizedtrigger.SubProjectsAction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import jenkins.model.Jenkins;

import org.kohsuke.stapler.bind.JavaScriptMethod;

/**
 * @author Centrum Systems
 * 
 *         Representation of a set of projects
 * 
 */
public class ProjectForm {
    /**
     * project name
     */
    private final String name;
    /**
     * last build result
     */
    private final String result;
    /**
     * overall health
     */
    private final String health;
    /**
     * project url
     */
    private final String url;
    /**
     * downstream projects
     */
    private final List<ProjectForm> dependencies;
    /**
     * display manual build
     */
    private Boolean displayTrigger;

    /**
     * the latest successful build number
     */
    private String lastSuccessfulBuildNumber;

    /**
     * the parameters used in the last successful build
     */
    private Map<String, String> lastSuccessfulBuildParams;

    /**
     * keep reference to the project so that we can update it
     */
    private final AbstractProject<?, ?> project;

    /**
     * keep reference to the first project in the current pipeline view
     */
    private final AbstractProject<?, ?> firstProject;

    /**
     * Hold the row this project is placed in (handle duplicate project-ids)
     */
    private int row;

    /**
     * Hold the column this project is placed in (handle duplicate project-ids)
     */
    private int col;

    /**
     * @param name
     *            project name
     */
    public ProjectForm(final String name) {
        this.name = name;
        result = "";
        health = "";
        url = "";
        lastSuccessfulBuildNumber = "";
        lastSuccessfulBuildParams = new HashMap<String, String>();
        dependencies = new ArrayList<ProjectForm>();
        this.displayTrigger = true;
        project = null;
        firstProject = null;
    }

    /**
     * @param project
     *            - the project to wrap
     */
    public ProjectForm(final AbstractProject<?, ?> project) {
        this(project, null);
    }

    /**
     * @param project
     *            - the project to wrap
     * @param firstProject
     *            - the first project associated with the grid for this
     *            projectform
     */
    public ProjectForm(final AbstractProject<?, ?> project, final AbstractProject<?, ?> firstProject) {

        final PipelineBuild pipelineBuild = new PipelineBuild(project.getLastBuild(), project, null);

        name = pipelineBuild.getProject().getFullName();
        result = pipelineBuild.getCurrentBuildResult();
        health = pipelineBuild.getProject().getBuildHealth().getIconUrl().replaceAll("\\.gif", "\\.png");
        url = pipelineBuild.getProjectURL();
        dependencies = new ArrayList<ProjectForm>();
        for (final AbstractProject<?, ?> dependency : project.getDownstreamProjects()) {
            dependencies.add(new ProjectForm(dependency, firstProject));
        }
        if (Jenkins.getInstance().getPlugin("parameterized-trigger") != null) {
            for (SubProjectsAction action : Util.filter(project.getActions(), SubProjectsAction.class)) {
                for (hudson.plugins.parameterizedtrigger.BlockableBuildTriggerConfig config : action.getConfigs()) {
                    for (final AbstractProject<?, ?> dependency : config.getProjectList(project.getParent(), null)) {
                        final ProjectForm candidate = new ProjectForm(dependency, firstProject);
                        // if subprojects come back as downstreams someday, no
                        // duplicates wanted
                        if (!dependencies.contains(candidate)) {
                            dependencies.add(candidate);
                        }
                    }
                }
            }
        }
        this.displayTrigger = true;

        // Adjust retrieval of lastSuccessfulBuild per pipeline (if jobs are
        // used in different pipelines, to avoid wrong parameters in the
        // headers)
        handleLastSuccessfulBuild(pipelineBuild.getProject().getLastSuccessfulBuild());

        this.project = project;
        this.firstProject = firstProject;
    }

    /**
     * Wraps possibly null {@link AbstractProject} into {@link ProjectForm}.
     * This method is only called for a starting project in a pipeline view,
     * therefore save it as such.
     * 
     * @param p
     *            project to be wrapped.
     * @return possibly null.
     */
    public static ProjectForm as(AbstractProject<?, ?> p) {
        return p != null ? new ProjectForm(p, p) : null;
    }

    public String getName() {
        return name;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return col;
    }

    /**
     * Set the coordinates for this project form.
     * 
     * @param row
     *            - the row this form is set into
     * @param col
     *            - the column this form is set into
     */
    public void setCoords(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public String getHealth() {
        return health;
    }

    public String getResult() {
        return result;
    }

    public String getUrl() {
        return url;
    }

    public String getLastSuccessfulBuildNumber() {
        return lastSuccessfulBuildNumber;
    }

    public Map<String, String> getLastSuccessfulBuildParams() {
        return lastSuccessfulBuildParams;
    }

    public List<ProjectForm> getDependencies() {
        return dependencies;
    }

    /**
     * Gets a display value to determine whether a manual jobs 'trigger' button
     * will be shown. This is used along with isTriggerOnlyLatestJob property
     * allow only the latest version of a job to run.
     * 
     * Works by: Initially always defaulted to true. If isTriggerOnlyLatestJob
     * is set to true then as the html code is rendered the first job which
     * should show the trigger button will render and then a call will be made
     * to 'setDisplayTrigger' to change the value to both so all future jobs
     * will not display the trigger. see main.jelly
     * 
     * @return boolean whether to display or not
     */
    public Boolean getDisplayTrigger() {
        return displayTrigger;
    }

    /**
     * Sets a display value to determine whether a manual jobs 'trigger' button
     * will be shown. This is used along with isTriggerOnlyLatestJob property
     * allow only the latest version of a job to run.
     * 
     * Works by: Initially always defaulted to true. If isTriggerOnlyLatestJob
     * is set to true then as the html code is rendered the first job which
     * should show the trigger button will render and then a call will be made
     * to 'setDisplayTrigger' to change the value to both so all future jobs
     * will not display the trigger. see main.jelly
     * 
     * @param display
     *            - boolean to indicate whether the trigger button should be
     *            shown
     */
    public void setDisplayTrigger(final Boolean display) {
        displayTrigger = display;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ProjectForm other = (ProjectForm) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }

    public int getId() {
        return name.hashCode();
    }

    /**
     * Project as JSON
     * 
     * @return JSON string
     */
    @JavaScriptMethod
    public String asJSON() {
        return ProjectJSONBuilder.asJSON(this); // new ProjectForm(project,
                                                // firstProject));
    }

    /**
     * Filter last successful build variables with sensitive information.
     * 
     * @param build
     *            the Build object to get the Variables from
     * 
     * @return Map<String, String> the vars, pixeled out sensitive information
     */
    public Map<String, String> filterSensitiveBuildVariables(AbstractBuild<?, ?> build) {
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

    /**
     * Correct the last successful build settings of this form, if that build is
     * not on the current BuildPipelineView (when Jobs are used on multiple
     * Pipelines)
     * 
     * @param buildGrids
     *            - the build-grids present on this view
     */
    public void correctLastSuccessfulBuilds(Iterable<BuildGrid> buildGrids) {
        AbstractBuild<?, ?> lastSuccessfulBuild = project.getLastSuccessfulBuild();

        outer: while (lastSuccessfulBuild != null) {
            // Search the buildGrid for the lastSuccessfulBuild given above
            BuildForm buildForm = null;
            for (BuildGrid buildGrid : buildGrids) {
                buildForm = buildGrid.findBuildForm(lastSuccessfulBuild);
                if (buildForm != null) {
                    break outer;
                }
            }
            lastSuccessfulBuild = lastSuccessfulBuild.getPreviousSuccessfulBuild();
        }
        // Set the (may be) newly found last successful build
        handleLastSuccessfulBuild(lastSuccessfulBuild);
    }

    /**
     * Set build-number and build-params for last successful build.
     * 
     * @param lastSuccessfulBuild
     *            - the last successful build (can be null)
     */
    private void handleLastSuccessfulBuild(AbstractBuild<?, ?> lastSuccessfulBuild) {
        lastSuccessfulBuildNumber = (null == lastSuccessfulBuild) ? "" : "" + lastSuccessfulBuild.getNumber();
        lastSuccessfulBuildParams = (null == lastSuccessfulBuild) ? new HashMap<String, String>()
                : filterSensitiveBuildVariables(lastSuccessfulBuild);
    }
}
