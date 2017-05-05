package strike.handler.receive;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import strike.common.model.Protocol;
import strike.handler.CommonHandler;
import strike.handler.IProtocolHandler;
import strike.model.event.MessageReceiveEvent;

public class MessageReceiveHandler extends CommonHandler implements IProtocolHandler {

    public MessageReceiveHandler(JSONObject jsonMessage) {
        super(jsonMessage);
    }

    @Override
    public void handle() {
        // server forwards message

        String identity = (String) jsonMessage.get(Protocol.identity.toString());
        String content = (String) jsonMessage.get(Protocol.content.toString());

        logger.trace(identity + ": " + content);

        eventBus.post(new MessageReceiveEvent(identity, content));
    }

    private static final Logger logger = LogManager.getLogger(MessageReceiveHandler.class);
}
