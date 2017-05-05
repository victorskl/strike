package strike.handler.receive;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import strike.common.model.Protocol;
import strike.handler.CommonHandler;
import strike.handler.IProtocolHandler;
import strike.model.event.RoomCreateEvent;

public class CreateRoomReceiveHandler extends CommonHandler implements IProtocolHandler {

    public CreateRoomReceiveHandler(JSONObject jsonMessage) {
        super(jsonMessage);
    }

    @Override
    public void handle() {
        // server reply of #createroom

        boolean approved = Boolean.parseBoolean((String) jsonMessage.get(Protocol.approved.toString()));

        String roomId = (String) jsonMessage.get(Protocol.roomid.toString());

        if (!approved) {
            logger.debug("Create room " + roomId + " failed.");
        }
        else {
            logger.debug("Room " + roomId + " is created.");
        }

        eventBus.post(new RoomCreateEvent(approved, roomId));

        // TODO Nothing special need to be done. Bec server will send follow up room change if approved.
    }

    private static final Logger logger = LogManager.getLogger(CreateRoomReceiveHandler.class);
}
