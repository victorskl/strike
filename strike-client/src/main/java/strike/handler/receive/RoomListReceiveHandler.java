package strike.handler.receive;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import strike.common.model.Protocol;
import strike.handler.CommonHandler;
import strike.handler.IProtocolHandler;
import strike.model.event.RoomListEvent;

import java.util.HashSet;

public class RoomListReceiveHandler extends CommonHandler implements IProtocolHandler {

    public RoomListReceiveHandler(JSONObject jsonMessage) {
        super(jsonMessage);
    }

    @Override
    public void handle() {
        // server reply of #list

        JSONArray rooms = (JSONArray) jsonMessage.get(Protocol.rooms.toString());

        HashSet<String> roomSet = new HashSet<>();

        // print all the rooms

        for (Object anArray : rooms) {
            roomSet.add((String) anArray);
        }

        eventBus.post(new RoomListEvent(roomSet));
    }
}
