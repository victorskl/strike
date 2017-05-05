package strike.model.event;

public class RoomChangeEvent {
    private String from;
    private String to;

    public RoomChangeEvent(String from, String to) {
        this.from = from;
        this.to = to;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }
}
