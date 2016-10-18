package strike.handler.client;

import com.google.common.base.Strings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.json.simple.JSONObject;
import strike.common.model.Protocol;
import strike.handler.IProtocolHandler;
import strike.model.RemoteUserSession;
import strike.model.UserInfo;
import strike.model.UserSession;

public class MoveJoinProtocolHandler extends CommonHandler implements IProtocolHandler {

    public MoveJoinProtocolHandler(JSONObject jsonMessage, Runnable connection) {
        super(jsonMessage, connection);
    }

    @Override
    public void handle() {

        String transitUsername = (String) jsonMessage.get(Protocol.username.toString());
        String transitSessionId = (String) jsonMessage.get(Protocol.sessionid.toString());
        String transitPassword = (String) jsonMessage.get(Protocol.password.toString());
        if (!Strings.isNullOrEmpty(transitUsername) && !Strings.isNullOrEmpty(transitSessionId)) {
            RemoteUserSession remoteUserSession = serverState.getRemoteUserSessions().get(transitSessionId);
            if (remoteUserSession != null) {

                try {
                    Subject currentUser = clientConnection.getCurrentUser();
                    UsernamePasswordToken token = new UsernamePasswordToken(transitUsername, transitPassword);

                    currentUser.login(token);

                    String username = (String) currentUser.getPrincipal();
                    String sessionId = (String) currentUser.getSession().getId();

                    UserSession userSession = new UserSession();
                    userSession.setUsername(username);
                    userSession.setSessionId(sessionId);
                    userSession.setStatus("login");
                    userSession.setPassword(transitPassword);
                    serverState.getLocalUserSessions().put(sessionId, userSession);

                    logger.info("Added user session to the local user session list: " + username + " [" + sessionId + "]");

                    serverState.getRemoteUserSessions().remove(transitSessionId);

                    // let peers know my new local user session
                    peerClient.relayPeers(messageBuilder.notifyUserSession(username, sessionId, "login"));

                } catch (AuthenticationException aex) {

                    write(messageBuilder.serverChange("false", serverInfo.getServerId()));
                    //messageQueue.add(new Message(false, messageBuilder.authResponse("false", "AuthenticationException")));
                    logger.warn("MoveJoinProtocolHandler: an attempt to transparent transit authentication has failed.");
                    return;
                }
            }
        }

        if (clientConnection.getCurrentUser().isAuthenticated()) {
            doNormalMoveJoin();
        } else {
            write(messageBuilder.serverChange("false", serverInfo.getServerId()));
        }
    }

    private void doNormalMoveJoin() {
        // {"type" : "movejoin", "former" : "MainHall-s1", "roomid" : "jokes", "identity" : "Maria"}
        String joiningRoomId = (String) jsonMessage.get(Protocol.roomid.toString());
        String former = (String) jsonMessage.get(Protocol.former.toString());
        String identity = (String) jsonMessage.get(Protocol.identity.toString());
        boolean roomExistedLocally = serverState.isRoomExistedLocally(joiningRoomId);

        userInfo = new UserInfo();
        userInfo.setIdentity(identity);
        userInfo.setManagingThread(clientConnection);
        userInfo.setSocket(clientSocket);

        clientConnection.setUserInfo(userInfo);

        String roomId;
        if (roomExistedLocally) {
            roomId = joiningRoomId;
        } else {
            // room has gone, place in MainHall
            roomId = mainHall;
        }
        userInfo.setCurrentChatRoom(roomId);
        serverState.getConnectedClients().put(identity, userInfo);
        serverState.getLocalChatRooms().get(roomId).addMember(identity);

        logger.info("Client connected: " + identity);

        write(messageBuilder.serverChange("true", serverInfo.getServerId()));
        broadcastMessageToRoom(messageBuilder.roomChange(former, roomId, userInfo.getIdentity()), roomId);
    }

    private static final Logger logger = LogManager.getLogger(MoveJoinProtocolHandler.class);
}
