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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;


public class GossipJob implements Job {

    private final ServerState serverState = ServerState.getInstance();
    private final JSONMessageBuilder jsonMessageBuilder = JSONMessageBuilder.getInstance();
    private final PeerClient peerClient = new PeerClient();

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

        if (null != serverState.getCoordinator()) {
            logger.debug("Current coordinator is : " + serverState.getCoordinator().getServerId());
        }

        for (ServerInfo serverInfo : serverState.getServerInfoList()) {
            String serverId = serverInfo.getServerId();
            String myServerId = serverState.getServerInfo().getServerId();
            Integer count = serverState.getHeartbeatCountList().get(serverId);
            if (serverId.equalsIgnoreCase(myServerId)) {
                serverState.getHeartbeatCountList().put(serverId, 0);
            } else {
                if (count == null) {
                    serverState.getHeartbeatCountList().put(serverId, 1);
                } else {
                    serverState.getHeartbeatCountList().put(serverId, count + 1);
                }

            }

            JobDataMap dataMap = context.getJobDetail().getJobDataMap();
            String aliveErrorFactor = dataMap.get("aliveErrorFactor").toString();

            if (count != null) {
                if (count > Integer.parseInt(aliveErrorFactor)) {
                    serverState.getSuspectList().put(serverId, 1);
                    //System.out.println("Suspecting server " + serverId);
                } else {
                    serverState.getSuspectList().put(serverId, 0);
                }
            }

        }

        int numOfServers = serverState.getServerInfoList().size();


        //after updating the heartbeatCountList, randomly select a server and send

        int serverIndex = ThreadLocalRandom.current().nextInt(numOfServers - 1);
        ArrayList<String> remoteServer = new ArrayList<>();
        for (ServerInfo serverInfo : serverState.getServerInfoList()) {
            String serverId = serverInfo.getServerId();
            String myServerId = serverState.getServerInfo().getServerId();
            if (!serverId.equalsIgnoreCase(myServerId)) {
                remoteServer.add(serverId);
            }
        }

        //build json msg, send commpeeroneway
        //change concurrent hashmap to hashmap before sending
        HashMap<String, Integer> heartbeatCountList = new HashMap<String, Integer>(serverState.getHeartbeatCountList());
        String gossipMessage = jsonMessageBuilder.gossipMessage(serverState.getServerInfo().getServerId(), heartbeatCountList);
        peerClient.commPeerOneWay(serverState.getServerInfoById(remoteServer.get(serverIndex)), gossipMessage);

        //System.out.println("Sending to:"+(remoteServer.get(serverIndex))+"    "+ gossipMessage);

    }

    private static final Logger logger = LogManager.getLogger(AliveJob.class);
}
