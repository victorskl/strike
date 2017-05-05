package strike.model.event;

import strike.model.Chatter;

public class UserQuitEvent extends AbstractUserEvent {

    public UserQuitEvent(Chatter chatter) {
        this.chatter = chatter;
    }
}
