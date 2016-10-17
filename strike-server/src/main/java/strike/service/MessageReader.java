package strike.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import strike.model.Message;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;

public class MessageReader implements Runnable {

	private BufferedReader reader; 
	private BlockingQueue<Message> messageQueue;
	
	public MessageReader(BufferedReader reader, BlockingQueue<Message> messageQueue) {
		this.reader = reader;
		this.messageQueue = messageQueue;
	}
	
	@Override
	public void run() {
		try {

            logger.trace("Reading messages from client connection");

			String clientMsg;
			while ((clientMsg = reader.readLine()) != null) {

                logger.trace("Message from client received: " + clientMsg);

                Message msg = new Message(true, clientMsg);
				messageQueue.add(msg);
			}

			// Ctrl+C
			Message exit = new Message(false, "exit");
			messageQueue.add(exit);
			
		} catch (SocketException e) {
            // Ctrl+C on Windows
			Message exit = new Message(false, "exit");
			messageQueue.add(exit);		
		} catch (IOException ioe) {
            //Remote host closed connection during handshake
            if (ioe.getMessage().equalsIgnoreCase("Remote host closed connection during handshake")) {
                logger.warn("[KNOW ISSUE #1] Remote host closed connection during handshake");
                /**
                 * TODO KNOW ISSUE #1
                 * Apparently this exception is thrown on first time connection.
                 * Can't figure out why...
                 * But the connection is working either way. Put it as known issue for now.
                 * Otherwise uncomment stack trace to debug.
                 */
                //ioe.printStackTrace();
            } else {
                ioe.printStackTrace();
            }
		}
	}

	private static final Logger logger = LogManager.getLogger(MessageReader.class);
}
