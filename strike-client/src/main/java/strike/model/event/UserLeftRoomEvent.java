package strike.model.event;

import strike.model.Chatter;

public class UserLeftRoomEvent extends AbstractUserEvent {

    public UserLeftRoomEvent(Chatter chatter) {
        this.chatter = chatter;
    }
}
