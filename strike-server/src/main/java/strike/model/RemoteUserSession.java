package strike.model;

public class RemoteUserSession extends UserSession {
    private String managingServerId;

    public String getManagingServerId() {
        return managingServerId;
    }

    public void setManagingServerId(String managingServerId) {
        this.managingServerId = managingServerId;
    }
}
