package strike.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import strike.handler.ProtocolHandlerFactory;
import strike.model.Message;

import javax.net.ssl.SSLSocket;
import java.io.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public class ManagementConnection implements Runnable {

    private BufferedReader reader;
    private BufferedWriter writer;
    private ExecutorService pool;
    private JSONParser parser;

    private SSLSocket clientSocket;
    private BlockingQueue<Message> messageQueue;

    public ManagementConnection(SSLSocket clientSocket) {
        try {
            this.clientSocket = clientSocket;
            this.reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));;
            this.writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"));
            this.messageQueue = new LinkedBlockingQueue<>();
            this.parser = new JSONParser();
            this.pool = Executors.newSingleThreadExecutor();
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

                if (!msg.isFromClient() && msg.getMessage().equals("exit")) {
                    //logger.trace("EOF");
                    break;
                }

                if (msg.isFromClient()) {

                    JSONObject jsonMessage = (JSONObject) parser.parse(msg.getMessage());
                    logger.debug("[S2S]Receiving: " + msg.getMessage());

                    ProtocolHandlerFactory.newManagementHandler(jsonMessage, this).handle();

                } else {
                    logger.debug("[S2S]Sending  : " + msg.getMessage());
                    write(msg.getMessage());
                }
            }

            pool.shutdown();
            clientSocket.close();
            writer.close();
            reader.close();

        } catch (InterruptedException | IOException | ParseException e) {
            logger.trace(e.getMessage());
            pool.shutdownNow();
        }
    }

    private void write(String msg) {
        try {
            writer.write(msg + "\n");
            writer.flush();
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

    private static final Logger logger = LogManager.getLogger(ManagementConnection.class);
}
