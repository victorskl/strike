package strike.service;

import org.apache.logging.log4j.Logger;
import org.quartz.InterruptableJob;
import org.quartz.Job;
import org.quartz.UnableToInterruptJobException;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 */
public abstract class MessageTimeoutFinalizer implements Job, InterruptableJob {
    protected ServerState serverState = ServerState.getInstance();
    protected AtomicBoolean interrupted = new AtomicBoolean(false);

    @Override
    public void interrupt() throws UnableToInterruptJobException {
        interrupted.set(true);
        getLogger().debug("Job was interrupted...");
    }

    public abstract Logger getLogger();
}
