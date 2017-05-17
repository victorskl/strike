package strike.service.election;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;

public class FastBullyNominationMessageTimeoutFinalizer extends MessageTimeoutFinalizer {

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        if (!interrupted.get()) {
            try {
                // stop any ongoing election
                new FastBullyElectionManagementService()
                        .stopElection(serverState.getServerInfo(), new StdSchedulerFactory().getScheduler());
                // restart the election procedure
                new FastBullyElectionManagementService().startElection(serverState.getServerInfo(), serverState
                        .getCandidateServerInfoList(), serverState.getElectionAnswerTimeout());
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

    private static final Logger logger = LogManager.getLogger(FastBullyNominationMessageTimeoutFinalizer.class);
}
