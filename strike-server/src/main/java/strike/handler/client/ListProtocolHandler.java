package strike.handler.client;

import org.json.simple.JSONObject;
import strike.handler.IProtocolHandler;
import strike.model.Message;

public class ListProtocolHandler extends CommonHandler implements IProtocolHandler {

    public ListProtocolHandler(JSONObject jsonMessage, Runnable connection) {
        super(jsonMessage, connection);
    }

    @Override
    public void handle() {
        messageQueue.add(new Message(false, messageBuilder.listRooms()));
    }
}
