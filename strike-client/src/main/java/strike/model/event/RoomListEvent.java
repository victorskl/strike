package strike.model.event;

import java.util.HashSet;
import java.util.Set;

public class RoomListEvent {
    private Set<String> rooms;

    public RoomListEvent(Set<String> rooms) {
        this.rooms = rooms;
    }

    public Set<String> getRooms() {
        return new HashSet<>(rooms); // dereference it
    }
}
