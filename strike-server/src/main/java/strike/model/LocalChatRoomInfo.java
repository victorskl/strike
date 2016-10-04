package strike.model;

import java.util.ArrayList;
import java.util.List;

public class LocalChatRoomInfo extends ChatRoomInfo {
    private String owner;
    private List<String> members = new ArrayList<>();

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public synchronized void addMember(String identity) {
        members.add(identity);
    }

    public synchronized void removeMember(String identity) {
        members.remove(identity);
    }

    public synchronized List<String> getMembers() {
        return members;
    }
}
