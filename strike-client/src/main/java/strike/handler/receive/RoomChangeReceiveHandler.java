package strike.handler.receive;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import strike.common.model.Protocol;
import strike.handler.CommonHandler;
import strike.handler.IProtocolHandler;
import strike.model.Chatter;
import strike.model.event.*;

public class RoomChangeReceiveHandler extends CommonHandler implements IProtocolHandler {

    public RoomChangeReceiveHandler(JSONObject jsonMessage) {
        super(jsonMessage);
    }

    @Override
    public void handle() {
        // {"identity":"vic","type":"roomchange","former":"","roomid":"MainHall-s1"}

        String identity = (String) jsonMessage.get(Protocol.identity.toString());
        String former = (String) jsonMessage.get(Protocol.former.toString());
        String roomId = (String) jsonMessage.get(Protocol.roomid.toString());

        Chatter chatter = new Chatter(identity);

        // server sends roomchange

        // identify whether the user has quit!
        if (roomId.equals("")) {

            // quit initiated by the current client
            logger.info(identity + " has quit!");
            eventBus.post(new UserQuitEvent(chatter));

            // this is to say that an identity always should has attached to chat room
            // otherwise consider user has quit.
        }

        // identify whether the client is new or not
        else if (former.equals("")) {

            // change state if it's the current client
            if (identity.equals(clientState.getIdentity())) {
                String from = clientState.getRoomId();
                eventBus.post(new RoomChangeEvent(from, roomId));
                clientState.setRoomId(roomId);
            } else {
                eventBus.post(new UserJoinRoomEvent(chatter));
            }

            logger.debug(identity + " moves to " + roomId);
        }

        // identify whether roomchange actually happens
        else if (former.equals(roomId)) {
            logger.info("Room unchanged");
            eventBus.post(new OwnerLeaveRoomEvent(chatter));
        }

        // print the normal roomchange message
        else {

            // change state if it's the current client
            if (identity.equals(clientState.getIdentity())) {

                String from = clientState.getRoomId();
                eventBus.post(new RoomChangeEvent(from, roomId));
                clientState.setRoomId(roomId);

            } else {

                // The user is leaving.
                if (former.equalsIgnoreCase(clientState.getRoomId())) {
                    eventBus.post(new UserLeftRoomEvent(chatter));
                } else {
                    eventBus.post(new UserJoinRoomEvent(chatter));
                }
            }

            logger.debug(identity + " moves from " + former + " to " + roomId);
        }
    }

    private static final Logger logger = LogManager.getLogger(RoomChangeReceiveHandler.class);
}
