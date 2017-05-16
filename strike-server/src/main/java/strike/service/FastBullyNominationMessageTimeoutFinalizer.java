package strike.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;

/**
 *
 */
public class FastBullyNominationMessageTimeoutFinalizer extends MessageTimeoutFinalizer {
    private static final Logger logger = LogManager.getLogger(FastBullyNominationMessageTimeoutFinalizer.class);

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        if (!interrupted.get()) {
            try {
                // stop any ongoing election
                new FastBullyElectionManagementService()
                        .stopElection(serverState.getServerInfo(), StdSchedulerFactory.getDefaultScheduler(),
                                serverState);
                // restart the election procedure
                new FastBullyElectionManagementService().startElection(serverState.getServerInfo(), serverState
                        .getCandidateServerInfoList(), serverState.getElectionAnswerTimeout(), serverState);
            } catch (SchedulerException e) {
                logger.error("Error while starting election because no coordinator or " +
                        "nomination message was received : " + e.getLocalizedMessage());
            }
        }
    }

    @Override
    public Logger getLogger() {
        return logger;
    }
}
