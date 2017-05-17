package strike.service.election.timeout;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import strike.common.model.ServerInfo;
import strike.service.election.FastBullyElectionManagementService;

@DisallowConcurrentExecution
public class FastBullyCoordinatorMessageTimeoutFinalizer extends MessageTimeoutFinalizer {

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        if (!interrupted.get()) {
            // coordinator message was not received
            // so get the next top candidate and send a nomination request
            FastBullyElectionManagementService fastBullyElectionManagementService =
                    new FastBullyElectionManagementService();

            try {

                ServerInfo topCandidate = serverState.getTopCandidate();

                if (null != topCandidate) {
                    // if there's a candidate
                    fastBullyElectionManagementService.sendNominationMessage(topCandidate);

                    // reset the timer to trigger the timeout
                    fastBullyElectionManagementService
                            .resetWaitingForCoordinatorMessageTimer(context, context.getTrigger().getKey(),
                                    serverState.getElectionCoordinatorTimeout());

                } else {
                    // if there are no candidates, start an election
                    fastBullyElectionManagementService
                            .stopElection(serverState.getServerInfo());

                    fastBullyElectionManagementService.startElection(serverState.getServerInfo(), serverState
                            .getCandidateServerInfoList(), serverState.getElectionAnswerTimeout());

                }
            } catch (NullPointerException ne) {
                // FIXME expect calling serverState.getTopCandidate() throw null.
                // look like tempCandidateServerInfoMap.pollFirstEntry() call is null,
                // trying to access .getValue() on it. cant do much from calling side.
                // how to fix this? look safe to just log and bypass now
                // as condition:   if (null != topCandidate)
                logger.debug(ne.getLocalizedMessage());
            }
        }
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    private static final Logger logger = LogManager.getLogger(FastBullyCoordinatorMessageTimeoutFinalizer.class);
}
