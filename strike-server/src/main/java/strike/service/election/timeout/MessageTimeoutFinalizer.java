package strike.service.election.timeout;

import org.apache.logging.log4j.Logger;
import org.quartz.*;
import strike.service.ServerState;

import java.util.concurrent.atomic.AtomicBoolean;

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
