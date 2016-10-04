package strike.model;

import strike.service.ClientConnection;

import java.net.Socket;

public class UserInfo {
    private String identity;
    private String currentChatRoom;
    private Socket socket;
    private ClientConnection managingThread;
    private boolean roomOwner = false;

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public String getCurrentChatRoom() {
        return currentChatRoom;
    }

    public void setCurrentChatRoom(String currentChatRoom) {
        this.currentChatRoom = currentChatRoom;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public ClientConnection getManagingThread() {
        return managingThread;
    }

    public void setManagingThread(ClientConnection managingThread) {
        this.managingThread = managingThread;
    }

    public boolean isRoomOwner() {
        return roomOwner;
    }

    public void setRoomOwner(boolean roomOwner) {
        this.roomOwner = roomOwner;
    }
}
