package strike.service;

public class ClientState {

    private static ClientState instance = null;

    public static synchronized ClientState getInstance() {
        if (instance == null) instance = new ClientState();
        return instance;
    }

    private String identity;
    private String roomId;

    private ClientState() {
    }

    public synchronized String getRoomId() {
        return roomId;
    }

    public synchronized void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public synchronized String getIdentity() {
        return identity;
    }

    public synchronized void setIdentity(String identity) {
        this.identity = identity;
    }
}
