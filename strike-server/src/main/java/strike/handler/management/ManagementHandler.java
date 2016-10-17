package strike.handler.management;

import org.json.simple.JSONObject;
import strike.model.Message;
import strike.service.JSONMessageBuilder;
import strike.service.ManagementConnection;
import strike.service.ServerState;

import javax.net.ssl.SSLSocket;
import java.util.concurrent.BlockingQueue;

public class ManagementHandler {

    protected JSONMessageBuilder messageBuilder = JSONMessageBuilder.getInstance();
    protected ServerState serverState = ServerState.getInstance();

    protected BlockingQueue<Message> messageQueue;
    private SSLSocket clientSocket;
    protected JSONObject jsonMessage;
    private ManagementConnection managementConnection;

    public ManagementHandler(JSONObject jsonMessage, Runnable connection) {
        this.jsonMessage = jsonMessage;

        this.managementConnection = (ManagementConnection) connection;
        this.messageQueue = managementConnection.getMessageQueue();
        this.clientSocket = managementConnection.getClientSocket();
    }
}
