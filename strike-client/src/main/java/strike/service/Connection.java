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
import java.net.Inet4Address;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This Connection should better be managed by ConnectionService.
 */
public class Connection implements Runnable {

    private SSLSocket socket;
    private BufferedReader reader;
    private BufferedWriter writer;
    private JSONParser parser;
    private BlockingQueue<Message> messageQueue;
    private ExecutorService pool;
    private AtomicBoolean halt = new AtomicBoolean(false);

    Connection(SSLSocket socket) {
        try {
            this.socket = socket;
            this.reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream(), "UTF-8"));
            this.writer = new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream(), "UTF-8"));
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

            pool.execute(new MessageReader(getId(), reader, messageQueue));

            while (true) {

                if (halt.get()) {
                    break;
                }

                Message msg = messageQueue.take();
                logger.trace(String.format("Processing messages: [incoming %b], %s", msg.isFromClient(), msg.getMessage()));

                if (msg.isFromClient()) {

                    JSONObject jsonMessage = (JSONObject) parser.parse(msg.getMessage());
                    logger.debug("Receiving: " + msg.getMessage());

                    ProtocolHandlerFactory.newReceiveHandler(jsonMessage).handle();

                    //String type = (String) jsonMessage.get(Protocol.type.toString());
                    //if (type.equalsIgnoreCase(Protocol.quit.toString())) break;

                } else {

                    logger.debug("Sending  : " + msg.getMessage());
                    send(msg.getMessage());
                }
            }

        } catch (InterruptedException | ParseException | IOException e) {
            logger.trace(e.getMessage());
            pool.shutdownNow();
            e.printStackTrace();
        } finally {
            // cleanup - must be in order - writer > reader > socket > pool
            logger.debug("Closing up connection... " + getId());
            try {
                writer.close();
                reader.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            pool.shutdown();
        }
    }

    private void send(String msg) throws IOException {
        writer.write(msg + "\n");
        writer.flush();
        logger.trace("Message flush");
    }

    void stop() {
        halt.getAndSet(true);
        logger.info("Initiated closing connection... " + getId());
    }

    public BlockingQueue<Message> getMessageQueue() {
        return messageQueue;
    }

    public String getIpAddress() {
        Inet4Address inet4Address = (Inet4Address) socket.getInetAddress();
        return inet4Address.toString();
    }

    public int getPort() {
        return socket.getPort();
    }

    public String getId() {
        return getIpAddress() + ":" + getPort();
    }

    private static final Logger logger = LogManager.getLogger(Connection.class);
}
