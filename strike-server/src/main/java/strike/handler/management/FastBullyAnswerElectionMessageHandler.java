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
public class FastBullyAnswerElectionMessageHandler extends ManagementHandler implements IProtocolHandler {
    private static final Logger logger = LogManager.getLogger(FastBullyAnswerElectionMessageHandler.class);

    public FastBullyAnswerElectionMessageHandler(JSONObject jsonMessage, Runnable connection) {
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

        serverState.addToTemporaryCandidateMap(potentialCandidate);
        // send nomination message to the top potential candidate
        try {
            new FastBullyElectionManagementService().setAnswerReceivedFlag(
                    StdSchedulerFactory.getDefaultScheduler());
        } catch (SchedulerException e) {
            logger.error("Unable to set answer received flag : " + e.getLocalizedMessage());
        }

    }
}
