package strike.handler.management.election;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import strike.common.model.Protocol;
import strike.common.model.ServerInfo;
import strike.handler.IProtocolHandler;
import strike.handler.management.ManagementHandler;

public class FastBullyAnswerElectionMessageHandler extends ManagementHandler implements IProtocolHandler {

    public FastBullyAnswerElectionMessageHandler(JSONObject jsonMessage, Runnable connection) {
        super(jsonMessage, connection);
    }

    @Override
    public void handle() {
        serverState.setAnswerMessageReceived(true);
        String potentialCandidateId = (String) jsonMessage.get(Protocol.serverid.toString());
        String potentialCandidateAddress = (String) jsonMessage.get(Protocol.address.toString());
        Integer potentialCandidatePort = Integer.parseInt((String) jsonMessage.get(Protocol.port.toString()));
        Integer potentialCandidateManagementPort =
                Integer.parseInt((String) jsonMessage.get(Protocol.managementport.toString()));
        ServerInfo potentialCandidate =
                new ServerInfo(potentialCandidateId, potentialCandidateAddress, potentialCandidatePort,
                        potentialCandidateManagementPort);

        serverState.addToTemporaryCandidateMap(potentialCandidate);
    }

    private static final Logger logger = LogManager.getLogger(FastBullyAnswerElectionMessageHandler.class);
}
