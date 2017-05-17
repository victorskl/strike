package strike.handler.management.consensus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import strike.common.model.Protocol;
import strike.handler.IProtocolHandler;
import strike.handler.management.ManagementHandler;
import strike.model.Lingo;
import strike.service.PeerClient;

public class StartVoteMessageHandler extends ManagementHandler implements IProtocolHandler {

    private PeerClient peerClient;

    public StartVoteMessageHandler(JSONObject jsonMessage, Runnable connection) {
        super(jsonMessage, connection);
        peerClient = new PeerClient();
    }

    @Override
    public void handle() {

        String suspectServerId = (String) jsonMessage.get(Protocol.suspectserverid.toString());
        String serverId = (String) jsonMessage.get(Protocol.serverid.toString());
        String myServerId = serverState.getServerInfo().getServerId();

        if (serverState.getSuspectList().containsKey(suspectServerId)) {
            if (serverState.getSuspectList().get(suspectServerId) == Lingo.Gossip.SUSPECTED) {
                //messageQueue.add(new Message(false, messageBuilder.answerVoteMessage(serverId, "yes")));
                peerClient.commPeerOneWay(serverState.getServerInfoById(serverId),
                        messageBuilder.answerVoteMessage(suspectServerId, Lingo.Consensus.YES.toString(), myServerId));
                logger.debug(String.format("Voting on suspected server: [%s] vote: [%s]", suspectServerId, Lingo.Consensus.YES));

            } else {
                //messageQueue.add(new Message(false, messageBuilder.answerVoteMessage(serverId, "no")));
                peerClient.commPeerOneWay(serverState.getServerInfoById(serverId),
                        messageBuilder.answerVoteMessage(suspectServerId, Lingo.Consensus.NO.toString(), myServerId));
                logger.debug(String.format("Voting on suspected server: [%s] vote: [%s]", suspectServerId, Lingo.Consensus.NO));
            }
        }
    }

    private static final Logger logger = LogManager.getLogger(StartVoteMessageHandler.class);
}
