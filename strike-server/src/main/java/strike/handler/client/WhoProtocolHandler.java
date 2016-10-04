package strike.handler.client;

import org.json.simple.JSONObject;
import strike.handler.IProtocolHandler;
import strike.model.Message;

public class WhoProtocolHandler extends CommonHandler implements IProtocolHandler {

    public WhoProtocolHandler(JSONObject jsonMessage, Runnable connection) {
        super(jsonMessage, connection);
    }

    @Override
    public void handle() {
        messageQueue.add(new Message(false, messageBuilder.whoByRoom(userInfo.getCurrentChatRoom())));
    }
}
