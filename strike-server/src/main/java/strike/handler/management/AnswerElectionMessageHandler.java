package strike.handler.management;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import strike.common.model.Protocol;
import strike.handler.IProtocolHandler;
import strike.service.BullyElectionManagementService;

/**
 *
 */
public class AnswerElectionMessageHandler extends ManagementHandler implements IProtocolHandler {
    private static final Logger logger = LogManager.getLogger(AnswerElectionMessageHandler.class);

    public AnswerElectionMessageHandler(JSONObject jsonMessage, Runnable connection) {
        super(jsonMessage, connection);
    }

    @Override
    public void handle() {
        // received an answer message from a higher priority server
        // start waiting for the coordinator message
        logger.debug("Received answer from : " + jsonMessage.get(Protocol.serverid.toString()));
        try {
            // since the answer message timeout is no longer needed, stop that timeout first
            Scheduler simpleScheduler = new StdSchedulerFactory().getScheduler();
            new BullyElectionManagementService().stopWaitingForAnswerMessage(serverState.getServerInfo(),
                    simpleScheduler);

            // start waiting for the coordinator message
            new BullyElectionManagementService().startWaitingForCoordinatorMessage(serverState.getServerInfo(),
                    simpleScheduler, serverState.getElectionCoordinatorTimeout());
        } catch (SchedulerException e) {
            // this exception occurs when scheduler is unable to create another timeout, which is fine.
            logger.debug(
                    "Error while answering the election : " + e.getLocalizedMessage());
        }
    }
}
