package strike.heartbeat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import strike.common.model.ServerInfo;
import strike.service.JSONMessageBuilder;
import strike.service.PeerClient;
import strike.service.ServerState;
import strike.service.election.BullyElectionManagementService;
import strike.service.election.FastBullyElectionManagementService;

public class AliveJob implements Job {

    private ServerState serverState = ServerState.getInstance();
    private JSONMessageBuilder messageBuilder = JSONMessageBuilder.getInstance();
    private PeerClient peerClient = new PeerClient();

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

                            new FastBullyElectionManagementService().startElection(serverState.getServerInfo(),
                                    serverState.getCandidateServerInfoList(), serverState.getElectionAnswerTimeout());

                        } else {

                            new BullyElectionManagementService().startElection(
                                    serverState.getServerInfo(),
                                    serverState.getCandidateServerInfoList(),
                                    serverState.getElectionAnswerTimeout());

                            new BullyElectionManagementService().startWaitingForAnswerMessage(
                                    serverState.getServerInfo(), serverState.getElectionAnswerTimeout());

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
