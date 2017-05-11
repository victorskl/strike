package strike.handler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import strike.common.model.Protocol;
import strike.handler.client.*;
import strike.handler.management.*;
import strike.service.ClientConnection;
import strike.service.ManagementConnection;

public class ProtocolHandlerFactory {

    /**
     * @deprecated use newClientHandler() or newManagementHandler()
     */
    @Deprecated
    public static IProtocolHandler newHandler(JSONObject jsonMessage, Runnable connection) {

        if (connection instanceof ClientConnection) newClientHandler(jsonMessage, connection);

        if (connection instanceof ManagementConnection) newManagementHandler(jsonMessage, connection);

        return new BlackHoleHandler();
    }

    public static IProtocolHandler newClientHandler(JSONObject jsonMessage, Runnable connection) {

        if (connection instanceof ManagementConnection) new BlackHoleHandler();

        if (jsonMessage == null) {
            return new AbruptExitHandler(null, connection);
        }

        String type = (String) jsonMessage.get(Protocol.type.toString());

        //-- START Public protocols i.e before login

        if (type.equalsIgnoreCase(Protocol.authenticate.toString())) {
            return new AuthenticateProtocolHandler(jsonMessage, connection);
        }

        // Added 16/20/16 by Ray
        if (type.equalsIgnoreCase(Protocol.listserver.toString())){
            return new ListServerProtocolHandler(jsonMessage,connection);
        }

        // will check authentication inside handler itself
        if (type.equalsIgnoreCase(Protocol.movejoin.toString())) {
            return new MoveJoinProtocolHandler(jsonMessage, connection);
        }

        //-- END Public protocols i.e before login

        //-- Protocols that required to be authenticated - sweep check point

        ClientConnection clientConnection = (ClientConnection) connection;

        if (!clientConnection.getCurrentUser().isAuthenticated()) {
            return new UnauthorisedExitHandler(jsonMessage, connection);
        }

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

        if (type.equalsIgnoreCase(Protocol.message.toString())) {
            return new MessageProtocolHandler(jsonMessage, connection);
        }

        if (type.equalsIgnoreCase(Protocol.quit.toString())) {
            return new QuitProtocolHandler(jsonMessage, connection);
        }

        if (type.equalsIgnoreCase(Protocol.deleteroom.toString())) {
            return new DeleteRoomProtocolHandler(jsonMessage, connection);
        }

        return new BlackHoleHandler();
    }

    public static IProtocolHandler newManagementHandler(JSONObject jsonMessage, Runnable connection) {

        if (connection instanceof ClientConnection) return new BlackHoleHandler();

        // Management Protocols

        String type = (String) jsonMessage.get(Protocol.type.toString());

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

        if (type.equalsIgnoreCase(Protocol.deleteroom.toString())) {
            return new DeleteRoomServerProtocolHandler(jsonMessage, connection);
        }

        if (type.equalsIgnoreCase(Protocol.notifyusersession.toString())) {
            return new NotifyUserSessionHandler(jsonMessage, connection);
        }

        if (type.equalsIgnoreCase(Protocol.serverup.toString())) {
            return new ServerUpProtocolHandler(jsonMessage, connection);
        }

        if (type.equalsIgnoreCase(Protocol.notifyserverdown.toString())) {
            return new NotifyServerDownProtocolHandler(jsonMessage, connection);
        }

        if (type.equalsIgnoreCase(Protocol.startelection.toString())){
            return new StartElectionMessageHandler(jsonMessage, connection);
        }

        if (type.equalsIgnoreCase(Protocol.answerelection.toString())) {
            return new AnswerElectionMessageHandler(jsonMessage, connection);
        }
        if (type.equalsIgnoreCase(Protocol.coordinator.toString())){
            return new SetCoordinatorHandler(jsonMessage, connection);
        }

        return new BlackHoleHandler();
    }

    private static final Logger logger = LogManager.getLogger(ProtocolHandlerFactory.class);
}
