package strike.handler.management;

import org.json.simple.JSONObject;
import strike.handler.IProtocolHandler;
import strike.model.Message;
import strike.common.model.Protocol;

public class DeleteRoomServerProtocolHandler extends ManagementHandler implements IProtocolHandler {

    public DeleteRoomServerProtocolHandler(JSONObject jsonMessage, Runnable connection) {
        super(jsonMessage, connection);
    }

    @Override
    public void handle() {
        //{"type" : "deleteroom", "serverid" : "s1", "roomid" : "jokes"}
        String deletingRoomId = (String) jsonMessage.get(Protocol.roomid.toString());
        serverState.getRemoteChatRooms().remove(deletingRoomId);
        messageQueue.add(new Message(false, "exit"));
    }
}
