package strike.handler.receive;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import strike.common.model.Protocol;
import strike.handler.CommonHandler;
import strike.handler.IProtocolHandler;
import strike.model.Chatter;
import strike.model.event.RoomContentsEvent;

import java.util.HashSet;

public class RoomContentsReceiveHandler extends CommonHandler implements IProtocolHandler {

    public RoomContentsReceiveHandler(JSONObject jsonMessage) {
        super(jsonMessage);
    }

    @Override
    public void handle() {
        // server reply of #who

        HashSet<Chatter> clients = new HashSet<>();

        String roomOwner = (String) jsonMessage.get(Protocol.owner.toString());
        JSONArray identities = (JSONArray) jsonMessage.get(Protocol.identities.toString());

        for (Object object : identities) {
            String identity = (String) object;
            Chatter chatter = new Chatter(identity);
            if (identity.equalsIgnoreCase(roomOwner))
                chatter.setRoomOwner(true);
            clients.add(chatter);
        }

        eventBus.post(new RoomContentsEvent(clients));
    }

    private static final Logger logger = LogManager.getLogger(RoomContentsReceiveHandler.class);
}
