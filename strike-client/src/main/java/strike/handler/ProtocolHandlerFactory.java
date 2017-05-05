package strike.handler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import strike.common.model.Protocol;
import strike.handler.receive.*;
import strike.handler.send.ChatMessageSendHandler;
import strike.handler.send.SimpleSendHandler;

import java.util.stream.Stream;

public class ProtocolHandlerFactory {

    private static String[] SIMPLE_SEND_REGISTRY = {
            Protocol.authenticate.toString(),
            Protocol.newidentity.toString(),
            Protocol.who.toString()
    };

    public static IProtocolHandler newSendHandler(JSONObject jsonMessage) {

        String type = (String) jsonMessage.get(Protocol.type.toString());

        logger.trace("newSendHandler: " + type);

        if (Stream.of(SIMPLE_SEND_REGISTRY).anyMatch(s -> s.equalsIgnoreCase(type))) {
            return new SimpleSendHandler(jsonMessage);
        }

        if (type.equalsIgnoreCase(Protocol.message.toString())) {
            return new ChatMessageSendHandler(jsonMessage);
        }

        return new BlackHoleHandler();
    }

    public static IProtocolHandler newReceiveHandler(JSONObject jsonMessage) {

        String type = (String) jsonMessage.get(Protocol.type.toString());

        logger.trace("newReceiveHandler: " + type);

        if (type.equalsIgnoreCase(Protocol.authresponse.toString())) {
            return new AuthResponseReceiveHandler(jsonMessage);
        }

        if (type.equalsIgnoreCase(Protocol.newidentity.toString())) {
            return new NewIdentityReceiveHandler(jsonMessage);
        }

        if (type.equalsIgnoreCase(Protocol.roomlist.toString())) {
            return new RoomListReceiveHandler(jsonMessage);
        }

        if (type.equalsIgnoreCase(Protocol.roomchange.toString())) {
            return new RoomChangeReceiveHandler(jsonMessage);
        }

        if (type.equalsIgnoreCase(Protocol.roomcontents.toString())) {
            return new RoomContentsReceiveHandler(jsonMessage);
        }

        if (type.equalsIgnoreCase(Protocol.message.toString())) {
            return new MessageReceiveHandler(jsonMessage);
        }

        if (type.equalsIgnoreCase(Protocol.createroom.toString())) {
            return new CreateRoomReceiveHandler(jsonMessage);
        }

        if (type.equalsIgnoreCase(Protocol.deleteroom.toString())) {
            return new DeleteRoomReceiveHandler(jsonMessage);
        }

        if (type.equalsIgnoreCase(Protocol.route.toString())) {
            return new RouteReceiveHandler(jsonMessage);
        }

        return new BlackHoleHandler();
    }

    private static final Logger logger = LogManager.getLogger(ProtocolHandlerFactory.class);
}
