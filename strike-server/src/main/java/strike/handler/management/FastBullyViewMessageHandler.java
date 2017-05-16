package strike.handler.management;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import strike.common.model.Protocol;
import strike.common.model.ServerInfo;
import strike.handler.IProtocolHandler;
import strike.service.FastBullyElectionManagementService;
import strike.service.ServerPriorityComparator;

/**
 *
 */
public class FastBullyViewMessageHandler extends ManagementHandler implements IProtocolHandler {
    private static final Logger logger = LogManager.getLogger(FastBullyViewMessageHandler.class);

    public FastBullyViewMessageHandler(JSONObject jsonMessage, Runnable connection) {
        super(jsonMessage, connection);
    }

    @Override
    public void handle() {
        String currentCoordinatorId = (String) jsonMessage.get(Protocol.currentcoordinatorid.toString());
        String coordinatorAddress = (String) jsonMessage.get(Protocol.currentcoordinatoraddress.toString());
        Integer coordinatorPort =
                Integer.parseInt((String) jsonMessage.get(Protocol.currentcoordinatorport.toString()));
        Integer coordinatorManagementPort =
                Integer.parseInt((String) jsonMessage.get(Protocol.currentcoordinatormanagementport.toString()));
        ServerInfo currentCoordinator = new ServerInfo(currentCoordinatorId, coordinatorAddress, coordinatorPort,
                coordinatorManagementPort);

        serverState.addToTemporaryCandidateMap(currentCoordinator);

        FastBullyElectionManagementService fastBullyElectionManagementService =
                new FastBullyElectionManagementService();

        ServerInfo myServerInfo = serverState.getServerInfo();
        String myServerId = myServerInfo.getServerId();
        // if the current coordinator has lower priority than me
        if (new ServerPriorityComparator().compare(myServerId, currentCoordinatorId) < 0) {
            // send new coordinator to lower priority processes
            fastBullyElectionManagementService.sendCoordinatorMessage(myServerInfo,
                    serverState.getSubordinateServerInfoList());
            fastBullyElectionManagementService.acceptNewCoordinator(myServerInfo, serverState);
        } else if (new ServerPriorityComparator().compare(myServerId, currentCoordinatorId) > 0) {
            // accept the new coordinator
            fastBullyElectionManagementService.acceptNewCoordinator(currentCoordinator, serverState);
        } else {
            // i am the existing coordinator
            fastBullyElectionManagementService.sendCoordinatorMessage(myServerInfo, serverState
                    .getSubordinateServerInfoList());
            fastBullyElectionManagementService.acceptNewCoordinator(myServerInfo, serverState);
        }
        // stop the election
        try {
            fastBullyElectionManagementService
                    .stopWaitingForViewMessage(new StdSchedulerFactory().getScheduler());
        } catch (SchedulerException e) {
            logger.error("Error while stopping the election : " + e.getLocalizedMessage());
        }
    }
}
