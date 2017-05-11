package strike.service;

import strike.model.ServerInfo;

import java.io.Serializable;
import java.util.Comparator;

/**
 *
 */
public class ServerPriorityComparator implements Comparator<ServerInfo>, Serializable {

    @Override
    public int compare(ServerInfo server1, ServerInfo server2) {
        if (null != server1 && null != server2) {
            Integer server1Id = Integer.parseInt(server1.getServerId());
            Integer server2Id = Integer.parseInt(server2.getServerId());
            return server1Id - server2Id;
        } else {
            return 0;
        }
    }
}
