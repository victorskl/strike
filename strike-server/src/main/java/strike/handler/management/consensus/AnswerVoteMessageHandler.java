package strike.handler.management.consensus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import strike.common.model.Protocol;
import strike.handler.IProtocolHandler;
import strike.handler.management.ManagementHandler;
import strike.handler.management.election.StartElectionMessageHandler;

public class AnswerVoteMessageHandler extends ManagementHandler implements IProtocolHandler {

    public AnswerVoteMessageHandler(JSONObject jsonMessage, Runnable connection) {
        super(jsonMessage, connection);
    }

    @Override
    public void handle() {

        String vote = (String) jsonMessage.get(Protocol.vote.toString());
        Integer voteCount = serverState.getVoteSet().get(vote);

        logger.debug("Receiving voting message: " + jsonMessage);

        if (voteCount == null) {
            serverState.getVoteSet().put(vote, 1);
        } else {
            serverState.getVoteSet().put(vote, voteCount + 1);
        }

//        for (String vote : serverState.getVoteSet().keySet()) {
//            Integer voteCount = serverState.getVoteSet().get(vote);
//
//        }

    }

    private static final Logger logger = LogManager.getLogger(StartElectionMessageHandler.class);
}

