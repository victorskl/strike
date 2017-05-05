package strike.model.event;

import strike.model.Chatter;

public abstract class AbstractUserEvent {
    Chatter chatter;

    public Chatter getChatter() {
        return chatter;
    }
}
