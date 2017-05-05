package strike.controller;

import com.google.common.eventbus.Subscribe;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import org.apache.commons.configuration2.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import strike.StrikeClient;
import strike.handler.ProtocolHandlerFactory;
import strike.service.*;

import java.io.IOException;
import java.net.ConnectException;
import java.util.Comparator;
import java.util.HashMap;

public class Login {

    // TODO find out a better way to do this dependency injection
    private StrikeClient strikeClient;
    public void setStrikeClient(StrikeClient strikeClient) {
        this.strikeClient = strikeClient;
    }

    private JSONParser parser = new JSONParser();
    private JSONMessageBuilder messageBuilder = JSONMessageBuilder.getInstance();

    private HashMap<String, ServerInfo> serverInformationMap = new HashMap<>();
    private ObservableList<String> listOfServers = FXCollections.observableArrayList();

    @FXML
    private TextField idUsername;

    @FXML
    private PasswordField idPassword;

    @FXML
    private ToggleGroup idAccountType;

    @FXML
    private ComboBox<String> idServer;

    @FXML
    private void initialize() {
        logger.info("Login Controller Init...");

        Configuration systemProperties = InitService.getInstance().getSystemProperties();
        String host = systemProperties.getString("client.seed.server");
        int port = systemProperties.getInt("client.seed.server.port");

        try {
            ShortLiveTcpClient slTcpClient = new ShortLiveTcpClient(host, port);
            String response = slTcpClient.comm(messageBuilder.createServerListMessage());

            // {"servers":[{"address":"localhost","port":4444,"serverid":"s1"}],"type":"serverlist"}
            JSONObject serverList = (JSONObject) parser.parse(response);
            JSONArray servers = (JSONArray) serverList.get("servers");
            for (Object o : servers) {
                JSONObject server = (JSONObject) o;
                String serverId = (String) server.get("serverid");
                Number serverPort = (Number) server.get("port");
                String address = (String) server.get("address");
                serverInformationMap.put(serverId, new ServerInfo(address, serverPort.intValue(), serverId));
            }

            listOfServers.addAll(serverInformationMap.keySet());
            listOfServers.sort(Comparator.naturalOrder());

            idServer.setItems(listOfServers);

        } catch (ConnectException e) {

            String content = String.format("Can't connect to discovery seed server: %s %d", host, port);

            //e.printStackTrace();
            logger.error(e.getMessage());
            logger.error(content);

            Alert alert = new Alert(Alert.AlertType.ERROR, content, ButtonType.OK);
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            alert.show();

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void login(ActionEvent actionEvent) {

        String username = idUsername.getText();

        String password = idPassword.getText();

        String accType = ((RadioButton) idAccountType.getSelectedToggle()).getText();

        String selectedServerId = idServer.getSelectionModel().getSelectedItem();

        try {

            if (selectedServerId == null) {

                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Select server");
                alert.setHeaderText("Please select server.");
                alert.showAndWait();

            } else {

                ServerInfo info = serverInformationMap.get(selectedServerId);

                ConnectionService connectionService = ConnectionService.getInstance();
                connectionService.getEventBus().register(this);
                connectionService.start(info.address, info.port);

                JSONObject authMessage = messageBuilder.createAuthMessage(username, password, false);
                ProtocolHandlerFactory.newSendHandler(authMessage).handle();
            }

        } catch (ConnectException e) {
            e.printStackTrace();
        }
    }

    @Subscribe
    public void success(Boolean success) {
        Platform.runLater(() -> {
            if (success) {

                try {
                    // Get the screenname.
                    FXMLLoader loader = new FXMLLoader();
                    loader.setLocation(this.strikeClient.getClass().getResource("view/ScreenName.fxml"));
                    Pane screenNameWindow = loader.load();

                    // Show the scene containing the root layout.
                    Scene scene = new Scene(screenNameWindow);
                    strikeClient.getPrimaryStage().setScene(scene);
                    strikeClient.getPrimaryStage().show();

                    // Pass the client to the screen name window.
                    ScreenNameController controller = loader.getController();
                    controller.setStrikeClient(this.strikeClient);
                    //controller.setAuthenticatedSocket(slTcpClient.getSocket());

                } catch (IOException e) {
                    e.printStackTrace();
                }

                // user has success with login, so unregister from EventBus
                ConnectionService.getInstance().getEventBus().unregister(this);

            } else {

                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Unable to authenticate!");
                alert.setHeaderText("Unable to authenticate your credentials. Please try again.");
                alert.showAndWait();
            }
        });
    }

    private class ServerInfo {
        String address;
        Integer port;
        String serverId;

        ServerInfo(String address, Integer port, String serverId) {
            this.address = address;
            this.port = port;
            this.serverId = serverId;
        }
    }

    private static final Logger logger = LogManager.getLogger(Login.class);
}
