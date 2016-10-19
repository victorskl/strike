package strike.heartbeat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import strike.model.ServerInfo;
import strike.service.JSONMessageBuilder;
import strike.service.PeerClient;
import strike.service.ServerState;

import java.util.Calendar;

public class AliveJob implements Job {

    private PeerClient peerClient = new PeerClient();
    private JSONMessageBuilder messageBuilder = JSONMessageBuilder.getInstance();
    private ServerState serverState = ServerState.getInstance();

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

        //peerClient.relayPeers(messageBuilder.aliveMessage());

        //System.out.println("alive...");

        for (ServerInfo serverInfo : serverState.getServerInfoList()) {
            String serverId = serverInfo.getServerId();
            boolean online = serverState.isOnline(serverInfo);
            System.out.println(serverId + " is online " + online + " " + Calendar.getInstance().getTime());
        }

    }

    private static final Logger logger = LogManager.getLogger(AliveJob.class);
}
