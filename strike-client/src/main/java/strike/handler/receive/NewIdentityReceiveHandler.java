package strike.handler.receive;

import org.json.simple.JSONObject;
import strike.common.model.Protocol;
import strike.handler.CommonHandler;
import strike.handler.IProtocolHandler;

public class NewIdentityReceiveHandler extends CommonHandler implements IProtocolHandler {

    public NewIdentityReceiveHandler(JSONObject jsonMessage) {
        super(jsonMessage);
    }

    @Override
    public void handle() {
        // {"type" : "newidentity", "approved" : "true"}
        Boolean approved = Boolean.parseBoolean((String) jsonMessage.get(Protocol.approved.toString()));

        // if not approved from server, reset it
        if (!approved) {
            clientState.setIdentity("");
            clientState.setRoomId("");
        }

        eventBus.post(approved);
    }
}
