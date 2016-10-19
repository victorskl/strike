package strike.controller;

import au.edu.unimelb.tcp.client.Client;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import strike.StrikeClient;

import javax.net.ssl.SSLSocket;
import java.io.IOException;

/**
 * Created by Phillip on 16/10/2016.
 */

public class ScreenNameController implements Client.INewUserHandler {

    private StrikeClient strikeClient;
    private SSLSocket authenticatedSocket;
    AnchorPane chatWindow = null;

    private boolean chatWindowLoaded = false;

    public void setStrikeClient(StrikeClient strikeClient) {

        this.strikeClient = strikeClient;
        this.strikeClient.getClient().onUserApprovalOrDeny("approval", this);
    }

    public void setAuthenticatedSocket(SSLSocket authenticatedSocket) {
        this.authenticatedSocket = authenticatedSocket;
    }

    @FXML
    private TextField idScreenName;

    @FXML
    private void initialize() {
        /**
         * Initializes the controller class. This method is automatically called
         * after the fxml file has been loaded.
         */
        System.out.println("ScreenNameController Init...");
    }

    @FXML
    public void login(ActionEvent actionEvent) {

        String screenName = idScreenName.getText();

        if(screenName.equals("")) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Empty Screen Name");
            alert.setHeaderText("Please enter a screen name. It cannot be blank.");
            alert.showAndWait();
        }
        else {

            //this.strikeClient.getClient().setIdentity(screenName);
            try {

                if(!chatWindowLoaded) {
                    loadChatWindow();
                }
                // Start the client.
                if(!this.strikeClient.getClient().isRunning()) {
                    this.strikeClient.getClient().run(screenName, authenticatedSocket);
                }
                else {
                    this.strikeClient.getClient().attemptLoginWith(screenName);
                }
            }
            catch(Exception e) {
                    e.printStackTrace();
            }
        }
    }

    @Override
    public void userWasApproved() {
        // Show the chat window if our username was approved.

        Platform.runLater(() -> {
            try {
                // Show the scene containing the root layout.
                Scene scene = new Scene(chatWindow);
                strikeClient.getPrimaryStage().setScene(scene);
                strikeClient.getPrimaryStage().show();
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void userWasDenied() {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Screen Name Denied");
            alert.setHeaderText("Your username has been denied. It is probably already being used.");
            alert.showAndWait();
        });
    }

    private void loadChatWindow() {

        try {
            // Load the ChatWindow
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(this.strikeClient.getClass().getResource("view/ChatWindow.fxml"));
            chatWindow = loader.load();

            // Give the chat window controller any details it needs.
            ChatWindowController controller = loader.getController();
            controller.setStrikeClient(this.strikeClient);
            //controller.setAuthenticatedSocket(authenticatedSocket);

            chatWindowLoaded = true;
        }
        catch(IOException e) {
            e.printStackTrace();
        }

    }
}
