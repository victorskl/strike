package strike.handler;

import org.json.simple.JSONObject;
import strike.handler.client.*;
import strike.handler.management.*;
import strike.model.Protocol;

public class ProtocolHandlerFactory {

    public static IProtocolHandler newHandler(JSONObject jsonMessage, Runnable connection) {

        if (jsonMessage == null) {
            return new AbruptExitHandler(jsonMessage, connection);
        }

        String type = (String) jsonMessage.get(Protocol.type.toString());

        if (type.equalsIgnoreCase(Protocol.newidentity.toString())) {
            return new NewIdentityProtocolHandler(jsonMessage, connection);
        }

        if (type.equalsIgnoreCase(Protocol.list.toString())) {
            return new ListProtocolHandler(jsonMessage, connection);
        }

        if (type.equalsIgnoreCase(Protocol.who.toString())) {
            return new WhoProtocolHandler(jsonMessage, connection);
        }

        if (type.equalsIgnoreCase(Protocol.createroom.toString())) {
            return new CreateRoomProtocolHandler(jsonMessage, connection);
        }

        if (type.equalsIgnoreCase(Protocol.join.toString())) {
            return new JoinProtocolHandler(jsonMessage, connection);
        }

        if (type.equalsIgnoreCase(Protocol.movejoin.toString())) {
            return new MoveJoinProtocolHandler(jsonMessage, connection);
        }

        if (type.equalsIgnoreCase(Protocol.message.toString())) {
            return new MessageProtocolHandler(jsonMessage, connection);
        }

        if (type.equalsIgnoreCase(Protocol.quit.toString())) {
            return new QuitProtocolHandler(jsonMessage, connection);
        }

        // Resolve Protocol type clashes

        if (type.equalsIgnoreCase(Protocol.deleteroom.toString())) {
            return resolveDeleteRoomProtocol(jsonMessage, connection);
        }

        // Management Protocols

        // acquire lock for user id
        if (type.equalsIgnoreCase(Protocol.lockidentity.toString())) {
            return new LockIdentityProtocolHandler(jsonMessage, connection);
        }

        // release lock for user id
        if (type.equalsIgnoreCase(Protocol.releaseidentity.toString())) {
            return new ReleaseIdentityProtocolHandler(jsonMessage, connection);
        }

        // acquire vote for locking room id
        if (type.equalsIgnoreCase(Protocol.lockroomid.toString())) {
            return new LockRoomIdProtocolHandler(jsonMessage, connection);
        }

        // release lock for room id
        if (type.equalsIgnoreCase(Protocol.releaseroomid.toString())) {
            return new ReleaseRoomIdProtocolHandler(jsonMessage, connection);
        }

        return null;
    }

    private static IProtocolHandler resolveDeleteRoomProtocol(JSONObject jsonMessage, Runnable connection) {
        String serverId = (String) jsonMessage.get(Protocol.serverid.toString());

        if (serverId == null) {
            return new DeleteRoomProtocolHandler(jsonMessage, connection);
        }

        // delete room
        return new DeleteRoomServerProtocolHandler(jsonMessage, connection);
    }
}
