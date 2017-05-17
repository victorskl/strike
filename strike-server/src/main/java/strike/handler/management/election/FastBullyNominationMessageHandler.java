package strike.handler.management.election;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import strike.handler.IProtocolHandler;
import strike.handler.management.ManagementHandler;
import strike.service.election.FastBullyElectionManagementService;

public class FastBullyNominationMessageHandler extends ManagementHandler implements IProtocolHandler {

    public FastBullyNominationMessageHandler(JSONObject jsonMessage, Runnable connection) {
        super(jsonMessage, connection);
    }

    @Override
    public void handle() {
        // accept the nomination and inform all the subordinate processes
        FastBullyElectionManagementService fastBullyElectionManagementService =
                new FastBullyElectionManagementService();

        // send coordinator to all the lower priority servers
        fastBullyElectionManagementService.sendCoordinatorMessage(
                serverState.getServerInfo(),
                serverState.getSubordinateServerInfoList());

        fastBullyElectionManagementService.acceptNewCoordinator(serverState.getServerInfo());

        // stop the election
        fastBullyElectionManagementService.stopElection(serverState.getServerInfo());
    }

    private static final Logger logger = LogManager.getLogger(FastBullyNominationMessageHandler.class);
}
