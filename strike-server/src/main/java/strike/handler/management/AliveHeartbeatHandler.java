package strike.handler.management;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import strike.common.model.Protocol;
import strike.handler.IProtocolHandler;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class AliveHeartbeatHandler extends ManagementHandler implements IProtocolHandler {

    private SimpleDateFormat simpleDateFormat;

    public AliveHeartbeatHandler(JSONObject jsonMessage, Runnable connection) {
        super(jsonMessage, connection);
        simpleDateFormat = new SimpleDateFormat("d MMM yyyy HH:mm:ss");
    }

    @Override
    public void handle() {
        String serverId = (String) jsonMessage.get(Protocol.serverid.toString());
        Date date = Calendar.getInstance().getTime();
        serverState.getAliveMap().put(serverId, date);
        logger.trace("Update server [" + serverId + "] last seen on " + simpleDateFormat.format(date));
    }

    private static final Logger logger = LogManager.getLogger(AliveHeartbeatHandler.class);
}
