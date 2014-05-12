package au.com.centrumsystems.hudson.plugin.buildpipeline;

import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Hudson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hudson.plugins.parameterizedtrigger.SubProjectsAction;
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
    private final String lastSuccessfulBuildNumber;

    /**
     * the parameters used in the last successful build
     */
    private final Map<String, String> lastSuccessfulBuildParams;

    /**
     * keep reference to the project so that we can update it
     */
    private final AbstractProject<?, ?> project;

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
    }

    /**
     * @param project
     *            project
     */
    public ProjectForm(final AbstractProject<?, ?> project) {

        final PipelineBuild pipelineBuild = new PipelineBuild(project.getLastBuild(), project, null);

        name = pipelineBuild.getProject().getFullName();
        result = pipelineBuild.getCurrentBuildResult();
        health = pipelineBuild.getProject().getBuildHealth().getIconUrl().replaceAll("\\.gif", "\\.png");
        url = pipelineBuild.getProjectURL();
        dependencies = new ArrayList<ProjectForm>();
        for (final AbstractProject<?, ?> dependency : project.getDownstreamProjects()) {
            dependencies.add(new ProjectForm(dependency));
        }
        if (Hudson.getInstance().getPlugin("parameterized-trigger") != null) {
            for (SubProjectsAction action : Util.filter(project.getActions(), SubProjectsAction.class)) {
                for (hudson.plugins.parameterizedtrigger.BlockableBuildTriggerConfig config : action.getConfigs()) {
                    for (final AbstractProject<?, ?> dependency : config.getProjectList(project.getParent(), null)) {
                        final ProjectForm candidate = new ProjectForm(dependency);
                        // if subprojects come back as downstreams someday, no duplicates wanted
                        if (!dependencies.contains(candidate)) {
                            dependencies.add(candidate);
                        }
                    }
                }
            }
        }
        this.displayTrigger = true;

        final AbstractBuild<?, ?> lastSuccessfulBuild = pipelineBuild.getProject().getLastSuccessfulBuild();
        lastSuccessfulBuildNumber = (null == lastSuccessfulBuild) ? "" : "" + lastSuccessfulBuild.getNumber();
        lastSuccessfulBuildParams = (null == lastSuccessfulBuild) ? new HashMap<String, String>() : lastSuccessfulBuild.getBuildVariables();
        this.project = project;
    }

    /**
     * Wraps possibly null {@link AbstractProject} into {@link ProjectForm}.
     *
     * @param p
     *      project to be wrapped.
     * @return
     *      possibly null.
     */
    public static ProjectForm as(AbstractProject<?, ?> p) {
        return p != null ? new ProjectForm(p) : null;
    }

    public String getName() {
        return name;
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
     * Gets a display value to determine whether a manual jobs 'trigger' button will be shown. This is used along with
     * isTriggerOnlyLatestJob property allow only the latest version of a job to run.
     *
     * Works by: Initially always defaulted to true. If isTriggerOnlyLatestJob is set to true then as the html code is rendered the first
     * job which should show the trigger button will render and then a call will be made to 'setDisplayTrigger' to change the value to both
     * so all future jobs will not display the trigger. see main.jelly
     *
     * @return boolean whether to display or not
     */
    public Boolean getDisplayTrigger() {
        return displayTrigger;
    }

    /**
     * Sets a display value to determine whether a manual jobs 'trigger' button will be shown. This is used along with
     * isTriggerOnlyLatestJob property allow only the latest version of a job to run.
     *
     * Works by: Initially always defaulted to true. If isTriggerOnlyLatestJob is set to true then as the html code is rendered the first
     * job which should show the trigger button will render and then a call will be made to 'setDisplayTrigger' to change the value to both
     * so all future jobs will not display the trigger. see main.jelly
     *
     * @param display
     *            - boolean to indicate whether the trigger button should be shown
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
        return ProjectJSONBuilder.asJSON(new ProjectForm(project));
    }

}
