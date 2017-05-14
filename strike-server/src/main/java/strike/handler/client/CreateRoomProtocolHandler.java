package strike.handler.client;

import org.json.simple.JSONObject;
import strike.common.Utilities;
import strike.handler.IProtocolHandler;
import strike.model.LocalChatRoomInfo;
import strike.common.model.Protocol;

public class CreateRoomProtocolHandler extends CommonHandler implements IProtocolHandler {

    public CreateRoomProtocolHandler(JSONObject jsonMessage, Runnable connection) {
        super(jsonMessage, connection);
    }

    @Override
    public void handle() {
        String requestRoomId = (String) jsonMessage.get(Protocol.roomid.toString());

        boolean isRoomExisted = serverState.isRoomExistedGlobally(requestRoomId);
        boolean hasRoomAlreadyLocked = serverState.isRoomIdLocked(requestRoomId);
        boolean isRoomIdValid = Utilities.isIdValid(requestRoomId);

        // if and only if the client is not the owner of another chat room
        if (userInfo.isRoomOwner() || hasRoomAlreadyLocked || isRoomExisted || !isRoomIdValid) {
            write(messageBuilder.createRoomResp(requestRoomId, "false"));
        } else {

            boolean canLock = peerClient.canPeersLockId(messageBuilder.lockRoom(requestRoomId));
            if (canLock) {
                // release lock
                peerClient.relayPeers(messageBuilder.releaseRoom(requestRoomId, "true"));

                // create and update room
                LocalChatRoomInfo newRoom = new LocalChatRoomInfo();
                newRoom.setChatRoomId(requestRoomId);
                newRoom.setOwner(userInfo.getIdentity());
                newRoom.addMember(userInfo.getIdentity());
                serverState.getLocalChatRooms().put(requestRoomId, newRoom);

                // update former room
                String former = userInfo.getCurrentChatRoom();
                serverState.getLocalChatRooms().get(former).removeMember(userInfo.getIdentity());

                // update this client
                userInfo.setCurrentChatRoom(requestRoomId);
                userInfo.setRoomOwner(true);

                // response client
                write(messageBuilder.createRoomResp(requestRoomId, "true"));
                write(messageBuilder.roomChange(former, userInfo.getCurrentChatRoom(), userInfo.getIdentity()));
                broadcastMessageToRoom(messageBuilder.roomChange(former, userInfo.getCurrentChatRoom(), userInfo.getIdentity()), former);

            } else {
                peerClient.relayPeers(messageBuilder.releaseRoom(requestRoomId, "false"));
                write(messageBuilder.createRoomResp(requestRoomId, "false"));
            }
        }
    }
}
