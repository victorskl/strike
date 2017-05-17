package strike.heartbeat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import strike.common.model.ServerInfo;
import strike.model.Lingo;
import strike.service.JSONMessageBuilder;
import strike.service.PeerClient;
import strike.service.ServerState;
import strike.service.election.BullyElectionManagementService;
import strike.service.election.FastBullyElectionManagementService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class GossipJob implements Job {

    private ServerState serverState = ServerState.getInstance();
    private JSONMessageBuilder jsonMessageBuilder = JSONMessageBuilder.getInstance();
    private PeerClient peerClient = new PeerClient();

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        String aliveErrorFactor = dataMap.get("aliveErrorFactor").toString();

        // first work on heart beat vector and suspect failure server list

        for (ServerInfo serverInfo : serverState.getServerInfoList()) {
            String serverId = serverInfo.getServerId();
            String myServerId = serverState.getServerInfo().getServerId();

            // get current heart beat count of a server
            Integer count = serverState.getHeartbeatCountList().get(serverId);

            // first update heart beat count vector
            if (serverId.equalsIgnoreCase(myServerId)) {
                serverState.getHeartbeatCountList().put(serverId, 0); // reset my own vector always
            } else {
                // up count all others
                if (count == null) {
                    serverState.getHeartbeatCountList().put(serverId, 1);
                } else {
                    serverState.getHeartbeatCountList().put(serverId, count + 1);
                }
            }

            // FIX get the fresh updated current count again
            count = serverState.getHeartbeatCountList().get(serverId);

            if (count != null) {
                // if heart beat count is more than error factor
                if (count > Integer.parseInt(aliveErrorFactor)) {
                    serverState.getSuspectList().put(serverId, Lingo.Gossip.SUSPECTED); // 1 = true = suspected
                } else {
                    serverState.getSuspectList().put(serverId, Lingo.Gossip.NOT_SUSPECTED); // 0 = false = not-suspected
                }
            }
        }

        // next challenge leader election if a coordinator is in suspect list

        if (null != serverState.getCoordinator()) {

            String leaderServerId = serverState.getCoordinator().getServerId();
            logger.debug("Current coordinator is : " + leaderServerId);

            // if the leader/coordinator server is in suspect list, start the election process
            if (serverState.getSuspectList().get(leaderServerId) == Lingo.Gossip.SUSPECTED) {


                // send the start election message to every server with a higher priority
                if (serverState.getIsFastBully()) {

                    new FastBullyElectionManagementService().startElection(
                            serverState.getServerInfo(),
                            serverState.getCandidateServerInfoList(),
                            serverState.getElectionAnswerTimeout());

                } else {

                    new BullyElectionManagementService().startElection(
                            serverState.getServerInfo(),
                            serverState.getCandidateServerInfoList(),
                            serverState.getElectionAnswerTimeout());

                    new BullyElectionManagementService().startWaitingForAnswerMessage(
                            serverState.getServerInfo(), serverState.getElectionAnswerTimeout());

                }
            }
        }

        // finally gossip about heart beat vector to a next peer

        int numOfServers = serverState.getServerInfoList().size();

        if (numOfServers > 1) { // Gossip required at least 2 servers to be up

            // after updating the heartbeatCountList, randomly select a server and send
            int serverIndex = ThreadLocalRandom.current().nextInt(numOfServers - 1);
            ArrayList<String> remoteServer = new ArrayList<>();
            for (ServerInfo serverInfo : serverState.getServerInfoList()) {
                String serverId = serverInfo.getServerId();
                String myServerId = serverState.getServerInfo().getServerId();
                if (!serverId.equalsIgnoreCase(myServerId)) {
                    remoteServer.add(serverId);
                }
            }
            Collections.shuffle(remoteServer, new Random(System.nanoTime())); // another way of randomize the list

            // change concurrent hashmap to hashmap before sending
            HashMap<String, Integer> heartbeatCountList = new HashMap<>(serverState.getHeartbeatCountList());
            String gossipMessage = jsonMessageBuilder.gossipMessage(serverState.getServerInfo().getServerId(), heartbeatCountList);
            peerClient.commPeerOneWay(serverState.getServerInfoById(remoteServer.get(serverIndex)), gossipMessage);
        }

    }

    private static final Logger logger = LogManager.getLogger(GossipJob.class);
}
