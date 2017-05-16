package strike.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import strike.common.model.ServerInfo;

/**
 *
 */
public class FastBullyCoordinatorMessageTimeoutFinalizer extends MessageTimeoutFinalizer {
    private static final Logger logger = LogManager.getLogger(FastBullyCoordinatorMessageTimeoutFinalizer.class);

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        if (!interrupted.get()) {
            // coordinator message was not received
            // so get the next top candidate and send a nomination request
            FastBullyElectionManagementService fastBullyElectionManagementService =
                    new FastBullyElectionManagementService();
            ServerInfo topCandidate = serverState.getTopCandidate();
            if (null != topCandidate) {
                // if there's a candidate
                fastBullyElectionManagementService.sendNominationMessage(topCandidate);
                try {
                    // reset the timer to trigger the timeout
                    fastBullyElectionManagementService
                            .resetWaitingForCoordinatorMessageTimer(context, context.getTrigger().getKey(),
                                    serverState.getElectionCoordinatorTimeout());
                } catch (SchedulerException e) {
                    logger.error("Unable to reset the timer : " + e.getLocalizedMessage());
                }
            } else {
                // if there are no candidates, start an election
                try {
                    fastBullyElectionManagementService
                            .stopElection(serverState.getServerInfo(), new StdSchedulerFactory().getScheduler(),
                                    serverState);
                    fastBullyElectionManagementService.startElection(serverState.getServerInfo(), serverState
                            .getCandidateServerInfoList(), serverState.getElectionAnswerTimeout(), serverState);
                } catch (SchedulerException e) {
                    logger.error("Error while trying to restart an election due to timeout while waiting for " +
                            "coordinator :" + e.getLocalizedMessage());
                }
            }
        }
    }

    @Override
    public Logger getLogger() {
        return logger;
    }
}
