package strike;

import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.ThreadLocalRandom;

public class GossipTest {

    @Test @Ignore
    public void testRandom() {
        int numOfServers = 3;
        int serverIndex = ThreadLocalRandom.current().nextInt(numOfServers - 1);
        System.out.println(serverIndex);
        // assertEquals(1, serverIndex);
    }
}
