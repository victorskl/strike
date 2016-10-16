package strike.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import strike.handler.ProtocolHandlerFactory;
import strike.model.Message;
import strike.model.Protocol;
import strike.model.UserInfo;

import javax.net.ssl.SSLSocket;
import java.io.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class ClientConnection implements Runnable {

    private BufferedReader reader;
    private BufferedWriter writer;
    private ExecutorService pool;
    private JSONParser parser;

    private SSLSocket clientSocket;
    private BlockingQueue<Message> messageQueue;
    private UserInfo userInfo;
    private boolean routed = false;

    private Subject currentUser;

    public ClientConnection(SSLSocket clientSocket) {
        try {
            this.clientSocket = clientSocket;
            this.reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));
            this.writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"));
            this.messageQueue = new LinkedBlockingQueue<>();
            this.parser = new JSONParser();
            this.pool = Executors.newSingleThreadExecutor();

            currentUser = SecurityUtils.getSubject(); // bind it to this connection thread context
        } catch (Exception e) {
            logger.trace(e.getMessage());
        }
    }

    @Override
    public void run() {

        try {

            pool.execute(new MessageReader(reader, messageQueue));

            while (true) {

                Message msg = messageQueue.take();
                logger.trace("Processing client messages: " + msg.getMessage());

                if (!msg.isFromClient() && msg.getMessage().equalsIgnoreCase("exit")) {
                    //The client program is abruptly terminated (e.g. using Ctrl-C)
                    ProtocolHandlerFactory.newHandler(null, this).handle();
                    logger.trace("EOF");
                    break;
                }

                if (msg.isFromClient()) {

                    JSONObject jsonMessage = (JSONObject) parser.parse(msg.getMessage());
                    logger.debug("Receiving: " + msg.getMessage());

                    ProtocolHandlerFactory.newHandler(jsonMessage, this).handle();

                    String type = (String) jsonMessage.get(Protocol.type.toString());
                    if (type.equalsIgnoreCase(Protocol.quit.toString())) break;

                } else {
                    logger.debug("Sending  : " + msg.getMessage());
                    write(msg.getMessage());
                }
            }

            pool.shutdown();
            clientSocket.close();
            writer.close();
            reader.close();

            if (userInfo != null) {
                logger.info("Client disconnected: " + userInfo.getIdentity());
            }

        } catch (Exception e) {
            logger.trace(e.getMessage());
            pool.shutdownNow();
        } finally {
            if (currentUser != null) {
                logger.info("Client disconnected: " + currentUser.getPrincipal());
                currentUser.logout();
            }
        }
    }

    public void write(String msg) {
        try {
            writer.write(msg + "\n");
            writer.flush();

            logger.trace("Message flush");

        } catch (IOException e) {
            logger.trace(e.getMessage());
        }
    }

    public BlockingQueue<Message> getMessageQueue() {
        return messageQueue;
    }

    public SSLSocket getClientSocket() {
        return clientSocket;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public boolean isRouted() {
        return routed;
    }

    public void setRouted(boolean routed) {
        this.routed = routed;
    }

    public Subject getCurrentUser() {
        return currentUser;
    }

    private static final Logger logger = LogManager.getLogger(ClientConnection.class);
}
