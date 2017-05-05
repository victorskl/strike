package strike.model.event;

public class RoomDeleteEvent {
    private boolean approved;
    private String roomId;

    public RoomDeleteEvent(boolean approved, String roomId) {
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
