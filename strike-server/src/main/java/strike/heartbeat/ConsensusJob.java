package strike.heartbeat;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import strike.service.JSONMessageBuilder;
import strike.service.PeerClient;
import strike.service.ServerState;


public class ConsensusJob implements Job {
    private final ServerState serverState = ServerState.getInstance();
    private final JSONMessageBuilder jsonMessageBuilder = JSONMessageBuilder.getInstance();
    private final PeerClient peerClient = new PeerClient();
    private String suspectServerId;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        //initialise voteset
        serverState.getVoteSet().put("yes", 0);
        serverState.getVoteSet().put("no", 0);

        //if I am leader, and suspect someone, I want to start voting to KICK him!
        if (serverState.getCoordinator() != null) {
            if (serverState.getCoordinator().getServerId() == serverState.getServerInfo().getServerId()) {
                String leaderServerId = serverState.getCoordinator().getServerId();
                String myServerId = serverState.getServerInfo().getServerId();
                //find the next suspect to vote and break the loop
                for (String serverId : serverState.getSuspectList().keySet()) {
                    if (serverState.getSuspectList().get(serverId) == 1) {
                        suspectServerId = serverId;

                        //build JSON message i.e. vote to kick server 2
                        // {"type":"vote","serverId":"2"}
                        String startVoteMessage = jsonMessageBuilder.startVoteMessage(myServerId, suspectServerId);
                        System.out.println("this is leader, lets vote!: " + startVoteMessage);
                        peerClient.relayPeers(startVoteMessage);
                        break;
                    }
                }
            }


            JobDataMap dataMap = context.getJobDetail().getJobDataMap();
            String consensusVoteDuration = dataMap.get("consensusVoteDuration").toString();


            //w8 for consensus.vote.duration determined in system.properties,
            try {
                //System.out.println("sleeping");
                Thread.sleep(Integer.parseInt(consensusVoteDuration) * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


            //remove server or do nothing
            if (serverState.getVoteSet().size() > 0) {
                System.out.println("checking the vote set: " + serverState.getVoteSet());

                if (serverState.getVoteSet().get("yes") > serverState.getVoteSet().get("no")) {
                    if (suspectServerId != null) {
                        peerClient.relayPeers(jsonMessageBuilder.notifyServerDownMessage(suspectServerId));
                        serverState.removeServer(suspectServerId);
                        serverState.removeRemoteChatRoomsByServerId(suspectServerId);
                        serverState.removeRemoteUserSessionsByServerId(suspectServerId);
                        serverState.removeServerInCountList(suspectServerId);
                        serverState.removeServerInSuspectList(suspectServerId);
                        System.out.println("!!!Number of servers: " + serverState.getServerInfoList().size());
                    }
                }
            }


        }


    }
}
