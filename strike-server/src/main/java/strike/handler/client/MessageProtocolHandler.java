package strike.handler.client;

import org.json.simple.JSONObject;
import strike.handler.IProtocolHandler;
import strike.common.model.Protocol;

public class MessageProtocolHandler extends CommonHandler implements IProtocolHandler {

    public MessageProtocolHandler(JSONObject jsonMessage, Runnable connection) {
        super(jsonMessage, connection);
    }

    @Override
    public void handle() {
        // {"type" : "message", "content" : "Hi there!"}
        String content = (String) jsonMessage.get(Protocol.content.toString());
        broadcastMessageToRoom(messageBuilder.message(userInfo.getIdentity(), content), userInfo.getCurrentChatRoom());
    }
}
