package strike.controller;

import com.google.common.base.Strings;
import com.google.common.eventbus.Subscribe;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import strike.StrikeClient;
import strike.common.Utilities;
import strike.handler.ProtocolHandlerFactory;
import strike.service.ClientState;
import strike.service.ConnectionService;
import strike.service.JSONMessageBuilder;

import java.io.IOException;

public class ScreenNameController {

    // TODO find out a better way to do this dependency injection
    private StrikeClient strikeClient;

    public void setStrikeClient(StrikeClient strikeClient) {
        this.strikeClient = strikeClient;
    }

    // -----

    private AnchorPane chatWindow = null;
    private boolean chatWindowLoaded = false;

    @FXML
    private TextField idScreenName;

    @FXML
    private ProgressIndicator progressIndicator;

    private String screenName;

    @FXML
    private void initialize() {
        logger.info("ScreenNameController Init...");
    }

    @FXML
    public void login(ActionEvent actionEvent) {

        screenName = idScreenName.getText();

        if (Strings.isNullOrEmpty(screenName)) {

            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Empty Screen Name");
            alert.setHeaderText("Please enter a screen name. It cannot be blank.");
            alert.showAndWait();

        }

        else if (!Utilities.isIdValid(screenName)) {

            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Screen Name Invalid");
            alert.setHeaderText("Please re-enter a screen name." +
                    "\nIt must be an alphanumeric and length between 3 to 16.");
            alert.showAndWait();
        }

        else {

            try {

                progressIndicator.setVisible(true);

                if (!chatWindowLoaded) {
                    loadChatWindow();
                }

                JSONObject jsonMessage = JSONMessageBuilder.getInstance().getNewIdentityRequest(screenName);
                ConnectionService connectionService = ConnectionService.getInstance();
                connectionService.getEventBus().register(this);
                ProtocolHandlerFactory.newSendHandler(jsonMessage).handle();

                // request state, this may reset if server disapprove at NewIdentityReceiveHandler
                ClientState clientState = ClientState.getInstance();
                clientState.setIdentity(screenName);
                clientState.setRoomId("");

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Subscribe
    public void _approved(Boolean approved) {
        Platform.runLater(() -> {

            progressIndicator.setVisible(false);

            if (approved) {
                try {
                    Scene scene = new Scene(chatWindow);
                    strikeClient.getPrimaryStage().setScene(scene);
                    strikeClient.getPrimaryStage().show();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // user has approved with screen name, so unregister from EventBus
                ConnectionService.getInstance().getEventBus().unregister(this);

            } else {

                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Screen Name Denied");
                alert.setHeaderText("Your username has been denied. It is probably already being used.");
                alert.showAndWait();

            }
        });
    }

    private void loadChatWindow() {
        try {

            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(this.strikeClient.getClass().getResource("view/ChatWindow.fxml"));
            chatWindow = loader.load();

            // Give the chat window controller any details it needs.
            ChatWindowController controller = loader.getController();
            controller.setStrikeClient(this.strikeClient);

            chatWindowLoaded = true;

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static final Logger logger = LogManager.getLogger(ScreenNameController.class);
}
