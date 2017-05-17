package strike.heartbeat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import strike.service.JSONMessageBuilder;
import strike.service.PeerClient;
import strike.service.ServerState;

public class ConsensusJob implements Job {

    private ServerState serverState = ServerState.getInstance();
    private JSONMessageBuilder jsonMessageBuilder = JSONMessageBuilder.getInstance();
    private PeerClient peerClient = new PeerClient();

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        String consensusVoteDuration = dataMap.get("consensusVoteDuration").toString();
        String suspectServerId = null;

        //initialise vote set
        serverState.getVoteSet().put("yes", 0);
        serverState.getVoteSet().put("no", 0);

        //if I am leader, and suspect someone, I want to start voting to KICK him!
        if (serverState.getCoordinator() != null) {
            if (serverState.getCoordinator().getServerId().equalsIgnoreCase(serverState.getServerInfo().getServerId())) {

                String leaderServerId = serverState.getCoordinator().getServerId();
                String myServerId = serverState.getServerInfo().getServerId();

                //find the next suspect to vote and break the loop
                for (String serverId : serverState.getSuspectList().keySet()) {
                    if (serverState.getSuspectList().get(serverId) == 1) {
                        suspectServerId = serverId;
                        serverState.getVoteSet().put("yes", 1); // I suspect it already, so I vote yes.

                        String startVoteMessage = jsonMessageBuilder.startVoteMessage(myServerId, suspectServerId);
                        peerClient.relayPeers(startVoteMessage);
                        logger.debug("Leader calling to vote to kick fail-server: " + startVoteMessage);

                        break;
                    }
                }
            }

            //w8 for consensus.vote.duration determined in system.properties
            try {
                Thread.sleep(Integer.parseInt(consensusVoteDuration) * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            //remove server or do nothing

            logger.debug("Checking the vote set: " + serverState.getVoteSet());

            if (serverState.getVoteSet().get("yes") > serverState.getVoteSet().get("no")) {
                if (suspectServerId != null) {
                    peerClient.relayPeers(jsonMessageBuilder.notifyServerDownMessage(suspectServerId));
                    logger.debug("Notify server " + suspectServerId + " down. Removing...");

                    serverState.removeServer(suspectServerId);
                    serverState.removeRemoteChatRoomsByServerId(suspectServerId);
                    serverState.removeRemoteUserSessionsByServerId(suspectServerId);
                    serverState.removeServerInCountList(suspectServerId);
                    serverState.removeServerInSuspectList(suspectServerId);
                }
            }

            logger.debug("Number of servers in group: " + serverState.getServerInfoList().size());

        } else {
            // FIXME
            // if I am not a leader and I have some suspects, should ask leader to start kick
            // consensus job for the suspected servers. Bec when server up, it always read server.tab
            // and get populated servers again - which of them might be already kicked at leader.
            // Need to kick them again. Because subordinate  do not kick, delegate this task to leader.
        }
    }

    private static final Logger logger = LogManager.getLogger(ConsensusJob.class);
}
