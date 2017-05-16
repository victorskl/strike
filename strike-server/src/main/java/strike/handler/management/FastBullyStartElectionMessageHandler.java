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
public class FastBullyStartElectionMessageHandler extends ManagementHandler implements IProtocolHandler {
    private static final Logger logger = LogManager.getLogger(FastBullyStartElectionMessageHandler.class);

    public FastBullyStartElectionMessageHandler(JSONObject jsonMessage, Runnable connection) {
        super(jsonMessage, connection);
    }

    @Override
    public void handle() {
        String potentialCandidateId = (String) jsonMessage.get(Protocol.serverid.toString());
        String potentialCandidateAddress = (String) jsonMessage.get(Protocol.address.toString());
        Integer potentialCandidatePort = Integer.parseInt((String) jsonMessage.get(Protocol.port.toString()));
        Integer potentialCandidateManagementPort =
                Integer.parseInt((String) jsonMessage.get(Protocol.managementport.toString()));
        ServerInfo potentialCandidate =
                new ServerInfo(potentialCandidateId, potentialCandidateAddress, potentialCandidatePort,
                        potentialCandidateManagementPort);

        FastBullyElectionManagementService fastBullyElectionManagementService =
                new FastBullyElectionManagementService();

        fastBullyElectionManagementService
                .replyAnswerForElectionMessage(potentialCandidate, serverState.getServerInfo());

        try {
            fastBullyElectionManagementService.startWaitingForNominationOrCoordinationMessage(
                    StdSchedulerFactory.getDefaultScheduler(), serverState.getElectionNominationTimeout());
        } catch (SchedulerException e) {
            logger.error("Error while waiting for nomination or coordination message : " + e.getLocalizedMessage());
        }
    }
}
