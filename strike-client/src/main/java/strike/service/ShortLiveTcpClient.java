package strike.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.ConnectException;

/**
 * This is a short live connection. It will close the socket after every communication made.
 *
 * TODO: potential merger with server PeerClient
 */
public class ShortLiveTcpClient {

    private SSLSocket socket;

    public ShortLiveTcpClient(String host, int port) throws ConnectException {
        try {
            SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            socket = (SSLSocket) sslsocketfactory.createSocket(host, port);
            socket.startHandshake();

            // TODO: remove
            //for (String s : socket.getSupportedProtocols()) {
            //    System.out.println(s);
            //}

        } catch (IOException e) {
            if (e instanceof ConnectException) {
                throw new ConnectException(e.getMessage());
            } else {
                e.printStackTrace();
            }
        }
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

            writer.close();
            reader.close();

            return resp;

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }

    private static final Logger logger = LogManager.getLogger(ShortLiveTcpClient.class);
}
