package strike.handler.management.election;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import strike.common.model.Protocol;
import strike.common.model.ServerInfo;
import strike.handler.IProtocolHandler;
import strike.handler.management.ManagementHandler;
import strike.service.election.FastBullyElectionManagementService;

public class FastBullySetCoordinatorMessageHandler extends ManagementHandler implements IProtocolHandler {

    public FastBullySetCoordinatorMessageHandler(JSONObject jsonMessage, Runnable connection) {
        super(jsonMessage, connection);
    }

    @Override
    public void handle() {
        // stop its election
        logger.debug("Received coordinator from : " + jsonMessage.get(Protocol.serverid.toString()));

        new FastBullyElectionManagementService().stopElection(serverState.getServerInfo());

        // accept the new coordinator
        String newCoordinatorId = (String) jsonMessage.get(Protocol.serverid.toString());
        String newCoordinatorAddress = (String) jsonMessage.get(Protocol.address.toString());
        Integer newCoordinatorPort = Integer.parseInt((String) jsonMessage.get(Protocol.port.toString()));
        Integer newCoordinatorManagementPort =
                Integer.parseInt((String) jsonMessage.get(Protocol.managementport.toString()));
        ServerInfo newCoordinator = new ServerInfo(newCoordinatorId, newCoordinatorAddress, newCoordinatorPort,
                newCoordinatorManagementPort);
        new FastBullyElectionManagementService().acceptNewCoordinator(newCoordinator);
    }

    private static final Logger logger = LogManager.getLogger(FastBullySetCoordinatorMessageHandler.class);
}
