package strike.service.election.timeout;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import strike.service.election.FastBullyElectionManagementService;

@DisallowConcurrentExecution
public class FastBullyNominationMessageTimeoutFinalizer extends MessageTimeoutFinalizer {

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        if (!interrupted.get()) {
            // stop any ongoing election
            new FastBullyElectionManagementService().stopElection(serverState.getServerInfo());

            // restart the election procedure
            new FastBullyElectionManagementService()
                    .startElection(
                            serverState.getServerInfo(),
                            serverState.getCandidateServerInfoList(),
                            serverState.getElectionAnswerTimeout()
                    );

        }
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    private static final Logger logger = LogManager.getLogger(FastBullyNominationMessageTimeoutFinalizer.class);
}
