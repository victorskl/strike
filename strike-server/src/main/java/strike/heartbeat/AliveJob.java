package strike.heartbeat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import strike.common.model.ServerInfo;
import strike.service.*;

public class AliveJob implements Job {

    private final PeerClient peerClient = new PeerClient();
    private final JSONMessageBuilder messageBuilder = JSONMessageBuilder.getInstance();
    private final ServerState serverState = ServerState.getInstance();

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

        if (null != serverState.getCoordinator()) {
            // let it put in trace for now, bec take debug as default dev mode
            logger.trace("Current coordinator is : " + serverState.getCoordinator().getServerId());
        }

        for (ServerInfo serverInfo : serverState.getServerInfoList()) {
            String serverId = serverInfo.getServerId();
            String myServerId = serverState.getServerInfo().getServerId();
            if (serverId.equalsIgnoreCase(myServerId)) {
                continue;
            }

            boolean online = serverState.isOnline(serverInfo);
            if (!online) {
                Integer count = serverState.getAliveMap().get(serverId);
                if (count == null) {
                    serverState.getAliveMap().put(serverId, 1);
                } else {
                    serverState.getAliveMap().put(serverId, count + 1);
                }

                JobDataMap dataMap = context.getJobDetail().getJobDataMap();
                String aliveErrorFactor = dataMap.get("aliveErrorFactor").toString();

                count = serverState.getAliveMap().get(serverId);

                if (count > Integer.parseInt(aliveErrorFactor)) {
                    // if the offline server is the coordinator, start the election process
                    if (null != serverState.getCoordinator() && serverInfo.getServerId().equalsIgnoreCase(serverState
                            .getCoordinator().getServerId())) {
                        // send the start election message to every server with a higher priority
                        if (serverState.getIsFastBully()) {
                            try {
                                new FastBullyElectionManagementService().startElection(serverState.getServerInfo(),
                                        serverState.getCandidateServerInfoList(), serverState.getElectionAnswerTimeout(),
                                        serverState);
                            } catch (SchedulerException e) {
                                logger.error("Unable to start the election : " + e.getLocalizedMessage());
                            }
                        } else {
                            try {
                                new BullyElectionManagementService()
                                        .startElection(serverState.getServerInfo(), serverState.getCandidateServerInfoList(),
                                                serverState.getElectionAnswerTimeout());
                                new BullyElectionManagementService().startWaitingForAnswerMessage(serverState.getServerInfo(),
                                        new StdSchedulerFactory().getScheduler(), serverState.getElectionAnswerTimeout());
                            } catch (SchedulerException e) {
                                logger.error("Unable to start the default bully election : "
                                        + e.getLocalizedMessage());
                            }
                        }
                    }
                    peerClient.relayPeers(messageBuilder.notifyServerDownMessage(serverId));
                    logger.debug("Notify server " + serverId + " down. Removing...");

                    serverState.removeServer(serverId);
                    serverState.removeRemoteChatRoomsByServerId(serverId);
                    serverState.removeRemoteUserSessionsByServerId(serverId);
                }
            }
        }
    }

    private static final Logger logger = LogManager.getLogger(AliveJob.class);
}
