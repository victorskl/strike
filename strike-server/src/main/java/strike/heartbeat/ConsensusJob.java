package strike.heartbeat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.*;
import strike.model.Lingo;
import strike.service.JSONMessageBuilder;
import strike.service.PeerClient;
import strike.service.ServerState;

public class ConsensusJob implements Job {

    private ServerState serverState = ServerState.getInstance();
    private JSONMessageBuilder jsonMessageBuilder = JSONMessageBuilder.getInstance();
    private PeerClient peerClient = new PeerClient();

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

        if (!serverState.onGoingConsensus().get()) {
            // This is a leader based Consensus.
            // If no leader elected at the moment then no consensus task to perform.
            if (serverState.getCoordinator() != null) {
                serverState.onGoingConsensus().set(true);
                _performConsensus(context); // critical region
                serverState.onGoingConsensus().set(false);
            }
        } else {
            logger.debug("[SKIP] There seems to be on going consensus at the moment, skip.");
        }
    }

    private void _performConsensus(JobExecutionContext context) {

        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        String consensusVoteDuration = dataMap.get("consensusVoteDuration").toString();

        String suspectServerId = null;

        // initialise vote set
        serverState.getVoteSet().put(Lingo.Consensus.YES, 0);
        serverState.getVoteSet().put(Lingo.Consensus.NO, 0);

        String leaderServerId = serverState.getCoordinator().getServerId();
        String myServerId = serverState.getServerInfo().getServerId();

        // if I am leader, and suspect someone, I want to start voting to KICK him!
        if (leaderServerId.equalsIgnoreCase(myServerId)) {

            // find the next suspect to vote and break the loop
            for (String serverId : serverState.getSuspectList().keySet()) {
                if (serverState.getSuspectList().get(serverId) == Lingo.Gossip.SUSPECTED) {
                    suspectServerId = serverId;
                    break;
                }
            }

            // got a suspect
            if (suspectServerId != null) {

                serverState.getVoteSet().put(Lingo.Consensus.YES, 1); // I suspect it already, so I vote yes.

                String startVoteMessage = jsonMessageBuilder.startVoteMessage(myServerId, suspectServerId);
                peerClient.relayPeers(startVoteMessage);
                logger.debug("Leader calling for vote to kick suspect-server: " + startVoteMessage);

                // w8 for consensus.vote.duration determined in system.properties
                try {
                    //TimeUnit.SECONDS.sleep(Integer.parseInt(consensusVoteDuration));
                    Thread.sleep(Integer.parseInt(consensusVoteDuration) * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // remove server or do nothing

                logger.debug(String.format("Consensus votes to kick server [%s]: %s", suspectServerId, serverState.getVoteSet()));

                if (serverState.getVoteSet().get(Lingo.Consensus.YES) > serverState.getVoteSet().get(Lingo.Consensus.NO)) {

                    peerClient.relayPeers(jsonMessageBuilder.notifyServerDownMessage(suspectServerId));
                    logger.info("Notify server " + suspectServerId + " down. Removing...");

                    serverState.removeServer(suspectServerId);
                    serverState.removeRemoteChatRoomsByServerId(suspectServerId);
                    serverState.removeRemoteUserSessionsByServerId(suspectServerId);
                    serverState.removeServerInCountList(suspectServerId);
                    serverState.removeServerInSuspectList(suspectServerId);
                }

                logger.debug("Number of servers in group: " + serverState.getServerInfoList().size());
            }
        }
    }

    private static final Logger logger = LogManager.getLogger(ConsensusJob.class);
}
