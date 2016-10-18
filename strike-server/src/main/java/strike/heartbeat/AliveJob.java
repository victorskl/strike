package strike.heartbeat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import strike.service.JSONMessageBuilder;
import strike.service.PeerClient;

public class AliveJob implements Job {

    private PeerClient peerClient = new PeerClient();
    private JSONMessageBuilder messageBuilder = JSONMessageBuilder.getInstance();

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

        peerClient.relayPeers(messageBuilder.aliveMessage());

    }

    private static final Logger logger = LogManager.getLogger(AliveJob.class);
}
