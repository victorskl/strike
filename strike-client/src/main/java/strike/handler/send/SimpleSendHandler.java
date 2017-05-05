package strike.handler.send;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import strike.handler.CommonHandler;
import strike.handler.IProtocolHandler;
import strike.model.Message;

public class SimpleSendHandler extends CommonHandler implements IProtocolHandler {

    public SimpleSendHandler(JSONObject jsonMessage) {
        super(jsonMessage);
    }

    @Override
    public void handle() {
        messageQueue.add(new Message(false, jsonMessage.toJSONString()));
    }

    private static final Logger logger = LogManager.getLogger(SimpleSendHandler.class);
}
