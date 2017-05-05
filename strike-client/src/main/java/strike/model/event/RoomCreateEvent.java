package strike.model.event;

public class RoomCreateEvent {
    private boolean approved;
    private String roomId;

    public RoomCreateEvent(boolean approved, String roomId) {
        this.approved = approved;
        this.roomId = roomId;
    }

    public boolean isApproved() {
        return approved;
    }

    public String getRoomId() {
        return roomId;
    }
}
