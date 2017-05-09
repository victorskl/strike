package strike.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.*;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 */
public class ElectionCoordinatorMessageTimeoutFinalizer implements Job, InterruptableJob {
    private static final Logger logger = LogManager.getLogger(ElectionCoordinatorMessageTimeoutFinalizer.class);
    private final ServerState serverState = ServerState.getInstance();
    private final AtomicBoolean interrupted = new AtomicBoolean(false);

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        if (!interrupted.get()) {
            // no coordinator message was received from a higher priority server
            // therefore restart the election
            try {
                new BullyElectionManagementService()
                        .startElection(serverState.getServerInfo(), serverState.getCandidateServerInfoList());
            } catch (SchedulerException e) {
                logger.error("Unable to start the election : " + e.getLocalizedMessage());
            }
        }
        try {
            jobExecutionContext.getScheduler().deleteJob(jobExecutionContext.getJobDetail().getKey());
        } catch (SchedulerException e) {
            logger.error("Unable to delete the job from scheduler : " + e.getLocalizedMessage());
        }
    }

    @Override
    public void interrupt() throws UnableToInterruptJobException {
        logger.debug("Job was interrupted...");
        interrupted.set(true);
    }
}
