package strike.handler.receive;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import strike.common.model.Protocol;
import strike.handler.CommonHandler;
import strike.handler.IProtocolHandler;
import strike.service.ConnectionService;
import strike.service.NonClosingTcpClient;

import javax.net.ssl.SSLSocket;
import java.io.IOException;

public class RouteReceiveHandler extends CommonHandler implements IProtocolHandler {

    public RouteReceiveHandler(JSONObject jsonMessage) {
        super(jsonMessage);
    }

    @Override
    public void handle() {

        // server directs the client to another server

        String tmpRoom = (String) jsonMessage.get(Protocol.roomid.toString());
        String host = (String) jsonMessage.get(Protocol.host.toString());
        int port = Integer.parseInt((String) jsonMessage.get(Protocol.port.toString()));
        String username = (String) jsonMessage.get(Protocol.username.toString());
        String sessionId = (String) jsonMessage.get(Protocol.sessionid.toString());
        String password = (String) jsonMessage.get(Protocol.password.toString());

        // connect to the new server
        logger.debug("Connecting to server " + host + ":" + port);

        try {

            // we need a new socket to mutate the existing connection
            SSLSocket newSocket = ConnectionService.createSocket(host, port);
            NonClosingTcpClient ncClient = new NonClosingTcpClient(newSocket);

            // send #movejoin
            // wait to receive serverchange

            JSONObject request = messageBuilder.getMoveJoinRequest(clientState.getIdentity(), clientState.getRoomId(), tmpRoom, username, sessionId, password);
            String resp = ncClient.comm(request.toJSONString());

            JSONParser parser = new JSONParser();
            JSONObject response = (JSONObject) parser.parse(resp);

            if (response.get(Protocol.type.toString()).equals(Protocol.serverchange.toString()) &&
                    response.get(Protocol.approved.toString()).equals("true")) {

                ConnectionService.getInstance().mutate(newSocket);
                String serverId = (String) response.get(Protocol.serverid.toString());

                logger.debug(clientState.getIdentity() + " switches to server " + serverId);
            }

            // receive invalid message
            else {
                logger.debug("Server change failed");
            }

        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }
    }

    private static final Logger logger = LogManager.getLogger(RouteReceiveHandler.class);
}
