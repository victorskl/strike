package strike.handler.management.election;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import strike.common.model.Protocol;
import strike.handler.IProtocolHandler;
import strike.common.model.ServerInfo;
import strike.handler.management.ManagementHandler;
import strike.service.election.BullyElectionManagementService;

public class StartElectionMessageHandler extends ManagementHandler implements IProtocolHandler {

    public StartElectionMessageHandler(JSONObject jsonMessage, Runnable connection) {
        super(jsonMessage, connection);
    }

    @Override
    public void handle() {
        String potentialCandidateId = (String) jsonMessage.get(Protocol.serverid.toString());
        logger.debug("Received election msg from : " + potentialCandidateId);
        String myServerId = serverState.getServerInfo().getServerId();

        if (Integer.parseInt(myServerId) < Integer.parseInt(potentialCandidateId)) {
            // tell the election requester that I have a higher priority than him
            String potentialCandidateAddress = (String) jsonMessage.get(Protocol.address.toString());
            Integer potentialCandidatePort = Integer.parseInt((String) jsonMessage.get(Protocol.port.toString()));
            Integer potentialCandidateManagementPort =
                    Integer.parseInt((String) jsonMessage.get(Protocol.managementport.toString()));
            ServerInfo potentialCandidate =
                    new ServerInfo(potentialCandidateId, potentialCandidateAddress, potentialCandidatePort,
                            potentialCandidateManagementPort);

            new BullyElectionManagementService()
                    .replyAnswerForElectionMessage(potentialCandidate, serverState.getServerInfo());

            // start a new election among the servers that have a higher priority
            try {
                new BullyElectionManagementService()
                        .startElection(serverState.getServerInfo(), serverState.getCandidateServerInfoList(),
                                serverState.getElectionAnswerTimeout());
                new BullyElectionManagementService().startWaitingForAnswerMessage(serverState.getServerInfo(),
                        new StdSchedulerFactory().getScheduler(), serverState.getElectionAnswerTimeout());
            } catch (SchedulerException e) {
                logger.error("Unable to start the election : " + e.getLocalizedMessage());
            }
        }
    }

    private static final Logger logger = LogManager.getLogger(StartElectionMessageHandler.class);
}
