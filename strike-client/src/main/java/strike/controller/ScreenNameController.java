package strike.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import strike.StrikeClient;

import java.io.IOException;

/**
 * Created by Phillip on 16/10/2016.
 */

public class ScreenNameController {

    private StrikeClient strikeClient;
    public void setStrikeClient(StrikeClient strikeClient) {
        this.strikeClient = strikeClient;
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

            this.strikeClient.getClient().setIdentity(screenName);

            try {
                // Load the ChatWindow
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(this.strikeClient.getClass().getResource("view/ChatWindow.fxml"));
                AnchorPane chatWindow = loader.load();

                // Show the scene containing the root layout.
                Scene scene = new Scene(chatWindow);
                strikeClient.getPrimaryStage().setScene(scene);
                strikeClient.getPrimaryStage().show();

                // Give the chat window controller any details it needs.
                ChatWindowController controller = loader.getController();
                controller.setStrikeClient(this.strikeClient);

                // Super temporary.
                try {
                    this.strikeClient.getClient().run();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
            catch(IOException e) {
                e.printStackTrace();
            }
        }
    }
}
