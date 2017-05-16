package strike.handler.management;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import strike.handler.IProtocolHandler;
import strike.service.FastBullyElectionManagementService;

/**
 *
 */
public class FastBullyNominationMessageHandler extends ManagementHandler implements IProtocolHandler {
    private static final Logger logger = LogManager.getLogger(FastBullyNominationMessageHandler.class);

    public FastBullyNominationMessageHandler(JSONObject jsonMessage, Runnable connection) {
        super(jsonMessage, connection);
    }

    @Override
    public void handle() {
        // accept the nomination and inform all the subordinate processes
        FastBullyElectionManagementService fastBullyElectionManagementService =
                new FastBullyElectionManagementService();

        // send coordinator to all the lower priority servers
        fastBullyElectionManagementService.sendCoordinatorMessage(serverState.getServerInfo(), serverState
                .getSubordinateServerInfoList());
        fastBullyElectionManagementService.acceptNewCoordinator(serverState.getServerInfo(), serverState);

        try {
            // stop the election
            fastBullyElectionManagementService
                    .stopElection(serverState.getServerInfo(), StdSchedulerFactory.getDefaultScheduler(), serverState);
        } catch (SchedulerException e) {
            logger.error("Error while stopping the election upon receipt of nomination message : " +
                    e.getLocalizedMessage());
        }
    }
}
