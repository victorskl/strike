package strike.model.event;

import strike.model.Chatter;

public class OwnerLeaveRoomEvent {
    private Chatter chatter;
    public OwnerLeaveRoomEvent(Chatter chatter) {
        this.chatter = chatter;
    }

    public Chatter getChatter() {
        return chatter;
    }
}
