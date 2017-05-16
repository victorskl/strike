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
public class FastBullyAnswerMessageTimeoutFinalizer extends MessageTimeoutFinalizer {
    private static final Logger logger = LogManager.getLogger(FastBullyAnswerMessageTimeoutFinalizer.class);

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        FastBullyElectionManagementService fastBullyElectionManagementService =
                new FastBullyElectionManagementService();

        if (interrupted.get()) {
            // answer messages were received
            ServerInfo topCandidate = serverState.getTopCandidate();
            fastBullyElectionManagementService.sendNominationMessage(topCandidate);
            logger.debug("Answer message received. Sending nomination to : " + topCandidate.getServerId());
            try {
                fastBullyElectionManagementService
                        .startWaitingForCoordinatorMessage(StdSchedulerFactory.getDefaultScheduler(),
                                serverState.getElectionCoordinatorTimeout());
            } catch (SchedulerException e) {
                logger.error("Error while starting the timer for waiting for coordinator message : " +
                        e.getLocalizedMessage());
            }
        } else {
            // answer messages were not received
            // send coordinator message to lower priority servers
            fastBullyElectionManagementService.sendCoordinatorMessage(serverState.getServerInfo(),
                    serverState.getSubordinateServerInfoList());

            fastBullyElectionManagementService.acceptNewCoordinator(serverState.getServerInfo(), serverState);
            try {
                fastBullyElectionManagementService
                        .stopElection(serverState.getServerInfo(), StdSchedulerFactory.getDefaultScheduler(), serverState);
            } catch (SchedulerException e) {
                logger.error("Unable to stop the election : " + e.getLocalizedMessage());
            }
        }
    }

    @Override
    public Logger getLogger() {
        return logger;
    }
}
