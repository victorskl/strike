package strike.handler.management;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import strike.common.model.Protocol;
import strike.handler.IProtocolHandler;
import strike.service.PeerClient;

public class StartVoteMessageHandler extends ManagementHandler implements IProtocolHandler {
    private static final Logger logger = LogManager.getLogger(StartElectionMessageHandler.class);
    private final PeerClient peerClient = new PeerClient();

    public StartVoteMessageHandler(JSONObject jsonMessage, Runnable connection) {
        super(jsonMessage, connection);
    }

    @Override
    public void handle() {
        String suspectServerId = (String) jsonMessage.get(Protocol.suspectserverid.toString());
        String serverId = (String) jsonMessage.get(Protocol.serverid.toString());
        logger.debug("Voting on suspected server : " + suspectServerId);
        String myServerId = serverState.getServerInfo().getServerId();

        if (serverState.getSuspectList().get(suspectServerId) == 1) {
            System.out.println("sending votes");
            //messageQueue.add(new Message(false, messageBuilder.answerVoteMessage(serverId, "yes")));
            peerClient.commPeerOneWay(serverState.getServerInfoById(serverId), messageBuilder.answerVoteMessage(serverId, "yes"));

        } else {
            //messageQueue.add(new Message(false, messageBuilder.answerVoteMessage(serverId, "no")));
            peerClient.commPeerOneWay(serverState.getServerInfoById(serverId), messageBuilder.answerVoteMessage(serverId, "no"));

        }
    }
}
