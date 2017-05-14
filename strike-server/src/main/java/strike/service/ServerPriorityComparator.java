package strike.service;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Compares server priority based on the server id.
 */
public class ServerPriorityComparator implements Comparator<String>, Serializable {

    @Override
    public int compare(String server1, String server2) {
        if (null != server1 && null != server2) {
            Integer server1Id = Integer.parseInt(server1);
            Integer server2Id = Integer.parseInt(server2);
            return server1Id - server2Id;
        }
        return 0;
    }
}
