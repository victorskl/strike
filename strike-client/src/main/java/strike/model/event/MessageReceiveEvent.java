package strike.model.event;

public class MessageReceiveEvent {
    private String sender;
    private String message;

    public MessageReceiveEvent(String sender, String message) {
        this.sender = sender;
        this.message = message;
    }

    public String getSender() {
        return sender;
    }

    public String getMessage() {
        return message;
    }
}
