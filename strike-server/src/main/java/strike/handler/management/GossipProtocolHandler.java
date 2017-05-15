package strike.handler.management;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import strike.common.model.Protocol;
import strike.handler.IProtocolHandler;

import java.util.HashMap;

/**
 * Created by Administrator on 15/05/2017.
 */
public class GossipProtocolHandler extends ManagementHandler implements IProtocolHandler {
    private static final Logger logger = LogManager.getLogger(GossipProtocolHandler.class);

    public GossipProtocolHandler(JSONObject jsonMessage, Runnable connection) {
        super(jsonMessage, connection);
    }

    public void handle() {

        HashMap<String, Long> gossipFromOthers = (HashMap<String, Long>) jsonMessage.get(Protocol.heartbeatcountlist.toString());
        String fromServer = (String) jsonMessage.get(Protocol.serverid.toString());
        logger.debug("Received gossip from : " + jsonMessage.get(Protocol.serverid.toString()));

        System.out.println("Receving from: "+ fromServer + "     "+gossipFromOthers);

        //update the heartbeatcountlist by taking minimum
        for (String serverId : gossipFromOthers.keySet()) {
            Integer localHeartbeatCount = serverState.getHeartbeatCountList().get(serverId);
            Integer remoteHeartbeatCount = ((Long)gossipFromOthers.get(serverId)).intValue();
            if (localHeartbeatCount!= null && remoteHeartbeatCount < localHeartbeatCount) {
                serverState.getHeartbeatCountList().put(serverId, remoteHeartbeatCount);
            }
        }

        System.out.println("Current server state is :" + serverState.getHeartbeatCountList());


    }
}
