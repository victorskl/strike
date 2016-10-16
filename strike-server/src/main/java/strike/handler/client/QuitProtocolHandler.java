package strike.handler.client;

import org.apache.shiro.subject.Subject;
import org.json.simple.JSONObject;
import strike.handler.IProtocolHandler;

public class QuitProtocolHandler extends CommonHandler implements IProtocolHandler {

    public QuitProtocolHandler(JSONObject jsonMessage, Runnable connection) {
        super(jsonMessage, connection);
    }

    @Override
    public void handle() {
        //{"type" : "roomchange", "identity" : "Adel", "former" : "MainHall-s1", "roomid" : ""}

        String former = userInfo.getCurrentChatRoom();

        doGracefulQuit();

        // update about quitting user
        if (userInfo.isRoomOwner()) {
            broadcastMessageToRoom(messageBuilder.roomChange(former, "", userInfo.getIdentity()), mainHall, userInfo.getIdentity());
        } else {
            broadcastMessageToRoom(messageBuilder.roomChange(former, "", userInfo.getIdentity()), former, userInfo.getIdentity());
        }

        write(messageBuilder.roomChange(former, "", userInfo.getIdentity()));

        clientConnection.getCurrentUser().logout();
    }
}
