package strike.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import strike.common.model.Protocol;
import strike.model.ServerInfo;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;

public class PeerClient {

    private JSONMessageBuilder messageBuilder = JSONMessageBuilder.getInstance();
    private ServerState serverState = ServerState.getInstance();
    private ServerInfo serverInfo = serverState.getServerInfo();
    private JSONParser parser;
    private SSLSocketFactory sslsocketfactory;

    public PeerClient() {
        parser = new JSONParser();
        sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
    }

    public String commPeer(ServerInfo server, String message) {

        SSLSocket socket = null;
        try {
            socket = (SSLSocket) sslsocketfactory.createSocket(server.getAddress(), server.getManagementPort());
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
            writer.write(message + "\n");
            writer.flush();

            logger.debug("[S2S]Sending  : [" + server.getServerId()
                    + "@" + server.getAddress() + ":" + server.getManagementPort() + "] " + message);

            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            return reader.readLine();

        } catch (IOException ioe) {
            //ioe.printStackTrace();
            logger.trace("Can't Connect: " + server.getServerId() + "@"
                    + server.getAddress() + ":" + server.getManagementPort());
        } finally {
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

    public String commPeer(ServerInfo server, String... messages) {

        if (messages.length < 1) return null;

        SSLSocket socket = null;
        try {
            socket = (SSLSocket) sslsocketfactory.createSocket(server.getAddress(), server.getManagementPort());
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
            for (String message : messages) {
                writer.write(message + "\n");
                writer.flush();
                logger.debug("[S2S]Sending  : [" + server.getServerId()
                        + "@" + server.getAddress() + ":" + server.getManagementPort() + "] " + message);
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            return reader.readLine();

        } catch (IOException ioe) {
            //ioe.printStackTrace();
            logger.trace("Can't Connect: " + server.getServerId() + "@"
                    + server.getAddress() + ":" + server.getManagementPort());
        } finally {
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

    public boolean canPeersLockId(String jsonMessage) {
        boolean canLock = true;

        for (ServerInfo server : serverState.getServerInfoList()) {
            if (!server.getServerId().equalsIgnoreCase(this.serverInfo.getServerId())) {

                String resp = commPeer(server, jsonMessage);

                if (resp == null) continue;

                JSONObject jj = null;
                try {
                    jj = (JSONObject) parser.parse(resp);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                logger.debug("[S2S]Receiving: [" + server.getServerId()
                        + "@" + server.getAddress() + ":" + server.getManagementPort() + "] " + jj.toJSONString());

                // {"identity":"Adel","type":"lockidentity","locked":"false","serverid":"s2"}
                String status = (String) jj.get(Protocol.locked.toString());
                if (status.equalsIgnoreCase("false")) {
                    canLock = false; // denied lock
                }

            }
        }

        return canLock;
    }

    public void relayPeers(String jsonMessage) {
        serverState.getServerInfoList().stream()
                .filter(server -> !server.getServerId().equalsIgnoreCase(this.serverInfo.getServerId()))
                .forEach(server -> {
                    commPeer(server, jsonMessage);
                });
    }

    public String commServerSingleResp(ServerInfo server, String message) {

        SSLSocket socket = null;
        try {
            socket = (SSLSocket) sslsocketfactory.createSocket(server.getAddress(), server.getPort());
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));

            performSystemDaemonAuthentication(writer);

            writer.write(message + "\n");
            writer.flush();

            logger.trace("[A52]Sending  : [" + server.getServerId()
                    + "@" + server.getAddress() + ":" + server.getPort() + "] " + message);

            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));

            String resp = null;
            for (int i = 0; i < 2; i++) { // 2 = one for performSystemDaemonAuthentication, one for actual message response
                resp = reader.readLine();
            }

            return resp;

        } catch (IOException ioe) {
            //ioe.printStackTrace();
            logger.trace("[A52]Can't Connect: " + server.getServerId() + "@"
                    + server.getAddress() + ":" + server.getPort());
        } finally {
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

    private void performSystemDaemonAuthentication(BufferedWriter writer) throws IOException {
        logger.trace("Sending systemdaemon login request...");
        writer.write(messageBuilder.makeLoginMessage("systemdaemon", "gaja5EPrEB5T") + "\n");
        writer.flush();
    }

    private static final Logger logger = LogManager.getLogger(PeerClient.class);
}
