package strike.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ManagementService implements Runnable {

    private final ServerSocket serverSocket;
    private final ExecutorService pool;
    private ServerState serverState = ServerState.getInstance();

    public ManagementService(int port, int poolSize) throws IOException {
        serverSocket = new ServerSocket(port);
        pool = Executors.newFixedThreadPool(poolSize);
    }

    @Override
    public void run() {
        try {

            logger.info("Server listening peer on management port "+ serverSocket.getLocalPort() +" for a connection...");

            while (!serverState.isStopRunning()) {
                pool.execute(new ManagementConnection(serverSocket.accept()));
            }

            pool.shutdown();
        } catch (IOException ex) {
            pool.shutdown();
        } finally {
            pool.shutdownNow();
        }
    }

    private static final Logger logger = LogManager.getLogger(ManagementService.class);
}
