package strike.handler.management.gossip;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import strike.common.model.Protocol;
import strike.handler.IProtocolHandler;
import strike.handler.management.ManagementHandler;
import strike.model.Lingo;

import java.util.HashMap;

public class GossipProtocolHandler extends ManagementHandler implements IProtocolHandler {

    public GossipProtocolHandler(JSONObject jsonMessage, Runnable connection) {
        super(jsonMessage, connection);
    }

    public void handle() {

        HashMap<String, Long> gossipFromOthers = (HashMap<String, Long>) jsonMessage.get(Protocol.heartbeatcountlist.toString());
        String fromServer = (String) jsonMessage.get(Protocol.serverid.toString());

        logger.debug("Receiving gossip from server: [" + fromServer + "] gossipping: " + gossipFromOthers);

        //update the heartbeatcountlist by taking minimum
        for (String serverId : gossipFromOthers.keySet()) {
            Integer localHeartbeatCount = serverState.getHeartbeatCountList().get(serverId);
            Integer remoteHeartbeatCount = gossipFromOthers.get(serverId).intValue();
            if (localHeartbeatCount != null && remoteHeartbeatCount < localHeartbeatCount) {
                serverState.getHeartbeatCountList().put(serverId, remoteHeartbeatCount);
            }
        }

        logger.debug("Current cluster heart beat state is: " + serverState.getHeartbeatCountList());

        // FIX
        // If this server is a leader and, remote heart beat has more suspects than me, leader will have to
        // check this condition. Because when a subordinate server come up, it will read server.tab
        // and get populated all servers again - in which some of them might be already kicked at leader.
        // That's why leader has to check this situation and put them in suspect list, so that this will
        // get pick up in next consensus run and voting cycle.
        // TODO Another way to fix this issue is, to change ServerState.heartbeatCountList data structure
        if (null != serverState.getCoordinator() && serverState.getCoordinator().getServerId()
                .equalsIgnoreCase(serverState.getServerInfo().getServerId())) {
            if (serverState.getHeartbeatCountList().size() < gossipFromOthers.size()) {
                for (String serverId : gossipFromOthers.keySet()) {
                    if (!serverState.getHeartbeatCountList().containsKey(serverId)) {
                        serverState.getSuspectList().put(serverId, Lingo.Gossip.SUSPECTED);
                    }
                }
            }
        }
    }

    private static final Logger logger = LogManager.getLogger(GossipProtocolHandler.class);
}
