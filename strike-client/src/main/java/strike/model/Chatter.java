package strike.model;

import java.util.Objects;

public class Chatter {
    private String id;
    private boolean roomOwner;

    public Chatter(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public boolean isRoomOwner() {
        return roomOwner;
    }

    public void setRoomOwner(boolean roomOwner) {
        this.roomOwner = roomOwner;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.id);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof Chatter)) return false;
        // if (obj == this) return true;
        Chatter other = (Chatter) obj;
        return (other.id.equalsIgnoreCase(this.id));
    }

    @Override
    public String toString() {
        return this.id;
    }
}
