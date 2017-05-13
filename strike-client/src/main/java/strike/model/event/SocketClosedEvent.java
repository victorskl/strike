package strike.model.event;

public class SocketClosedEvent {
    private String message;

    public SocketClosedEvent(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
