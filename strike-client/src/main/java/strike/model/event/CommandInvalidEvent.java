package strike.model.event;

public class CommandInvalidEvent {
    private String message;

    public CommandInvalidEvent(String s) {
        this.message = s;
    }

    public String getMessage() {
        return message;
    }
}
