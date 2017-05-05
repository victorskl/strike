package strike.model.event;

import strike.model.Chatter;

public class UserJoinRoomEvent extends AbstractUserEvent {

    public UserJoinRoomEvent(Chatter chatter) {
        this.chatter = chatter;
    }
}
