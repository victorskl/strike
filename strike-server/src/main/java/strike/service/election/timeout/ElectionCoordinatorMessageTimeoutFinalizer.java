package strike.service.election.timeout;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.*;
import strike.service.ServerState;
import strike.service.election.BullyElectionManagementService;

import java.util.concurrent.atomic.AtomicBoolean;

@DisallowConcurrentExecution
public class ElectionCoordinatorMessageTimeoutFinalizer implements Job, InterruptableJob {

    private ServerState serverState = ServerState.getInstance();
    private AtomicBoolean interrupted = new AtomicBoolean(false);

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        if (!interrupted.get()) {
            // no coordinator message was received from a higher priority server
            // therefore restart the election

            new BullyElectionManagementService()
                    .startElection(serverState.getServerInfo(), serverState.getCandidateServerInfoList(),
                            serverState.getElectionAnswerTimeout());

            new BullyElectionManagementService()
                    .startWaitingForAnswerMessage(serverState.getServerInfo(), serverState.getElectionAnswerTimeout());
        }

/*
        try {
            jobExecutionContext.getScheduler().deleteJob(jobExecutionContext.getJobDetail().getKey());
        } catch (SchedulerException e) {
            logger.error("Unable to delete the job from scheduler : " + e.getLocalizedMessage());
        }
*/
    }

    @Override
    public void interrupt() throws UnableToInterruptJobException {
        logger.debug("Job was interrupted...");
        interrupted.set(true);
    }

    private static final Logger logger = LogManager.getLogger(ElectionCoordinatorMessageTimeoutFinalizer.class);
}
