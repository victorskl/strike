package strike.handler.receive;

import org.json.simple.JSONObject;
import strike.common.model.Protocol;
import strike.handler.CommonHandler;
import strike.handler.IProtocolHandler;

public class AuthResponseReceiveHandler extends CommonHandler implements IProtocolHandler {

    public AuthResponseReceiveHandler(JSONObject jsonMessage) {
        super(jsonMessage);
    }

    @Override
    public void handle() {
        Boolean success = Boolean.parseBoolean((String) jsonMessage.get(Protocol.success.toString()));
        eventBus.post(success);
    }
}
