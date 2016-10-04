package strike.handler.client;

import org.json.simple.JSONObject;
import strike.handler.IProtocolHandler;

public class AbruptExitHandler extends CommonHandler implements IProtocolHandler {

    public AbruptExitHandler(JSONObject jsonMessage, Runnable connection) {
        super(jsonMessage, connection);
    }

    @Override
    public void handle() {
        if (userInfo != null) {
            doGracefulQuit();
            if (!clientConnection.isRouted()) {
                String former = userInfo.getCurrentChatRoom();
                broadcastMessageToRoom(messageBuilder.roomChange(former, "", userInfo.getIdentity()), former, userInfo.getIdentity());
            }
        }
    }
}
