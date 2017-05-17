package strike.handler.management.election;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import strike.common.model.Protocol;
import strike.handler.IProtocolHandler;
import strike.handler.management.ManagementHandler;
import strike.service.election.BullyElectionManagementService;

public class AnswerElectionMessageHandler extends ManagementHandler implements IProtocolHandler {

    public AnswerElectionMessageHandler(JSONObject jsonMessage, Runnable connection) {
        super(jsonMessage, connection);
    }

    @Override
    public void handle() {
        // received an answer message from a higher priority server
        // start waiting for the coordinator message
        logger.debug("Received answer from : " + jsonMessage.get(Protocol.serverid.toString()));

        // since the answer message timeout is no longer needed, stop that timeout first
        new BullyElectionManagementService().stopWaitingForAnswerMessage(serverState.getServerInfo());

        // start waiting for the coordinator message
        new BullyElectionManagementService().startWaitingForCoordinatorMessage(
                serverState.getServerInfo(),
                serverState.getElectionCoordinatorTimeout());

    }

    private static final Logger logger = LogManager.getLogger(AnswerElectionMessageHandler.class);
}
