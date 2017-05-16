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
public class FastBullyViewMessageTimeoutFinalizer extends MessageTimeoutFinalizer {
    private static final Logger logger = LogManager.getLogger(FastBullyViewMessageTimeoutFinalizer.class);

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        FastBullyElectionManagementService fastBullyElectionManagementService =
                new FastBullyElectionManagementService();
        ServerInfo myServerInfo = serverState.getServerInfo();
        if (!interrupted.get()) {
            // a view message was not received
            try {
                // stop the election
                fastBullyElectionManagementService
                        .stopElection(myServerInfo, StdSchedulerFactory.getDefaultScheduler(), serverState);
            } catch (SchedulerException e) {
                logger.error("Error while stopping the election upon timeout at view message: "
                        + e.getLocalizedMessage());
            }
            // inform subordinates of new coordinator
            fastBullyElectionManagementService.sendCoordinatorMessage(myServerInfo, serverState
                    .getSubordinateServerInfoList());
            // accept myself as the new coordinator
            fastBullyElectionManagementService.acceptNewCoordinator(myServerInfo, serverState);
        }
    }
}
