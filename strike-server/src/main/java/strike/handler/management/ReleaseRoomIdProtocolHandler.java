package strike.handler.management;

import org.json.simple.JSONObject;
import strike.handler.IProtocolHandler;
import strike.model.Message;
import strike.model.Protocol;
import strike.model.RemoteChatRoomInfo;

public class ReleaseRoomIdProtocolHandler extends ManagementHandler implements IProtocolHandler {

    public ReleaseRoomIdProtocolHandler(JSONObject jsonMessage, Runnable connection) {
        super(jsonMessage, connection);
    }

    @Override
    public void handle() {
        // "type" : "releaseroomid", "serverid" : "s1", "roomid" : "jokes", "approved":"true"}
        String requestRoomId = (String) jsonMessage.get(Protocol.roomid.toString());
        String serverId = (String) jsonMessage.get(Protocol.serverid.toString());
        String approved = (String) jsonMessage.get(Protocol.approved.toString());

        if (approved.equalsIgnoreCase("true")) {
            // Servers receiving a releaseroomid message with "approved" : "true" must
            // then release the lock and
            // record it as a new chat room with id "jokes" that was created in server s1.
            serverState.unlockRoomIdentity(requestRoomId);

            RemoteChatRoomInfo remoteChatRoomInfo = new RemoteChatRoomInfo();
            remoteChatRoomInfo.setChatRoomId(requestRoomId);
            remoteChatRoomInfo.setManagingServer(serverId);
            serverState.getRemoteChatRooms().put(requestRoomId, remoteChatRoomInfo);
        } else {
            // do nothing
        }

        messageQueue.add(new Message(false, "exit"));
    }
}
