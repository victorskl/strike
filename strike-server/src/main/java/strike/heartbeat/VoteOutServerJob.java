package strike.heartbeat;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import strike.service.ServerState;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

public class VoteOutServerJob implements Job {

    private ServerState serverState = ServerState.getInstance();

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {

        for (Map.Entry entry : serverState.getAliveMap().entrySet()) {
            String serverId = (String) entry.getKey();
            Date lastSeen = (Date) entry.getValue();
            long diff = (Calendar.getInstance().getTime().getTime() - lastSeen.getTime()) / DateUtils.MILLIS_PER_SECOND;
            System.out.println("serverId: " + serverId + " diff: " + diff);
        }
    }

    private static final Logger logger = LogManager.getLogger(VoteOutServerJob.class);
}
