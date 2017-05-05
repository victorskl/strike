package strike.model.event;

import strike.model.Chatter;

import java.util.HashSet;
import java.util.Set;

public class RoomContentsEvent {

    private Set<Chatter> clients;

    public RoomContentsEvent(Set<Chatter> clients) {
        this.clients = clients;
    }

    public Set<Chatter> getClients() {
        return new HashSet<>(clients); // dereference it
    }
}
