package strike.handler.management;

import org.json.simple.JSONObject;
import strike.handler.IProtocolHandler;
import strike.model.Message;
import strike.model.Protocol;

public class LockIdentityProtocolHandler extends ManagementHandler implements IProtocolHandler {

    public LockIdentityProtocolHandler(JSONObject jsonMessage, Runnable connection) {
        super(jsonMessage, connection);
    }

    @Override
    public void handle() {
        // {"type" : "lockidentity", "serverid" : "s1", "identity" : "Adel"}
        String requestUserId = (String) jsonMessage.get(Protocol.identity.toString());
        String serverId = (String) jsonMessage.get(Protocol.serverid.toString());
        String lok = serverId.concat(requestUserId);

        boolean isUserExisted = serverState.isUserExisted(requestUserId);
        boolean isUserLocked = serverState.isIdentityLocked(lok);

        if (isUserExisted || isUserLocked) {
            messageQueue.add(new Message(false, messageBuilder.lockIdentity(serverId, requestUserId, "false")));
        } else {
            serverState.lockIdentity(lok);
            messageQueue.add(new Message(false, messageBuilder.lockIdentity(serverId, requestUserId, "true")));
        }
    }
}
