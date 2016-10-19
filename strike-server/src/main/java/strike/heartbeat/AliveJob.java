package strike.heartbeat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import strike.model.ServerInfo;
import strike.service.JSONMessageBuilder;
import strike.service.PeerClient;
import strike.service.ServerState;

public class AliveJob implements Job {

    private PeerClient peerClient = new PeerClient();
    private JSONMessageBuilder messageBuilder = JSONMessageBuilder.getInstance();
    private ServerState serverState = ServerState.getInstance();

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

        for (ServerInfo serverInfo : serverState.getServerInfoList()) {
            String serverId = serverInfo.getServerId();
            if (serverId.equalsIgnoreCase(serverState.getServerInfo().getServerId())) {
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
