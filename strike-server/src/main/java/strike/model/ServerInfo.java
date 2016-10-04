package strike.model;

import com.opencsv.bean.CsvBindByPosition;

public class ServerInfo {

    @CsvBindByPosition(position = 0)
    private String serverId;

    @CsvBindByPosition(position = 1)
    private String address;

    @CsvBindByPosition(position = 2)
    private Integer port;

    @CsvBindByPosition(position = 3)
    private Integer managementPort;

    public ServerInfo() {
    }

    public ServerInfo(String serverName, String address, Integer port, Integer managementPort) {
        this.serverId = serverName;
        this.address = address;
        this.port = port;
        this.managementPort = managementPort;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Integer getManagementPort() {
        return managementPort;
    }

    public void setManagementPort(Integer managementPort) {
        this.managementPort = managementPort;
    }
}
