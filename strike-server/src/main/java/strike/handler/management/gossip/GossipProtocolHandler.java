package strike.handler.management.gossip;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import strike.common.model.Protocol;
import strike.handler.IProtocolHandler;
import strike.handler.management.ManagementHandler;

import java.util.HashMap;

public class GossipProtocolHandler extends ManagementHandler implements IProtocolHandler {

    public GossipProtocolHandler(JSONObject jsonMessage, Runnable connection) {
        super(jsonMessage, connection);
    }

    public void handle() {

        HashMap<String, Long> gossipFromOthers = (HashMap<String, Long>) jsonMessage.get(Protocol.heartbeatcountlist.toString());
        String fromServer = (String) jsonMessage.get(Protocol.serverid.toString());

        logger.debug("Receiving gossip from server: [" + fromServer + "]\tgossipping: " + gossipFromOthers);

        //update the heartbeatcountlist by taking minimum
        for (String serverId : gossipFromOthers.keySet()) {
            Integer localHeartbeatCount = serverState.getHeartbeatCountList().get(serverId);
            Integer remoteHeartbeatCount = gossipFromOthers.get(serverId).intValue();
            if (localHeartbeatCount != null && remoteHeartbeatCount < localHeartbeatCount) {
                serverState.getHeartbeatCountList().put(serverId, remoteHeartbeatCount);
            }
        }

        logger.debug("Current servers state are: " + serverState.getHeartbeatCountList());
    }

    private static final Logger logger = LogManager.getLogger(GossipProtocolHandler.class);
}
