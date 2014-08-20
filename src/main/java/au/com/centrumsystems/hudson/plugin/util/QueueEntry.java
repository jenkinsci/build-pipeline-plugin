package au.com.centrumsystems.hudson.plugin.util;

import hudson.model.AbstractBuild;

/**
 * Utility Class to capture a link from a build form to a queue item.
 * @author rhirt
 *
 */
public class QueueEntry {
    /**
     * The name of the upstream project 
     */
    private String upstreamProjectName;
    
    /**
     * The externalizable id of the upstream build.
     */
    private String upstreamExtId;
        
    /**
     * The id of the item in the queue. 
     */
    private int queueId;
    
    /**
     * Construct from upstreamBuild
     * @param upstreamBuild - the upstream build this entry is referring to
     */
    public QueueEntry(AbstractBuild<?, ?> upstreamBuild) {
        this(upstreamBuild.getProject().getName(), upstreamBuild.getExternalizableId());
    }
    /**
     * Construct from projectName and externalizable id of upstreamBuild
     * @param upstreamProjectName - the project name of the build upstream
     * @param upstreamExtId - the externalizable id of the build upstream
     */
    public QueueEntry(String upstreamProjectName, String upstreamExtId) {
        this(upstreamProjectName, upstreamExtId, 0);
    }
    /**
     * Construct from projectName and externalizable id of upstreamBuild
     * @param upstreamProjectName - the project name of the build upstream
     * @param upstreamBuildId - the build number of the build upstream
     */
    public QueueEntry(String upstreamProjectName, int upstreamBuildId) {
        this(upstreamProjectName, upstreamProjectName + "#" + upstreamBuildId, 0);
    }
    /**
     * Construct from projectName and externalizable id of upstreamBuild, add a queueId
     * @param upstreamProjectName - the project name of the build upstream
     * @param upstreamExtId - the ext id of the build upstream
     * @param queueId - the id of the build in the queue
     */
    public QueueEntry(String upstreamProjectName, String upstreamExtId, int queueId) {
        this.upstreamProjectName = upstreamProjectName;
        this.upstreamExtId = upstreamExtId;
        this.queueId = queueId;
    }

    /**
     * Derive a map key from the ext-id
     * @return the key for the map
     */
    public String getKey() {
        return upstreamExtId;
    }

    public String getUpstreamExtId() {
        return upstreamExtId;
    }

    public void setUpstreamExtId(String upstreamExtId) {
        this.upstreamExtId = upstreamExtId;
    }

    public int getQueueId() {
        return queueId;
    }

    public void setQueueId(int queueId) {
        this.queueId = queueId;
    }
    
    public String getUpstreamProjectName() {
        return upstreamProjectName;
    }
    
    public void setUpstreamProjectName(String upstreamProjectName) {
        this.upstreamProjectName = upstreamProjectName;
    }
    
    @Override
    public String toString() {
        return "QueueEntry [upstreamProjectName=" + upstreamProjectName + ", upstreamExtId=" + upstreamExtId + ", queueId=" + queueId + "]";
    }
    
}
