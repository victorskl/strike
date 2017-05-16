package strike.handler.management;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import strike.common.model.Protocol;
import strike.common.model.ServerInfo;
import strike.handler.IProtocolHandler;
import strike.service.FastBullyElectionManagementService;

/**
 *
 */
public class FastBullyIAmUpMessageHandler extends ManagementHandler implements IProtocolHandler {
    private static final Logger logger = LogManager.getLogger(FastBullyIAmUpMessageHandler.class);

    public FastBullyIAmUpMessageHandler(JSONObject jsonMessage, Runnable connection) {
        super(jsonMessage, connection);
    }

    @Override
    public void handle() {
        // reply with my view to the sender
        String senderId = (String) jsonMessage.get(Protocol.serverid.toString());
        String senderAddress = (String) jsonMessage.get(Protocol.address.toString());
        Integer senderPort = Integer.parseInt((String) jsonMessage.get(Protocol.port.toString()));
        Integer senderManagementPort =
                Integer.parseInt((String) jsonMessage.get(Protocol.managementport.toString()));
        ServerInfo sender = new ServerInfo(senderId, senderAddress, senderPort, senderManagementPort);
        ServerInfo coordinator = serverState.getCoordinator();

        serverState.addToTemporaryCandidateMap(sender);
        new FastBullyElectionManagementService().sendViewMessage(sender, coordinator);
    }
}
