package strike.service.election.timeout;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import strike.common.model.ServerInfo;
import strike.service.election.FastBullyElectionManagementService;

@DisallowConcurrentExecution
public class FastBullyViewMessageTimeoutFinalizer extends MessageTimeoutFinalizer {

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        FastBullyElectionManagementService fastBullyElectionManagementService =
                new FastBullyElectionManagementService();
        ServerInfo myServerInfo = serverState.getServerInfo();
        if (!interrupted.get() && !serverState.viewMessageReceived()) {

            // a view message was not received
            // stop the election
            fastBullyElectionManagementService.stopElection(myServerInfo);


            // FIX: At boot up time, if we already accepted a leader, it shouldn't proceed these.
            // Otherwise, once timeout happen, these will get executed and overwrite the accepted
            // leader state - which is already handled by FastBullyViewMessageHandler.
            // If this FastBullyViewMessageTimeoutFinalizer job is only used at boot time
            // then it can safely guard with if condition ->  if i have an accepted leader

            if (null == serverState.getCoordinator()) {
                // inform subordinates of new coordinator
                fastBullyElectionManagementService.sendCoordinatorMessage(myServerInfo, serverState
                        .getSubordinateServerInfoList());
                // accept myself as the new coordinator
                fastBullyElectionManagementService.acceptNewCoordinator(myServerInfo);
            }

            serverState.setViewMessageReceived(false);
        }
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    private static final Logger logger = LogManager.getLogger(FastBullyViewMessageTimeoutFinalizer.class);
}
