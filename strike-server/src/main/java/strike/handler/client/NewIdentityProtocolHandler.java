package strike.handler.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import strike.handler.IProtocolHandler;
import strike.model.Message;
import strike.model.Protocol;
import strike.model.UserInfo;

public class NewIdentityProtocolHandler extends CommonHandler implements IProtocolHandler {

    public NewIdentityProtocolHandler(JSONObject jsonMessage, Runnable connection) {
        super(jsonMessage, connection);
    }

    @Override
    public void handle() {

        String requestIdentity = (String) jsonMessage.get(Protocol.identity.toString());

        boolean isUserExisted = serverState.isUserExisted(requestIdentity);
        boolean isUserIdValid = serverState.isIdValid(requestIdentity);

        if (isUserExisted || !isUserIdValid) {
            // {"type" : "newidentity", "approved" : "false"}
            messageQueue.add(new Message(false, messageBuilder.newIdentityResp("false")));
        } else {

            boolean canLock = peerClient.canPeersLockId(messageBuilder.lockIdentity(requestIdentity));

            if (canLock) {
                userInfo = new UserInfo();
                userInfo.setIdentity(requestIdentity);
                userInfo.setCurrentChatRoom(mainHall);
                userInfo.setManagingThread(clientConnection);
                userInfo.setSocket(clientSocket);

                clientConnection.setUserInfo(userInfo);

                serverState.getConnectedClients().put(requestIdentity, userInfo);
                serverState.getLocalChatRooms().get(mainHall).addMember(requestIdentity);

                logger.info("Client connected: " + requestIdentity);

                //{"type" : "newidentity", "approved" : "true"}
                messageQueue.add(new Message(false, messageBuilder.newIdentityResp("true")));

                //{"type" : "roomchange", "identity" : "Adel", "former" : "", "roomid" : "MainHall-s1"}
                broadcastMessageToRoom(messageBuilder.roomChange("", mainHall, userInfo.getIdentity()), mainHall);
            } else {
                messageQueue.add(new Message(false, messageBuilder.newIdentityResp("false")));
            }

            // release identity on peers
            peerClient.relayPeers(messageBuilder.releaseIdentity(requestIdentity));
        }
    }

    private static final Logger logger = LogManager.getLogger(NewIdentityProtocolHandler.class);
}
