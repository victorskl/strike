package strike.service;

import com.google.common.eventbus.EventBus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.ConnectException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConnectionService {

    // TODO this better be container managed, singleton for now
    private static ConnectionService instance = null;

    public static synchronized ConnectionService getInstance() {
        if (instance == null) instance = new ConnectionService();
        return instance;
    }

    private Connection connection;
    private ExecutorService pool;
    private EventBus eventBus; // we need it to be on JavaFX Application Thread

    private ConnectionService() {
        eventBus = new EventBus(); // new AsyncEventBus() better?
        pool = Executors.newSingleThreadExecutor();
    }

    public synchronized static SSLSocket createSocket(String serverAddress, int port) throws ConnectException {
        // do not hold any instance properties here
        try {
            SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            SSLSocket socket = (SSLSocket) sslsocketfactory.createSocket(serverAddress, port);
            socket.startHandshake();
            return socket;
        } catch (IOException e) {
            if (e instanceof ConnectException) {
                throw new ConnectException(e.getMessage());
            } else {
                e.printStackTrace();
            }
        }
        return null;
    }

    public synchronized Connection start(String serverAddress, int port) throws ConnectException {
        if (connection == null) { // ConnectionService is 1to1 to Connection for now
            connection = new Connection(createSocket(serverAddress, port));
            logger.info("New connection has established.");
            pool.execute(connection);
        } else {
            // this is by design at the moment
            logger.warn("ConnectionService can have only one active connection at a time.");
            logger.warn("Return a previously established connection. " + connection.getId());
        }
        return connection;
    }

    public synchronized void stop() {
        if (connection != null) {
            connection.stop();
        }
        pool.shutdown();
        logger.info("ConnectionService has shutdown...");
    }

    public synchronized void mutate(SSLSocket sslSocket) {
        connection.stop();
        connection = new Connection(sslSocket);
        logger.info("New connection has established. " + connection.getId());
        pool.execute(connection);
    }

    public synchronized Connection getConnection() {
        return connection;
    }

    public synchronized EventBus getEventBus() {
        return eventBus;
    }

    private static final Logger logger = LogManager.getLogger(ConnectionService.class);
}
