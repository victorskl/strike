package strike.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.SSLSocket;
import java.io.*;

/**
 * This connection live as long as the caller keep the socket alive. i.e. the caller owns the socket.
 * It is a caller responsibility to close the socket when no longer needed and handle the socket exceptions.
 */
public class NonClosingTcpClient {

    private SSLSocket socket;

    public NonClosingTcpClient(SSLSocket sslSocket) {
        this.socket = sslSocket;
    }

    public String comm(String message) {

        try {

            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
            writer.write(message + "\n");
            writer.flush();

            logger.debug("Sending : " + message);

            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            String resp = reader.readLine();
            logger.debug("Response : " + resp);

            return resp;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static final Logger logger = LogManager.getLogger(NonClosingTcpClient.class);
}
