package au.com.centrumsystems.hudson.plugin.util;

import hudson.Extension;
import hudson.model.Cause;
import hudson.model.CauseAction;
import hudson.model.Cause.UpstreamCause;
import hudson.model.Queue.LeftItem;
import hudson.model.Queue.WaitingItem;
import hudson.model.queue.QueueListener;

import java.util.logging.Logger;

/**
 * Utility Class to provide listener functionality for the queue.
 * @author rhirt
 */
@Extension
public class PipelineQueueListener extends QueueListener {
    /** A Logger object is used to log messages */
    private static final Logger LOGGER = Logger.getLogger(PipelineQueueListener.class.getName());

    @Override
    public void onEnterWaiting(WaitingItem wi) {
        super.onEnterWaiting(wi);
        for (final CauseAction action : wi.getActions(CauseAction.class)) {
            for (final Cause cause : action.getCauses()) {
                if (cause instanceof UpstreamCause) {
                    final UpstreamCause upstreamCause = (UpstreamCause) cause;
                    final QueueEntry qentry = new QueueEntry(upstreamCause.getUpstreamProject(), upstreamCause.getUpstreamBuild());
                    qentry.setQueueId(wi.id);
                    QueueUtil.addQueueEntry(qentry);
                }
            }
        }
    }

    @Override
    public void onLeft(LeftItem li) {
        super.onLeft(li);
        LOGGER.info("onLeft: " + li.toString());
        for (final CauseAction action : li.getActions(CauseAction.class)) {
            for (final Cause cause : action.getCauses()) {
                if (cause instanceof UpstreamCause) {
                    final UpstreamCause upstreamCause = (UpstreamCause) cause;
                    QueueUtil.removeQueueEntry(new QueueEntry(upstreamCause.getUpstreamProject(), upstreamCause.getUpstreamBuild()));
                }
            }
        }
    }
}
