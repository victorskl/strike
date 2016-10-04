package strike.handler.client;

import org.json.simple.JSONObject;
import strike.model.LocalChatRoomInfo;
import strike.model.Message;
import strike.model.ServerInfo;
import strike.model.UserInfo;
import strike.service.ClientConnection;
import strike.service.JSONMessageBuilder;
import strike.service.PeerClient;
import strike.service.ServerState;

import java.net.Socket;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

public class CommonHandler {

    protected JSONMessageBuilder messageBuilder = JSONMessageBuilder.getInstance();
    protected ServerState serverState = ServerState.getInstance();
    protected ServerInfo serverInfo;
    protected String mainHall;
    protected PeerClient peerClient;

    protected JSONObject jsonMessage;
    protected ClientConnection clientConnection;
    protected BlockingQueue<Message> messageQueue;
    protected UserInfo userInfo;
    protected Socket clientSocket;

    public CommonHandler(JSONObject jsonMessage, Runnable connection) {
        this.jsonMessage = jsonMessage;
        this.clientConnection = (ClientConnection) connection;
        this.messageQueue = clientConnection.getMessageQueue();
        this.userInfo = clientConnection.getUserInfo();
        this.clientSocket = clientConnection.getClientSocket();

        this.serverInfo = serverState.getServerInfo();
        this.mainHall = "MainHall-" + serverInfo.getServerId();
        this.peerClient = new PeerClient();
    }

    protected void doGracefulQuit() {
        String former = userInfo.getCurrentChatRoom();

        // remove user from room
        serverState.getLocalChatRooms().get(former).removeMember(userInfo.getIdentity());

        // follow delete room protocol if owner
        if (userInfo.isRoomOwner()) {
            doDeleteRoomProtocol(serverState.getLocalChatRooms().get(former));
        }

        // remove user
        serverState.getConnectedClients().remove(userInfo.getIdentity());
    }

    protected void doDeleteRoomProtocol(LocalChatRoomInfo deletingRoom) {
        // put all users to main hall
        serverState.getLocalChatRooms().get(mainHall).getMembers().addAll(deletingRoom.getMembers());
        for (String member : deletingRoom.getMembers()) {
            UserInfo client = serverState.getConnectedClients().get(member);
            if (client.getIdentity().equalsIgnoreCase(userInfo.getIdentity())) continue;

            // TODO option#1 - work from this thread, option#2 - work on each connected client thread
            client.setCurrentChatRoom(mainHall);
            String msg = messageBuilder.roomChange(deletingRoom.getChatRoomId(), mainHall, client.getIdentity());
            broadcastMessageToRoom(msg, deletingRoom.getChatRoomId());
            broadcastMessageToRoom(msg, mainHall);
        }

        // delete the room
        serverState.getLocalChatRooms().remove(deletingRoom.getChatRoomId());

        // inform peers
        peerClient.relayPeers(messageBuilder.deleteRoomPeers(deletingRoom.getChatRoomId()));
    }

    protected void broadcastMessageToRoom(String message, String room) {
        Message msg = new Message(false, message);

        Map<String, UserInfo> connectedClients = serverState.getConnectedClients();

        connectedClients.values().stream()
                .filter(client -> client.getCurrentChatRoom().equalsIgnoreCase(room))
                .forEach(client -> {
                    client.getManagingThread().getMessageQueue().add(msg);
                });
    }

    protected void broadcastMessageToRoom(String message, String room, String exceptUserId) {
        Message msg = new Message(false, message);

        Map<String, UserInfo> connectedClients = serverState.getConnectedClients();

        connectedClients.values().stream()
                .filter(client -> client.getCurrentChatRoom().equalsIgnoreCase(room))
                .filter(client -> !client.getIdentity().equalsIgnoreCase(exceptUserId))
                .forEach(client -> {
                    client.getManagingThread().getMessageQueue().add(msg);
                });
    }

    protected void write(String msg) {
        clientConnection.write(msg);
    }
}
