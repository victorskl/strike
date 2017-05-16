package strike.handler.management;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import strike.common.model.Protocol;
import strike.common.model.ServerInfo;
import strike.handler.IProtocolHandler;
import strike.service.FastBullyElectionManagementService;

/**
 *
 */
public class FastBullySetCoordinatorMessageHandler extends ManagementHandler implements IProtocolHandler {
    private static final Logger logger = LogManager.getLogger(FastBullySetCoordinatorMessageHandler.class);

    public FastBullySetCoordinatorMessageHandler(JSONObject jsonMessage, Runnable connection) {
        super(jsonMessage, connection);
    }

    @Override
    public void handle() {
        // stop its election
        logger.debug("Received coordinator from : " + jsonMessage.get(Protocol.serverid.toString()));
        try {
            new FastBullyElectionManagementService().stopElection(serverState.getServerInfo(),
                    StdSchedulerFactory.getDefaultScheduler(), serverState);
        } catch (SchedulerException e) {
            logger.error("Error while stopping the election : " + e.getLocalizedMessage());
        }

        // accept the new coordinator
        String newCoordinatorId = (String) jsonMessage.get(Protocol.serverid.toString());
        String newCoordinatorAddress = (String) jsonMessage.get(Protocol.address.toString());
        Integer newCoordinatorPort = Integer.parseInt((String) jsonMessage.get(Protocol.port.toString()));
        Integer newCoordinatorManagementPort =
                Integer.parseInt((String) jsonMessage.get(Protocol.managementport.toString()));
        ServerInfo newCoordinator = new ServerInfo(newCoordinatorId, newCoordinatorAddress, newCoordinatorPort,
                newCoordinatorManagementPort);
        new FastBullyElectionManagementService().acceptNewCoordinator(newCoordinator, serverState);
    }
}
