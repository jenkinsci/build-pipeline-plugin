package au.com.centrumsystems.hudson.plugin.util;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Queue;
import hudson.model.Queue.WaitingItem;
import hudson.model.queue.QueueListener;

import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import jenkins.model.Jenkins;

/**
 * Utitily class to help scheduling Jobs in the context of the build-pipeline
 * plugin.
 * 
 * @author rhirt
 * 
 */
public final class QueueUtil extends QueueListener {
    /** A Logger object is used to log messages */
    private static final Logger LOGGER = Logger.getLogger(QueueUtil.class.getName());

    /** A static hashmap to bind queued jobs to builds **/
    private static final HashMap<String, QueueEntry> QUEUE_ENTRIES = new HashMap<String, QueueEntry>();

    /**
     * Adding an entry into the list of queued items.
     * 
     * @param queueEntry
     *            - entry to be added
     */
    public static void addQueueEntry(QueueEntry queueEntry) {
        LOGGER.info("Add entry to QueueUtil: " + queueEntry.toString());
        QUEUE_ENTRIES.put(queueEntry.getKey(), queueEntry);
    }

    /**
     * Remove an entry from the list of queued items.
     * 
     * @param queueEntry
     *            - entry to be removed
     */
    public static void removeQueueEntry(QueueEntry queueEntry) {
        LOGGER.info("Remove entry from QueueUtil: " + queueEntry.toString());
        if (QUEUE_ENTRIES.remove(queueEntry.getKey()) != null) {
            LOGGER.info("Done - removed entry from QueueUtil: " + queueEntry.toString());            
        }
    }

    /**
     * Retrieve a queue entry based on an upstream build
     * 
     * @param upstreamBuild
     *            - the upstream build to look for
     * @return QueueEntry - the entry if found
     */
    public static QueueEntry getQueueEntry(AbstractBuild<?, ?> upstreamBuild) {
        if (upstreamBuild == null) {
            return null;
        }
        return QUEUE_ENTRIES.get(new QueueEntry(upstreamBuild).getKey());
    }

    /**
     * Retrieve the queued items for a project
     * 
     * @param project
     *            - the project to search queued items for
     * @return List<Queue.Item> - the items
     */
    public static List<Queue.Item> getQueuedItemsFor(AbstractProject<?, ?> project) {
        return Jenkins.getInstance().getQueue().getItems(project);
    }

    /**
     * Retrieve the id of a new queue item relative to the list of items before
     * 
     * @param project
     *            - the project to search queued items for
     * @param oldItems
     *            - list of items to check the new list against
     * @return int - the id of the new queue item
     */
    public static int getNewQueuedItemId(AbstractProject<?, ?> project, List<Queue.Item> oldItems) {
        final List<Queue.Item> newItems = Jenkins.getInstance().getQueue().getItems(project);

        // Now search the new list for items that are not in the old list, if
        // one does exist, return it's id
        for (Queue.Item qitem : newItems) {
            if (!oldItems.contains(qitem)) {
                LOGGER.fine("Found last queued item for project " + project.getDisplayName() + " with id " + qitem.id);
                return qitem.id;
            }
        }
        return 0;
    }

    /**
     * Retrieve a queued item from the build history
     * 
     * @param project
     *            - the project to search queued items for
     * @param qentry
     *            the entry to derive items from
     * @return Item - the item from the queue for the given name
     */
    public static Queue.Item getQueuedItem(AbstractProject<?, ?> project, QueueEntry qentry) {
        return getQueuedItem(project, qentry.getQueueId());
    }

    /**
     * Retrieve a Queue.Item of type WaitingItem
     * 
     * @param project
     *            - the project to search queued items for
     * @param upstreamBuild
     *            the upstream build we derive the queue item from
     * @return WaitingItem - the waiting item from the queue for the given
     *         upstreamBuild
     */
    public static WaitingItem getQueuedWaitingItem(AbstractProject<?, ?> project, AbstractBuild<?, ?> upstreamBuild) {
        final QueueEntry qentry = getQueueEntry(upstreamBuild);
        if (qentry != null) {
            final Queue.Item qItem = QueueUtil.getQueuedItem(project, qentry);

            if (qItem instanceof WaitingItem) {
                return (WaitingItem) qItem;
            }
        }
        return null;
    }

    /**
     * Retrieve a queued item with the given id from the build history
     * 
     * @param project
     *            - the project to search queued items for
     * @param queueId
     *            - the id of the queued item
     * @return Item - the item from the queue for the given name
     */
    private static Queue.Item getQueuedItem(AbstractProject<?, ?> project, int queueId) {
        final List<Queue.Item> qitems = Jenkins.getInstance().getQueue().getItems(project);

        // Now search queued items for given queue id
        for (Queue.Item qitem : qitems) {
            if (qitem.id == queueId) {
                LOGGER.fine("Found queued item for Project " + project.getDisplayName() + " and id " + queueId);
                return qitem;
            }
        }
        return null;
    }
}
