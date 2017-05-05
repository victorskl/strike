package strike.model;

public class Message {

    private boolean isFromClient;
    private String message;

    /**
     * This flag control incoming message or outgoing message.
     * True if incoming message (receiving),
     * otherwise outgoing message (sending).
     * If you want to create outgoing message:
     *      new Message(false, message)
     * TODO: rather change it to enum message type rather than boolean
     * @return
     */
    public Message(boolean isFromClient, String message) {
        this.isFromClient = isFromClient;
        this.message = message;
    }

    /**
     * This flag control incoming message or outgoing message.
     * True if incoming message (receiving),
     * otherwise outgoing message (sending).
     * If you want to create outgoing message:
     *      new Message(false, message)
     * TODO: rather change it to enum message type rather than boolean
     * @return
     */
    public boolean isFromClient() {
        return isFromClient;
    }

    public String getMessage() {
        return message;
    }
}
