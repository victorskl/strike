package strike.handler.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import strike.handler.IProtocolHandler;
import strike.model.Protocol;
import strike.model.UserInfo;

public class MoveJoinProtocolHandler extends CommonHandler implements IProtocolHandler {

    public MoveJoinProtocolHandler(JSONObject jsonMessage, Runnable connection) {
        super(jsonMessage, connection);
    }

    @Override
    public void handle() {
        // {"type" : "movejoin", "former" : "MainHall-s1", "roomid" : "jokes", "identity" : "Maria"}
        String joiningRoomId = (String) jsonMessage.get(Protocol.roomid.toString());
        String former = (String) jsonMessage.get(Protocol.former.toString());
        String identity = (String) jsonMessage.get(Protocol.identity.toString());
        boolean roomExistedLocally = serverState.isRoomExistedLocally(joiningRoomId);

        userInfo = new UserInfo();
        userInfo.setIdentity(identity);
        userInfo.setManagingThread(clientConnection);
        userInfo.setSocket(clientSocket);

        clientConnection.setUserInfo(userInfo);

        String roomId;
        if (roomExistedLocally) {
            roomId = joiningRoomId;
        } else {
            // room has gone, place in MainHall
            roomId = mainHall;
        }
        userInfo.setCurrentChatRoom(roomId);
        serverState.getConnectedClients().put(identity, userInfo);
        serverState.getLocalChatRooms().get(roomId).addMember(identity);

        logger.info("Client connected: " + identity);

        write(messageBuilder.serverChange("true", serverInfo.getServerId()));
        broadcastMessageToRoom(messageBuilder.roomChange(former, roomId, userInfo.getIdentity()), roomId);
    }

    private static final Logger logger = LogManager.getLogger(MoveJoinProtocolHandler.class);
}
